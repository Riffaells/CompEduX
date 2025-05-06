"""
Dependencies for API endpoints
"""
import uuid
from datetime import datetime, timezone
from typing import AsyncGenerator

import jwt
from app.core.config import settings
from app.db.db import get_async_session
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jwt.exceptions import PyJWTError
from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger

# Set up logger
logger = get_logger("course_service.api.deps")

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
        payload = jwt.decode(
            token,
            settings.AUTH_SECRET_KEY,
            algorithms=["HS256"]
        )

        # Check if token is expired
        exp = payload.get("exp")
        if not exp:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token: missing expiration"
            )

        # If token has expired
        if datetime.now(timezone.utc).timestamp() > exp:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token expired"
            )

        # Return user_id from token if needed
        # user_id = payload.get("sub")

        return True
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
            algorithms=["HS256"]
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
