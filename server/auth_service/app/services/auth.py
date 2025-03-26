from datetime import UTC, datetime, timedelta
from typing import Optional, Dict, Any, Union
import random
import string
import uuid
from uuid import UUID

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from passlib.context import CryptContext
from sqlalchemy.orm import Session

from ..core.config import settings
from ..db.session import get_db
from ..models.auth import RefreshTokenModel
from ..models.user import UserModel
from ..schemas.auth import TokenRefreshSchema
from ..schemas.user import UserCreateSchema

# JWT settings
ALGORITHM = "HS256"
oauth2_scheme = OAuth2PasswordBearer(tokenUrl=f"{settings.API_V1_STR}/auth/login")

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


def authenticate_user(db: Session, email: str, password: str) -> Optional[UserModel]:
    """Authenticate user"""
    user = get_user_by_email(db, email)

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
    # Start with the part before @ in email
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


def create_user(db: Session, user_data: UserCreateSchema) -> UserModel:
    """Create new user"""
    # Check if user with this email already exists
    if get_user_by_email(db, user_data.email):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email already registered"
        )

    # Generate username if not provided
    username = user_data.username
    if not username:
        username = generate_username(user_data.email, db)
    elif get_user_by_username(db, username):
        # If provided username is taken
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Username already taken"
        )

    # Create user
    hashed_password = get_password_hash(user_data.password)
    db_user = UserModel(
        id=uuid.uuid4(),
        email=user_data.email,
        username=username,
        hashed_password=hashed_password,
        first_name=user_data.first_name or "",
        last_name=user_data.last_name or "",
        lang=user_data.lang or "en"
    )

    db.add(db_user)
    db.commit()
    db.refresh(db_user)

    return db_user


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

    except JWTError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid refresh token",
            headers={"WWW-Authenticate": "Bearer"},
        )
