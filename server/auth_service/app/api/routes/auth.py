from datetime import timedelta
import logging
from typing import Dict, Any, Optional

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
    revoke_refresh_token,
    get_user_by_id,
    decode_jwt_token,
)
from ..utils import prepare_user_response

# Настройка логирования
logger = logging.getLogger(__name__)

router = APIRouter()

# Схема для JSON login
class LoginRequest(BaseModel):
    username: str  # Может быть email или username
    password: str

# Схема для ответа verify-token
class TokenVerifyResponse(BaseModel):
    id: str
    username: str
    email: str
    is_active: bool
    is_admin: bool
    created_at: Optional[str] = None

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


@router.post("/logout", status_code=status.HTTP_200_OK)
@router.get("/logout", status_code=status.HTTP_200_OK)
async def logout(
    request: Request,
    response: Response,
    token_data: TokenRefreshSchema = None,
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Logout - revoke refresh token.

    Token can be provided either in the request body or in the Authorization header.
    Returns 200 OK status on success.
    """
    logger.info(f"Logout request for user: {current_user.id}")
    success = False

    # Проверяем, есть ли тело запроса с refresh_token
    if token_data and token_data.refresh_token:
        success = revoke_refresh_token(token_data.refresh_token, db)
    else:
        # Если refresh токен не передан в теле, проверяем заголовок Authorization
        auth_header = request.headers.get('Authorization')
        if auth_header and auth_header.startswith('Bearer '):
            token = auth_header.replace('Bearer ', '')
            # Отзываем все активные токены пользователя
            tokens = db.query(RefreshTokenModel).filter(
                RefreshTokenModel.user_id == current_user.id,
                RefreshTokenModel.revoked == False
            ).all()

            if tokens:
                for token_obj in tokens:
                    token_obj.revoked = True
                db.commit()
                success = True

    if success:
        logger.info(f"Successfully logged out user: {current_user.id}")
        return {"message": "Successfully logged out"}
    else:
        logger.warning(f"Failed to logout user: {current_user.id}")
        return {"message": "No active tokens to revoke"}


@router.post("/verify-token", response_model=TokenVerifyResponse)
async def verify_token(
    request: Request,
    db: Session = Depends(get_db)
):
    """
    Verify token and return user info if token is valid.
    Used internally by API Gateway for authentication.

    Token can be provided in the Authorization header or in the request body.
    """
    # Пытаемся получить токен из разных источников
    token = None

    # 1. Проверяем заголовок Authorization
    auth_header = request.headers.get('Authorization')
    if auth_header and auth_header.startswith('Bearer '):
        token = auth_header.replace('Bearer ', '')

    # 2. Если в заголовке нет, проверяем тело запроса
    if not token:
        try:
            # Пытаемся прочитать тело как JSON
            body = await request.json()
            token = body.get('token')
        except Exception:
            # Игнорируем ошибки при парсинге JSON
            pass

    # Если токен так и не найден, возвращаем ошибку
    if not token:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Token not provided"
        )

    # Проверяем токен через current_user
    try:
        # Получаем полезную нагрузку из токена
        payload = decode_jwt_token(token)

        if not payload:
            logger.warning("Invalid token format")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token format"
            )

        # Получаем пользователя по ID из токена
        user_id = payload.get("sub")
        if not user_id:
            logger.warning("User ID not found in token")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="User ID not found in token"
            )

        # Проверяем существование пользователя
        user = get_user_by_id(db, user_id)
        if not user:
            logger.warning(f"User not found: {user_id}")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="User not found"
            )

        # Логируем успешную верификацию
        logger.info(f"Successfully verified token for user: {user.id}")

        # Возвращаем информацию о пользователе
        return {
            "id": str(user.id),
            "username": user.username,
            "email": user.email,
            "is_active": user.is_active,
            "is_admin": user.is_admin,
            "created_at": user.created_at.isoformat() if user.created_at else None
        }

    except Exception as e:
        logger.warning(f"Token verification failed: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token"
        )
