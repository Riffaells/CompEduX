from typing import Dict, Any

import httpx
from app.core.config import settings
from fastapi import APIRouter

from common.logger import get_logger

# Создаем логгер для health-модуля
logger = get_logger("api_gateway.health")

router = APIRouter()


######################################################################
# ПРОВЕРКА ЗДОРОВЬЯ СЕРВИСОВ
######################################################################

@router.get("/", summary="Проверка состояния всех сервисов")
async def health_check() -> Dict[str, Any]:
    """
    Проверяет состояние всех микросервисов.
    Возвращает статус каждого сервиса.
    """
    logger.info("API Gateway health check called")
    services = {
        "auth": settings.AUTH_SERVICE_URL,
        "course": settings.COURSE_SERVICE_URL,
        "room": settings.ROOM_SERVICE_URL,
        "competition": settings.COMPETITION_SERVICE_URL,
        "achievement": settings.ACHIEVEMENT_SERVICE_URL
    }

    results = {}
    api_paths = ["/health", "/api/v1/health"]

    async with httpx.AsyncClient(timeout=5.0) as client:
        for service_name, service_url in services.items():
            if not service_url:
                results[service_name] = {"status": "unknown", "message": "Service URL not configured"}
                continue

            # Проверяем разные варианты URL с учетом версионирования
            service_available = False
            error_message = ""

            for path in api_paths:
                try:
                    full_url = f"{service_url}{path}"
                    logger.debug(f"Checking health of {service_name} at {full_url}")
                    response = await client.get(full_url, timeout=3.0)
                    if response.status_code == 200:
                        results[service_name] = {
                            "status": "ok",
                            "message": "Service is healthy",
                            "endpoint": path,
                            "version": response.headers.get("X-API-Version", "unknown")
                        }
                        service_available = True
                        break
                    else:
                        error_message = f"Service returned status code {response.status_code} for {path}"
                except httpx.RequestError as exc:
                    error_message = f"Error connecting to service at {path}: {str(exc)}"

            # Если сервис недоступен по всем URL
            if not service_available:
                results[service_name] = {"status": "error", "message": error_message}

    # Определяем общий статус API Gateway
    gateway_status = "ok" if all(r.get("status") == "ok" for r in results.values()) else "degraded"

    return {
        "status": gateway_status,
        "gateway_version": "0.1.0",
        "api_version": "v1",
        "services": results
    }
