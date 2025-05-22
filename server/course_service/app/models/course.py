"""
Course model definitions for the course service
"""
import enum
import secrets
import string
import uuid
from datetime import datetime, timezone
from typing import List, Optional, Dict, Any

from app.models.base import Base
from sqlalchemy import Column, DateTime, ForeignKey, String, Table, Index, Boolean, Enum
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.ext.declarative import declared_attr
from sqlalchemy.orm import relationship

# Связующая таблица между курсами и тегами
course_tag = Table(
    "course_tag",
    Base.metadata,
    Column("course_id", UUID(as_uuid=True), ForeignKey("courses.id"), primary_key=True),
    Column("tag_id", UUID(as_uuid=True), ForeignKey("tags.id"), primary_key=True)
)


def generate_slug(length: int = 8) -> str:
    """
    Generate a random slug for course URLs

    Args:
        length: Length of the slug to generate

    Returns:
        A random string of specified length
    """
    alphabet = string.ascii_lowercase + string.digits
    return ''.join(secrets.choice(alphabet) for _ in range(length))


class CourseVisibility(str, enum.Enum):
    """Course visibility levels"""
    PUBLIC = "PUBLIC"  # Available to everyone
    PRIVATE = "PRIVATE"  # Only available to the author
    FRIENDS = "FRIENDS"  # Available to friends of the author
    LINK = "LINK"  # Available only via direct link
    PAID = "PAID"  # Available after payment
    ORGANIZATION = "ORGANIZATION"  # Available to members of specific organization
    ENROLLED = "ENROLLED"  # Available only to enrolled users


