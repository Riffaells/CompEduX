import uuid
from datetime import datetime, timezone
from typing import Optional

from sqlalchemy import Column, DateTime, ForeignKey, String, Enum as SQLAlchemyEnum, Table, Boolean, Index, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import relationship

from .base import Base
from .enums import OAuthProvider, FriendshipStatus

# Association table for users and OAuth providers
user_oauth_providers = Table(
    "user_oauth_providers",
    Base.metadata,
    Column("user_id", UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), primary_key=True),
    Column("oauth_provider_id", UUID(as_uuid=True), ForeignKey("oauth_providers.id", ondelete="CASCADE"),
           primary_key=True),
    Column("provider", String, nullable=True),
    Column("provider_user_id", String, nullable=True),
    Column("provider_user_login", String, nullable=True),
    Column("provider_user_name", String, nullable=True),
    Column("provider_user_email", String, nullable=True),
    Column("access_token", String, nullable=True),
    Column("refresh_token", String, nullable=True),
    Column("token_expiry", DateTime(timezone=True), nullable=True),
    Column("provider_metadata", JSONB, nullable=True),
    Column("joined_at", DateTime(timezone=True), default=lambda: datetime.now()),
)


# Association table for users and rooms - Temporarily commented out to troubleshoot database connection issues
# user_rooms = Table(
#     "user_rooms",
#     Base.metadata,
#     Column("user_id", UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), primary_key=True),
#     Column("room_id", UUID(as_uuid=True), ForeignKey("rooms.id"), primary_key=True),
#     Column("joined_at", DateTime, default=lambda: datetime.now(UTC)),
# )


# Model for OAuth providers
class UserOAuthProviderModel(Base):
    """
    Model for OAuth provider connections.

    Stores information about user's connections to OAuth providers.
    """
    __tablename__ = "oauth_providers"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    provider = Column(SQLAlchemyEnum(OAuthProvider), nullable=False)
    provider_user_id = Column(String, nullable=False)
    access_token = Column(String, nullable=True)
    refresh_token = Column(String, nullable=True)
    expires_at = Column(DateTime(timezone=True), nullable=True)
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # Define the relationship with users - commented out due to schema mismatch
    # users = relationship(
    #     "UserModel",
    #     secondary=user_oauth_providers,
    #     back_populates="oauth_providers",
    #     primaryjoin="UserOAuthProviderModel.id == user_oauth_providers.c.oauth_provider_id",
    #     secondaryjoin="UserModel.id == user_oauth_providers.c.user_id"
    # )

# Placeholder for the Room model from room_service - Temporarily commented out to troubleshoot database connection issues
# class RoomModel(Base):
#     """
#     Placeholder for the Room model from room_service.
#
#     In a microservices architecture, this model will be defined in a separate service.
#     It's used here only for defining relationships in SQLAlchemy.
#     """
#     __tablename__ = "rooms"
#
#     id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
#     name = Column(String, nullable=False)
#     description = Column(String, nullable=True)
#     created_at = Column(DateTime, default=lambda: datetime.now(UTC))
#     updated_at = Column(DateTime, default=lambda: datetime.now(UTC), onupdate=lambda: datetime.now(UTC))

class FriendshipModel(Base):
    """
    Модель для хранения информации о дружбе между пользователями.
    
    Атрибуты:
        id (UUID): Уникальный идентификатор записи о дружбе
        user_id (UUID): ID пользователя, который отправил запрос на дружбу
        friend_id (UUID): ID пользователя, которому отправлен запрос на дружбу
        status (FriendshipStatus): Статус дружбы (PENDING, ACCEPTED, REJECTED, BLOCKED)
        requested_at (datetime): Время отправки запроса на дружбу
        updated_at (datetime): Время последнего обновления статуса дружбы
        
    Связи:
        user (UserModel): Пользователь, отправивший запрос на дружбу
        friend (UserModel): Пользователь, получивший запрос на дружбу
    """
    __tablename__ = "friendships"
    
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    friend_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    status = Column(String, default=FriendshipStatus.PENDING, nullable=False)
    requested_at = Column(DateTime, default=datetime.utcnow, nullable=False)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow, nullable=False)
    
    # Связи с пользователями
    user = relationship("UserModel", foreign_keys=[user_id], back_populates="outgoing_friendships")
    friend = relationship("UserModel", foreign_keys=[friend_id], back_populates="incoming_friendships")
    
    # Уникальное ограничение для предотвращения дублирования запросов на дружбу
    __table_args__ = (
        UniqueConstraint('user_id', 'friend_id', name='unique_friendship'),
    )
    
    def __repr__(self):
        return f"<Friendship(user_id={self.user_id}, friend_id={self.friend_id}, status={self.status})>"
