from fastapi import FastAPI, Request, Response
from fastapi.middleware.cors import CORSMiddleware
import logging
import time
import json

from .api.routes import router as api_router
from .core.config import settings
from .db.init_db import init_db

# Configure logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)
# Отключаем предупреждения от passlib
logging.getLogger('passlib').setLevel(logging.ERROR)

app = FastAPI(
    title=settings.PROJECT_NAME,
    description=settings.DESCRIPTION,
    version=settings.VERSION,
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.BACKEND_CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Middleware для логирования запросов и ответов
@app.middleware("http")
async def log_requests_and_responses(request: Request, call_next):
    # Логируем запрос
    request_body = await request.body()
    request_body_str = request_body.decode() if request_body else ""
    logger.debug(f"Request: {request.method} {request.url.path}")
    if request_body_str:
        try:
            logger.debug(f"Request body: {json.loads(request_body_str)}")
        except:
            logger.debug(f"Request body: {request_body_str}")

    # Обрабатываем запрос
    response = await call_next(request)

    # Логируем ответ
    logger.debug(f"Response status: {response.status_code}")

    return response

# Include routers
app.include_router(api_router)

@app.get("/health")
async def health_check():
    """Health check endpoint for monitoring and load balancers"""
    return {"status": "healthy"}

@app.on_event("startup")
async def startup_event():
    """
    Выполняется при запуске приложения.
    Инициализирует базу данных и выполняет другие необходимые действия.
    """
    logger.info("Запуск приложения...")

    # Инициализация базы данных
    db_initialized = init_db()
    if db_initialized:
        logger.info("База данных успешно инициализирована")
    else:
        logger.warning("Не удалось инициализировать базу данных. Приложение может работать некорректно.")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.DEBUG,
    )
