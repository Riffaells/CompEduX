from fastapi import FastAPI, Request, Response, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import httpx
import logging
from typing import Dict, Any
from contextlib import asynccontextmanager

from app.core.config import settings
from app.core.utils import check_auth_service
from app.api.routes import api_router
from app.middleware.auth import AuthMiddleware

# Настройка логирования - установим более высокий уровень для некоторых библиотек
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("api_gateway")

# Уменьшаем логирование для библиотек
logging.getLogger("uvicorn").setLevel(logging.WARNING)
logging.getLogger("httpx").setLevel(logging.WARNING)
logging.getLogger("asyncio").setLevel(logging.WARNING)
logging.getLogger("fastapi").setLevel(logging.WARNING)

# Глобальный HTTP-клиент для повторного использования соединений
# Это значительно ускорит все HTTP-запросы через API Gateway
http_client = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Выполнится при запуске приложения
    global http_client

    logger.info("API Gateway starting up...")
    logger.info(f"AUTH_SERVICE_URL: {settings.AUTH_SERVICE_URL}")

    # Создаем глобальный HTTP-клиент для повторного использования соединений
    http_client = httpx.AsyncClient(
        timeout=5.0,
        limits=httpx.Limits(
            max_keepalive_connections=10,
            max_connections=50,
            keepalive_expiry=30.0
        )
    )

    # Проверяем доступность auth_service без лишних логов
    auth_status = await check_auth_service()
    if auth_status and auth_status["status"] == "ok":
        logger.info("Auth service is available")
    else:
        logger.warning("Auth service health check failed")

    yield  # Здесь приложение работает

    # Выполнится при завершении работы приложения
    if http_client:
        await http_client.aclose()
    logger.info("API Gateway shutting down...")

app = FastAPI(
    title="CompEduX API Gateway",
    description="API Gateway для микросервисов CompEduX",
    version="0.1.0",
    openapi_url=f"{settings.API_V1_STR}/openapi.json",
    lifespan=lifespan
)

# Настройка CORS
if settings.BACKEND_CORS_ORIGINS:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=[str(origin) for origin in settings.BACKEND_CORS_ORIGINS],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

# Добавляем middleware для аутентификации
app.middleware("http")(AuthMiddleware())

# Middleware для логирования запросов и ответов
@app.middleware("http")
async def log_requests_and_responses(request: Request, call_next):
    # Логируем только важные эндпоинты или ошибки
    path = request.url.path
    method = request.method

    # Не логируем запросы к docs и другим служебным эндпоинтам
    should_log = not any(path.startswith(p) for p in ['/docs', '/redoc', '/openapi.json', '/healthz'])

    if should_log:
        logger.debug(f"Request: {method} {path}")

    # Обрабатываем запрос
    response = await call_next(request)

    # Логируем только ошибки и важные статусы
    if response.status_code >= 400 or (should_log and path.startswith(('/auth', '/api/v1/auth'))):
        logger.info(f"Response: {method} {path} - {response.status_code}")

    return response

# Простой эндпоинт для проверки работоспособности
@app.get("/healthz", include_in_schema=False)
async def healthz() -> Dict[str, Any]:
    """
    Проверяет работоспособность API Gateway и доступность сервисов.
    Используется для проверки состояния в Kubernetes/Docker.
    """
    # Проверка доступности auth_service
    auth_status = await check_auth_service()

    # Собираем информацию о состоянии сервисов
    services_status = {
        "auth_service": auth_status or {"status": "unknown", "message": "Not configured"}
    }

    # Определяем общий статус
    overall_status = "ok"
    for service_status in services_status.values():
        if service_status.get("status") != "ok":
            overall_status = "degraded"
            break

    return {
        "status": overall_status,
        "api_gateway": "ok",
        "services": services_status
    }

# Тестовый эндпоинт для проверки соединения с auth_service
@app.get("/test-auth-connection", include_in_schema=False)
async def test_auth_connection_endpoint() -> Dict[str, Any]:
    """
    Тестирует соединение с сервисом авторизации и возвращает подробную информацию.
    """
    auth_service_url = settings.AUTH_SERVICE_URL
    logger.info(f"Testing connection to auth service at {auth_service_url}")

    try:
        async with httpx.AsyncClient() as client:
            # Проверка здоровья сервиса
            try:
                health_url = f"{auth_service_url}/health"
                logger.info(f"Testing health endpoint: {health_url}")
                health_response = await client.get(health_url, timeout=5.0)
                logger.info(f"Health response status: {health_response.status_code}")
                health_result = {
                    "url": health_url,
                    "status_code": health_response.status_code,
                    "content": health_response.json() if health_response.status_code == 200 else str(health_response.content)
                }
            except Exception as e:
                logger.error(f"Health check error: {str(e)}")
                health_result = {"error": str(e)}

            return {
                "auth_service_url": auth_service_url,
                "health_check": health_result,
                "connection_status": "success" if "status_code" in health_result else "failed"
            }
    except Exception as e:
        logger.error(f"Connection test error: {str(e)}")
        return {"error": str(e), "connection_status": "failed"}

# Подключение роутеров API с префиксом
app.include_router(api_router, prefix=settings.API_V1_STR)

# Добавляем те же роутеры без префикса для удобства
from app.api.routes.auth import router as auth_router_root
app.include_router(auth_router_root, prefix="/auth", tags=["authentication_root"])

# Добавляем корневые маршруты для основных операций аутентификации
@app.post("/login", include_in_schema=True, tags=["authentication_root"])
async def root_login(request: Request):
    """Вход в систему без префикса пути"""
    from app.api.routes.auth import login, check_auth_service_health
    # Проверяем доступность сервиса
    await check_auth_service_health()
    return await login(request)

@app.post("/register", include_in_schema=True, tags=["authentication_root"])
async def root_register(request: Request):
    """Регистрация без префикса пути"""
    from app.api.routes.auth import register, check_auth_service_health
    # Проверяем доступность сервиса
    await check_auth_service_health()
    return await register(request)

# Также добавляем доступ к OpenAPI документации на корневом уровне
# для удобства и совместимости
app.openapi_url = "/openapi.json"

# Добавляем основные эндпоинты без префикса API_V1_STR для удобства использования
@app.get("/docs", include_in_schema=False)
async def get_swagger_documentation():
    from fastapi.responses import RedirectResponse
    return RedirectResponse(url=f"{settings.API_V1_STR}/docs")

@app.get("/redoc", include_in_schema=False)
async def get_redoc_documentation():
    from fastapi.responses import RedirectResponse
    return RedirectResponse(url=f"{settings.API_V1_STR}/redoc")

if __name__ == "__main__":
    import uvicorn
    logger.info(f"Starting API Gateway with AUTH_SERVICE_URL: {settings.AUTH_SERVICE_URL}")
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
