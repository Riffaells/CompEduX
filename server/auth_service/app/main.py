# -*- coding: utf-8 -*-
# Добавляем путь к корневой директории в начале файла
import os
import sys

# Получаем абсолютный путь к корневой директории проекта
root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../.."))
if root_dir not in sys.path:
    sys.path.insert(0, root_dir)

from fastapi import FastAPI, Request, Response, Depends, HTTPException, Security
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import APIKeyHeader
from fastapi.exceptions import RequestValidationError
import time
from fastapi import APIRouter
from contextlib import asynccontextmanager
from typing import Optional, Dict, Any
from fastapi import status
import uuid
from jose import jwt
from uuid import UUID

# Импорты из проекта
from .api.routes import auth, users
from .core.config import settings
from .db.init_db import init_db
from .db.database import connect_to_db, disconnect_from_db
from .core.exceptions import APIException, api_exception_handler, validation_exception_handler

# Импортируем единый логгер из common модуля
from common.logger import get_logger
from common.logger.middleware import setup_request_logging

# Получаем настроенный логгер
logger = get_logger("auth_service")

# Completely disable SQLAlchemy logging
sqlalchemy_loggers = [
    'sqlalchemy',
    'sqlalchemy.engine',
    'sqlalchemy.engine.base',
    'sqlalchemy.dialects',
    'sqlalchemy.pool',
    'sqlalchemy.orm',
    'sqlalchemy.engine.base.Engine'
]

for name in sqlalchemy_loggers:
    logger_sa = get_logger(name)
    logger_sa.setLevel(50)  # CRITICAL
    logger_sa.propagate = False
    logger_sa.disabled = True
    if hasattr(logger_sa, 'handlers'):
        for handler in logger_sa.handlers[:]:
            logger_sa.removeHandler(handler)


# Настраиваем обработчик ошибок подключения к БД
def handle_db_error(logger, error_msg, e):
    exc_type, exc_value, exc_traceback = sys.exc_info()
    log_msg = f"{error_msg}: {str(e)}"
    logger.error(f"[bold red]{log_msg}[/bold red]")
    return HTTPException(status_code=503, detail=log_msg)


