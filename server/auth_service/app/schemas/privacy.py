from typing import Optional

from pydantic import BaseModel, ConfigDict

from ..models.enums import PrivacyLevel


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
