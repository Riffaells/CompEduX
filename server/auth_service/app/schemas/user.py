from datetime import datetime
import re
from typing import Dict, List, Optional
from uuid import UUID

from pydantic import BaseModel, EmailStr, Field, HttpUrl, field_validator, ConfigDict

from .base import UserBaseSchema
from .privacy import PrivacySettingsSchema
# Temporarily commented out UserRoomSchema for troubleshooting
from .associations import UserOAuthProviderSchema  # , UserRoomSchema
from ..models.enums import UserRole, PrivacyLevel, OAuthProvider


# Username validation pattern: letters, numbers, underscore, hyphen
USERNAME_PATTERN = re.compile(r'^[a-zA-Z0-9_-]+$')


# Privacy settings schema
class PrivacySettingsSchema(BaseModel):
    """
    Schema for user privacy settings.

    Defines which user data is visible to other users.
    """
    email_privacy: PrivacyLevel = PrivacyLevel.PRIVATE
    location_privacy: PrivacyLevel = PrivacyLevel.FRIENDS
    achievements_privacy: PrivacyLevel = PrivacyLevel.PUBLIC
    rooms_privacy: PrivacyLevel = PrivacyLevel.PUBLIC
    rating_privacy: PrivacyLevel = PrivacyLevel.PUBLIC

    model_config = ConfigDict(from_attributes=True)


# OAuth provider schema
class UserOAuthProviderSchema(BaseModel):
    """
    Schema for user's OAuth provider.

    Contains information about the connected OAuth provider.
    """
    id: UUID
    provider: OAuthProvider
    provider_user_id: str
    access_token: str = None
    refresh_token: str = None
    expires_at: datetime = None
    created_at: datetime
    updated_at: datetime

    model_config = ConfigDict(from_attributes=True)


# Room schema - Temporarily commented out for troubleshooting
# class UserRoomSchema(BaseModel):
#     """
#     Schema for a room in which the user participates.
#
#     TODO: Will be expanded in the future with the Room model from room_service.
#     """
#     room_id: str
#     joined_at: datetime
#
#     model_config = ConfigDict(from_attributes=True)


# Схема для создания пользователя
class UserCreateSchema(BaseModel):
    """
    Schema for creating a new user.

    Contains only the necessary fields for registration.
    Email and password are required, username is optional and will be generated if not provided.
    Username can only contain letters, numbers, underscore (_) and hyphen (-).
    """
    email: EmailStr
    username: Optional[str] = None
    password: str = Field(..., min_length=8)
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    preferred_language: Optional[str] = None

    @field_validator('username', mode='before')
    def validate_username(cls, v):
        if v is not None:
            if len(v) < 3:
                raise ValueError('Username must be at least 3 characters long')
            if len(v) > 30:
                raise ValueError('Username must be at most 30 characters long')
            if not USERNAME_PATTERN.match(v):
                raise ValueError('Username can only contain letters, numbers, underscore (_) and hyphen (-)')
        return v


# Схема для обновления пользователя
class UserUpdateSchema(BaseModel):
    """
    Schema for updating user data.

    All fields are optional, only provided fields are updated.
    Username can only contain letters, numbers, underscore (_) and hyphen (-).
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

    @field_validator('username', mode='before')
    def validate_username(cls, v):
        if v is not None:
            if len(v) < 3:
                raise ValueError('Username must be at least 3 characters long')
            if len(v) > 30:
                raise ValueError('Username must be at most 30 characters long')
            if not USERNAME_PATTERN.match(v):
                raise ValueError('Username can only contain letters, numbers, underscore (_) and hyphen (-)')
        return v


# Схема для обновления настроек приватности
class PrivacySettingsUpdateSchema(BaseModel):
    """
    Schema for updating user privacy settings.

    All fields are optional, only provided fields are updated.
    """
    email_privacy: Optional[PrivacyLevel] = None
    location_privacy: Optional[PrivacyLevel] = None
    achievements_privacy: Optional[PrivacyLevel] = None
    rooms_privacy: Optional[PrivacyLevel] = None
    rating_privacy: Optional[PrivacyLevel] = None


# Схема для ответа с данными пользователя
class UserResponseSchema(UserBaseSchema):
    """
    Schema for user response with full data.

    Used for returning user data to the authenticated user.
    """
    id: UUID
    is_verified: bool
    created_at: datetime
    updated_at: datetime
    last_login_at: Optional[datetime] = None
    privacy_settings: PrivacySettingsSchema
    oauth_providers: List[UserOAuthProviderSchema] = []
    # Temporarily commented out rooms for troubleshooting
    # rooms: List[UserRoomSchema] = []
    settings: Dict = {}

    model_config = ConfigDict(from_attributes=True)


# Схема для публичного профиля пользователя (с учетом настроек приватности)
class UserPublicProfileSchema(BaseModel):
    """
    Schema for user's public profile.

    Contains only fields that can be visible to other users
    according to privacy settings.
    """
    id: UUID
    username: str
    avatar_url: Optional[HttpUrl] = None
    bio: Optional[str] = None
    location: Optional[str] = None
    rating: Optional[int] = None
    role: UserRole
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)


# Схема для аутентификации
class UserLoginSchema(BaseModel):
    """
    Schema for user authentication.

    Used for logging into the system.
    """
    email: EmailStr
    password: str


# Схема для токенов
class TokenSchema(BaseModel):
    """
    Schema for authentication tokens.

    Contains access token and refresh token.
    """
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


# Схема для обновления токена
class TokenRefreshSchema(BaseModel):
    """
    Schema for refreshing access token.

    Used to obtain a new access token using a refresh token.
    """
    refresh_token: str
