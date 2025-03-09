import httpx
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
import os
from typing import Optional, Dict, Any

# URL для сервиса аутентификации
AUTH_SERVICE_URL = os.getenv("AUTH_SERVICE_URL", "http://auth_service:8000")

# OAuth2 схема для получения токена из заголовка Authorization
oauth2_scheme = OAuth2PasswordBearer(tokenUrl=f"{AUTH_SERVICE_URL}/token")

async def verify_token(token: str) -> Dict[str, Any]:
    """
    Проверяет токен через сервис аутентификации.

    Args:
        token: JWT токен для проверки

    Returns:
        Dict с информацией о пользователе, если токен действителен

    Raises:
        HTTPException: Если токен недействителен
    """
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{AUTH_SERVICE_URL}/verify-token",
            params={"token": token}
        )

        if response.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Could not validate credentials",
                headers={"WWW-Authenticate": "Bearer"},
            )

        result = response.json()
        if not result.get("valid"):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid authentication credentials",
                headers={"WWW-Authenticate": "Bearer"},
            )

        return result

async def get_current_user(token: str = Depends(oauth2_scheme)) -> Dict[str, Any]:
    """
    Получает информацию о текущем пользователе из токена.

    Args:
        token: JWT токен из заголовка Authorization

    Returns:
        Dict с информацией о пользователе

    Raises:
        HTTPException: Если токен недействителен
    """
    return await verify_token(token)

async def get_current_user_id(token: str = Depends(oauth2_scheme)) -> int:
    """
    Получает ID текущего пользователя из токена.

    Args:
        token: JWT токен из заголовка Authorization

    Returns:
        ID пользователя

    Raises:
        HTTPException: Если токен недействителен
    """
    user_info = await verify_token(token)
    return user_info.get("user_id")

async def check_admin_role(token: str = Depends(oauth2_scheme)) -> Dict[str, Any]:
    """
    Проверяет, имеет ли текущий пользователь роль администратора.

    Args:
        token: JWT токен из заголовка Authorization

    Returns:
        Dict с информацией о пользователе, если у него роль администратора

    Raises:
        HTTPException: Если у пользователя нет прав администратора
    """
    user_info = await verify_token(token)

    if user_info.get("role") != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )

    return user_info

async def check_moderator_role(token: str = Depends(oauth2_scheme)) -> Dict[str, Any]:
    """
    Проверяет, имеет ли текущий пользователь роль модератора или администратора.

    Args:
        token: JWT токен из заголовка Authorization

    Returns:
        Dict с информацией о пользователе, если у него роль модератора или администратора

    Raises:
        HTTPException: Если у пользователя нет прав модератора или администратора
    """
    user_info = await verify_token(token)

    if user_info.get("role") not in ["admin", "moderator"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )

    return user_info
