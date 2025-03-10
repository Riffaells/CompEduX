from datetime import UTC, datetime
from enum import Enum

from app.db.session import Base
from sqlalchemy import (Boolean, Column, DateTime, Enum as SQLAlchemyEnum,
                        ForeignKey, Integer, String, Table, Text, JSON)
from sqlalchemy.orm import relationship


class UserRole(str, Enum):
    ADMIN = "admin"
    USER = "user"
    MODERATOR = "moderator"


class OAuthProvider(str, Enum):
    GOOGLE = "google"
    GITHUB = "github"
    EMAIL = "email"


class PrivacyLevel(str, Enum):
    PUBLIC = "public"      # Видно всем
    FRIENDS = "friends"    # Видно только друзьям
    PRIVATE = "private"    # Видно только самому пользователю




# Таблица связи пользователей с OAuth провайдерами
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


# Таблица связи пользователей с комнатами
user_rooms = Table(
    "user_rooms",
    Base.metadata,
    Column("user_id", Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True),
    Column("room_id", String, primary_key=True),
    Column("joined_at", DateTime, default=lambda: datetime.now(UTC)),
)


class UserPrivacy(Base):
    """Настройки приватности пользователя"""
    __tablename__ = "user_privacy"

    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), primary_key=True)
    email_privacy = Column(SQLAlchemyEnum(PrivacyLevel), default=PrivacyLevel.PRIVATE)
    location_privacy = Column(SQLAlchemyEnum(PrivacyLevel), default=PrivacyLevel.FRIENDS)
    achievements_privacy = Column(SQLAlchemyEnum(PrivacyLevel), default=PrivacyLevel.PUBLIC)
    rooms_privacy = Column(SQLAlchemyEnum(PrivacyLevel), default=PrivacyLevel.PUBLIC)
    rating_privacy = Column(SQLAlchemyEnum(PrivacyLevel), default=PrivacyLevel.PUBLIC)

    user = relationship("User", back_populates="privacy_settings")


class User(Base):
    """
    Модель пользователя системы.

    Содержит основную информацию о пользователе, его настройки, статистику и связи с другими сущностями.
    Модель спроектирована с учетом микросервисной архитектуры, где некоторые связи (например, с комнатами)
    являются внешними по отношению к сервису аутентификации.

    Основные группы полей:
    - Идентификация: id, email, username, hashed_password
    - Персональная информация: first_name, last_name, avatar_url, bio, location, preferred_language
    - Статистика: rating
    - Статус и роль: is_active, is_verified, role
    - Аутентификация: auth_provider, oauth_providers
    - Временные метки: created_at, updated_at, last_login_at
    - Связи: refresh_tokens, rooms
    - Настройки: privacy_settings, settings
    """
    __tablename__ = "users"

    # Идентификация
    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=True)  # Может быть NULL для OAuth пользователей

    # Персональная информация
    first_name = Column(String, nullable=True)
    last_name = Column(String, nullable=True)
    avatar_url = Column(String, nullable=True)
    bio = Column(Text, nullable=True)  # Краткая биография
    location = Column(String, nullable=True)  # Местоположение
    preferred_language = Column(String, default="ru")  # Предпочитаемый язык

    # Статистика и рейтинг
    rating = Column(Integer, default=0)  # Рейтинг пользователя

    # Статус и роль
    is_active = Column(Boolean, default=True)
    is_verified = Column(Boolean, default=False)
    role = Column(SQLAlchemyEnum(UserRole), default=UserRole.USER)

    # Основной провайдер аутентификации
    auth_provider = Column(SQLAlchemyEnum(OAuthProvider), default=OAuthProvider.EMAIL)

    # Временные метки
    created_at = Column(DateTime, default=lambda: datetime.now(UTC))
    updated_at = Column(DateTime, default=lambda: datetime.now(UTC), onupdate=lambda: datetime.now(UTC))
    last_login_at = Column(DateTime, nullable=True)

    # Отношения с другими таблицами
    refresh_tokens = relationship("RefreshToken", back_populates="user", cascade="all, delete-orphan")

    # OAuth провайдеры пользователя
    oauth_providers = relationship(
        "OAuthProvider",
        secondary=user_oauth_providers,
        collection_class=list,
    )

    # Комнаты, в которых участвует пользователь
    rooms = relationship(
        "Room",
        secondary=user_rooms,
        collection_class=list,
        primaryjoin="User.id == user_rooms.c.user_id",
        secondaryjoin="Room.id == user_rooms.c.room_id",
        backref="users",
    )

    # Настройки приватности
    privacy_settings = relationship("UserPrivacy", uselist=False, back_populates="user", cascade="all, delete-orphan")

    # Дополнительные настройки пользователя в формате JSON
    settings = Column(JSON, default=dict)

    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        # Автоматически создаем настройки приватности при создании пользователя
        if not self.privacy_settings:
            self.privacy_settings = UserPrivacy()


# Заглушка для модели Room, которая будет определена в room_service
class Room:
    id = None


class RefreshToken(Base):
    __tablename__ = "refresh_tokens"

    id = Column(Integer, primary_key=True, index=True)
    token = Column(String, unique=True, index=True, nullable=False)
    expires_at = Column(DateTime, nullable=False)
    revoked = Column(Boolean, default=False)

    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    user = relationship("User", back_populates="refresh_tokens")

    created_at = Column(DateTime, default=lambda: datetime.now(UTC))
    updated_at = Column(DateTime, default=lambda: datetime.now(UTC), onupdate=lambda: datetime.now(UTC))
