from datetime import datetime
from typing import Dict, List, Optional

from pydantic import BaseModel, EmailStr, Field, HttpUrl

from app.models.user import OAuthProvider, PrivacyLevel, UserRole


# Схема для настроек приватности
class PrivacySettings(BaseModel):
    """
    Схема для настроек приватности пользователя.

    Определяет, какие данные пользователя видны другим пользователям.
    """
    email_privacy: PrivacyLevel = PrivacyLevel.PRIVATE
    location_privacy: PrivacyLevel = PrivacyLevel.FRIENDS
    achievements_privacy: PrivacyLevel = PrivacyLevel.PUBLIC
    rooms_privacy: PrivacyLevel = PrivacyLevel.PUBLIC
    rating_privacy: PrivacyLevel = PrivacyLevel.PUBLIC

    class Config:
        from_attributes = True


# Схема для OAuth провайдера
class UserOAuthProvider(BaseModel):
    """
    Схема для OAuth провайдера пользователя.

    Содержит информацию о подключенном OAuth провайдере.
    """
    provider: OAuthProvider
    provider_user_id: str

    class Config:
        from_attributes = True


# Схема для комнаты
class UserRoom(BaseModel):
    """
    Схема для комнаты, в которой участвует пользователь.

    TODO: В будущем будет расширена с учетом модели Room из room_service.
    """
    room_id: str
    joined_at: datetime

    class Config:
        from_attributes = True


# Базовая схема пользователя
class UserBase(BaseModel):
    """
    Базовая схема пользователя с основными полями.

    Используется как основа для других схем пользователя.
    """
    email: EmailStr
    username: str
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    avatar_url: Optional[HttpUrl] = None
    bio: Optional[str] = None
    location: Optional[str] = None
    preferred_language: str = "ru"
    rating: int = 0
    is_active: bool = True
    role: UserRole = UserRole.USER
    auth_provider: OAuthProvider = OAuthProvider.EMAIL


# Схема для создания пользователя
class UserCreate(BaseModel):
    """
    Схема для создания нового пользователя.

    Содержит только необходимые поля для регистрации.
    """
    email: EmailStr
    username: str
    password: str = Field(..., min_length=8)
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    preferred_language: Optional[str] = None


# Схема для обновления пользователя
class UserUpdate(BaseModel):
    """
    Схема для обновления данных пользователя.

    Все поля опциональны, обновляются только предоставленные поля.
    """
    email: Optional[EmailStr] = None
    username: Optional[str] = None
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    avatar_url: Optional[HttpUrl] = None
    bio: Optional[str] = None
    location: Optional[str] = None
    preferred_language: Optional[str] = None
    password: Optional[str] = Field(None, min_length=8)
    is_active: Optional[bool] = None
    is_verified: Optional[bool] = None
    role: Optional[UserRole] = None


# Схема для обновления настроек приватности
class PrivacySettingsUpdate(BaseModel):
    """
    Схема для обновления настроек приватности пользователя.

    Все поля опциональны, обновляются только предоставленные поля.
    """
    email_privacy: Optional[PrivacyLevel] = None
    location_privacy: Optional[PrivacyLevel] = None
    achievements_privacy: Optional[PrivacyLevel] = None
    rooms_privacy: Optional[PrivacyLevel] = None
    rating_privacy: Optional[PrivacyLevel] = None


# Схема для ответа с данными пользователя
class UserResponse(UserBase):
    """
    Схема для ответа с полными данными пользователя.

    Используется для возврата данных аутентифицированному пользователю.
    """
    id: int
    is_verified: bool
    created_at: datetime
    updated_at: datetime
    last_login_at: Optional[datetime] = None
    privacy_settings: PrivacySettings
    oauth_providers: List[UserOAuthProvider] = []
    rooms: List[UserRoom] = []
    settings: Dict = {}

    class Config:
        from_attributes = True


# Схема для публичного профиля пользователя (с учетом настроек приватности)
class UserPublicProfile(BaseModel):
    """
    Схема для публичного профиля пользователя.

    Содержит только те поля, которые могут быть видны другим пользователям
    в соответствии с настройками приватности.
    """
    id: int
    username: str
    avatar_url: Optional[HttpUrl] = None
    bio: Optional[str] = None
    location: Optional[str] = None
    rating: Optional[int] = None
    role: UserRole
    created_at: datetime

    class Config:
        from_attributes = True


# Схема для аутентификации
class UserLogin(BaseModel):
    """
    Схема для аутентификации пользователя.

    Используется для входа в систему.
    """
    email: EmailStr
    password: str


# Схема для токенов
class Token(BaseModel):
    """
    Схема для токенов аутентификации.

    Содержит токен доступа и токен обновления.
    """
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


# Схема для обновления токена
class TokenRefresh(BaseModel):
    """
    Схема для обновления токена доступа.

    Используется для получения нового токена доступа с помощью токена обновления.
    """
    refresh_token: str
