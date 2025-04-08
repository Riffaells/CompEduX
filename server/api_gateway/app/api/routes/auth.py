from fastapi import APIRouter, Request, Response, HTTPException, Depends
import httpx
import time
from typing import Any, Dict
import logging

from app.core.config import settings

# Настройка логирования
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("auth_proxy")

router = APIRouter()

# Переменные для кэширования состояния сервиса
_auth_service_healthy = None
_last_health_check = 0
_HEALTH_CHECK_INTERVAL = 5  # секунды между проверками

# Создаем зависимость для проверки здоровья сервиса с кэшированием
async def check_auth_service_health():
    """Проверяет доступность сервиса авторизации с кэшированием результатов"""
    global _auth_service_healthy, _last_health_check

    # Проверяем, прошло ли достаточно времени с последней проверки
    current_time = time.time()
    if _auth_service_healthy is not None and current_time - _last_health_check < _HEALTH_CHECK_INTERVAL:
        return _auth_service_healthy

    auth_service_url = settings.AUTH_SERVICE_URL
    try:
        async with httpx.AsyncClient(timeout=2.0) as client:
            # Пробуем самый вероятный вариант
            try:
                response = await client.get(f"{auth_service_url}/health", timeout=1.0)
                if response.status_code == 200:
                    _auth_service_healthy = True
                    _last_health_check = current_time
                    return True
            except Exception:
                pass

            # Если первый не сработал, пробуем второй вариант
            try:
                response = await client.get(f"{auth_service_url}/api/v1/health", timeout=1.0)
                if response.status_code == 200:
                    _auth_service_healthy = True
                    _last_health_check = current_time
                    return True
            except Exception:
                pass

            # Сервис недоступен
            logger.warning(f"Auth service health check failed")
            _auth_service_healthy = False
            _last_health_check = current_time

            raise HTTPException(
                status_code=503,
                detail="Сервис авторизации недоступен. Пожалуйста, повторите попытку позже."
            )
    except httpx.RequestError as exc:
        logger.error(f"Auth service connection error: {str(exc)}")
        _auth_service_healthy = False
        _last_health_check = current_time

        raise HTTPException(
            status_code=503,
            detail="Ошибка связи с сервисом авторизации"
        )

    return True

# Функция-helper для выполнения запросов к сервису авторизации
async def proxy_request_to_auth(path: str, request: Request) -> Response:
    """
    Проксирует запрос к сервису авторизации и возвращает ответ.
    """
    # Импортируем глобальный HTTP-клиент
    from app.main import http_client

    auth_service_url = settings.AUTH_SERVICE_URL

    # Исправлено: Проверяем наличие трейлинг слеша в auth_service_url
    if auth_service_url.endswith("/"):
        auth_service_url = auth_service_url[:-1]

    # Формируем URL для запроса
    full_url = f"/auth/{path}"

    # Получаем тело и заголовки запроса с минимальными логами
    body = await request.body()
    headers = dict(request.headers)
    headers.pop("host", None)

    try:
        # Используем глобальный HTTP-клиент для быстрых запросов
        auth_response = await http_client.request(
            method=request.method,
            url=f"{auth_service_url}{full_url}",
            headers=headers,
            content=body,
            params=request.query_params
        )

        # Логируем только ошибки для экономии времени
        if auth_response.status_code >= 400:
            logger.warning(f"Auth service error: {auth_response.status_code} for {full_url}")

        # Возвращаем ответ клиенту
        return Response(
            content=auth_response.content,
            status_code=auth_response.status_code,
            headers=dict(auth_response.headers)
        )
    except httpx.RequestError as exc:
        logger.error(f"Error proxying to auth service: {str(exc)}")
        raise HTTPException(
            status_code=503,
            detail=f"Ошибка связи с сервисом авторизации: {str(exc)}"
        )

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
