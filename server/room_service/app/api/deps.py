"""
Dependencies for API endpoints
"""
import uuid
from datetime import datetime, timezone
from typing import AsyncGenerator, Dict, Any, Optional

import jwt
from app.core.config import settings
from app.db.db import get_async_session
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jwt.exceptions import PyJWTError
from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger

# Set up logger
logger = get_logger("room_service.api.deps")

# Security scheme for JWT tokens
security = HTTPBearer()


# Dependency for getting DB session
async def get_db() -> AsyncGenerator[AsyncSession, None]:
    """
    Get database session dependency

    Returns:
        AsyncSession: SQLAlchemy async session
    """
    async with get_async_session() as session:
        yield session


async def verify_token(
        credentials: HTTPAuthorizationCredentials = Depends(security),
) -> bool:
    """
    Verify JWT token from Authorization header

    This is a simplified token verification that doesn't retrieve the user.
    It only checks if the token is valid and not expired.

    Args:
        credentials: JWT token from Authorization header

    Returns:
        bool: True if token is valid

    Raises:
        HTTPException: If token is invalid or expired
    """
    try:
        token = credentials.credentials
        logger.debug(f"Верификация токена: {token[:20]}...")
        
        # Попытка декодировать токен
        try:
            payload = jwt.decode(
                token,
                settings.AUTH_SECRET_KEY,
                algorithms=[settings.JWT_ALGORITHM]
            )
            logger.debug(f"Токен успешно декодирован. Полезная нагрузка: {payload}")
        except PyJWTError as jwt_error:
            logger.error(f"Ошибка декодирования JWT: {str(jwt_error)}")
            raise jwt_error

        # Check if token is expired
        exp = payload.get("exp")
        if not exp:
            logger.error("Отсутствует поле exp в токене")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token: missing expiration"
            )

        # If token has expired
        current_time = datetime.now(timezone.utc).timestamp()
        if current_time > exp:
            logger.error(f"Токен истек. Текущее время: {current_time}, exp: {exp}")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token expired"
            )

        # Return user_id from token if needed
        # user_id = payload.get("sub")
        logger.debug("Токен прошел проверку")
        return True
    except PyJWTError as e:
        logger.error(f"JWT validation error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Invalid token: {str(e)}"
        )
    except Exception as e:
        logger.error(f"Unexpected error in token verification: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials"
        )


async def get_current_user_id(
        credentials: HTTPAuthorizationCredentials = Depends(security),
) -> uuid.UUID:
    """
    Get user ID from JWT token

    Args:
        credentials: JWT token from Authorization header

    Returns:
        UUID: User ID from token

    Raises:
        HTTPException: If token is invalid or expired
    """
    try:
        token = credentials.credentials
        payload = jwt.decode(
            token,
            settings.AUTH_SECRET_KEY,
            algorithms=[settings.JWT_ALGORITHM]
        )

        # Get user ID from token
        user_id_str = payload.get("sub")
        if not user_id_str:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token: missing user ID"
            )

        try:
            # Convert user ID to UUID
            user_id = uuid.UUID(user_id_str)
            return user_id
        except ValueError:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid user ID format"
            )
    except PyJWTError as e:
        logger.error(f"JWT validation error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Invalid token: {str(e)}"
        )
    except Exception as e:
        logger.error(f"Unexpected error in token verification: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials"
        )


async def get_current_user(
        credentials: HTTPAuthorizationCredentials = Depends(security),
) -> Dict[str, Any]:
    """
    Get current user from JWT token

    Args:
        credentials: JWT token from Authorization header

    Returns:
        Dict[str, Any]: User data from token

    Raises:
        HTTPException: If token is invalid or expired
    """
    try:
        token = credentials.credentials
        logger.debug(f"[get_current_user] Получен токен: {token[:20]}...")
        
        try:
            payload = jwt.decode(
                token,
                settings.AUTH_SECRET_KEY,
                algorithms=[settings.JWT_ALGORITHM]
            )
            logger.debug(f"[get_current_user] Токен успешно декодирован")
        except PyJWTError as jwt_error:
            logger.error(f"[get_current_user] Ошибка декодирования JWT: {str(jwt_error)}")
            raise jwt_error

        # Check if token is expired
        exp = payload.get("exp")
        if not exp:
            logger.error("[get_current_user] Отсутствует поле exp в токене")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token: missing expiration"
            )

        # If token has expired
        current_time = datetime.now(timezone.utc).timestamp()
        if current_time > exp:
            logger.error(f"[get_current_user] Токен истек. Текущее время: {current_time}, exp: {exp}")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token expired"
            )

        # Get user ID from token
        user_id = payload.get("sub")
        if not user_id:
            logger.error("[get_current_user] Отсутствует user_id (sub) в токене")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token: missing user ID"
            )

        # Convert payload to user data
        user_data = {
            "id": uuid.UUID(user_id),
            "email": payload.get("email", ""),
            "roles": payload.get("roles", []),
            "is_admin": "admin" in payload.get("roles", []),
            "display_name": payload.get("name", ""),
        }
        
        logger.debug(f"[get_current_user] Успешная аутентификация пользователя ID: {user_id}")
        return user_data
    except PyJWTError as e:
        logger.error(f"[get_current_user] JWT validation error: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Invalid token: {str(e)}"
        )
    except Exception as e:
        logger.error(f"[get_current_user] Unexpected error in token verification: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials"
        )


async def get_current_active_user(
        current_user: Dict[str, Any] = Depends(get_current_user),
) -> Dict[str, Any]:
    """
    Get current active user

    This dependency checks if the user is active.
    For now, we just assume all authenticated users are active.

    Args:
        current_user: User data from token

    Returns:
        Dict[str, Any]: User data

    Raises:
        HTTPException: If user is inactive
    """
    # TODO: Add check for inactive users when user management is implemented
    # if user disabled status is stored in the token or a database
    if current_user.get("disabled", False):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Inactive user"
        )
    return current_user
