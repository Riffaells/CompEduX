"""
Room model definitions for the room service
"""
import enum
import secrets
import string
import uuid
from datetime import datetime, timezone
from typing import List, Optional

from app.models.base import Base
from sqlalchemy import Column, DateTime, ForeignKey, String, Table, Index, Boolean, Enum, Integer
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.ext.declarative import declared_attr
from sqlalchemy.orm import relationship

# Связующая таблица между комнатами и пользователями
room_participant = Table(
    "room_participant",
    Base.metadata,
    Column("room_id", UUID(as_uuid=True), ForeignKey("rooms.id"), primary_key=True),
    Column("user_id", UUID(as_uuid=True), primary_key=True)
)


def generate_code(length: int = 6) -> str:
    """
    Generate a random code for room joining

    Args:
        length: Length of the code to generate

    Returns:
        A random string of specified length
    """
    alphabet = string.ascii_uppercase + string.digits
    return ''.join(secrets.choice(alphabet) for _ in range(length))


class RoomStatus(str, enum.Enum):
    """Room status levels"""
    ACTIVE = "ACTIVE"  # Room is currently active
    ARCHIVED = "ARCHIVED"  # Room is archived
    COMPLETED = "COMPLETED"  # Course in the room is completed
    PENDING = "PENDING"  # Room is waiting for participants


class RoomParticipantRole(str, enum.Enum):
    """Participant roles in a room"""
    OWNER = "OWNER"  # Creator of the room
    TEACHER = "TEACHER"  # Teacher with administrative permissions
    STUDENT = "STUDENT"  # Regular student
    OBSERVER = "OBSERVER"  # Observer with read-only access


class Room(Base):
    """
    Room model representing course instances for groups of users
    """
    __tablename__ = "rooms"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    code = Column(String, unique=True, nullable=False, default=generate_code)

    # Название комнаты может быть на разных языках
    # Мультиязычные поля в формате JSON: {"en": "Title", "ru": "Заголовок", "fr": "Titre", ...}
    name = Column(JSONB, nullable=False)
    description = Column(JSONB, nullable=True)

    # Внешний ключ на курс
    course_id = Column(UUID(as_uuid=True), nullable=False)
    
    # Внешний ключ на владельца (user_id из сервиса auth)
    owner_id = Column(UUID(as_uuid=True), nullable=False)

    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))
    

    # Status of the room
    status = Column(
        Enum(RoomStatus),
        default=RoomStatus.PENDING,
        nullable=False,
        server_default=RoomStatus.PENDING.value
    )

    # Максимальное количество участников (0 = без ограничений)
    max_participants = Column(Integer, default=0, nullable=False)
    
    # Настройки комнаты в формате JSON
    settings = Column(JSONB, nullable=True)

    @declared_attr
    def __table_args__(cls):
        indices = [Index('ix_rooms_name_gin', 'name', postgresql_using='gin', postgresql_ops={'name': 'jsonb_ops'}),
                   Index('ix_rooms_description_gin', 'description', postgresql_using='gin',
                         postgresql_ops={'description': 'jsonb_ops'}), Index('ix_rooms_code', 'code'),
                   Index('ix_rooms_course_id', 'course_id'), Index('ix_rooms_owner_id', 'owner_id'),
                   Index('ix_rooms_status', 'status')]

        # Добавляем индекс для code

        # Добавляем индекс для course_id

        # Добавляем индекс для owner_id

        # Добавляем индекс для status

        return tuple(indices)

    def __repr__(self):
        return f"<Room id={self.id}, code={self.code}>"

    def get_name(self, language: str = 'en', fallback: bool = True) -> str:
        """
        Get name in specific language

        Args:
            language: ISO language code (e.g., 'en', 'ru', 'fr')
            fallback: If True and requested language not found, returns first available
                      name in any language

        Returns:
            Name in requested language or fallback
        """
        if not self.name:
            return ""

        if isinstance(self.name, dict):
            if language in self.name:
                return self.name[language]
            if fallback and self.name:
                return next(iter(self.name.values()), "")
        return str(self.name)

    def get_description(self, language: str = 'en', fallback: bool = True) -> Optional[str]:
        """
        Get description in specific language

        Args:
            language: ISO language code (e.g., 'en', 'ru', 'fr')
            fallback: If True and requested language not found, returns first available
                      description in any language

        Returns:
            Description in requested language or fallback
        """
        if not self.description:
            return None

        if isinstance(self.description, dict):
            if language in self.description:
                return self.description[language]
            if fallback and self.description:
                return next(iter(self.description.values()), None)
        return str(self.description)

    def add_language(self, language: str, name: str, description: Optional[str] = None) -> None:
        """
        Add or update a language version of room content

        Args:
            language: ISO language code (e.g., 'en', 'ru', 'fr')
            name: Room name in the specified language
            description: Optional room description in the specified language
        """
        # Ensure name is a dict
        if not isinstance(self.name, dict):
            self.name = {}

        # Ensure description is a dict
        if self.description is None:
            self.description = {}
        elif not isinstance(self.description, dict):
            self.description = {}

        # Update name and description
        self.name[language] = name
        if description is not None:
            self.description[language] = description

    def remove_language(self, language: str) -> bool:
        """
        Remove a language version from room content

        Args:
            language: ISO language code to remove

        Returns:
            True if language was removed, False if it didn't exist
        """
        name_removed = False
        desc_removed = False

        if isinstance(self.name, dict) and language in self.name:
            del self.name[language]
            name_removed = True

        if isinstance(self.description, dict) and language in self.description:
            del self.description[language]
            desc_removed = True

        return name_removed or desc_removed

    def available_languages(self) -> List[str]:
        """
        Get list of all languages available for this room

        Returns:
            List of ISO language codes
        """
        languages = set()

        if isinstance(self.name, dict):
            languages.update(self.name.keys())

        if isinstance(self.description, dict):
            languages.update(self.description.keys())

        return sorted(list(languages))


