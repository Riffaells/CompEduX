from datetime import UTC, datetime
from sqlalchemy import Column, DateTime, ForeignKey, Integer, String, Enum as SQLAlchemyEnum, Table

from .base import Base
from .enums import OAuthProvider

# Association table for users and OAuth providers
user_oauth_providers = Table(
    "user_oauth_providers",
    Base.metadata,
    Column("user_id", Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True),
    Column("provider", SQLAlchemyEnum(OAuthProvider), primary_key=True),
    Column("provider_user_id", String, nullable=False),
    Column("access_token", String, nullable=True),
    Column("refresh_token", String, nullable=True),
    Column("expires_at", DateTime, nullable=True),
    Column("created_at", DateTime, default=lambda: datetime.now(UTC)),
    Column("updated_at", DateTime, default=lambda: datetime.now(UTC), onupdate=lambda: datetime.now(UTC)),
)


# Association table for users and rooms
user_rooms = Table(
    "user_rooms",
    Base.metadata,
    Column("user_id", Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True),
    Column("room_id", String, primary_key=True),
    Column("joined_at", DateTime, default=lambda: datetime.now(UTC)),
)


# Placeholder for the Room model from room_service
class Room:
    """
    Placeholder for the Room model from room_service.

    In a microservices architecture, this model will be defined in a separate service.
    It's used here only for defining relationships in SQLAlchemy.
    """
    id = None
