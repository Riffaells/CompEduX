from fastapi import APIRouter, Request, Response, HTTPException, Depends
import time
from typing import Any, Dict
import logging

from app.core.config import settings
from app.core.proxy import check_service_health, proxy_request

# Настройка логирования
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("auth_proxy")

router = APIRouter()

# Создаем зависимость для проверки здоровья сервиса с кэшированием
async def check_auth_service_health():
    """Проверяет доступность сервиса авторизации"""
    await check_service_health(
        service_name="auth",
        force=False
    )
    return True

# Функция-helper для выполнения запросов к сервису авторизации
async def proxy_request_to_auth(path: str, request: Request) -> Response:
    """
    Проксирует запрос к сервису авторизации и возвращает ответ.
    """
    auth_service_url = settings.AUTH_SERVICE_URL
    return await proxy_request(auth_service_url, path, request)

# Определяем endpoints для auth сервиса

@router.post("/register", include_in_schema=True)
async def register(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Регистрация нового пользователя"""
    return await proxy_request_to_auth("register", request)

@router.post("/login", include_in_schema=True)
async def login(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Вход в систему и получение токенов"""
    return await proxy_request_to_auth("login", request)

@router.post("/refresh", include_in_schema=True)
async def refresh_token(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Обновление access token с помощью refresh token"""
    return await proxy_request_to_auth("refresh", request)

@router.post("/logout", include_in_schema=True)
async def logout(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Выход из системы (отзыв refresh token)"""
    return await proxy_request_to_auth("logout", request)

@router.get("/me", include_in_schema=True)
async def get_current_user(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Получение информации о текущем пользователе"""
    return await proxy_request_to_auth("me", request)

@router.post("/verify-token", include_in_schema=True)
async def verify_token(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Проверка токена"""
    return await proxy_request_to_auth("verify-token", request)

# Добавляем универсальный маршрут для других эндпоинтов auth_service
@router.api_route("/{path:path}", methods=["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"])
async def proxy_auth_requests(path: str, request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """
    Проксирует любые другие запросы к сервису авторизации.
    Это позволяет API Gateway перенаправлять любые запросы без явного определения всех эндпоинтов.
    """
    logger.info(f"Proxying generic request: {path}")
    return await proxy_request_to_auth(path, request)
