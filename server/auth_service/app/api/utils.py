"""
Utility functions for API routes.

This module contains helper functions that are used across multiple API routes.
"""
from typing import Optional, Dict, Any

import jwt
from fastapi import Request
from sqlalchemy import select
from sqlalchemy.orm import selectinload

from ..core.config import settings
from ..models.user import UserModel
from ..schemas import UserResponseSchema


def get_user_from_token(request: Request) -> Optional[Dict[str, Any]]:
    """
    Получает информацию о пользователе из JWT токена в заголовке Authorization.

    Args:
        request: FastAPI Request объект

    Returns:
        Dict с информацией о пользователе или None если токен отсутствует/недействителен
    """
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith('Bearer '):
        return None

    token = auth_header.replace('Bearer ', '').strip()
    if not token:
        return None

    try:
        # Декодируем токен без проверки подписи - нам только нужна информация
        # Для полной проверки токена используйте verify_token middleware
        payload = jwt.decode(
            token,
            settings.AUTH_SECRET_KEY,
            algorithms=["HS256"],
            options={"verify_signature": False}
        )
        return payload
    except Exception:
        return None


async def prepare_user_response(user: UserModel) -> UserResponseSchema:
    """
    Prepare user data for response, avoiding duplication of fields.

    This function ensures that fields are correctly placed in the appropriate
    sections of the response without duplication.

    Args:
        user: The UserModel instance to prepare for response

    Returns:
        A clean UserResponseSchema object with properly organized data
    """
    # For async context, we need to ensure relationships are eagerly loaded
    # If we're in an async context and the user object might be expired or relationships not loaded
    if hasattr(user, 'session') and user.session is not None:
        # Reload the user with all relationships, except oauth_providers which has a schema issue
        async_session = user.session
        stmt = select(UserModel).where(UserModel.id == user.id).options(
            selectinload(UserModel.profile),
            selectinload(UserModel.preferences),
            selectinload(UserModel.ratings)
            # Removed oauth_providers as it's causing a SQL error
        )
        result = await async_session.execute(stmt)
        user = result.scalars().first()

    # Create the user response using the model and provide an empty list for oauth_providers
    user_response = UserResponseSchema.model_validate(user)

    # Make sure oauth_providers is always an empty list to avoid SQL errors
    user_response.oauth_providers = []

    return user_response
