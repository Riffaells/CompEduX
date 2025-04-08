from typing import Optional
from uuid import UUID

from pydantic import BaseModel, EmailStr, HttpUrl, ConfigDict

from ..models.enums import OAuthProvider, UserRole


class UserBaseSchema(BaseModel):
    """
    Base user schema with essential fields.

    Used as a foundation for other user schemas.
    """
    email: EmailStr
    username: str
    first_name: str = ""
    last_name: str = ""
    avatar_url: Optional[HttpUrl] = None
    bio: Optional[str] = ""
    location: Optional[str] = ""
    lang: str = "en"
    rating: int = 0
    is_active: bool = True
    role: UserRole = UserRole.USER
    auth_provider: OAuthProvider = OAuthProvider.EMAIL

    model_config = ConfigDict(from_attributes=True)
