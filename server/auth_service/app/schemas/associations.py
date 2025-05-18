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