class RoomParticipant(Base):
    """
    Model representing a participant in a room
    """
    __tablename__ = "room_participants"

    room_id = Column(UUID(as_uuid=True), ForeignKey("rooms.id"), primary_key=True)
    user_id = Column(UUID(as_uuid=True), primary_key=True)
    
    role = Column(
        Enum(RoomParticipantRole),
        default=RoomParticipantRole.STUDENT,
        nullable=False,
        server_default=RoomParticipantRole.STUDENT.value
    )
    
    joined_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    last_activity_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                             onupdate=lambda: datetime.now(timezone.utc))
    
    # Метаданные пользователя (имя, аватар и т.д.)
    participant_metadata = Column(JSONB, nullable=True)

    @declared_attr
    def __table_args__(cls):
        return (
            Index('ix_room_participants_user_id', 'user_id'),
            Index('ix_room_participants_role', 'role'),
        )

    def __repr__(self):
        return f"<RoomParticipant room_id={self.room_id}, user_id={self.user_id}, role={self.role}>"


class RoomProgress(Base):
    """
    Model for tracking user progress within a room
    """
    __tablename__ = "room_progress"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    room_id = Column(UUID(as_uuid=True), ForeignKey("rooms.id"), nullable=False)
    user_id = Column(UUID(as_uuid=True), nullable=False)
    
    # Узел дерева курса (node_id из технологического дерева)
    node_id = Column(String, nullable=False)
    
    # Статус прогресса (completed, in_progress, etc.)
    status = Column(String, nullable=False)
    
    # Дополнительные данные о прогрессе (оценки, комментарии и т.д.)
    data = Column(JSONB, nullable=True)
    
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    @declared_attr
    def __table_args__(cls):
        return (
            Index('ix_room_progress_room_id', 'room_id'),
            Index('ix_room_progress_user_id', 'user_id'),
            Index('ix_room_progress_node_id', 'node_id'),
            Index('ix_room_progress_status', 'status'),
            # Уникальный индекс для комбинации room_id, user_id, и node_id
            Index('ix_room_progress_unique', 'room_id', 'user_id', 'node_id', unique=True),
        )

    def __repr__(self):
        return f"<RoomProgress id={self.id}, room_id={self.room_id}, user_id={self.user_id}, node_id={self.node_id}>"
