from datetime import UTC, datetime
from sqlalchemy import (Boolean, Column, DateTime, Enum as SQLAlchemyEnum,
                        ForeignKey, Integer, String, Text, JSON)
from sqlalchemy.orm import relationship

from .base import Base
from .enums import UserRole, OAuthProvider
from .associations import user_oauth_providers, user_rooms, Room
from .privacy import UserPrivacy


class User(Base):
    """
    User model for the system.

    Contains core information about the user, their settings, statistics, and relationships with other entities.
    The model is designed with a microservices architecture in mind, where some relationships (e.g., with rooms)
    are external to the authentication service.

    Main field groups:
    - Identification: id, email, username, hashed_password
    - Personal information: first_name, last_name, avatar_url, bio, location, preferred_language
    - Statistics: rating
    - Status and role: is_active, is_verified, role
    - Authentication: auth_provider, oauth_providers
    - Timestamps: created_at, updated_at, last_login_at
    - Relationships: refresh_tokens, rooms
    - Settings: privacy_settings, settings
    """
    __tablename__ = "users"

    # Identification
    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=True)  # Can be NULL for OAuth users

    # Personal information
    first_name = Column(String, nullable=True)
    last_name = Column(String, nullable=True)
    avatar_url = Column(String, nullable=True)
    bio = Column(Text, nullable=True)  # Brief biography
    location = Column(String, nullable=True)  # Location
    preferred_language = Column(String, default="en")  # Preferred language

    # Statistics and rating
    rating = Column(Integer, default=0)  # User rating

    # Status and role
    is_active = Column(Boolean, default=True)
    is_verified = Column(Boolean, default=False)
    role = Column(SQLAlchemyEnum(UserRole), default=UserRole.USER)

    # Main authentication provider
    auth_provider = Column(SQLAlchemyEnum(OAuthProvider), default=OAuthProvider.EMAIL)

    # Timestamps
    created_at = Column(DateTime, default=lambda: datetime.now(UTC))
    updated_at = Column(DateTime, default=lambda: datetime.now(UTC), onupdate=lambda: datetime.now(UTC))
    last_login_at = Column(DateTime, nullable=True)

    # Relationships with other tables
    refresh_tokens = relationship("RefreshToken", back_populates="user", cascade="all, delete-orphan")

    # User's OAuth providers
    oauth_providers = relationship(
        "OAuthProvider",
        secondary=user_oauth_providers,
        collection_class=list,
    )

    # Rooms in which the user participates
    rooms = relationship(
        "Room",
        secondary=user_rooms,
        collection_class=list,
        primaryjoin="User.id == user_rooms.c.user_id",
        secondaryjoin="Room.id == user_rooms.c.room_id",
        backref="users",
    )

    # Privacy settings
    privacy_settings = relationship("UserPrivacy", uselist=False, back_populates="user", cascade="all, delete-orphan")

    # Additional user settings in JSON format
    settings = Column(JSON, default=dict)

    def __init__(self, **kwargs):
        """
        Initialize a new User instance.

        Automatically creates privacy settings when a user is created.

        Args:
            **kwargs: Keyword arguments for user attributes
        """
        super().__init__(**kwargs)
        # Automatically create privacy settings when creating a user
        if not self.privacy_settings:
            self.privacy_settings = UserPrivacy()


# Заглушка для модели Room, которая будет определена в room_service
class Room:
    id = None
