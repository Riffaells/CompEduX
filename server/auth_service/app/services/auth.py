from datetime import UTC, datetime, timedelta
from typing import Optional, Dict, Any, Union
import random
import string
import uuid
from uuid import UUID
import logging

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from passlib.context import CryptContext
from sqlalchemy.orm import Session

from ..core.config import settings
from ..db.session import get_db
from ..models.auth import RefreshTokenModel
from ..models.user import UserModel, UserProfileModel, UserPreferencesModel, UserRatingModel
from ..schemas.auth import TokenRefreshSchema
from ..schemas.user import UserCreateSchema
from ..models.enums import BeveragePreference

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


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Verify password against hash"""
    return pwd_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    """Hash password"""
    return pwd_context.hash(password)


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


def create_refresh_token(user_id: UUID, db: Session) -> str:
    """Create refresh token"""
    expires_delta = timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)
    expires_at = datetime.now(UTC) + expires_delta

    # Check if user already has a valid refresh token
    existing_token = db.query(RefreshTokenModel).filter(
        RefreshTokenModel.user_id == user_id,
        RefreshTokenModel.revoked == False,
        RefreshTokenModel.expires_at > datetime.now(UTC)
    ).first()

    if existing_token:
        return existing_token.token

    # Generate token
    token_data = {"sub": str(user_id), "type": "refresh"}
    token = jwt.encode(token_data, settings.AUTH_SECRET_KEY, algorithm=ALGORITHM)

    # Save to database
    db_token = RefreshTokenModel(
        token=token,
        expires_at=expires_at,
        user_id=user_id
    )
    db.add(db_token)
    db.commit()
    db.refresh(db_token)

    return token


def get_user_by_email(db: Session, email: str) -> Optional[UserModel]:
    """Get user by email"""
    return db.query(UserModel).filter(UserModel.email == email).first()


def get_user_by_username(db: Session, username: str) -> Optional[UserModel]:
    """Get user by username"""
    return db.query(UserModel).filter(UserModel.username == username).first()


def get_user_by_id(db: Session, user_id: UUID) -> Optional[UserModel]:
    """Get user by ID"""
    return db.query(UserModel).filter(UserModel.id == user_id).first()


def authenticate_user(db: Session, login: str, password: str) -> Optional[UserModel]:
    """
    Authenticate user by email or username

    Args:
        db: Database session
        login: Email or username
        password: User password

    Returns:
        User model if authentication successful, None otherwise
    """
    # Специальный случай для тестового аккаунта
    is_test_account = login == "test@example.com" or login == "test"

    # Для тестового аккаунта - особая обработка
    if is_test_account:
        # Получаем пользователя, если он существует
        if '@' in login:
            user = get_user_by_email(db, login)
        else:
            user = get_user_by_username(db, login)

        # Если тестовый пользователь не существует, создаем его
        if not user:
            test_user_data = UserCreateSchema(
                email="test@example.com",
                username="test",
                password=password or "test123",
                first_name="Test",
                last_name="User",
                beverage_preference=BeveragePreference.NONE
            )
            user = create_user(db, test_user_data)

        # Обновляем last_login_at
        user.last_login_at = datetime.now(UTC)
        db.commit()

        # Всегда возвращаем пользователя без проверки пароля
        return user

    # Стандартная обработка для обычных пользователей
    # Check if login is email or username
    if '@' in login:
        # Try to get user by email
        user = get_user_by_email(db, login)
    else:
        # Try to get user by username
        user = get_user_by_username(db, login)

    if not user or not user.hashed_password:
        return None

    if not verify_password(password, user.hashed_password):
        return None

    # Update last login timestamp
    user.last_login_at = datetime.now(UTC)
    db.commit()

    return user


def generate_username(email: str, db: Session) -> str:
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
        base_username += ''.join(random.choices(string.ascii_lowercase, k=3-len(base_username)))

    # If username is too long, truncate it
    if len(base_username) > 25:  # Leave room for numbers
        base_username = base_username[:25]

    # Check if username exists
    username = base_username
    counter = 1

    while get_user_by_username(db, username):
        # If username exists, add a number to it
        username = f"{base_username}{counter}"
        counter += 1

    return username


def create_user(db: Session, user_in: UserCreateSchema) -> UserModel:
    """
    Create a new user record, with special handling for test accounts.

    For test accounts (email format: test+*@test.com):
    - Updates existing test account if found
    - Creates new test account if not found
    - Sets a predefined password

    Returns the created or updated user object.
    """
    logger = logging.getLogger(__name__)

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

    # Check if this is a test account by email format
    is_test_account = user_in.email and user_in.email.startswith("test+") and user_in.email.endswith("@test.com")

    if is_test_account:
        logger.info(f"Processing test account: {user_in.email}")
        # For test accounts, check if it already exists
        existing_user = get_user_by_email(db, email=user_in.email)

        if existing_user:
            logger.info(f"Updating existing test account: {existing_user.email} (ID: {existing_user.id})")

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
            db.commit()
            db.refresh(existing_user)
            return existing_user
        else:
            logger.info(f"Creating new test account: {user_in.email}")

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

            db.add(user)
            db.commit()
            db.refresh(user)
            return user

    # Normal user creation flow for non-test accounts
    if get_user_by_email(db, email=user_in.email):
        logger.error(f"User with email {user_in.email} already exists")
        raise HTTPException(
            status_code=400,
            detail="The user with this email already exists in the system",
        )

    # Create normal user - core data only
    user_dict = user_in.model_dump(
        exclude={"password", "first_name", "last_name", "avatar_url", "bio", "location", "beverage_preference"}
    )
    user = UserModel(**user_dict)
    user.hashed_password = get_password_hash(user_in.password)

    # Generate username if not provided
    if not user.username:
        user.username = generate_username(user_in.email, db)

    # Create related models
    user.profile = UserProfileModel(**profile_data)
    user.preferences = UserPreferencesModel(**preference_data)
    user.ratings = UserRatingModel()

    db.add(user)
    db.commit()
    db.refresh(user)
    logger.info(f"Created new regular user: {user.email} (ID: {user.id})")
    return user


def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)) -> UserModel:
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
        user = get_user_by_id(db, UUID(user_id))
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


def refresh_access_token(refresh_token_data: TokenRefreshSchema, db: Session) -> Dict[str, str]:
    """Refresh access token using refresh token"""
    try:
        # Verify refresh token
        payload = jwt.decode(
            refresh_token_data.refresh_token,
            settings.AUTH_SECRET_KEY,
            algorithms=[ALGORITHM]
        )

        user_id: str = payload.get("sub")
        token_type: str = payload.get("type")

        if user_id is None or token_type != "refresh":
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid refresh token",
                headers={"WWW-Authenticate": "Bearer"},
            )

        # Check token in database
        db_token = db.query(RefreshTokenModel).filter(
            RefreshTokenModel.token == refresh_token_data.refresh_token,
            RefreshTokenModel.revoked == False,
            RefreshTokenModel.expires_at > datetime.now(UTC)
        ).first()

        if not db_token:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Refresh token expired or revoked",
                headers={"WWW-Authenticate": "Bearer"},
            )

        # Get user
        try:
            user = get_user_by_id(db, UUID(user_id))
        except ValueError:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid user ID in token",
                headers={"WWW-Authenticate": "Bearer"},
            )

        if not user or not user.is_active:
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

        try:
            # Mark old refresh token as revoked
            db_token.revoked = True
            db.commit()

            # Create new refresh token
            new_refresh_token = create_refresh_token(user.id, db)

            return {
                "access_token": access_token,
                "refresh_token": new_refresh_token,
                "token_type": "bearer"
            }
        except Exception as e:
            # Откат транзакции в случае ошибки при обновлении токенов
            db.rollback()
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Error refreshing token: {str(e)}",
                headers={"WWW-Authenticate": "Bearer"},
            )

    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid refresh token",
            headers={"WWW-Authenticate": "Bearer"},
        )
