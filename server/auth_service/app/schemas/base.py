from typing import Optional

from pydantic import BaseModel, EmailStr, HttpUrl, ConfigDict

from ..models.enums import OAuthProvider, UserRole


class UserBaseSchema(BaseModel):
    """
    Base user schema with essential fields.

    Used as a foundation for other user schemas.
    """
    email: EmailStr
    username: str
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    avatar_url: Optional[HttpUrl] = None
    bio: Optional[str] = None
    location: Optional[str] = None
    preferred_language: str = "en"
    rating: int = 0
    is_active: bool = True
    role: UserRole = UserRole.USER
    auth_provider: OAuthProvider = OAuthProvider.EMAIL

    model_config = ConfigDict(from_attributes=True)
