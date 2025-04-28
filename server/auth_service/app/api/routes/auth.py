import uuid
from datetime import timedelta, datetime, timezone
from typing import Optional, Tuple, Dict, Any

from fastapi import APIRouter, Depends, HTTPException, status, Request, FastAPI
from pydantic import BaseModel
from slowapi import Limiter
from slowapi.util import get_remote_address
from sqlalchemy import update, select, func, text
from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger
from ..utils import prepare_user_response
from ...core import settings
from ...db.session import get_db
from ...models.auth import RefreshTokenModel
from ...models.enums import UserRole
from ...models.user import UserModel
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
    decode_jwt_token,
    get_user_by_id,
)

# Create base logger
logger = get_logger(__name__)

router = APIRouter()

limiter = Limiter(key_func=get_remote_address)
app = FastAPI()
app.state.limiter = limiter


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
async def register(user_in: UserCreateSchema,
                   db: AsyncSession = Depends(get_db)):
    """
    Register a new user and get access token.

    Handles regular users and test accounts with special email formats.
    Test accounts use format: test+anything@test.com
    """
    logger.info(f"Starting registration for email: {user_in.email}")
    try:
        # Ensure the session won't expire objects after commit
        db.expire_on_commit = False

        # Check if this is a test account
        is_test_account = user_in.email and user_in.email.startswith("test+") and user_in.email.endswith("@test.com")

        if is_test_account:
            logger.info(f"Processing test account registration: {user_in.email}")

        # Create user with proper lazy loading handling
        logger.debug("Calling create_user function")
        user = await create_user(db, user_in)
        logger.debug(f"create_user completed successfully, user id: {user.id}")

        # Generate tokens (no database access needed for access token)
        logger.debug("Generating access token")
        access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
        access_token = create_access_token(
            data={"sub": str(user.id)},
            expires_delta=access_token_expires
        )
        logger.debug("Access token generated")

        # Create refresh token
        logger.debug("Generating refresh token")
        refresh_token = await create_refresh_token(user.id, db)
        logger.debug("Refresh token generated")

        if is_test_account:
            logger.info(f"Test account created/updated successfully: {user.email} (ID: {user.id})")

        logger.info(f"Registration successful for: {user.email}")
        return {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "token_type": "bearer"
        }
    except Exception as e:
        # Handle errors, with special handling for greenlet_spawn errors
        logger.error(f"Error during user registration: {str(e)}")
        logger.error(f"Error type: {type(e)}")

        # Specific handling for transaction errors
        if "transaction is already begun" in str(e):
            logger.error("Transaction conflict detected")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Database transaction error. Please try again."
            )
        # Specific handling for greenlet_spawn errors
        elif "greenlet_spawn has not been called" in str(e):
            logger.error("Detected greenlet_spawn error in async context")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Database access error in async context. This is usually caused by trying to access lazy-loaded attributes."
            )
        elif isinstance(e, HTTPException):
            # Re-raise HTTPExceptions as-is
            raise e
        else:
            # Generic error handling
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Registration failed: {str(e)}"
            )


