from fastapi import Request, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.security.utils import get_authorization_scheme_param
from typing import Optional, Dict, Any
import jwt
from jwt.exceptions import PyJWTError
import logging
import httpx
from app.core.config import settings
import time

logger = logging.getLogger(__name__)

# Используем глобальную переменную для кэширования состояния сервиса авторизации
_auth_service_up = None
_last_auth_check = 0
_AUTH_CHECK_INTERVAL = 60  # 1 минута между проверками

# Создаем объект для проверки Bearer токена
security = HTTPBearer(auto_error=False)

class AuthMiddleware:
    """
    Middleware для проверки JWT токена и добавления информации о пользователе
    к запросу.
    """

    def __init__(self):
        """Инициализация middleware"""
        self.public_key = settings.JWT_PUBLIC_KEY
        self.algorithm = settings.JWT_ALGORITHM

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
            try:
                # Проверяем токен с помощью JWT
                payload = self._verify_jwt_token(credentials.credentials)

                if payload:
                    # Добавляем информацию о пользователе к запросу
                    user = {
                        "id": str(payload.get("user_id", "")),
                        "email": payload.get("email", ""),
                        "username": payload.get("username", ""),
                        "is_admin": payload.get("is_admin", False),
                        "scopes": payload.get("scopes", [])
                    }

                    # Логгируем успешное получение пользователя
                    logger.debug(f"User authenticated: {user['username']}")
            except Exception as e:
                # Если токен невалидный, продолжаем запрос без добавления
                # информации о пользователе
                logger.warning(f"Invalid token: {str(e)}")

        # Добавляем информацию о пользователе к запросу (или None, если токен невалидный)
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
        Проверяет JWT токен и возвращает payload, если токен валидный
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
