from datetime import datetime
from enum import Enum
from typing import List, Optional

from sqlalchemy import Boolean, Column, DateTime, Enum as SQLAlchemyEnum, ForeignKey, Integer, String, Table
from sqlalchemy.orm import relationship

from app.db.session import Base


class UserRole(str, Enum):
    ADMIN = "admin"
    USER = "user"
    MODERATOR = "moderator"


class OAuthProvider(str, Enum):
    GOOGLE = "google"
    GITHUB = "github"
    EMAIL = "email"


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True, nullable=False)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=True)  # Может быть NULL для OAuth пользователей

    first_name = Column(String, nullable=True)
    last_name = Column(String, nullable=True)

    is_active = Column(Boolean, default=True)
    is_verified = Column(Boolean, default=False)

    role = Column(SQLAlchemyEnum(UserRole), default=UserRole.USER)
    auth_provider = Column(SQLAlchemyEnum(OAuthProvider), default=OAuthProvider.EMAIL)

    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    # Отношения с другими таблицами могут быть добавлены здесь
    # Например, отношение с таблицей токенов обновления
    refresh_tokens = relationship("RefreshToken", back_populates="user", cascade="all, delete-orphan")


class RefreshToken(Base):
    __tablename__ = "refresh_tokens"

    id = Column(Integer, primary_key=True, index=True)
    token = Column(String, unique=True, index=True, nullable=False)
    expires_at = Column(DateTime, nullable=False)
    revoked = Column(Boolean, default=False)

    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    user = relationship("User", back_populates="refresh_tokens")

    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
