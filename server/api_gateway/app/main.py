# Add path to the root directory at the beginning of the file
import os
import sys

# Get the absolute path to the project's root directory
root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../.."))
if root_dir not in sys.path:
    sys.path.insert(0, root_dir)

from fastapi import FastAPI, Request, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from contextlib import asynccontextmanager
from datetime import datetime

from common.logger import initialize_logging

# Initialize the service logger
logger = initialize_logging("api_gateway")

# Import app-specific modules
from .core.config import settings, SERVICE_ROUTES
from .core.proxy import close_http_client, get_all_services_health
from .api.routes import api_router
from .middleware.auth import AuthMiddleware
from .core.errors import register_exception_handlers


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Executed when the application starts
    logger.info("[bold green]API Gateway starting up...[/bold green]")
    logger.info(f"Environment: {settings.ENV}, Debug mode: {settings.DEBUG}")
    logger.info(f"API Gateway listening on: {settings.API_GATEWAY_HOST}:{settings.API_GATEWAY_PORT}")
    logger.info(f"Configured services: {list(SERVICE_ROUTES.keys())}")

    # Check service availability at startup
    try:
        health_status = await get_all_services_health()
        if health_status["status"] == "ok":
            logger.info("[success]All configured services are available[/success]")
        else:
            logger.warning("[bold yellow]Some services are not available[/bold yellow]")
            for service, status in health_status["services"].items():
                if status["status"] != "ok":
                    logger.warning(f"  - {service}: {status.get('message', 'Unavailable')}")
    except Exception as e:
        logger.error(f"[bold red]Error checking services: {str(e)}[/bold red]")

    yield  # Application runs here

    # Close HTTP client on shutdown
    await close_http_client()
    logger.info("[bold red]API Gateway shutting down...[/bold red]")


app = FastAPI(
    title="CompEduX API Gateway",
    description="CompEduX API Gateway",
    version=settings.VERSION,
    openapi_url=f"{settings.API_V1_STR}/openapi.json",
    docs_url=f"{settings.API_V1_STR}/docs",
    redoc_url=f"{settings.API_V1_STR}/redoc",
    lifespan=lifespan,
    # Настройки для улучшения документации
    swagger_ui_parameters={
        "docExpansion": "list",  # Показывать только заголовки методов
        "deepLinking": True,  # Для удобной навигации по документации
        "defaultModelsExpandDepth": 0,  # Не показывать модели по умолчанию
        "displayOperationId": False,  # Не показывать operation ID
        "filter": True,  # Включить строку поиска
        "operationsSorter": "alpha",  # Сортировать операции по алфавиту
        "showExtensions": False,  # Не показывать расширения
        "showCommonExtensions": False,  # Не показывать общие расширения
        "tryItOutEnabled": True,  # Включить Try it out по умолчанию
        "persistAuthorization": True  # Сохранять авторизацию между перезагрузками
    }
)

# Configure CORS
if settings.BACKEND_CORS_ORIGINS:
    app.add_middleware(
        CORSMiddleware,
        allow_origins=[str(origin) for origin in settings.BACKEND_CORS_ORIGINS],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

# Add authentication middleware
app.middleware("http")(AuthMiddleware())

# Configure middleware for request and response logging
try:
    from common.logger.middleware import setup_request_logging

    setup_request_logging(
        app=app,
        logger=logger,
        exclude_paths=['/docs', '/redoc', '/openapi.json', '/healthz', '/health',
                       '/api/v1/auth/health', '/api/v1/courses/health',
                       '/api/v1/health'],
        log_request_headers=False
    )
except Exception as e:
    logger.error(f"Error setting up request logging middleware: {str(e)}")

# Регистрируем обработчики ошибок
register_exception_handlers(app)

# Endpoint to check the health of the API Gateway and all services
@app.get("/health", include_in_schema=True, tags=["health"])
async def health_check():
    """
    Checks the health of the API Gateway and availability of microservices.
    Used for health checks in Kubernetes/Docker.
    """
    return await get_all_services_health()


# Simple endpoint to check only the API Gateway health
@app.get("/healthz", include_in_schema=False)
async def healthz():
    """
    Quick check of the API Gateway only without checking microservices.
    """
    return {
        "status": "ok",
        "timestamp": datetime.utcnow().isoformat(),
        "service": "api_gateway",
        "version": settings.VERSION
    }


# Connecting API routers with prefix
app.include_router(api_router, prefix=settings.API_V1_STR)


# Add root routes for basic authentication operations
# This is for convenience of use without having to specify the full path
@app.get("/", include_in_schema=False)
async def root():
    """Redirects to API documentation"""
    from fastapi.responses import RedirectResponse
    return RedirectResponse(url=f"{settings.API_V1_STR}/docs")


# Add direct links to documentation
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
    import os

    logger.info(f"Starting API Gateway")

    try:
        # Запускаем API Gateway с настройками из конфигурации
        uvicorn.run(
            "app.main:app",
            host=settings.API_GATEWAY_HOST,
            port=settings.API_GATEWAY_PORT,
            reload=True
        )
    except KeyboardInterrupt:
        logger.info("[yellow]Uvicorn shutdown by keyboard interrupt[/yellow]")
    except Exception as e:
        logger.error(f"[bold red]Uvicorn error: {str(e)}[/bold red]")
