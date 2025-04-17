# Routes Package

from fastapi import APIRouter, Request, Depends, HTTPException
from app.core.config import SERVICE_ROUTES
from app.core.proxy import proxy_request, check_service_health

# Создаем основной роутер для API
api_router = APIRouter()

# Динамически создаем и регистрируем роутеры для каждого сервиса
for service_name, config in SERVICE_ROUTES.items():
    # Пропускаем сервис, если для него не указан URL
    if not config.get("base_url"):
        continue

    # Создаем отдельный роутер для сервиса
    service_router = APIRouter(
        prefix=config["prefix"],
        tags=[service_name]
    )

    # Заголовки документации
    service_description = f"{service_name.capitalize()} Service"

    # Регистрируем универсальный обработчик для всех запросов к сервису
    @service_router.api_route(
        "/{path:path}",
        methods=["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"],
        name=f"{service_name}_proxy",
        description=f"Проксирует запросы к сервису {service_description}"
    )
    async def proxy_handler(
        path: str,
        request: Request,
        current_service_name=service_name,
        current_service_url=config["base_url"],
        current_health_endpoint=config.get("health_endpoint", "/health")
    ):
        # Проверяем доступность сервиса
        await check_service_health(
            current_service_name,
            current_service_url,
            current_health_endpoint
        )

        # Формируем полный путь для правильной трансформации
        prefix = config["prefix"].strip("/")
        full_path = f"{prefix}/{path}"

        # Проксируем запрос на сервис
        return await proxy_request(
            current_service_url,
            full_path,
            request
        )

    # Добавляем роутер в основной API роутер
    api_router.include_router(service_router)

# Импортируем и добавляем специализированные роутеры
from app.api.routes.auth import router as auth_router
from app.api.routes.health import router as health_router

api_router.include_router(auth_router, prefix="/auth", tags=["auth"])
api_router.include_router(health_router, prefix="/health", tags=["health"])