class Course(Base):
    """
    Course model representing educational courses
    """
    __tablename__ = "courses"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    slug = Column(String, unique=True, nullable=False)

    # Мультиязычные поля в формате JSON: {"en": "Title", "ru": "Заголовок", "fr": "Titre", ...}
    title = Column(JSONB, nullable=False)
    description = Column(JSONB, nullable=True)

    # Внешний ключ на автора (user_id из сервиса auth)
    author_id = Column(UUID(as_uuid=True), nullable=False)

    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # Publication status
    is_published = Column(Boolean, default=False)

    # Course visibility level
    visibility = Column(
        Enum(CourseVisibility),
        default=CourseVisibility.PRIVATE,
        nullable=False,
        server_default=CourseVisibility.PRIVATE.value
    )

    # If visibility is ORGANIZATION, this is the organization ID
    organization_id = Column(UUID(as_uuid=True), nullable=True)

    # TODO: Реализовать в будущем
    # Rating metrics
    # rating_avg = Column(JSONB, default={
    #     "value": 0.0,
    #     "count": 0
    # }, nullable=False)

    # TODO: Реализовать в будущем
    # Usage metrics
    # usage_count = Column(JSONB, default={
    # }, nullable=False)

    # Связь с тегами через связующую таблицу
    tags = relationship("Tag", secondary=course_tag, back_populates="courses")

    # Связь с технологическим деревом
    technology_tree = relationship("TechnologyTree", back_populates="course", uselist=False,
                                   cascade="all, delete-orphan")

    # Связь с статьями
    articles = relationship("Article", back_populates="course", cascade="all, delete-orphan")
    
    # Связь с уроками (устаревшая, оставлена для обратной совместимости)
    # DEPRECATED: Lessons are being replaced by articles with multilingual content
    lessons = relationship("Lesson", back_populates="course", cascade="all, delete-orphan")

    @declared_attr
    def __table_args__(cls):
        # Создаем общие индексы для JSONB полей вместо множества отдельных индексов по языкам
        indices = [
            Index('ix_courses_title_gin', 'title', postgresql_using='gin', postgresql_ops={'title': 'jsonb_ops'}),
            Index('ix_courses_description_gin', 'description', postgresql_using='gin',
                  postgresql_ops={'description': 'jsonb_ops'})
        ]

        # Добавляем индекс для slug
        indices.append(Index('ix_courses_slug', 'slug'))

        # Добавляем индекс для автора
        indices.append(Index('ix_courses_author_id', 'author_id'))

        # Добавляем индекс для is_published
        indices.append(Index('ix_courses_is_published', 'is_published'))

        # Добавляем индекс для visibility
        indices.append(Index('ix_courses_visibility', 'visibility'))

        # Добавляем индекс для organization_id
        indices.append(Index('ix_courses_organization_id', 'organization_id'))

        return tuple(indices)

    def __repr__(self):
        return f"<Course id={self.id}, slug={self.slug}>"

    def get_title(self, language: str = 'en', fallback: bool = True) -> str:
        """
        Get title in specific language

        Args:
            language: ISO language code (e.g., 'en', 'ru', 'fr')
            fallback: If True and requested language not found, returns first available
                      title in any language

        Returns:
            Title in requested language or fallback
        """
        if not self.title:
            return ""

        if isinstance(self.title, dict):
            if language in self.title:
                return self.title[language]
            if fallback and self.title:
                return next(iter(self.title.values()), "")
        return str(self.title)

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

    def add_language(self, language: str, title: str, description: Optional[str] = None) -> None:
        """
        Add or update a language version of course content

        Args:
            language: ISO language code (e.g., 'en', 'ru', 'fr')
            title: Course title in the specified language
            description: Optional course description in the specified language
        """
        # Ensure title is a dict
        if not isinstance(self.title, dict):
            self.title = {}

        # Ensure description is a dict
        if self.description is None:
            self.description = {}
        elif not isinstance(self.description, dict):
            self.description = {}

        # Update title and description
        self.title[language] = title
        if description is not None:
            self.description[language] = description

    def remove_language(self, language: str) -> bool:
        """
        Remove a language version from course content

        Args:
            language: ISO language code to remove

        Returns:
            True if language was removed, False if it didn't exist
        """
        title_removed = False
        desc_removed = False

        if isinstance(self.title, dict) and language in self.title:
            del self.title[language]
            title_removed = True

        if isinstance(self.description, dict) and language in self.description:
            del self.description[language]
            desc_removed = True

        return title_removed or desc_removed

    def available_languages(self) -> List[str]:
        """
        Get list of all languages available for this course

        Returns:
            List of ISO language codes
        """
        languages = set()

        if isinstance(self.title, dict):
            languages.update(self.title.keys())

        if isinstance(self.description, dict):
            languages.update(self.description.keys())

        return sorted(list(languages))
        
    def get_localized_version(self, language: str = 'en', fallback: bool = True) -> Dict[str, Any]:
        """
        Get a localized version of the course

        Args:
            language: ISO language code (e.g., 'en', 'ru', 'fr')
            fallback: If True and requested language not found, returns first available
                      content in any language

        Returns:
            Dictionary with localized title and description
        """
        return {
            "id": self.id,
            "slug": self.slug,
            "title": self.get_title(language, fallback),
            "description": self.get_description(language, fallback),
            "author_id": self.author_id,
            "is_published": self.is_published,
            "visibility": self.visibility.value if self.visibility else None,
            "organization_id": self.organization_id,
            "created_at": self.created_at,
            "updated_at": self.updated_at
        }

    # Сохраняем обратную совместимость
    @property
    def title_en(self) -> str:
        return self.get_title("en")

    @property
    def description_en(self) -> Optional[str]:
        return self.get_description("en")

    def is_accessible_to(self, user_id: Optional[uuid.UUID], user_roles: Optional[List[str]] = None) -> bool:
        """
        Check if the course is accessible to a user

        Args:
            user_id: ID of the user trying to access the course
            user_roles: List of roles the user has

        Returns:
            True if the user can access the course, False otherwise
        """
        if not user_roles:
            user_roles = []

        # Admins can access everything
        if "admin" in user_roles:
            return True

        # Published courses with PUBLIC visibility are accessible to everyone
        if self.is_published and self.visibility == CourseVisibility.PUBLIC:
            return True

        # If no user_id provided, only public courses are accessible
        if not user_id:
            return False

        # Authors can always access their own courses
        if user_id == self.author_id:
            return True

        # Handle other visibility levels
        if self.visibility == CourseVisibility.PRIVATE:
            # Only the author can access private courses
            return False
        elif self.visibility == CourseVisibility.FRIENDS:
            # TODO: Implement friend check
            return False
        elif self.visibility == CourseVisibility.LINK:
            # Anyone with the link can access
            return self.is_published
        elif self.visibility == CourseVisibility.PAID:
            # TODO: Implement payment check
            return False
        elif self.visibility == CourseVisibility.ORGANIZATION:
            # TODO: Implement organization membership check
            return False
        elif self.visibility == CourseVisibility.ENROLLED:
            # TODO: Implement enrollment check
            return False

        return False
