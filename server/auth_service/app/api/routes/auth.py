from datetime import timedelta

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from app.core.config import settings
from app.db.session import get_db
from app.models.user import User
from app.schemas.user import Token, TokenRefresh, UserCreate, UserResponse
from app.services.auth import (
    authenticate_user,
    create_access_token,
    create_refresh_token,
    create_user,
    get_current_user,
    refresh_access_token,
)

router = APIRouter()


@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
async def register(user_data: UserCreate, db: Session = Depends(get_db)):
    """Регистрация нового пользователя"""
    return create_user(db, user_data)


@router.post("/login", response_model=Token)
async def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    """Аутентификация пользователя и получение токенов"""
    user = authenticate_user(db, form_data.username, form_data.password)

    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Создаем токен доступа
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": str(user.id)},
        expires_delta=access_token_expires
    )

    # Создаем токен обновления
    refresh_token = create_refresh_token(user.id, db)

    return {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer"
    }


@router.post("/refresh", response_model=Token)
async def refresh_token(token_data: TokenRefresh, db: Session = Depends(get_db)):
    """Обновление токена доступа с помощью токена обновления"""
    return refresh_access_token(token_data, db)


@router.post("/logout")
async def logout(
    token_data: TokenRefresh,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Выход из системы (отзыв токена обновления)"""
    # Находим токен обновления в базе данных
    db_token = db.query(RefreshToken).filter(
        RefreshToken.token == token_data.refresh_token,
        RefreshToken.user_id == current_user.id,
        RefreshToken.revoked == False
    ).first()

    if db_token:
        # Отмечаем токен как отозванный
        db_token.revoked = True
        db.commit()

    return {"message": "Successfully logged out"}


@router.get("/me", response_model=UserResponse)
async def read_users_me(current_user: User = Depends(get_current_user)):
    """Получение информации о текущем пользователе"""
    return current_user
