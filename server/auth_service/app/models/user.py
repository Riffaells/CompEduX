import uuid
from datetime import datetime, timezone

from sqlalchemy import (Boolean, Column, DateTime, Enum as SQLAlchemyEnum,
                        ForeignKey, Integer, String, Text, JSON, Float)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from .associations import user_oauth_providers
from .base import Base
from .enums import UserRole, OAuthProvider, BeveragePreference
from .privacy import UserPrivacyModel


class UserPreferencesModel(Base):
    """
    User preferences model.

    Stores user-specific preferences for application behavior and appearance.
    """
    __tablename__ = "user_preferences"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)

    # UI preferences
    theme = Column(String, default="light")
    font_size = Column(String, default="medium")

    # Notification preferences
    email_notifications = Column(Boolean, default=True)
    push_notifications = Column(Boolean, default=True)

    # Break time preferences
    beverage_preference = Column(SQLAlchemyEnum(BeveragePreference), default=BeveragePreference.NONE)
    break_reminder = Column(Boolean, default=True)
    break_interval_minutes = Column(Integer, default=60)

    # Additional preferences as JSON
    additional_preferences = Column(JSON, default=dict)

    # Relationship with user
    user = relationship("UserModel", back_populates="preferences")

    # Timestamps с поддержкой часовых поясов
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                       onupdate=lambda: datetime.now(timezone.utc))


class UserProfileModel(Base):
    """
    User profile model.

    Contains ALL personal information about the user.
    Any user-specific data that is not related to authentication or system status
    should be stored here, not in the UserModel.
    """
    __tablename__ = "user_profiles"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)

    # Personal information
    first_name = Column(String, nullable=True)
    last_name = Column(String, nullable=True)
    avatar_url = Column(String, nullable=True)
    bio = Column(Text, nullable=True)  # Brief biography
    location = Column(String, nullable=True)  # Location

    # Social links
    website = Column(String, nullable=True)
    github_url = Column(String, nullable=True)
    linkedin_url = Column(String, nullable=True)
    twitter_url = Column(String, nullable=True)

    # Additional profile data as JSON
    additional_data = Column(JSON, default=dict)

    # Relationship with user
    user = relationship("UserModel", back_populates="profile")

    # Timestamps с поддержкой часовых поясов
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))


class UserRatingModel(Base):
    """
    User rating model.

    Stores different types of ratings for a user, such as:
    - contribution_rating: Measure of user's contribution to the platform
    - bot_score: Likelihood that the user is a bot
    - expertise_rating: Measure of user's expertise in their field
    """
    __tablename__ = "user_ratings"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)

    # Different rating types
    contribution_rating = Column(Float, default=0.0)  # Platform contribution
    bot_score = Column(Float, default=0.0)  # Bot likelihood (0.0-1.0)
    expertise_rating = Column(Float, default=0.0)  # Subject matter expertise
    competition_rating = Column(Float, default=0.0)  # Competition performance

    # Additional ratings stored as JSON for flexibility
    additional_ratings = Column(JSON, default=dict)

    # Relationship with user
    user = relationship("UserModel", back_populates="ratings")

    # Timestamps с поддержкой часовых поясов
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))


class UserModel(Base):
    """
    User model for the system.

    Contains ONLY core authentication and system data:
    - Identification (id, email, username)
    - Authentication (password, auth provider)
    - System status (active, verified, role)
    - Language preference (essential for localization)

    All personal information is stored in UserProfileModel.
    All preferences are stored in UserPreferencesModel.
    All ratings are stored in UserRatingModel.
    """
    __tablename__ = "users"

    # Identification
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=True)  # Can be NULL for OAuth users

    # Language preference (kept here as it's essential for localization)
    lang = Column(String, nullable=True)  # Preferred language, nullable

    # Legacy rating field - to be deprecated in favor of UserRatingModel
    rating = Column(Integer, default=0)  # User rating

    # Status and role
    is_active = Column(Boolean, default=True)
    is_verified = Column(Boolean, default=False)
    role = Column(SQLAlchemyEnum(UserRole), default=UserRole.USER)

    # Main authentication provider
    auth_provider = Column(SQLAlchemyEnum(OAuthProvider), default=OAuthProvider.EMAIL)

    # Timestamps с поддержкой часовых поясов
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))
    last_login_at = Column(DateTime(timezone=True), nullable=True)

    # Relationships with other tables
    refresh_tokens = relationship("RefreshTokenModel", back_populates="user", cascade="all, delete-orphan")

    # User's OAuth providers
    # Due to database structure mismatch, we're not using this relationship directly
    # Instead, we'll handle OAuth providers in the API layer
    oauth_providers = []  # This will be populated manually in the API layer

    # Related models
    profile = relationship("UserProfileModel", uselist=False, back_populates="user", cascade="all, delete-orphan")
    preferences = relationship("UserPreferencesModel", uselist=False, back_populates="user",
                               cascade="all, delete-orphan")
    privacy_settings = relationship("UserPrivacyModel", uselist=False, back_populates="user",
                                    cascade="all, delete-orphan")
    ratings = relationship("UserRatingModel", uselist=False, back_populates="user", cascade="all, delete-orphan")

    # Settings preserved for backward compatibility - will be deprecated
    settings = Column(JSON, default=dict)

    def __init__(self, **kwargs):
        """
        Initialize a new User instance.

        Automatically creates privacy settings, profile, preferences and ratings when a user is created.
        Handles moving profile-related fields from kwargs to the profile model.

        Args:
            **kwargs: Keyword arguments for user attributes
        """
        # Extract profile and preference related attributes
        profile_attrs = {}
        pref_attrs = {}

        for key in list(kwargs.keys()):
            # Profile attributes
            if key in ["first_name", "last_name", "avatar_url", "bio", "location"]:
                profile_attrs[key] = kwargs.pop(key)

            # Preference attributes
            elif key in ["beverage_preference"]:
                pref_attrs[key] = kwargs.pop(key)

        super().__init__(**kwargs)

        # Automatically create related models
        if not self.privacy_settings:
            self.privacy_settings = UserPrivacyModel()

        if not self.profile:
            self.profile = UserProfileModel(**profile_attrs)

        if not self.preferences:
            self.preferences = UserPreferencesModel(**pref_attrs)

        if not self.ratings:
            self.ratings = UserRatingModel()


# Заглушка для модели Room, которая будет определена в room_service
class RoomModel:
    id = None
