from datetime import datetime
from typing import Optional

from pydantic import BaseModel, EmailStr, Field

from app.models.user import OAuthProvider, UserRole


# Базовая схема пользователя
class UserBase(BaseModel):
    email: EmailStr
    username: str
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    is_active: bool = True
    role: UserRole = UserRole.USER
    auth_provider: OAuthProvider = OAuthProvider.EMAIL


# Схема для создания пользователя
class UserCreate(BaseModel):
    email: EmailStr
    username: str
    password: str = Field(..., min_length=8)
    first_name: Optional[str] = None
    last_name: Optional[str] = None


# Схема для обновления пользователя
class UserUpdate(BaseModel):
    email: Optional[EmailStr] = None
    username: Optional[str] = None
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    password: Optional[str] = Field(None, min_length=8)
    is_active: Optional[bool] = None
    is_verified: Optional[bool] = None
    role: Optional[UserRole] = None


# Схема для ответа с данными пользователя
class UserResponse(UserBase):
    id: int
    is_verified: bool
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


# Схема для аутентификации
class UserLogin(BaseModel):
    email: EmailStr
    password: str


# Схема для токенов
class Token(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


# Схема для обновления токена
class TokenRefresh(BaseModel):
    refresh_token: str
