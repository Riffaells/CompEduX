"""
Dependencies module for API Gateway
"""
from typing import Dict, Any, Optional

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

from app.core.config import settings

# Создаем объект для авторизации через HTTP Bearer
security = HTTPBearer()


async def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)) -> Dict[str, Any]:
    """
    Get current user from JWT token
    
    Args:
        credentials: HTTP Authorization credentials
    
    Returns:
        Dict with user information
        
    Raises:
        HTTPException: If token is invalid or expired
    """
    try:
        # Get token from Authorization header
        token = credentials.credentials
        
        # Decode JWT token
        payload = jwt.decode(
            token, 
            settings.AUTH_SECRET_KEY, 
            algorithms=[settings.JWT_ALGORITHM]
        )
        
        # Get user ID from payload
        user_id = payload.get("sub")
        
        if user_id is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Could not validate credentials",
                headers={"WWW-Authenticate": "Bearer"},
            )
        
        return payload
    except jwt.PyJWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Could not validate credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )


async def get_current_active_user(current_user: Dict[str, Any] = Depends(get_current_user)) -> Dict[str, Any]:
    """
    Get current active user (checks if user is active)
    
    Args:
        current_user: Current user information
        
    Returns:
        Dict with user information
        
    Raises:
        HTTPException: If user is not active
    """
    if current_user.get("disabled", False):
        raise HTTPException(status_code=400, detail="Inactive user")
    return current_user 