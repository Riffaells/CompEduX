"""
Authentication services for API Gateway
"""
from fastapi import Depends, HTTPException, status, Request
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from app.core.config import settings

# Создаем объект для проверки Bearer токена
security = HTTPBearer(auto_error=True)


async def get_current_user_token(
    credentials: HTTPAuthorizationCredentials = Depends(security)
) -> str:
    """
    Получает токен из заголовка Authorization и проверяет его наличие.
    Используется для эндпоинтов, требующих аутентификации.
    """
    return credentials.credentials


async def get_current_admin_token(
    request: Request,
    token: str = Depends(get_current_user_token)
) -> str:
    """
    Проверяет, что пользователь является администратором.
    Используется для эндпоинтов, требующих прав администратора.
    """
    user = request.state.user
    
    if not user or not user.get("is_admin", False):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    
    return token 