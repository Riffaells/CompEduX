from fastapi import Depends, HTTPException, status, Header
from typing import Optional
import requests
import os

# Функция для проверки токена через auth_service
async def get_current_user_id(authorization: Optional[str] = Header(None)):
    if not authorization:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # В реальном приложении здесь будет запрос к auth_service
    # для проверки токена и получения ID пользователя
    AUTH_SERVICE_URL = os.getenv("AUTH_SERVICE_URL", "http://auth_service:8000")

    try:
        # Здесь должен быть запрос к auth_service для проверки токена
        # Для примера просто возвращаем фиктивный ID
        return 1  # Фиктивный ID пользователя
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Authentication error: {str(e)}",
            headers={"WWW-Authenticate": "Bearer"},
        )