# Определяем функцию lifespan
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application lifecycle context manager
    """
    # Startup: выполняется при запуске приложения
    logger.info("[bold green]Auth Service starting up...[/bold green]")
    logger.info(f"Environment: {settings.ENV}, Debug mode: {settings.DEBUG}")

    # Регистрируем обработчики исключений
    app.add_exception_handler(APIException, api_exception_handler)
    app.add_exception_handler(RequestValidationError, validation_exception_handler)

    # Инициализация базы данных - делаем это синхронно
    try:
        if await init_db():
            logger.info("Database initialized successfully")
        else:
            logger.error("Failed to initialize database")
            sys.exit(1)
    except Exception as e:
        logger.error(f"Error initializing database: {str(e)}")
        sys.exit(1)

    # Соединение с асинхронной базой данных
    try:
        await connect_to_db()
    except Exception as e:
        logger.error(f"[bold red]Failed to connect to async database: {e}[/bold red]")
        sys.exit(1)

    yield  # Application runs here

    # Shutdown: выполняется при завершении работы
    logger.info("[bold yellow]Auth Service shutting down...[/bold yellow]")

    # Отключение от асинхронной базы данных
    try:
        await disconnect_from_db()
    except Exception as e:
        logger.error(f"[bold red]Error disconnecting from async database: {e}[/bold red]")


# Создаем FastAPI приложение
app = FastAPI(
    title=settings.PROJECT_NAME,
    description=settings.DESCRIPTION,
    version=settings.VERSION,
    openapi_url=f"{settings.API_V1_STR}/openapi.json",
    docs_url=f"{settings.API_V1_STR}/docs",
    redoc_url=f"{settings.API_V1_STR}/redoc",
    lifespan=lifespan
)

# Добавляем поддержку CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.BACKEND_CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Настройка middleware для логирования запросов
setup_request_logging(
    app=app,
    logger=logger,
    exclude_paths=[
        "/docs", "/redoc", "/openapi.json", "/healthz",
        f"{settings.API_V1_STR}/docs", f"{settings.API_V1_STR}/redoc",
        f"{settings.API_V1_STR}/openapi.json"
    ],
    log_request_headers=False
)


# Создадим класс для добавления версии API в заголовок ответа
class VersionedAPIRoute(APIRouter):
    """
    Расширение APIRouter для добавления версии API в заголовок ответа
    """

    def __init__(self, version: str = "v1", **kwargs):
        super().__init__(**kwargs)
        self.version = version

    def get_route_handler(self):
        original_route_handler = super().get_route_handler()

        async def custom_route_handler(request: Request):
            response = await original_route_handler(request)
            # Добавляем версию API в заголовок ответа
            if isinstance(response, Response):
                response.headers["X-API-Version"] = self.version
            return response

        return custom_route_handler


# Получаем маршруты для статистики
from .api.routes import stats

# Изменим создание роутеров
v1_router = VersionedAPIRoute(version="v1", prefix=settings.API_V1_STR)
base_router = VersionedAPIRoute(version="v1")
api_router = VersionedAPIRoute()  # Роутер без версии в пути для гибридного подхода

# Подключаем эндпоинты с использованием версии API
v1_router.include_router(auth.router, prefix="/auth", tags=["auth"])
v1_router.include_router(users.router, prefix="/users", tags=["users"])
v1_router.include_router(stats.router, prefix="/stats", tags=["stats"])

# Подключаем те же эндпоинты без версии в пути для обратной совместимости
base_router.include_router(auth.router, prefix="/auth", tags=["auth"])
base_router.include_router(users.router, prefix="/users", tags=["users"])

# Подключаем роутеры для маршрутизации через заголовки
api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
api_router.include_router(users.router, prefix="/users", tags=["users"])


# Настройка маршрутизации и порядка роутеров
# ВАЖНО: порядок регистрации имеет значение!

# 1. Сначала регистрируем специальные маршруты напрямую в приложении
# Health check для корневого пути - кэшируемый для повышения производительности
@app.get("/health", response_model=Dict[str, str],
         tags=["monitoring"],
         summary="Проверка работоспособности API",
         response_description="Базовый статус сервиса")
async def health_check_root():
    """
    Базовый эндпоинт для проверки работоспособности сервиса.
    Используется системами мониторинга и балансировщиками нагрузки.
    """
    return {"status": "healthy"}


# Подключаем маршруты auth напрямую к корню для API Gateway
# Эти маршруты будут доступны как /login, /register и т.д.
app.include_router(auth.router, tags=["gateway-auth"])


# Информационный эндпоинт о API
@app.get("/api", response_model=Dict[str, str],
         tags=["info"],
         summary="Информация об API",
         response_description="Основная информация об API сервисе")
async def api_root(request: Request):
    """Информация о API"""
    return {
        "api": "CompEduX Auth Service API",
        "version": request.state.api_version,
        "docs_url": "/docs",
        "redoc_url": "/redoc"
    }


# Обработчик для версий API, которые не v1
@app.get("/api/v{version}/{rest_of_path:path}",
         tags=["info"],
         summary="Обработчик несуществующих версий API")
async def version_router(version: str, rest_of_path: str, request: Request):
    """
    Обработчик для поддержки разных версий API в URL.
    Поддерживает только v1, для остальных возвращает подсказку.
    """
    if version != "1":
        return {
            "error": "Unsupported API version",
            "message": f"Version v{version} is not supported",
            "supported_versions": ["v1"],
            "hints": [
                f"Use path /api/v1/{rest_of_path}",
                f"Use header X-API-Version: v1 with path /api/{rest_of_path}"
            ]
        }
    # Для v1 маршрутизация происходит через v1_router
    # Эта функция не должна выполняться для v1, она здесь для документации
    raise HTTPException(
        status_code=status.HTTP_404_NOT_FOUND,
        detail="Route not found"
    )


# 2. Регистрируем роутеры в правильном порядке:
# Сначала регистрируем версии API по путям (наиболее конкретные маршруты)
app.include_router(v1_router)  # Маршрутизация через путь /api/v1/

# Затем регистрируем версии API по заголовкам
app.include_router(api_router, prefix="/api")  # Маршрутизация через заголовки /api/

# В конце регистрируем базовые маршруты (наиболее общие)
app.include_router(base_router)  # Маршрутизация через корневой путь /


# Добавляем Health check в каждый роутер для их доступности через разные пути
# Health check для версионированного API (путь /api/v1/health)
@v1_router.get("/health", response_model=Dict[str, str],
               tags=["monitoring"],
               summary="Проверка работоспособности API (v1)")
async def health_check_v1():
    """Health check endpoint for versioned API (v1)"""
    return {"status": "healthy", "version": "v1"}


# Health check для API с версией через заголовки (путь /api/health)
@api_router.get("/health", response_model=Dict[str, Any],
                tags=["monitoring"],
                summary="Проверка работоспособности API (через заголовок)")
async def health_check_header(request: Request):
    """Health check endpoint for API with version in header"""
    return {
        "status": "healthy",
        "version": request.state.api_version,
        "header_version": True
    }


# Диагностический эндпоинт для отображения всех маршрутов
# Создаем защиту для отладочных эндпоинтов
admin_key_header = APIKeyHeader(name="X-Admin-Key", auto_error=False)


async def verify_admin_access(api_key: Optional[str] = Security(admin_key_header)):
    """Проверка доступа администратора через API ключ"""
    # В производственной среде всегда требуем ключ
    if settings.ENV != "development" and (not api_key or api_key != settings.ADMIN_API_KEY):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Доступ запрещен: требуется административный ключ"
        )
    # В среде разработки разрешаем доступ, но логируем предупреждение
    if settings.ENV == "development" and (not api_key or api_key != settings.ADMIN_API_KEY):
        logger.warning("Доступ к отладочному эндпоинту без ключа в среде разработки")
    return True


@app.get("/debug/routes")
async def debug_routes(_: bool = Depends(verify_admin_access)):
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


# Объединенный middleware для обработки запросов и версионирования API
@app.middleware("http")
async def combined_middleware(request: Request, call_next):
    # Генерируем уникальный ID для запроса
    request_id = str(uuid.uuid4())
    request.state.request_id = request_id

    # Получаем информацию о запросе
    path = request.url.path
    method = request.method
    client_ip = request.client.host if request.client else None

    # Обработка версии API
    api_version = request.headers.get("X-API-Version", "v1")
    request.state.api_version = api_version

    # Попытка получить пользователя из токена для статистики
    user_id = None
    try:
        auth_header = request.headers.get("Authorization")
        if auth_header and auth_header.startswith("Bearer "):
            token = auth_header.replace("Bearer ", "")
            # Разбор токена для получения ID - без полной валидации
            # для ускорения работы и избежания ненужных проверок подписи
            try:
                # Используем опцию verify_signature=False для ускорения
                # Это безопасно, так как мы только получаем ID для статистики
                payload = jwt.decode(
                    token,
                    settings.AUTH_SECRET_KEY,
                    algorithms=["HS256"],
                    options={"verify_signature": False}
                )
                user_id_str = payload.get("sub")
                if user_id_str:
                    try:
                        user_id = UUID(user_id_str)
                        request.state.user_id = user_id
                    except ValueError:
                        # Игнорируем ошибку парсинга UUID
                        pass
            except Exception as jwt_error:
                # Игнорируем ошибки JWT для статистики
                pass
    except Exception as e:
        # Игнорируем ошибки при получении пользователя, это не критично для статистики
        pass

    # Замеряем время выполнения
    start_time = time.time()

    try:
        # Выполняем запрос
        response = await call_next(request)
        process_time = (time.time() - start_time) * 1000  # время в миллисекундах

        response.headers["X-Request-ID"] = request_id

        return response
    except Exception as e:
        # Логируем исключения
        process_time = (time.time() - start_time) * 1000
        logger.error(
            f"Exception in {method} {path}: {str(e)}",
            extra={
                "request_id": request_id,
                "path": path,
                "method": method,
                "duration_ms": process_time,
                "exception": str(e),
                "exception_type": type(e).__name__
            },
            exc_info=True
        )
        raise


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8001,
        reload=settings.DEBUG,
    )
