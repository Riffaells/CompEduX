# -*- coding: utf-8 -*-
"""
Course Service API main module
"""
# Configure logging before any imports
from common.logger import get_logger

# Создаем базовый логгер для сервиса
logger = get_logger("course_service")

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

# Disable uvicorn logging
for name in ["uvicorn", "uvicorn.access", "uvicorn.error", "uvicorn.asgi"]:
    logger_u = get_logger(name)
    logger_u.setLevel(50)  # CRITICAL
    logger_u.propagate = False
    logger_u.disabled = True
    if hasattr(logger_u, 'handlers'):
        for handler in logger_u.handlers[:]:
            logger_u.removeHandler(handler)

import sys
import os

# Получаем абсолютный путь к корневой директории проекта
root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../.."))
if root_dir not in sys.path:
    sys.path.insert(0, root_dir)

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.exceptions import RequestValidationError
import time
from contextlib import asynccontextmanager
from typing import Dict, Any, AsyncGenerator
import uuid
import contextvars
from fastapi.responses import JSONResponse
import traceback
from sqlalchemy.sql import text

# Disable SQLAlchemy's built-in logging at module load time
db_logger = get_logger('sqlalchemy')
db_logger.setLevel(50)  # CRITICAL
get_logger('sqlalchemy.engine').setLevel(50)
get_logger('sqlalchemy.pool').setLevel(50)
get_logger('sqlalchemy.orm').setLevel(50)
get_logger('sqlalchemy.dialects').setLevel(50)

# Отключаем все подлоггеры SQLAlchemy
for logger_name in ['sqlalchemy.engine', 'sqlalchemy.pool', 'sqlalchemy.orm', 'sqlalchemy.dialects']:
    sub_logger = get_logger(logger_name)
    sub_logger.setLevel(50)  # CRITICAL
    sub_logger.propagate = False
    sub_logger.disabled = True

from common.logger.middleware import setup_request_logging

# Импортируем новый роутер из api вместо старого из routes
from .api.api import api_router
from .core.config import settings
from .db.db import database, init_db, engine
from .core.exceptions import APIException, api_exception_handler, validation_exception_handler

# Create a context variable for request IDs
request_id_var = contextvars.ContextVar("request_id", default=None)

# Initialize Redis client if enabled - Redis is disabled for now
redis_client = None
logger.info("Redis is disabled. Skipping Redis initialization.")


# Определяем функцию lifespan
@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None, None]:
    """
    Application lifecycle context
    """
    # Startup: runs at application startup
    logger.info("[bold green]Course Service starting up...[/bold green]")
    logger.info(f"Environment: {settings.ENV}, Debug mode: {settings.DEBUG}")

    # Register exception handlers
    app.add_exception_handler(APIException, api_exception_handler)
    app.add_exception_handler(RequestValidationError, validation_exception_handler)

    # Connect to the database
    try:
        await database.connect()
        logger.info("Connected to the database")
    except Exception as e:
        logger.error(f"[bold red]Failed to connect to the database: {str(e)}[/bold red]", exc_info=True)

    # Initialize the database
    try:
        logger.info("Initializing database...")
        init_result = await init_db()
        if init_result:
            logger.info("Database initialization successful")
        else:
            logger.warning("Database initialization failed, service may not work properly. See logs for details.")
    except Exception as e:
        logger.error(f"[bold red]Database initialization error: {str(e)}[/bold red]", exc_info=True)
        logger.warning("Unable to initialize database. Application may not work properly.")

    yield  # Application runs here

    # Shutdown: runs at application shutdown
    logger.info("[bold yellow]Course Service shutting down...[/bold yellow]")

    # Disconnect from the database
    try:
        await database.disconnect()
        logger.info("Database connection closed")
    except Exception as e:
        logger.error(f"[bold red]Error disconnecting from database: {e}[/bold red]")

    # Redis is disabled for now
    # # Закрываем соединение с Redis
    # if redis_client:
    #     logger.info("Cleaning up cache connections")
    #     await cleanup_cache()
    #     await redis_client.close()
    #     logger.info("Redis connection closed")


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


# Объединенный middleware для обработки запросов и добавления метаданных
@app.middleware("http")
async def combined_middleware(request: Request, call_next):
    # Генерируем уникальный ID для запроса
    request_id = str(uuid.uuid4())
    request.state.request_id = request_id
    request_id_var.set(request_id)

    # Получаем информацию о запросе
    path = request.url.path
    method = request.method
    client_ip = request.client.host if request.client else None

    # Обработка версии API, если поддерживаем версионирование
    api_version = request.headers.get("X-API-Version", "v1")
    request.state.api_version = api_version

    # Замеряем время выполнения
    start_time = time.time()

    try:
        # Выполняем запрос
        response = await call_next(request)
        process_time = (time.time() - start_time) * 1000  # время в миллисекундах

        # Добавляем request_id в заголовки ответа
        response.headers["X-Request-ID"] = request_id

        return response
    except Exception as e:
        # Логируем исключения
        process_time = (time.time() - start_time) * 1000
        logger.error(
            f"[bold red]Exception in {method} {path}: {str(e)}[/bold red]",
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


@app.get("/health",
         response_model=Dict[str, Any],
         tags=["monitoring"],
         summary="API health check",
         response_description="Базовый статус сервиса")
async def health_check():
    """
    Базовый эндпоинт для проверки работоспособности сервиса.
    Используется системами мониторинга и балансировщиками нагрузки.
    """
    # Check database connection
    try:
        # Используем напрямую соединение с движком
        async with engine.connect() as conn:
            await conn.execute(text("SELECT 1"))
            db_status = "ok"
    except Exception as e:
        logger.error(f"[bold red]Database health check failed: {str(e)}[/bold red]")
        db_status = "error"

    # Redis is disabled
    redis_status = "disabled"

    return {
        "status": "ok" if db_status == "ok" else "error",
        "services": {
            "database": db_status,
            "redis": redis_status
        },
        "version": settings.VERSION,
        "environment": settings.ENV
    }


# Connect API routes
app.include_router(api_router, prefix="")


# Global exception handler
@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    request_id = request_id_var.get()
    error_msg = f"Unhandled exception during {request.method} {request.url.path}"
    logger.error(f"[bold red]{error_msg} [Request ID: {request_id}][/bold red]", exc_info=exc)

    # Create error response with traceback in non-production environments
    if settings.ENV != "production":
        tb_str = ''.join(traceback.format_exception(type(exc), exc, exc.__traceback__))
        error_details = {
            "error": str(exc),
            "traceback": tb_str,
            "request_id": request_id
        }
    else:
        error_details = {
            "error": "Internal server error",
            "request_id": request_id
        }

    return JSONResponse(
        status_code=500,
        content=error_details
    )


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8002,
        reload=settings.DEBUG,
    )
