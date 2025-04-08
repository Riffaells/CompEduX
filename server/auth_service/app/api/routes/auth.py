from datetime import timedelta
import logging

from fastapi import APIRouter, Depends, HTTPException, status, Response, Body, Request
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session
from pydantic import BaseModel, EmailStr

from ...core import settings
from ...db.session import get_db
from ...models.user import UserModel
from ...models.auth import RefreshTokenModel
from ...schemas import (
    TokenSchema,
    TokenRefreshSchema,
    UserCreateSchema,
    UserResponseSchema
)
from ...services.auth import (
    authenticate_user,
    create_access_token,
    create_refresh_token,
    create_user,
    get_current_user,
    refresh_access_token,
)
from ..utils import prepare_user_response

# Настройка логирования
logger = logging.getLogger(__name__)

router = APIRouter()

# Схема для JSON login
class LoginRequest(BaseModel):
    username: str  # Может быть email или username
    password: str

@router.post("/register", response_model=TokenSchema)
async def register(user_in: UserCreateSchema, db: Session = Depends(get_db)):
    """
    Register a new user and get access token.

    Handles regular users and test accounts with special email formats.
    Test accounts use format: test+anything@test.com
    """
    try:
        # Check if this is a test account
        is_test_account = user_in.email and user_in.email.startswith("test+") and user_in.email.endswith("@test.com")

        if is_test_account:
            logger.info(f"Processing test account registration: {user_in.email}")

        # Создаем пользователя - включая логику для тестового аккаунта
        user = create_user(db, user_in)

        if is_test_account:
            logger.info(f"Test account created/updated successfully: {user.email} (ID: {user.id})")

        # Генерируем access token - логика JWT-авторизации
        access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
        access_token = create_access_token(
            data={"sub": str(user.id)},
            expires_delta=access_token_expires
        )

        # Генерируем refresh token
        refresh_token = create_refresh_token(user.id, db)

        return {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "token_type": "bearer"
        }
    except Exception as e:
        db.rollback()  # Откатываем транзакцию в случае ошибки
        logger.error(f"Error during user registration: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Registration failed: {str(e)}"
        )


@router.post("/login", response_model=TokenSchema)
async def login(
    login_data: LoginRequest,
    db: Session = Depends(get_db)
):
    """
    Authenticate user and get tokens.

    Accepts JSON with username and password.
    Username can be either email or username.

    Returns only authentication tokens, not user data.
    """
    username = login_data.username
    password = login_data.password

    # Аутентификация пользователя
    user = authenticate_user(db, username, password)

    if not user:
        logger.info(f"Failed login attempt for: {username}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username/email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Логируем успешный вход
    logger.info(f"Successful login for: {username} ({user.id})")

    # Create access token
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": str(user.id)},
        expires_delta=access_token_expires
    )

    # Create refresh token
    refresh_token = create_refresh_token(user.id, db)

    # Возвращаем только токены, без информации о пользователе
    return {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer"
    }


@router.post("/refresh", response_model=TokenSchema)
async def refresh(
    token_data: TokenRefreshSchema,
    db: Session = Depends(get_db)
):
    """
    Refresh access token using refresh token.

    Returns new access and refresh tokens.
    """
    return refresh_access_token(token_data, db)


@router.get("/me", response_model=UserResponseSchema)
async def get_current_user_info(
    current_user: UserModel = Depends(get_current_user)
):
    """
    Get information about the current authenticated user.

    Returns full user data, properly structured without duplication.
    """
    return prepare_user_response(current_user)


@router.post("/logout")
async def logout(
    token_data: TokenRefreshSchema,
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Logout (revoke refresh token)"""
    # Find refresh token in database
    db_token = db.query(RefreshTokenModel).filter(
        RefreshTokenModel.token == token_data.refresh_token,
        RefreshTokenModel.user_id == current_user.id,
        RefreshTokenModel.revoked == False
    ).first()

    if db_token:
        # Mark token as revoked
        db_token.revoked = True
        db.commit()

    return {"message": "Successfully logged out"}


@router.post("/verify-token")
async def verify_token(current_user: UserModel = Depends(get_current_user)):
    """Verify token and return user ID"""
    return {"user_id": str(current_user.id), "valid": True}
