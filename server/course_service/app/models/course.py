"""
Course model definitions for the course service
"""
import secrets
import string
import uuid
from datetime import datetime, UTC
from typing import Dict, List, Optional

from sqlalchemy import Column, DateTime, ForeignKey, Integer, String, Table, JSON
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.models.base import Base


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


class Course(Base):
    """
    Course model representing educational courses
    """
    __tablename__ = "courses"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    slug = Column(String, unique=True, index=True, nullable=False)

    # Мультиязычные поля в формате JSON: {"en": "Title", "ru": "Заголовок"}
    title = Column(JSON, nullable=False)
    description = Column(JSON, nullable=True)

    # Внешний ключ на инструктора
    instructor_id = Column(UUID(as_uuid=True), nullable=False, index=True)

    created_at = Column(DateTime, default=lambda: datetime.now(UTC))
    updated_at = Column(DateTime, default=lambda: datetime.now(UTC), onupdate=lambda: datetime.now(UTC))

    # Связь с тегами через связующую таблицу
    tags = relationship("Tag", secondary=course_tag, back_populates="courses")

    # Связь с технологическим деревом
    technology_tree = relationship("TechnologyTree", back_populates="course", uselist=False, cascade="all, delete-orphan")

    def __repr__(self):
        return f"<Course id={self.id}, slug={self.slug}>"

    @property
    def title_en(self) -> str:
        """Get English title or first available if English is not present"""
        if not self.title:
            return ""

        if isinstance(self.title, dict):
            return self.title.get("en", list(self.title.values())[0] if self.title else "")
        return str(self.title)

    @property
    def description_en(self) -> Optional[str]:
        """Get English description or first available if English is not present"""
        if not self.description:
            return None

        if isinstance(self.description, dict):
            return self.description.get("en", list(self.description.values())[0] if self.description else None)
        return str(self.description)
