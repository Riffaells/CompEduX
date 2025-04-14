# Schemas Package
from .base import UserBaseSchema
from .privacy import PrivacySettingsSchema, PrivacySettingsUpdateSchema
from .auth import UserLoginSchema, TokenSchema, TokenRefreshSchema, TokenData
# Temporarily commented out room-related schemas to troubleshoot database connection issues
from .associations import UserOAuthProviderSchema  # , UserRoomSchema, RoomSchema
from .user import (
    UserCreateSchema,
    UserUpdateSchema,
    UserResponseSchema,
    UserPublicProfileSchema,
    UserProfileSchema,
    UserPreferencesSchema
)
from .stats import (
    PlatformStatSchema,
    OSStatSchema,
    AppVersionStatSchema,
    ClientStatsResponse
)

__all__ = [
    'UserBaseSchema',
    'PrivacySettingsSchema',
    'PrivacySettingsUpdateSchema',
    'UserLoginSchema',
    'TokenSchema',
    'TokenRefreshSchema',
    'UserOAuthProviderSchema',
    # Temporarily commented out room-related schemas to troubleshoot database connection issues
    # 'UserRoomSchema',
    # 'RoomSchema',
    'UserCreateSchema',
    'UserUpdateSchema',
    'UserResponseSchema',
    'UserPublicProfileSchema',
    'UserProfileSchema',
    'UserPreferencesSchema',
    'PlatformStatSchema',
    'OSStatSchema',
    'AppVersionStatSchema',
    'ClientStatsResponse'
]
