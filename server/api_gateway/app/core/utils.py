import httpx
import logging
import asyncio
from typing import Dict, Any, Optional, List

from app.core.config import settings

logger = logging.getLogger("api_gateway.utils")

async def check_service_health(service_url: str, service_name: str) -> Dict[str, Any]:
    """
    Проверяет доступность сервиса по URL.

    Args:
        service_url: URL сервиса для проверки
        service_name: Имя сервиса (для логов)

    Returns:
        Dict с информацией о статусе сервиса
    """
    logger.debug(f"Checking health of {service_name}")

    # Сокращаем список до самых вероятных эндпоинтов
    health_endpoints = [
        "/health",
        "/api/v1/health"
    ]

    if service_url.endswith("/"):
        service_url = service_url[:-1]

    try:
        # Сокращаем общий таймаут
        async with httpx.AsyncClient(timeout=2.0) as client:
            # Проверяем эндпоинты параллельно
            tasks = []
            for endpoint in health_endpoints:
                url = f"{service_url}{endpoint}"
                tasks.append(client.get(url, timeout=1.0))

            # Ждем результаты, игнорируя ошибки
            responses = await asyncio.gather(*tasks, return_exceptions=True)

            # Обрабатываем результаты
            for i, result in enumerate(responses):
                # Пропускаем исключения
                if isinstance(result, Exception):
                    continue

                # Если нашли рабочий эндпоинт
                if result.status_code == 200:
                    endpoint = health_endpoints[i]
                    logger.debug(f"{service_name} is healthy at {endpoint}")
                    return {
                        "status": "ok",
                        "message": f"Service is healthy",
                        "endpoint": endpoint,
                        "version": result.headers.get("X-API-Version", "unknown")
                    }

            # Если до сих пор ничего не сработало, пробуем быстро корневой URL
            try:
                response = await client.get(service_url, timeout=0.5)
                if response.status_code < 500:  # Любой ответ, кроме серверной ошибки
                    return {"status": "ok", "message": "Service is reachable"}
            except:
                pass

            # Все проверки провалились
            logger.warning(f"{service_name} is not healthy")
            return {
                "status": "error",
                "message": f"Service is not responding on any health endpoint"
            }
    except Exception as e:
        logger.error(f"Error checking {service_name}: {str(e)}")
        return {"status": "error", "message": f"Error: {str(e)}"}

async def check_auth_service() -> Optional[Dict[str, Any]]:
    """
    Проверяет доступность сервиса авторизации.

    Returns:
        Информация о статусе или None, если сервис не настроен
    """
    if not settings.AUTH_SERVICE_URL:
        logger.warning("AUTH_SERVICE_URL not configured")
        return None

    return await check_service_health(settings.AUTH_SERVICE_URL, "auth_service")
