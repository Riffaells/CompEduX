from fastapi import Request, HTTPException
from fastapi.responses import JSONResponse
import httpx
from typing import List, Dict
import logging
import time

from app.core.config import settings

logger = logging.getLogger(__name__)

# Простой кэш токенов для избежания постоянных запросов к сервису авторизации
token_cache: Dict[str, Dict] = {}
# Время жизни кэша токенов (секунды)
TOKEN_CACHE_TTL = 60

class AuthMiddleware:
    """
    Middleware для проверки JWT токенов и добавления информации о пользователе в запрос.
    """
    def __init__(
        self,
        public_paths: List[str] = None,
        auth_service_url: str = None
    ):
        # Определяем публичные пути, которые не требуют авторизации
        self.public_paths = public_paths or [
            # Системные эндпоинты
            "/docs",
            "/redoc",
            "/openapi.json",

            # API Gateway эндпоинты
            "/healthz",
            "/test-auth-connection",

            # Auth эндпоинты (с префиксом и без)
            f"{settings.API_V1_STR}/auth/login",
            f"{settings.API_V1_STR}/auth/register",
            f"{settings.API_V1_STR}/auth/refresh",
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/login",
            "/register"
        ]
        self.auth_service_url = auth_service_url or settings.AUTH_SERVICE_URL

    async def __call__(self, request: Request, call_next):
        # Проверяем, является ли путь публичным
        path = request.url.path
        if any(path.startswith(public_path) for public_path in self.public_paths):
            # Если путь публичный, пропускаем проверку авторизации
            return await call_next(request)

        # Проверяем наличие токена в заголовке запроса
        auth_header = request.headers.get("Authorization")
        if not auth_header or not auth_header.startswith("Bearer "):
            return JSONResponse(
                status_code=401,
                content={"detail": "Not authenticated"},
                headers={"WWW-Authenticate": "Bearer"}
            )

        # Получаем токен из заголовка
        token = auth_header.split(" ")[1]

        # Проверяем токен в кэше
        current_time = time.time()
        cache_entry = token_cache.get(token)
        if cache_entry and current_time - cache_entry.get('timestamp', 0) < TOKEN_CACHE_TTL:
            # Токен найден в кэше и еще действителен
            request.state.user_id = cache_entry.get('user_id')
            request.state.authenticated = True
            request.state.token = token
        else:
            # Токена нет в кэше или он устарел, проверяем через auth_service
            try:
                # Импортируем глобальный HTTP-клиент
                from app.main import http_client

                verify_url = f"{self.auth_service_url}/auth/verify-token"

                # Отправляем запрос на проверку токена, используя глобальный клиент
                response = await http_client.post(
                    verify_url,
                    headers={"Authorization": f"Bearer {token}"},
                )

                # Если токен недействителен
                if response.status_code != 200:
                    # Удаляем из кэша, если был
                    if token in token_cache:
                        del token_cache[token]

                    return JSONResponse(
                        status_code=401,
                        content={"detail": "Invalid or expired token"},
                        headers={"WWW-Authenticate": "Bearer"}
                    )

                # Токен действителен, обновляем кэш
                user_info = response.json()
                token_cache[token] = {
                    'user_id': user_info.get('user_id'),
                    'timestamp': current_time
                }

                # Добавляем информацию о пользователе в state запроса
                request.state.user_id = user_info.get("user_id")
                request.state.authenticated = True
                request.state.token = token

            except httpx.RequestError as exc:
                # Если auth_service недоступен, возвращаем ошибку
                logger.error(f"Auth service unavailable: {str(exc)}")
                return JSONResponse(
                    status_code=503,
                    content={"detail": "Authorization service unavailable"}
                )

        # Добавляем версию API в заголовки запроса к следующим сервисам
        request.headers.__dict__["_list"].append(
            (b"X-API-Version", b"v1")
        )

        # Продолжаем обработку запроса
        return await call_next(request)
