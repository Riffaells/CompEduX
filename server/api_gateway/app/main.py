# Add path to the root directory at the beginning of the file
import sys
import os
# Get the absolute path to the project's root directory
root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../.."))
if root_dir not in sys.path:
    sys.path.insert(0, root_dir)

from fastapi import FastAPI, Request, Response, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import logging
import time
import asyncio
from contextlib import asynccontextmanager
from typing import Dict, Any
from datetime import datetime

# Import common/logger module
try:
    from common.logger import get_logger
    from common.logger.middleware import setup_request_logging
    from common.logger.config import format_log_time
    # Get the pre-configured logger
    logger = get_logger("api_gateway")
except ImportError:
    # If import fails, use the standard logger
    logging.basicConfig(level=logging.INFO)
    logger = logging.getLogger("api_gateway")

from common.logger.setup import log_service_lifecycle

from app.core.config import settings, SERVICE_ROUTES
from app.core.proxy import get_http_client, close_http_client, get_all_services_health
from app.api.routes import api_router
from app.middleware.auth import AuthMiddleware

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Executed when the application starts
    logger.info("[bold green]API Gateway starting up...[/bold green]")
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
    description="API Gateway for CompEduX microservices",
    version="0.1.0",
    openapi_url=f"{settings.API_V1_STR}/openapi.json",
    lifespan=lifespan
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
    setup_request_logging(
        app=app,
        logger=logger,
        exclude_paths=['/docs', '/redoc', '/openapi.json', '/healthz', '/health'],
        log_request_headers=False
    )
except Exception as e:
    logger.error(f"Error setting up request logging middleware: {str(e)}")

# Error handlers

@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    """Central handler for HTTP exceptions"""
    logger.warning(f"HTTP Exception: {exc.status_code} - {exc.detail}")
    return JSONResponse(
        status_code=exc.status_code,
        content={"detail": exc.detail}
    )

@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    """Handler for unexpected exceptions"""
    logger.error(f"Unexpected error: {str(exc)}", exc_info=True)
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"}
    )

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
        "service": "api_gateway"
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
