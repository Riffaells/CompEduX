from typing import Optional

from pydantic import BaseModel, EmailStr, ConfigDict

from ..models.enums import OAuthProvider, UserRole


class UserBaseSchema(BaseModel):
    """
    Base user schema with essential fields.

    This schema is used for creating and updating users, not for responses.
    For responses, use UserResponseSchema which avoids duplication.
    """
    email: EmailStr
    username: str
    lang: Optional[str] = None  # Language preference, optional
    is_active: bool = True
    role: UserRole = UserRole.USER
    auth_provider: OAuthProvider = OAuthProvider.EMAIL

    model_config = ConfigDict(from_attributes=True)
