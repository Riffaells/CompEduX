# -*- coding: utf-8 -*-
"""
Schemas package for auth_service
"""
from .associations import UserOAuthProviderSchema
from .auth import (
    TokenSchema, TokenPayloadSchema, TokenRefreshSchema, TokenDataSchema,
    LoginSchema, LoginResponseSchema, RegistrationSchema, RegistrationResponseSchema,
    UserLoginSchema, OAuthLoginSchema
)
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
    UserPreferencesSchema,
    UserList
)
from .friendship import (
    FriendshipBase, FriendshipCreate, FriendshipUpdate, FriendshipResponse,
    FriendMinimalSchema, FriendDetailSchema, FriendWithStatusSchema, FriendListResponse,
    FriendshipStatusEnum
)

__all__ = [
    'UserBaseSchema',
    'PrivacySettingsSchema',
    'PrivacySettingsUpdateSchema',
    'TokenSchema',
    'TokenPayloadSchema',
    'TokenRefreshSchema',
    'TokenDataSchema',
    'LoginSchema',
    'LoginResponseSchema',
    'RegistrationSchema',
    'RegistrationResponseSchema',
    'UserLoginSchema',
    'OAuthLoginSchema',
    'UserOAuthProviderSchema',
    'UserCreateSchema',
    'UserUpdateSchema',
    'UserResponseSchema',
    'UserPublicProfileSchema',
    'UserProfileSchema',
    'UserPreferencesSchema',
    'UserList',
    'PlatformStatSchema',
    'OSStatSchema',
    'AppVersionStatSchema',
    'ClientStatsResponse',
    'FriendshipBase',
    'FriendshipCreate',
    'FriendshipUpdate',
    'FriendshipResponse',
    'FriendMinimalSchema',
    'FriendDetailSchema',
    'FriendWithStatusSchema',
    'FriendListResponse',
    'FriendshipStatusEnum'
]
