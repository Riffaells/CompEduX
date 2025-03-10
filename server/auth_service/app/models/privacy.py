from sqlalchemy import Column, ForeignKey, Integer, Enum as SQLAlchemyEnum
from sqlalchemy.orm import relationship

from .base import Base
from .enums import PrivacyLevel


class UserPrivacy(Base):
    """
    User privacy settings.

    Defines which user data is visible to other users.
    Each field corresponds to a specific type of data and has its own privacy level.
    """
    __tablename__ = "user_privacy"

    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    email_privacy = Column(SQLAlchemyEnum(PrivacyLevel), default=PrivacyLevel.PRIVATE)
    location_privacy = Column(SQLAlchemyEnum(PrivacyLevel), default=PrivacyLevel.FRIENDS)
    achievements_privacy = Column(SQLAlchemyEnum(PrivacyLevel), default=PrivacyLevel.PUBLIC)
    rooms_privacy = Column(SQLAlchemyEnum(PrivacyLevel), default=PrivacyLevel.PUBLIC)
    rating_privacy = Column(SQLAlchemyEnum(PrivacyLevel), default=PrivacyLevel.PUBLIC)

    user = relationship("User", back_populates="privacy_settings")
