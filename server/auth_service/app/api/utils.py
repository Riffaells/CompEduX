"""
Utility functions for API routes.

This module contains helper functions that are used across multiple API routes.
"""
from typing import Optional, Dict, Any
from datetime import datetime, timezone

import jwt
from fastapi import Request
from sqlalchemy import select
from sqlalchemy.orm import selectinload
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.enums import UserRole
from app.core.config import settings
from app.models.user import UserModel
from app.schemas.user import UserResponseSchema
from common.logger import get_logger

# Create base logger
logger = get_logger(__name__)

async def get_user_from_token(request: Request) -> Optional[Dict[str, Any]]:
    """
    Extract user information from token in the request.

    Args:
        request: FastAPI request object

    Returns:
        Dict with user information or None if token is invalid
    """
    token = request.headers.get("Authorization")
    if not token:
        return None

    if token.startswith("Bearer "):
        token = token[7:]
    
    try:
        # Just decode the token without verification to get the user ID
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
    logger.debug(f"Preparing user response for user ID: {user.id}")
    
    try:
        # Создаем словарь с данными пользователя вручную
        user_data = {
            "id": user.id,
            "username": user.username,
            "email": user.email,
            "role": user.role,
            "is_verified": user.is_verified,
            "is_active": user.is_active,
            "created_at": user.created_at,
            "updated_at": user.updated_at,
            "oauth_providers": []  # Пустой список во избежание SQL ошибок
        }
        
        # Добавляем lang если он существует
        if hasattr(user, "lang"):
            user_data["lang"] = user.lang
            
        # Добавляем last_login_at если он существует
        if hasattr(user, "last_login_at"):
            user_data["last_login_at"] = user.last_login_at
            
        # Добавляем auth_provider если он существует
        if hasattr(user, "auth_provider"):
            user_data["auth_provider"] = user.auth_provider
        
        # Добавляем данные профиля, если он существует
        if hasattr(user, "profile") and user.profile is not None:
            logger.debug(f"Adding profile data for user {user.id}")
            user_data["profile"] = {
                "first_name": user.profile.first_name,
                "last_name": user.profile.last_name,
                "avatar_url": user.profile.avatar_url,
                "bio": user.profile.bio,
                "location": user.profile.location
            }
        else:
            logger.debug(f"User {user.id} has no profile")
            user_data["profile"] = None
        
        # Добавляем настройки, если они существуют
        if hasattr(user, "preferences") and user.preferences is not None:
            logger.debug(f"Adding preferences for user {user.id}")
            user_data["preferences"] = {
                "beverage_preference": user.preferences.beverage_preference,
                "theme": getattr(user.preferences, "theme", "light"),
                "font_size": getattr(user.preferences, "font_size", "medium"),
                "email_notifications": getattr(user.preferences, "email_notifications", True),
                "push_notifications": getattr(user.preferences, "push_notifications", True),
                "break_reminder": getattr(user.preferences, "break_reminder", True),
                "break_interval_minutes": getattr(user.preferences, "break_interval_minutes", 60),
            }
        else:
            logger.debug(f"User {user.id} has no preferences")
            user_data["preferences"] = None
        
        # Добавляем рейтинги, если они существуют
        if hasattr(user, "ratings") and user.ratings is not None:
            logger.debug(f"Adding ratings for user {user.id}")
            
            # Проверяем, является ли ratings списком или одним объектом
            if isinstance(user.ratings, list):
                if user.ratings:  # Если список не пустой
                    rating = user.ratings[0]  # Берем первый рейтинг
                    user_data["ratings"] = {
                        "contribution_rating": getattr(rating, "contribution_rating", 0.0),
                        "bot_score": getattr(rating, "bot_score", 0.0),
                        "expertise_rating": getattr(rating, "expertise_rating", 0.0),
                        "competition_rating": getattr(rating, "competition_rating", 0.0),
                        "created_at": user.created_at,  # Используем время создания пользователя
                        "updated_at": user.updated_at   # Используем время обновления пользователя
                    }
                else:
                    # Если список пустой, создаем базовый объект рейтингов
                    user_data["ratings"] = {
                        "contribution_rating": 0.0,
                        "bot_score": 0.0,
                        "expertise_rating": 0.0,
                        "competition_rating": 0.0,
                        "created_at": user.created_at,
                        "updated_at": user.updated_at
                    }
            else:
                # Если это один объект, добавляем его как словарь
                user_data["ratings"] = {
                    "contribution_rating": getattr(user.ratings, "contribution_rating", 0.0),
                    "bot_score": getattr(user.ratings, "bot_score", 0.0),
                    "expertise_rating": getattr(user.ratings, "expertise_rating", 0.0),
                    "competition_rating": getattr(user.ratings, "competition_rating", 0.0),
                    "created_at": getattr(user.ratings, "created_at", user.created_at),
                    "updated_at": getattr(user.ratings, "updated_at", user.updated_at)
                }
        else:
            logger.debug(f"User {user.id} has no ratings, creating default ratings")
            # Создаем базовый объект рейтингов
            user_data["ratings"] = {
                "contribution_rating": 0.0,
                "bot_score": 0.0,
                "expertise_rating": 0.0,
                "competition_rating": 0.0,
                "created_at": user.created_at,
                "updated_at": user.updated_at
            }
        
        # Создаем объект схемы из словаря
        logger.debug(f"Creating UserResponseSchema for user {user.id}")
        user_response = UserResponseSchema.model_validate(user_data)
        
        logger.debug(f"Successfully prepared response for user {user.id}")
        return user_response
        
    except Exception as e:
        logger.error(f"Error preparing user response for user {user.id}: {str(e)}")
        logger.error(f"Error type: {type(e).__name__}")
        
        # В случае ошибки создаем минимальный ответ с правильной структурой
        current_time = datetime.now(timezone.utc)
        minimal_data = {
            "id": user.id,
            "username": getattr(user, "username", "unknown"),
            "email": getattr(user, "email", "unknown@example.com"),
            "role": getattr(user, "role", UserRole.USER),
            "is_verified": getattr(user, "is_verified", False),
            "is_active": getattr(user, "is_active", False),
            "created_at": getattr(user, "created_at", current_time),
            "updated_at": getattr(user, "updated_at", current_time),
            "oauth_providers": [],
            "profile": None,
            "preferences": None,
            "ratings": {
                "contribution_rating": 0.0,
                "bot_score": 0.0,
                "expertise_rating": 0.0,
                "competition_rating": 0.0,
                "created_at": getattr(user, "created_at", current_time),
                "updated_at": getattr(user, "updated_at", current_time)
            }
        }
        
        logger.debug(f"Created minimal response for user {user.id} due to error")
        return UserResponseSchema.model_validate(minimal_data)
