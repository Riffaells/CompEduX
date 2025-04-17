# -*- coding: utf-8 -*-
"""
Course Service API main module
"""
import sys
import os
# Получаем абсолютный путь к корневой директории проекта
root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../.."))
if root_dir not in sys.path:
    sys.path.insert(0, root_dir)

from fastapi import FastAPI, Request, Response, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.exceptions import RequestValidationError
import time
import json
from contextlib import asynccontextmanager
import asyncio
from typing import Optional, Dict, Any
import uuid
from datetime import datetime

from common.logger import get_logger
from common.logger.middleware import setup_request_logging

from app.api.routes import router as api_router
from app.core.config import settings
from app.models.base import Base
from app.db.session import engine, get_db
from app.db.init_db import init_db
from app.db.database import database
from app.core.exceptions import APIException, api_exception_handler, validation_exception_handler

# Получаем настроенный логгер
logger = get_logger("course_service")

# Определяем функцию lifespan
@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Application lifecycle context
    """
    # Startup: runs at application startup
    logger.info("Course Service starting up...")

    # Register exception handlers
    app.add_exception_handler(APIException, api_exception_handler)
    app.add_exception_handler(RequestValidationError, validation_exception_handler)

    # Connect to the database
    try:
        await database.connect()
        logger.info("Connected to the database")
    except Exception as e:
        logger.error(f"Failed to connect to the database: {e}")

    # Initialize the database
    try:
        logger.info("Initializing database...")
        init_result = init_db()
        if init_result:
            logger.info("Database initialization successful")
        else:
            logger.warning("Database initialization failed, service may not work properly")
    except Exception as e:
        logger.error(f"Database initialization error: {e}")
        logger.warning("Unable to initialize database. Application may not work properly.")

    yield  # Application runs here

    # Shutdown: runs at application shutdown
    logger.info("Course Service shutting down...")

    # Disconnect from the database
    try:
        await database.disconnect()
        logger.info("Database connection closed")
    except Exception as e:
        logger.error(f"Error disconnecting from database: {e}")

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

@app.get("/")
async def root():
    """Root endpoint for health check"""
    logger.info("Health check endpoint called")
    return {"message": "Course Service is running"}

@app.get("/health", response_model=Dict[str, str],
         tags=["monitoring"],
         summary="API health check")
async def health_check():
    """
    Basic endpoint for service health check.
    Used by monitoring systems and load balancers.
    """
    return {"status": "healthy"}

# Connect API routes
app.include_router(api_router, prefix=settings.API_V1_STR)

# Request middleware
@app.middleware("http")
async def request_middleware(request: Request, call_next):
    # Generate unique ID for the request
    request_id = str(uuid.uuid4())
    request.state.request_id = request_id

    # Measure execution time
    start_time = time.time()

    try:
        # Process the request
        response = await call_next(request)
        process_time = (time.time() - start_time) * 1000  # time in milliseconds

        response.headers["X-Request-ID"] = request_id

        return response
    except Exception as e:
        # Log exceptions
        process_time = (time.time() - start_time) * 1000
        logger.error(
            f"Exception during request processing: {str(e)}",
            extra={
                "request_id": request_id,
                "path": request.url.path,
                "method": request.method,
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
        port=8002,
        reload=settings.DEBUG,
    )
