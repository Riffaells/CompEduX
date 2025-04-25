import time
from typing import Optional, Dict, Any, Tuple

import jwt
from app.core.config import settings, SERVICE_ROUTES
from app.core.proxy import get_http_client
from fastapi import Request
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.security.utils import get_authorization_scheme_param
from jwt.exceptions import PyJWTError

from common.logger import get_logger

# Create base logger
logger = get_logger(__name__)

# Кэш для проверки токенов
# Структура: {token: (timestamp, user_info)}
_token_cache: Dict[str, Tuple[float, Dict[str, Any]]] = {}
_TOKEN_CACHE_TTL = 60  # секунды, сколько хранить токен в кэше

# Создаем объект для проверки Bearer токена
security = HTTPBearer(auto_error=False)


class AuthMiddleware:
    """
    Middleware для проверки JWT токена и добавления информации о пользователе
    к запросу.

    Поддерживает два режима работы:
    1. Локальная проверка JWT-токена (быстро, но без возможности отзыва)
    2. Проверка токена через сервис авторизации (медленнее, но надежнее)
    """

    def __init__(self, use_remote_validation: bool = True):
        """
        Инициализация middleware

        Args:
            use_remote_validation: Использовать ли удаленную валидацию через сервис авторизации
        """
        self.public_key = settings.JWT_PUBLIC_KEY
        self.algorithm = settings.JWT_ALGORITHM
        self.use_remote_validation = use_remote_validation

        # Определяем URL сервиса авторизации
        auth_config = SERVICE_ROUTES.get("auth", {})
        self.auth_service_url = auth_config.get("base_url")

    async def __call__(self, request: Request, call_next):
        """
        Проверяет JWT токен и добавляет информации о пользователе к запросу.
        Если токен невалидный или отсутствует, запрос продолжается, но без
        добавления информации о пользователе.
        """
        user = None

        # Получаем токен из заголовков
        credentials = self._get_credentials(request)

        # Если есть токен, проверяем его и получаем информацию о пользователе
        if credentials:
            token = credentials.credentials

            # Проверяем токен
            if self.use_remote_validation and self.auth_service_url:
                # Проверка через сервис авторизации
                user = await self._verify_token_remote(token)
            else:
                # Локальная проверка JWT
                try:
                    payload = self._verify_jwt_token(token)
                    if payload:
                        user = {
                            "id": str(payload.get("user_id", "")),
                            "email": payload.get("email", ""),
                            "username": payload.get("username", ""),
                            "is_admin": payload.get("is_admin", False),
                            "scopes": payload.get("scopes", [])
                        }
                except Exception as e:
                    logger.warning(f"Invalid token: {str(e)}")

        # Добавляем информацию о пользователе к запросу
        request.state.user = user

        # Продолжаем обработку запроса
        return await call_next(request)

    def _get_credentials(self, request: Request) -> Optional[HTTPAuthorizationCredentials]:
        """
        Извлекает Bearer токен из заголовка Authorization
        """
        authorization = request.headers.get("Authorization")
        scheme, credentials = get_authorization_scheme_param(authorization)

        if not authorization or scheme.lower() != "bearer":
            return None

        return HTTPAuthorizationCredentials(scheme=scheme, credentials=credentials)

    def _verify_jwt_token(self, token: str) -> Optional[Dict[str, Any]]:
        """
        Проверяет JWT токен локально и возвращает payload, если токен валидный
        """
        try:
            payload = jwt.decode(
                token,
                self.public_key,
                algorithms=[self.algorithm],
                options={"verify_signature": True}
            )
            return payload
        except PyJWTError as e:
            logger.warning(f"JWT validation error: {str(e)}")
            return None

    async def _verify_token_remote(self, token: str) -> Optional[Dict[str, Any]]:
        """
        Проверяет токен через сервис авторизации
        """
        global _token_cache

        # Проверяем кэш
        current_time = time.time()
        if token in _token_cache:
            timestamp, user_info = _token_cache[token]
            if current_time - timestamp < _TOKEN_CACHE_TTL:
                return user_info

        # Если не в кэше или устарел, проверяем через сервис
        client = await get_http_client()

        try:
            # Формируем URL для проверки токена
            if self.auth_service_url.endswith("/"):
                auth_url = f"{self.auth_service_url}verify-token"
            else:
                auth_url = f"{self.auth_service_url}/verify-token"

            # Отправляем запрос на проверку токена
            response = await client.post(
                auth_url,
                json={"token": token},
                timeout=3.0,
                headers={"Content-Type": "application/json"}
            )

            # Если токен валидный, получаем информацию о пользователе
            if response.status_code == 200:
                user_info = response.json()
                # Кэшируем результат
                _token_cache[token] = (current_time, user_info)
                return user_info

            # Если токен невалидный, возвращаем None
            logger.warning(f"Token validation failed: {response.status_code}")
            return None

        except Exception as e:
            logger.error(f"Error validating token remotely: {str(e)}")

            # В случае ошибки связи с сервисом авторизации
            # пробуем локальную проверку как запасной вариант
            try:
                payload = self._verify_jwt_token(token)
                if payload:
                    user = {
                        "id": str(payload.get("user_id", "")),
                        "email": payload.get("email", ""),
                        "username": payload.get("username", ""),
                        "is_admin": payload.get("is_admin", False),
                        "scopes": payload.get("scopes", [])
                    }
                    logger.info("Fallback to local token validation")
                    return user
            except Exception:
                pass

            return None
