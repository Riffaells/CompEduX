from datetime import datetime, timedelta
from typing import Optional

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from passlib.context import CryptContext
from sqlalchemy.orm import Session

from app.core.config import settings
from app.db.session import get_db
from app.models.user import RefreshToken, User
from app.schemas.user import TokenRefresh, UserCreate

# Настройка JWT
ALGORITHM = "HS256"
oauth2_scheme = OAuth2PasswordBearer(tokenUrl=f"{settings.API_V1_STR}/auth/login")

# Настройка хеширования паролей
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Проверка пароля"""
    return pwd_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    """Хеширование пароля"""
    return pwd_context.hash(password)


def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    """Создание JWT токена доступа"""
    to_encode = data.copy()

    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)

    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, settings.AUTH_SECRET_KEY, algorithm=ALGORITHM)

    return encoded_jwt


def create_refresh_token(user_id: int, db: Session) -> str:
    """Создание токена обновления"""
    expires_delta = timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)
    expires_at = datetime.utcnow() + expires_delta

    # Генерируем токен
    token_data = {"sub": str(user_id), "type": "refresh"}
    token = jwt.encode(token_data, settings.AUTH_SECRET_KEY, algorithm=ALGORITHM)

    # Сохраняем в базу данных
    db_token = RefreshToken(
        token=token,
        expires_at=expires_at,
        user_id=user_id
    )
    db.add(db_token)
    db.commit()
    db.refresh(db_token)

    return token


def get_user_by_email(db: Session, email: str) -> Optional[User]:
    """Получение пользователя по email"""
    return db.query(User).filter(User.email == email).first()


def get_user_by_username(db: Session, username: str) -> Optional[User]:
    """Получение пользователя по username"""
    return db.query(User).filter(User.username == username).first()


def get_user_by_id(db: Session, user_id: int) -> Optional[User]:
    """Получение пользователя по ID"""
    return db.query(User).filter(User.id == user_id).first()


def authenticate_user(db: Session, email: str, password: str) -> Optional[User]:
    """Аутентификация пользователя"""
    user = get_user_by_email(db, email)

    if not user or not user.hashed_password:
        return None

    if not verify_password(password, user.hashed_password):
        return None

    return user


def create_user(db: Session, user_data: UserCreate) -> User:
    """Создание нового пользователя"""
    # Проверяем, что пользователь с таким email или username не существует
    if get_user_by_email(db, user_data.email):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Email already registered"
        )

    if get_user_by_username(db, user_data.username):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Username already taken"
        )

    # Создаем пользователя
    hashed_password = get_password_hash(user_data.password)
    db_user = User(
        email=user_data.email,
        username=user_data.username,
        hashed_password=hashed_password,
        first_name=user_data.first_name,
        last_name=user_data.last_name
    )

    db.add(db_user)
    db.commit()
    db.refresh(db_user)

    return db_user


def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)) -> User:
    """Получение текущего пользователя из токена"""
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

    user = get_user_by_id(db, int(user_id))

    if user is None:
        raise credentials_exception

    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Inactive user"
        )

    return user


def refresh_access_token(refresh_token_data: TokenRefresh, db: Session) -> dict:
    """Обновление токена доступа с помощью токена обновления"""
    try:
        # Проверяем токен обновления
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

        # Проверяем токен в базе данных
        db_token = db.query(RefreshToken).filter(
            RefreshToken.token == refresh_token_data.refresh_token,
            RefreshToken.revoked == False,
            RefreshToken.expires_at > datetime.utcnow()
        ).first()

        if not db_token:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Refresh token expired or revoked",
                headers={"WWW-Authenticate": "Bearer"},
            )

        # Получаем пользователя
        user = get_user_by_id(db, int(user_id))

        if not user or not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="User not found or inactive",
                headers={"WWW-Authenticate": "Bearer"},
            )

        # Создаем новый токен доступа
        access_token = create_access_token(
            data={"sub": str(user.id)},
            expires_delta=timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
        )

        # Отмечаем старый токен обновления как отозванный
        db_token.revoked = True
        db.commit()

        # Создаем новый токен обновления
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
