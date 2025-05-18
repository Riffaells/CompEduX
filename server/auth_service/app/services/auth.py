# -*- coding: utf-8 -*-
"""
Authentication service module for auth_service
"""
import random
import string
from datetime import UTC, datetime, timedelta
from typing import Optional, Dict, Any
from uuid import UUID

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from passlib.context import CryptContext
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from common.logger import get_logger
from ..core.config import settings
from ..core.constants import (
    JWT_ALGORITHM
)
from ..db.session import get_db
from ..models.auth import RefreshTokenModel
from ..models.enums import BeveragePreference
from ..models.user import UserModel, UserProfileModel, UserPreferencesModel, UserRatingModel
from ..schemas.auth import TokenRefreshSchema
from ..schemas.user import UserCreateSchema
from ..utils import handle_sqlalchemy_errors, db_transaction

# JWT settings
ALGORITHM = "HS256"
# Указываем несколько возможных URL для tokenUrl
# Это позволит Swagger UI корректно работать с разными версиями API
oauth2_scheme = OAuth2PasswordBearer(
    tokenUrl=f"{settings.API_V1_STR}/auth/login",
    # Дополнительные URL для безопасности (для работы без версии и через заголовки)
    auto_error=True,
)

# Password hashing settings
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

logger = get_logger(__name__)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verify password against hash"""
    return pwd_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    """Hash password"""
    return pwd_context.hash(password)


def decode_jwt_token(token: str) -> Dict[str, Any]:
    """
    Decode JWT token and return payload.

    Args:
        token: JWT token string

    Returns:
        Dictionary with token payload

    Raises:
        HTTPException: If token is invalid or expired
    """
    try:
        payload = jwt.decode(token, settings.AUTH_SECRET_KEY, algorithms=[ALGORITHM])
        return payload
    except JWTError as e:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Could not validate credentials: {str(e)}",
            headers={"WWW-Authenticate": "Bearer"},
        )


def create_access_token(data: Dict[str, Any], expires_delta: Optional[timedelta] = None) -> str:
    """Create JWT access token"""
    to_encode = data.copy()

    if expires_delta:
        expire = datetime.now(UTC) + expires_delta
    else:
        expire = datetime.now(UTC) + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)

    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, settings.AUTH_SECRET_KEY, algorithm=ALGORITHM)

    return encoded_jwt


async def create_refresh_token(user_id: UUID, db: AsyncSession) -> str:
    """
    Create a refresh token for a user and store it in the database

    Args:
        user_id: User ID
        db: Database session

    Returns:
        Refresh token string
    """
    logger = get_logger(__name__)
    logger.debug(f"Creating refresh token for user {user_id}")

    # Ensure the session won't expire objects after commit
    db.expire_on_commit = False

    # Use consistent timezone-aware datetime objects with UTC timezone
    now = datetime.now(UTC)
    expires_at = now + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)

    # For JWT token, use UTC timestamps
    token_data = {
        "sub": str(user_id),
        "type": "refresh",
        "exp": int(expires_at.timestamp()),  # Convert to integer timestamp
        "iat": int(now.timestamp())  # Convert to integer timestamp
    }
    refresh_token = jwt.encode(token_data, settings.AUTH_SECRET_KEY, algorithm=ALGORITHM)
    logger.debug("JWT token encoded successfully")

    # Создаем запись о токене обновления в БД
    db_token = RefreshTokenModel(
        user_id=user_id,
        token=refresh_token,
        expires_at=expires_at  # Using timezone-aware datetime
    )

    # Add token to database
    try:
        # Add to session
        db.add(db_token)

        # Try to commit - will raise an exception if there's a transaction already
        try:
            await db.commit()
            logger.debug("Refresh token committed to database")
        except Exception as commit_error:
            if "transaction is already begun" in str(commit_error):
                # We're in a transaction already - that's ok
                logger.debug("Refresh token added to session but not committed (in existing transaction)")
            else:
                # Some other error occurred
                logger.error(f"Error committing refresh token: {str(commit_error)}")
                raise

        return refresh_token
    except Exception as e:
        try:
            await db.rollback()
        except:
            # Rollback may fail if we're already in a transaction
            pass
        logger.error(f"Error during refresh token creation: {str(e)}")
        raise


async def get_user_by_email(db: AsyncSession, email: str) -> Optional[UserModel]:
    """
    Get user by email.

    Args:
        db: Async database session
        email: User email

    Returns:
        UserModel or None if not found
    """
    logger = get_logger(__name__)
    logger.debug(f"get_user_by_email: Looking up user with email: {email}")

    try:
        # Создаем запрос, не выполняя его
        query = select(UserModel).filter(UserModel.email == email)
        logger.debug(f"get_user_by_email: Created query: {query}")

        # Выполняем запрос
        result = await db.execute(query)
        logger.debug("get_user_by_email: Query executed successfully")

        # Получаем первый результат
        user = result.scalars().first()
        logger.debug(f"get_user_by_email: User found: {user is not None}")
        return user
    except Exception as e:
        logger.error(f"get_user_by_email: Error querying user: {str(e)}, {type(e)}")
        logger.error(f"get_user_by_email: Error details: {e.__traceback__}")
        raise


async def get_user_by_username(db: AsyncSession, username: str) -> Optional[UserModel]:
    """Get user by username"""
    logger = get_logger(__name__)
    logger.debug(f"get_user_by_username: Looking up user with username: {username}")

    try:
        # В SQLAlchemy 2.0 с AsyncSession используем select вместо query
        logger.debug("get_user_by_username: Creating select query")
        query = select(UserModel).filter(UserModel.username == username)
        logger.debug(f"get_user_by_username: Query: {query}")

        logger.debug("get_user_by_username: Executing query")
        result = await db.execute(query)
        logger.debug("get_user_by_username: Query executed successfully")

        user = result.scalars().first()
        logger.debug(f"get_user_by_username: User found: {user is not None}")
        return user
    except Exception as e:
        logger.error(f"get_user_by_username: Error querying user: {str(e)}, {type(e)}")
        logger.error(f"get_user_by_username: Error details: {e.__traceback__}")
        raise


async def get_user_by_id(db: AsyncSession, user_id: UUID) -> Optional[UserModel]:
    """
    Get user by ID with all related models using eager loading
    to prevent greenlet_spawn errors in async context
    """
    # Use selectinload to eagerly load all relationships and prevent
    # lazy loading which can cause greenlet_spawn errors
    result = await db.execute(
        select(UserModel)
        .filter(UserModel.id == user_id)
        .options(
            selectinload(UserModel.profile),
            selectinload(UserModel.preferences),
            selectinload(UserModel.ratings)
        )
    )
    return result.scalars().first()


async def authenticate_user(db: AsyncSession, login: str, password: str) -> Optional[UserModel]:
    """
    Authenticate user by email or username

    Args:
        db: Database session
        login: Email or username
        password: User password

    Returns:
        User model if authentication successful, None otherwise
    """
    logger = get_logger(__name__)
    from ..core.config import settings

    # Специальный случай для тестового аккаунта
    is_test_account = False

    # В любом режиме test@example.com или test считаются тестовыми
    is_main_test = login == "test@example.com" or login == "test"

    # В режиме разработки также считаем тестовыми любой логин со словом "test"
    if settings.ENV == "development":
        is_dev_test = (
                login and
                ("test" in login.lower() or
                 (login.lower().endswith("@test.com")))
        )
        is_test_account = is_main_test or is_dev_test
    else:
        # В продакшене только строгие "test" или "test@example.com" считаются тестовыми
        is_test_account = is_main_test

    # Для тестового аккаунта - особая обработка
    if is_test_account:
        logger.info(f"Processing test account login: {login}")

        # Получаем пользователя, если он существует
        if '@' in login:
            user = await get_user_by_email(db, login)
        else:
            user = await get_user_by_username(db, login)

        # Если тестовый пользователь не существует, создаем его
        if not user:
            test_email = login if '@' in login else "test@example.com"
            test_username = login if '@' not in login else "test"

            logger.info(f"Creating test account: {test_email} / {test_username}")

            test_user_data = UserCreateSchema(
                email=test_email,
                username=test_username,
                password=password or "test123",
                first_name="Test",
                last_name="User",
                beverage_preference=BeveragePreference.NONE
            )
            user = await create_user(db, test_user_data)

        # Обновляем last_login_at без использования вложенной транзакции
        if user:
            # Обновляем время последнего входа
            user.last_login_at = datetime.now(UTC)
            db.add(user)

            # Коммитим изменения
            await db.commit()
            logger.debug("Last login time updated for test user")

        # Всегда возвращаем пользователя без проверки пароля
        return user

    # Стандартная обработка для обычных пользователей
    # Check if login is email or username
    if '@' in login:
        # Try to get user by email
        user = await get_user_by_email(db, login)
    else:
        # Try to get user by username
        user = await get_user_by_username(db, login)

    if not user or not user.hashed_password:
        return None

    if not verify_password(password, user.hashed_password):
        return None

    # Update last login timestamp без использования вложенной транзакции
    user.last_login_at = datetime.now(UTC)
    db.add(user)

    # Коммитим изменения
    await db.commit()
    logger.debug("Last login time updated for user")

    return user


async def generate_username(email: str, db: AsyncSession) -> str:
    """
    Generate a unique username based on the email address.

    Args:
        email: User's email address
        db: Database session

    Returns:
        A unique username that follows validation rules
    """
    # Start with the part before @ in emailNF
    base_username = email.split('@')[0].lower()

    # Replace special characters with underscore
    base_username = ''.join(c if c.isalnum() else '_' for c in base_username)

    # Remove consecutive underscores
    while '__' in base_username:
        base_username = base_username.replace('__', '_')

    # Remove leading/trailing underscores
    base_username = base_username.strip('_')

    # If username is too short, add some random characters
    if len(base_username) < 3:
        base_username += ''.join(random.choices(string.ascii_lowercase, k=3 - len(base_username)))

    # If username is too long, truncate it
    if len(base_username) > 25:  # Leave room for numbers
        base_username = base_username[:25]

    # Check if username exists
    username = base_username
    counter = 1

    while await get_user_by_username(db, username):
        # If username exists, add a number to it
        username = f"{base_username}{counter}"
        counter += 1

    return username


async def create_user(db: AsyncSession, user_in: UserCreateSchema) -> UserModel:
    """
    Create a new user record, with special handling for test accounts.

    For test accounts (email format: test+*@test.com):
    - Updates existing test account if found
    - Creates new test account if not found
    - Sets a predefined password

    In development mode:
    - Any user with "test" in their email is treated as a test account
    - Test accounts can be freely updated/recreated

    Returns the created or updated user object.
    """
    logger = get_logger(__name__)
    logger.info("Starting create_user function...")

    # Ensure session won't expire objects after commit
    db.expire_on_commit = False

    # Extract profile and preference fields
    profile_data = {}
    preference_data = {}

    # Get profile fields
    for field in ["first_name", "last_name", "avatar_url", "bio", "location"]:
        if hasattr(user_in, field) and getattr(user_in, field) is not None:
            profile_data[field] = getattr(user_in, field)

    # Get preference fields
    if hasattr(user_in, "beverage_preference") and user_in.beverage_preference is not None:
        preference_data["beverage_preference"] = user_in.beverage_preference

    from ..core.config import settings

    # Определяем, является ли это тестовым аккаунтом
    is_test_account = False

    # Проверяем формат test+xxx@test.com (для любого окружения)
    is_plus_test = user_in.email and user_in.email.startswith("test+") and user_in.email.endswith("@test.com")

    # В режиме разработки также считаем тестовыми любые email со словом "test"
    if settings.ENV == "development":
        is_dev_test = (
                user_in.email and
                ("test" in user_in.email.lower() or
                 user_in.email.lower().endswith("@test.com"))
        )
        is_test_account = is_plus_test or is_dev_test
    else:
        # В production только test+xxx@test.com считаются тестовыми
        is_test_account = is_plus_test

    if is_test_account:
        logger.info(f"Processing test account: {user_in.email}")
        # For test accounts, check if it already exists with eager loading
        logger.debug("Attempting to get user by email with eager loading...")

        # Use eager loading to avoid lazy loading issues
        result = await db.execute(
            select(UserModel)
            .where(UserModel.email == user_in.email)
            .options(
                selectinload(UserModel.profile),
                selectinload(UserModel.preferences),
                selectinload(UserModel.ratings)
            )
        )
        existing_user = result.scalars().first()

        logger.debug(f"User lookup result: {existing_user is not None}")

        if existing_user:
            logger.info(f"Updating existing test account: {existing_user.email} (ID: {existing_user.id})")

            # Update user attributes without starting a new transaction
            # Update core user fields
            for field in ["username", "email", "lang"]:
                if hasattr(user_in, field) and getattr(user_in, field) is not None:
                    setattr(existing_user, field, getattr(user_in, field))

            # Update profile
            if not existing_user.profile:
                existing_user.profile = UserProfileModel(**profile_data)
            else:
                for field, value in profile_data.items():
                    setattr(existing_user.profile, field, value)

            # Update preferences
            if not existing_user.preferences:
                existing_user.preferences = UserPreferencesModel(**preference_data)
            else:
                for field, value in preference_data.items():
                    setattr(existing_user.preferences, field, value)

            # Always set test account password to a known value
            existing_user.hashed_password = get_password_hash("test_password")

            # Add to session
            db.add(existing_user)

            # Commit the changes
            await db.commit()

            # Refresh user with explicit eager loading after transaction completes
            result = await db.execute(
                select(UserModel)
                .where(UserModel.id == existing_user.id)
                .options(
                    selectinload(UserModel.profile),
                    selectinload(UserModel.preferences),
                    selectinload(UserModel.ratings)
                )
            )
            refreshed_user = result.scalars().first()

            logger.info("Returning updated test user")
            return refreshed_user
        else:
            logger.info(f"Creating new test account: {user_in.email}")

            # Create user without using begin() transaction
            # Create core user data
            user_dict = user_in.model_dump(
                exclude={"password", "first_name", "last_name", "avatar_url", "bio", "location", "beverage_preference"}
            )
            user = UserModel(**user_dict)
            user.hashed_password = get_password_hash("test_password")

            # Create profile
            user.profile = UserProfileModel(**profile_data)

            # Create preferences with beverage preference
            user.preferences = UserPreferencesModel(**preference_data)
            if "beverage_preference" not in preference_data:
                user.preferences.beverage_preference = BeveragePreference.COFFEE

            # Create ratings
            user.ratings = UserRatingModel()

            # Add to session
            db.add(user)

            # Commit the changes
            await db.commit()

            # Refresh user with explicit eager loading after transaction completes
            result = await db.execute(
                select(UserModel)
                .where(UserModel.email == user_in.email)
                .options(
                    selectinload(UserModel.profile),
                    selectinload(UserModel.preferences),
                    selectinload(UserModel.ratings)
                )
            )
            new_user = result.scalars().first()

            logger.info("Returning new test user")
            return new_user

    # Normal user creation flow for non-test accounts
    logger.info("Normal user creation flow starting")
    logger.debug("Checking if user already exists...")

    # Check if user exists with eager loading
    result = await db.execute(
        select(UserModel)
        .where(UserModel.email == user_in.email)
    )
    existing_user = result.scalars().first()

    if existing_user:
        logger.error(f"User with email {user_in.email} already exists")
        raise HTTPException(
            status_code=400,
            detail="The user with this email already exists in the system",
        )

    # Create user without using begin() transaction
    # Create normal user - core data only
    logger.debug("Creating user model...")
    user_dict = user_in.model_dump(
        exclude={"password", "first_name", "last_name", "avatar_url", "bio", "location", "beverage_preference"}
    )
    user = UserModel(**user_dict)
    user.hashed_password = get_password_hash(user_in.password)

    # Generate username if not provided
    if not user.username:
        logger.debug("Generating username...")
        user.username = await generate_username(user_in.email, db)

    # Create related models
    logger.debug("Creating related models (profile, preferences, ratings)...")
    user.profile = UserProfileModel(**profile_data)
    user.preferences = UserPreferencesModel(**preference_data)
    user.ratings = UserRatingModel()

    # Add to session
    db.add(user)

    # Commit the changes
    await db.commit()

    # Refresh user with explicit eager loading after transaction completes
    result = await db.execute(
        select(UserModel)
        .where(UserModel.email == user_in.email)
        .options(
            selectinload(UserModel.profile),
            selectinload(UserModel.preferences),
            selectinload(UserModel.ratings)
        )
    )
    new_user = result.scalars().first()

    logger.info(f"Created new regular user: {new_user.email} (ID: {new_user.id})")
    return new_user


async def get_current_user(token: str = Depends(oauth2_scheme), db: AsyncSession = Depends(get_db)) -> UserModel:
    """Get current user from token"""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )

    try:
        payload = jwt.decode(token, settings.AUTH_SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")

        if user_id is None:
            raise credentials_exception

    except JWTError:
        raise credentials_exception

    try:
        user = await get_user_by_id(db, UUID(user_id))
    except ValueError:
        raise credentials_exception

    if user is None:
        raise credentials_exception

    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Inactive user"
        )

    return user


@handle_sqlalchemy_errors
async def refresh_access_token(refresh_token_data: TokenRefreshSchema, db: AsyncSession) -> Dict[str, str]:
    """Refresh access token using refresh token"""
    # Verify refresh token
    try:
        payload = jwt.decode(
            refresh_token_data.refresh_token,
            settings.AUTH_SECRET_KEY,
            algorithms=[JWT_ALGORITHM]
        )

        user_id: str = payload.get("sub")
        token_type: str = payload.get("type")

        if user_id is None or token_type != "refresh":
            logger.warning(f"Invalid refresh token: missing sub or incorrect type")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid refresh token",
                headers={"WWW-Authenticate": "Bearer"},
            )

        # Ensure we're using timezone-aware datetime with UTC for comparison with the database
        current_time = datetime.now(UTC)

        # Check token in database
        result = await db.execute(
            select(RefreshTokenModel).where(
                RefreshTokenModel.token == refresh_token_data.refresh_token,
                RefreshTokenModel.revoked == False,
                RefreshTokenModel.expires_at > current_time
            )
        )
        db_token = result.scalars().first()

        if not db_token:
            logger.warning(f"Refresh token not found in database or expired/revoked: {user_id}")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Refresh token expired or revoked",
                headers={"WWW-Authenticate": "Bearer"},
            )

        # Get user
        try:
            user = await get_user_by_id(db, UUID(user_id))
        except ValueError:
            logger.error(f"Invalid user ID in token: {user_id}")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid user ID in token",
                headers={"WWW-Authenticate": "Bearer"},
            )

        if not user or not user.is_active:
            logger.warning(f"User not found or inactive: {user_id}")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="User not found or inactive",
                headers={"WWW-Authenticate": "Bearer"},
            )

        # Create new access token
        access_token = create_access_token(
            data={"sub": str(user.id)},
            expires_delta=timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
        )

        # Create new refresh token data
        now = datetime.now(UTC)
        expires_at = now + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)
        token_data = {
            "sub": str(user.id),
            "type": "refresh",
            "exp": int(expires_at.timestamp()),
            "iat": int(now.timestamp())
        }
        new_refresh_token = jwt.encode(token_data, settings.AUTH_SECRET_KEY, algorithm=ALGORITHM)

        # Используем одну транзакцию для обоих операций
        async with db_transaction(db) as session:
            # Mark old refresh token as revoked
            db_token.revoked = True
            session.add(db_token)

            # Create new refresh token record
            new_db_token = RefreshTokenModel(
                user_id=user.id,
                token=new_refresh_token,
                expires_at=expires_at
            )
            session.add(new_db_token)

            # Commit произойдет автоматически при выходе из контекста

        return {
            "access_token": access_token,
            "refresh_token": new_refresh_token,
            "token_type": "bearer"
        }

    except JWTError as e:
        logger.error(f"JWT error when decoding refresh token: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid refresh token",
            headers={"WWW-Authenticate": "Bearer"},
        )


async def revoke_refresh_token(refresh_token: str, db: AsyncSession) -> bool:
    """
    Revoke refresh token (logout)

    Args:
        refresh_token: Token to revoke
        db: Database session

    Returns:
        True if token was successfully revoked, False otherwise
    """
    logger = get_logger(__name__)
    logger.debug(f"Attempting to revoke refresh token")

    # Find token in database
    try:
        result = await db.execute(
            select(RefreshTokenModel).where(
                RefreshTokenModel.token == refresh_token,
                RefreshTokenModel.revoked == False
            )
        )
        db_token = result.scalars().first()
    except Exception as e:
        logger.error(f"Error querying refresh token: {str(e)}")
        return False

    if not db_token:
        logger.warning(f"Attempted to revoke non-existent token or already revoked token")
        return False

    try:
        # Mark token as revoked
        db_token.revoked = True
        db.add(db_token)

        # Коммит изменений
        await db.commit()
        logger.debug(f"Refresh token revoked successfully")

        logger.info(f"Successfully revoked token for user {db_token.user_id}")
        return True
    except Exception as e:
        # Откат при ошибке
        await db.rollback()
        logger.error(f"Error revoking token: {str(e)}")
        return False