@router.post("/login", response_model=TokenSchema)
async def login(
        login_data: LoginRequest,
        db: AsyncSession = Depends(get_db)
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
    user = await authenticate_user(db, username, password)

    if not user:
        logger.info(f"Failed login attempt for: {username}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username/email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Логируем успешный вход
    logger.info(f"Successful login for: {username}")

    # Create access token
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": str(user.id)},
        expires_delta=access_token_expires
    )

    # Create refresh token
    refresh_token = await create_refresh_token(user.id, db)

    # Возвращаем только токены, без информации о пользователе
    return {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer"
    }


@router.post("/refresh", response_model=TokenSchema)
async def refresh(
        token_data: TokenRefreshSchema,
        db: AsyncSession = Depends(get_db)
):
    """
    Refresh access token using refresh token.

    Returns new access and refresh tokens.
    """
    return await refresh_access_token(token_data, db)


@router.get("/me", response_model=UserResponseSchema)
async def get_current_user_info(
        current_user: UserModel = Depends(get_current_user)
):
    """
    Get information about the current authenticated user.

    Returns full user data, properly structured without duplication.
    """
    return await prepare_user_response(current_user)


@router.post("/logout")
@router.get("/logout")
async def logout(request: Request, db: AsyncSession = Depends(get_db)):
    """
    Выход пользователя из системы путем отзыва всех refresh токенов.
    Поддерживает как GET, так и POST запросы.
    """
    # Извлечение токена
    token, error = extract_token_from_request(request)
    if error:
        logger.warning(f"Token extraction error during logout: {error}")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=error)

    # Проверка токена и получение пользователя
    user, error = await validate_token_and_get_user(token, db)
    if error:
        logger.warning(f"Token validation error during logout: {error}")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=error)

    # Отзыв всех активных refresh токенов пользователя
    try:
        # First query to find tokens
        result = await db.execute(
            select(RefreshTokenModel)
            .filter(RefreshTokenModel.user_id == user.id)
            .filter(RefreshTokenModel.revoked == False)
        )
        tokens = result.scalars().all()
        tokens_count = len(tokens)

        # Update operation
        if tokens_count > 0:
            # Прямое обновление данных без контекстного менеджера
            await db.execute(
                update(RefreshTokenModel)
                .where(RefreshTokenModel.user_id == user.id)
                .where(RefreshTokenModel.revoked == False)
                .values(revoked=True)
            )
            # Коммит изменений
            await db.commit()
            logger.debug(f"Revoked {tokens_count} tokens for user {user.id}")

        logger.info(f"Successfully logged out user {user.id}, revoked {tokens_count} tokens")
        return {"message": "Successfully logged out", "revoked_tokens": tokens_count}
    except Exception as e:
        # Откатываем транзакцию при ошибке
        await db.rollback()
        logger.error(f"Failed to revoke tokens for user {user.id}: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to revoke tokens"
        )


@router.post("/verify-token", response_model=TokenVerifyResponse)
@router.get("/verify-token", response_model=TokenVerifyResponse)
async def verify_token(request: Request, db: AsyncSession = Depends(get_db)):
    """
    Проверяет токен и возвращает информацию о пользователе.
    Принимает токен из заголовка Authorization или из тела запроса.
    Поддерживает как GET, так и POST запросы.
    """
    # Извлечение токена
    token = None
    error = None

    # Получаем заголовок Authorization
    auth_header = request.headers.get('Authorization')
    if auth_header and auth_header.startswith('Bearer '):
        token = auth_header.replace('Bearer ', '').strip()

    # Если токен не найден в заголовке, попробуем получить его из тела запроса
    if not token:
        try:
            body = await request.json()
            token = body.get('token')
            if token:
                logger.info("Получен токен из тела запроса")
        except Exception as e:
            logger.warning(f"Не удалось получить токен из тела запроса: {str(e)}")

    # Если токен не найден
    if not token:
        logger.warning("Токен не найден ни в заголовке, ни в теле запроса")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED,
                            detail="Токен не предоставлен")

    # Проверка токена и получение пользователя
    user, error = await validate_token_and_get_user(token, db)
    if error:
        logger.warning(f"Token validation error: {error}")
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail=error)

    # Логирование успешной проверки
    logger.info(f"Token successfully verified for user: {user.id}")

    # Проверяем админа по роли
    is_admin = False
    if hasattr(user, 'role'):
        is_admin = user.role == UserRole.ADMIN

    # Возврат информации о пользователе
    return {
        "id": str(user.id),
        "username": user.username,
        "email": user.email,
        "is_active": user.is_active,
        "is_admin": is_admin
    }


