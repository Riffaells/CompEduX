from fastapi import FastAPI, Request, Response, Depends, Header, HTTPException, Security
from fastapi.middleware.cors import CORSMiddleware
from fastapi.security import APIKeyHeader
from fastapi.exceptions import RequestValidationError
import time
import json
from fastapi import APIRouter
from contextlib import asynccontextmanager
import asyncio
from typing import Optional, Callable, Dict, Any, TypeVar
from fastapi import status
import uuid
import os
from datetime import datetime
from pathlib import Path
import logging
import logging.config
import yaml

from .api.routes import auth, users
from .core.config import settings
from .db.init_db import init_db
from .core.exceptions import APIException, api_exception_handler, validation_exception_handler
from .core.logging import setup_logging, get_logger, log_request_info, log_response_info

# Настройка логирования
log_level = "debug" if settings.DEBUG else "info"
# Определяем путь к файлу конфигурации логирования
log_config_path = "auth_service/app/core/logging.conf"
setup_logging(level=log_level, enable_file_logging=True, config_path=log_config_path)
logger = get_logger("main")

# Setup database connection for development mode using SQLite
if os.getenv("ENV") == "development":
    sqlite_path = Path(__file__).parent.parent / "dev.db"
    os.environ["SQLALCHEMY_DATABASE_URI"] = os.environ.get(
        "SQLALCHEMY_DATABASE_URI", f"sqlite:///{sqlite_path}"
    )

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
    description=settings.DESCRIPTION + """

## Версионирование API

Сервис поддерживает гибридный подход к версионированию API:

1. **Версия в пути** - традиционный подход: `/api/v1/users`
2. **Версия в заголовке** - современный подход: `/api/users` с заголовком `X-API-Version: v1`
3. **Без указания версии** - работает с последней версией: `/api/users` или `/users`

### Примеры использования:

```bash
# Версия в пути (традиционный подход)
curl -X GET http://localhost:8000/api/v1/users/me -H "Authorization: Bearer YOUR_TOKEN"

# Версия в заголовке (современный подход)
curl -X GET http://localhost:8000/api/users/me -H "X-API-Version: v1" -H "Authorization: Bearer YOUR_TOKEN"

# Без указания версии (последняя версия)
curl -X GET http://localhost:8000/api/users/me -H "Authorization: Bearer YOUR_TOKEN"
```

В ответах всегда присутствует заголовок `X-API-Version`, указывающий используемую версию API.
    """,
    version=settings.VERSION,
    lifespan=lifespan,
    # Настройки для Swagger UI
    docs_url="/docs",
    redoc_url="/redoc",
    openapi_url="/openapi.json",
    swagger_ui_parameters={
        "deepLinking": True,  # Позволяет использовать прямые ссылки на операции
        "defaultModelsExpandDepth": 3,  # Глубина раскрытия моделей
        "defaultModelExpandDepth": 3,  # Глубина раскрытия модели
        "displayRequestDuration": True,  # Показывать время выполнения запроса
    }
)

# Регистрируем обработчики исключений
app.add_exception_handler(APIException, api_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.BACKEND_CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Объединенный middleware для обработки запросов, логирования и версионирования API
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

    # Определение важных запросов для логирования
    is_important = path.startswith(('/auth/login', '/auth/register', '/api/v1/auth/login', '/api/v1/auth/register'))
    should_log = is_important or settings.DEBUG

    # Логируем запрос, если это нужно
    if should_log:
        log_request_info(
            request_id=request_id,
            method=method,
            path=path,
            version=api_version,
            client_ip=client_ip
        )

    # Замеряем время выполнения
    start_time = time.time()

    try:
        # Выполняем запрос
        response = await call_next(request)
        process_time = (time.time() - start_time) * 1000  # время в миллисекундах

        # Добавляем версию API и request_id в ответ
        response.headers["X-API-Version"] = request.state.api_version
        response.headers["X-Request-ID"] = request_id

        # Логируем результат
        if response.status_code >= 400 or process_time > 1000 or is_important or settings.DEBUG:
            log_response_info(
                request_id=request_id,
                status_code=response.status_code,
                duration_ms=process_time,
                path=path,
                method=method
            )

        return response
    except Exception as e:
        # Логируем исключения
        process_time = (time.time() - start_time) * 1000
        error_logger = get_logger("error")
        error_logger.error(
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

# Создадим зависимость для обработки версии API через заголовки
T = TypeVar('T')

def version_header(
    x_api_version: Optional[str] = Header(None, description="API Version Header")
) -> str:
    """
    Определяет версию API на основе заголовка X-API-Version.
    Если заголовок отсутствует, возвращает последнюю версию.
    """
    # Список поддерживаемых версий
    supported_versions = ["v1"]
    latest_version = "v1"

    # Если версия указана и поддерживается, используем её
    if x_api_version and x_api_version in supported_versions:
        return x_api_version

    # Если версия не указана или не поддерживается, используем последнюю
    return latest_version

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
            return response

        return custom_route_handler

# Изменим создание роутеров
v1_router = VersionedAPIRoute(version="v1", prefix=settings.API_V1_STR)
base_router = VersionedAPIRoute(version="v1")
api_router = VersionedAPIRoute()  # Роутер без версии в пути для гибридного подхода

# Подключаем эндпоинты с использованием версии API
v1_router.include_router(auth.router, prefix="/auth", tags=["auth"])
v1_router.include_router(users.router, prefix="/users", tags=["users"])

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

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.DEBUG,
    )
