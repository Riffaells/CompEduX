from datetime import datetime
from uuid import UUID

from pydantic import BaseModel, ConfigDict

from ..models.enums import OAuthProvider


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


# Temporarily commented out for troubleshooting
# class UserRoomSchema(BaseModel):
#     """
#     Schema for a room in which the user participates.
#
#     TODO: Will be expanded in the future with the Room model from room_service.
#     """
#     room_id: UUID
#     name: str
#     description: str = None
#     joined_at: datetime
#
#     model_config = ConfigDict(from_attributes=True)


# Temporarily commented out for troubleshooting
# class RoomSchema(BaseModel):
#     """
#     Schema for a room.
#
#     Basic information about a room.
#     """
#     id: UUID
#     name: str
#     description: str = None
#     created_at: datetime
#     updated_at: datetime
#
#     model_config = ConfigDict(from_attributes=True)
