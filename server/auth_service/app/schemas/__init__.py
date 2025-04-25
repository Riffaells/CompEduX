# Schemas Package
# Temporarily commented out room-related schemas to troubleshoot database connection issues
from .associations import UserOAuthProviderSchema  # , UserRoomSchema, RoomSchema
from .auth import UserLoginSchema, TokenSchema, TokenRefreshSchema, TokenPayloadSchema
from .base import UserBaseSchema
from .privacy import PrivacySettingsSchema, PrivacySettingsUpdateSchema
from .stats import (
    PlatformStatSchema,
    OSStatSchema,
    AppVersionStatSchema,
    ClientStatsResponse
)
from .user import (
    UserCreateSchema,
    UserUpdateSchema,
    UserResponseSchema,
    UserPublicProfileSchema,
    UserProfileSchema,
    UserPreferencesSchema
)

__all__ = [
    'UserBaseSchema',
    'PrivacySettingsSchema',
    'PrivacySettingsUpdateSchema',
    'UserLoginSchema',
    'TokenSchema',
    'TokenRefreshSchema',
    'TokenPayloadSchema',
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
