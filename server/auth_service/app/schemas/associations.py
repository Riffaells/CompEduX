from datetime import datetime

from pydantic import BaseModel, ConfigDict

from ..models.enums import OAuthProvider


class UserOAuthProviderSchema(BaseModel):
    """
    Schema for user's OAuth provider.

    Contains information about the connected OAuth provider.
    """
    provider: OAuthProvider
    provider_user_id: str

    model_config = ConfigDict(from_attributes=True)


class UserRoomSchema(BaseModel):
    """
    Schema for a room in which the user participates.

    TODO: Will be expanded in the future with the Room model from room_service.
    """
    room_id: str
    joined_at: datetime

    model_config = ConfigDict(from_attributes=True)
