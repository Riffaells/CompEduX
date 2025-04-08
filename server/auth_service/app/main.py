from fastapi import FastAPI, Request, Response
from fastapi.middleware.cors import CORSMiddleware
import logging
import time
import json
from fastapi import APIRouter
from contextlib import asynccontextmanager
import asyncio

from .api.routes import auth, users
from .core.config import settings
from .db.init_db import init_db

# Configure logging: меняем уровень на INFO и оптимизируем логирование
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
# Отключаем предупреждения от passlib и других библиотек
logging.getLogger('passlib').setLevel(logging.ERROR)
logging.getLogger('sqlalchemy.engine').setLevel(logging.WARNING)
logging.getLogger('uvicorn').setLevel(logging.WARNING)
logging.getLogger('httpx').setLevel(logging.WARNING)
logging.getLogger('asyncio').setLevel(logging.WARNING)
logging.getLogger('fastapi').setLevel(logging.WARNING)

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Обработчик событий жизненного цикла приложения.
    """
    # Startup: выполняется при запуске приложения
    logger.info("Запуск приложения...")

    # Инициализация базы данных - делаем это асинхронно
    from concurrent.futures import ThreadPoolExecutor
    with ThreadPoolExecutor() as pool:
        loop = asyncio.get_event_loop()
        db_initialized = await loop.run_in_executor(pool, init_db)
        if db_initialized:
            logger.info("База данных успешно инициализирована")
        else:
            logger.warning("Не удалось инициализировать базу данных. Приложение может работать некорректно.")

    yield  # Здесь приложение работает

    # Shutdown: выполняется при завершении работы
    logger.info("Завершение работы приложения...")

app = FastAPI(
    title=settings.PROJECT_NAME,
    description=settings.DESCRIPTION,
    version=settings.VERSION,
    lifespan=lifespan
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.BACKEND_CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Middleware для логирования запросов и ответов - оптимизируем ещё больше
@app.middleware("http")
async def log_requests_and_responses(request: Request, call_next):
    # Логируем запрос только для определенных эндпоинтов и при ошибках
    path = request.url.path
    method = request.method

    # Пропускаем логирование для большинства запросов
    should_log = False
    is_important = path.startswith(('/auth/login', '/auth/register', '/api/v1/auth/login', '/api/v1/auth/register'))

    # Для важных запросов логируем только минимальную информацию
    if is_important:
        logger.info(f"Auth request: {method} {path}")

    # Обрабатываем запрос, засекая время
    start_time = time.time()
    response = await call_next(request)
    process_time = time.time() - start_time

    # Логируем только ошибки или очень долгие запросы
    if response.status_code >= 400:
        logger.warning(f"Error response: {method} {path} - {response.status_code} in {process_time:.3f}s")
    elif process_time > 1.0:  # Логируем медленные запросы
        logger.info(f"Slow response: {method} {path} - {response.status_code} in {process_time:.3f}s")
    elif is_important:
        logger.info(f"Auth response: {response.status_code} in {process_time:.3f}s")

    return response

# Создаем роутеры с версией API и без для обратной совместимости
v1_router = APIRouter(prefix=settings.API_V1_STR)
base_router = APIRouter()

# Подключаем эндпоинты с использованием версии API
v1_router.include_router(auth.router, prefix="/auth", tags=["auth"])
v1_router.include_router(users.router, prefix="/users", tags=["users"])

# Подключаем те же эндпоинты без версии для обратной совместимости
base_router.include_router(auth.router, prefix="/auth", tags=["auth"])
base_router.include_router(users.router, prefix="/users", tags=["users"])

# Подключаем роутеры к приложению
app.include_router(v1_router)
app.include_router(base_router)

# Health check для корневого пути
@app.get("/health")
async def health_check():
    """Health check endpoint for monitoring and load balancers"""
    return {"status": "healthy"}

# Health check для версионированного API
@v1_router.get("/health")
async def health_check_versioned():
    """Health check endpoint for versioned API"""
    return {"status": "healthy"}

# Диагностический эндпоинт для отображения всех маршрутов
@app.get("/debug/routes")
async def debug_routes():
    """Debug endpoint to show all registered routes"""
    routes = []
    for route in app.routes:
        path = getattr(route, "path", None)
        name = getattr(route, "name", None)
        methods = getattr(route, "methods", None)
        if hasattr(methods, "__iter__"):
            methods = list(methods)
        routes.append({
            "path": path,
            "name": name,
            "methods": methods,
        })
    return {"routes": routes}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.DEBUG,
    )