def extract_token_from_request(request: Request) -> Tuple[Optional[str], Optional[str]]:
    """
    Извлекает токен из запроса и возвращает его вместе с сообщением об ошибке, если есть.
    """
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith('Bearer '):
        return None, "Authorization header missing or invalid"

    token = auth_header.replace('Bearer ', '').strip()
    if not token:
        return None, "Empty token provided"

    return token, None


async def validate_token_and_get_user(token: str, db: AsyncSession) -> Tuple[Optional[UserModel], Optional[str]]:
    """
    Проверяет токен и возвращает пользователя или сообщение об ошибке.
    """
    try:
        # Проверка токена
        payload = decode_jwt_token(token)
        if not payload:
            return None, "Invalid token format"

        # Получение ID пользователя из токена
        user_id = payload.get("sub")
        if not user_id:
            return None, "User ID not found in token"

        # Получение пользователя из базы данных
        user = await get_user_by_id(db, uuid.UUID(user_id))
        if not user:
            return None, f"User not found: {user_id}"

        # Проверка активности пользователя
        if not user.is_active:
            return None, f"User account is inactive: {user_id}"

        return user, None
    except Exception as e:
        logger.error(f"Token validation error: {str(e)}")
        return None, f"Token validation failed: {str(e)}"


@router.get("/stats", response_model=Dict[str, Any])
async def get_auth_stats(
        db: AsyncSession = Depends(get_db),
        current_user: UserModel = Depends(get_current_user)
):
    """
    Получение статистики по аутентификации (только для администраторов).
    """
    # Проверка прав администратора
    if not hasattr(current_user, 'role') or current_user.role != UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )

    try:
        # Count active users
        active_users_result = await db.execute(
            select(func.count()).select_from(UserModel).where(UserModel.is_active == True)
        )
        active_users = active_users_result.scalar()

        # Count active tokens
        active_tokens_result = await db.execute(
            select(func.count()).select_from(RefreshTokenModel).where(RefreshTokenModel.revoked == False)
        )
        active_tokens = active_tokens_result.scalar()

        # Logins in last 24 hours
        yesterday = datetime.now(timezone.utc) - timedelta(days=1)
        logins_24h_result = await db.execute(
            select(func.count()).select_from(RefreshTokenModel).where(RefreshTokenModel.created_at >= yesterday)
        )
        logins_24h = logins_24h_result.scalar()

        return {
            "active_users": active_users,
            "active_tokens": active_tokens,
            "logins_24h": logins_24h,
            "server_time": datetime.now(timezone.utc).isoformat()
        }
    except Exception as e:
        logger.error(f"Failed to get auth stats: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to get auth stats"
        )


@router.get("/health")
async def health_check(db: AsyncSession = Depends(get_db)):
    """
    Проверка здоровья сервиса аутентификации.
    """
    health_status = {
        "status": "healthy",
        "components": {},
        "timestamp": datetime.now(timezone.utc).isoformat()
    }

    # Проверка подключения к базе данных
    try:
        # Более безопасный способ проверки соединения
        result = await db.execute(text("SELECT 1"))
        health_status["components"]["database"] = "connected" if result.scalar() == 1 else "error"
    except Exception as e:
        logger.error(f"Database health check failed: {str(e)}")
        health_status["components"]["database"] = "disconnected"
        health_status["status"] = "unhealthy"

    # Проверка JWT функциональности
    try:
        test_data = {"test": "data"}
        test_token = create_access_token(test_data, expires_delta=timedelta(minutes=1))
        test_payload = decode_jwt_token(test_token)

        if test_payload and "test" in test_payload and test_payload["test"] == "data":
            health_status["components"]["jwt"] = "working"
        else:
            health_status["components"]["jwt"] = "error"
            health_status["status"] = "unhealthy"
    except Exception as e:
        logger.error(f"JWT health check failed: {str(e)}")
        health_status["components"]["jwt"] = "error"
        health_status["status"] = "unhealthy"

    # Возвращаем статус с кодом 200, даже если сервис нездоров
    # Это позволяет получить детальную информацию о проблеме
    return health_status
