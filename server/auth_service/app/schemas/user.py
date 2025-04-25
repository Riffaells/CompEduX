import re
from datetime import datetime
from typing import Dict, List, Optional, Any
from uuid import UUID

from pydantic import BaseModel, EmailStr, Field, HttpUrl, field_validator, ConfigDict

# Temporarily commented out UserRoomSchema for troubleshooting
from ..models.enums import UserRole, PrivacyLevel, OAuthProvider, BeveragePreference

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


# User preferences schema
class UserPreferencesSchema(BaseModel):
    """
    Schema for user preferences.

    Contains settings for UI, notifications and other preferences.
    """
    # UI preferences
    theme: str = "light"
    font_size: str = "medium"

    # Notification preferences
    email_notifications: bool = True
    push_notifications: bool = True

    # Break time preferences
    beverage_preference: BeveragePreference = BeveragePreference.NONE
    break_reminder: bool = True
    break_interval_minutes: int = 60

    # Additional preferences
    additional_preferences: Dict = {}

    # Timestamps (optional in responses)
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)


# User profile schema
class UserProfileSchema(BaseModel):
    """
    Schema for user profile.

    Contains personal information and social links.
    """
    # Personal information
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    avatar_url: Optional[HttpUrl] = None
    bio: Optional[str] = None
    location: Optional[str] = None

    # Social links
    website: Optional[str] = None
    github_url: Optional[str] = None
    linkedin_url: Optional[str] = None
    twitter_url: Optional[str] = None

    # Additional data
    additional_data: Dict = {}

    # Timestamps (optional in responses)
    created_at: Optional[datetime] = None
    updated_at: Optional[datetime] = None

    model_config = ConfigDict(from_attributes=True)


# User ratings schema
class UserRatingSchema(BaseModel):
    """
    Schema for user ratings.

    Contains different types of ratings for various aspects of user activity.
    """
    # Different rating types
    contribution_rating: float = 0.0  # Platform contribution
    bot_score: float = 0.0  # Bot likelihood (0.0-1.0)
    expertise_rating: float = 0.0  # Subject matter expertise
    competition_rating: float = 0.0  # Competition performance

    # Additional ratings
    additional_ratings: Dict[str, Any] = {}

    # Timestamps
    created_at: datetime
    updated_at: datetime

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

    Contains fields for creating the core user account and related models:
    - Core fields: email, username, password, lang (for authentication and system)
    - Profile fields: first_name, last_name (personal information)
    - Preference fields: beverage_preference (user preferences)

    Email and password are required, username is optional and will be generated if not provided.
    Profile and preference fields are optional.
    """
    # Core user fields - essential for authentication
    email: EmailStr
    username: Optional[str] = None
    password: str = Field(..., min_length=8)
    lang: Optional[str] = None

    # Profile fields - will be stored in UserProfileModel
    first_name: str = ""
    last_name: str = ""

    # Preference fields - will be stored in UserPreferencesModel
    beverage_preference: Optional[BeveragePreference] = None

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
    password: Optional[str] = Field(None, min_length=8)
    lang: Optional[str] = None
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


# Схема для обновления профиля пользователя
class UserProfileUpdateSchema(BaseModel):
    """
    Schema for updating user profile.

    All fields are optional, only provided fields are updated.
    """
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    avatar_url: Optional[HttpUrl] = None
    bio: Optional[str] = None
    location: Optional[str] = None
    website: Optional[str] = None
    github_url: Optional[str] = None
    linkedin_url: Optional[str] = None
    twitter_url: Optional[str] = None


# Схема для обновления рейтингов пользователя
class UserRatingUpdateSchema(BaseModel):
    """
    Schema for updating user ratings.

    All fields are optional, only provided fields are updated.
    """
    contribution_rating: Optional[float] = None
    bot_score: Optional[float] = None
    expertise_rating: Optional[float] = None
    competition_rating: Optional[float] = None

    # For updating specific additional ratings
    additional_rating_updates: Optional[Dict[str, Any]] = None


# Схема для обновления настроек пользователя
class UserPreferencesUpdateSchema(BaseModel):
    """
    Schema for updating user preferences.

    All fields are optional, only provided fields are updated.
    """
    theme: Optional[str] = None
    font_size: Optional[str] = None
    email_notifications: Optional[bool] = None
    push_notifications: Optional[bool] = None
    beverage_preference: Optional[BeveragePreference] = None
    break_reminder: Optional[bool] = None
    break_interval_minutes: Optional[int] = None


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
class UserResponseSchema(BaseModel):
    """
    Schema for user response with full data.

    Used for returning user data to the authenticated user.
    All data is properly organized into appropriate sub-objects without duplication.
    """
    # Core identity
    id: UUID
    email: EmailStr
    username: str

    # System status
    is_active: bool = True
    is_verified: bool = False
    role: UserRole = UserRole.USER
    auth_provider: OAuthProvider = OAuthProvider.EMAIL

    # Language preference (kept at root as it's essential for localization)
    lang: Optional[str] = None

    # Timestamps - only core timestamps at root level
    created_at: datetime
    updated_at: datetime
    last_login_at: Optional[datetime] = None

    # Related entities with their own data
    profile: UserProfileSchema
    preferences: UserPreferencesSchema
    ratings: UserRatingSchema
    oauth_providers: List[UserOAuthProviderSchema] = []

    # Legacy field maintained for backward compatibility but hidden in docs
    settings: Dict = Field(default_factory=dict, exclude=True)

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
    role: UserRole
    created_at: datetime

    # Profile fields that may be visible based on privacy settings
    avatar_url: Optional[HttpUrl] = None
    bio: Optional[str] = None
    location: Optional[str] = None

    # Rating fields that may be visible based on privacy settings
    rating: Optional[float] = None  # Legacy rating field
    contribution_rating: Optional[float] = None
    expertise_rating: Optional[float] = None

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
