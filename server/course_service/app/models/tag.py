"""
Tag and TagTranslation models for storing course tags with multilingual support
"""
import uuid
from datetime import datetime, timezone
from typing import Optional

from app.models.base import Base
from sqlalchemy import Column, DateTime, ForeignKey, String, UniqueConstraint
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship


class TagTranslation(Base):
    """
    Model for tag translations in different languages
    """
    __tablename__ = "tag_translations"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    tag_id = Column(UUID(as_uuid=True), ForeignKey("tags.id", ondelete="CASCADE"), nullable=False, index=True)
    language = Column(String(5), nullable=False, index=True)  # ISO language code, e.g., 'en', 'ru'
    name = Column(String(100), nullable=False, index=True)

    # Relationship
    tag = relationship("Tag", back_populates="translations")

    # Unique constraint
    __table_args__ = (
        UniqueConstraint('tag_id', 'language', name='uix_tag_language'),
    )

    def __repr__(self):
        return f"<TagTranslation id={self.id}, tag_id={self.tag_id}, language={self.language}, name={self.name}>"


class Tag(Base):
    """
    Tag model for categorizing courses with multilingual support
    """
    __tablename__ = "tags"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)

    # Timestamps
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # Relationships
    translations = relationship("TagTranslation", back_populates="tag", cascade="all, delete-orphan")

    # Relationship with courses
    courses = relationship("Course", secondary="course_tag", back_populates="tags")

    def __repr__(self):
        return f"<Tag id={self.id}>"

    @property
    def name_en(self) -> Optional[str]:
        """Get English name of the tag"""
        for translation in self.translations:
            if translation.language == "en":
                return translation.name
        return None

    @property
    def name_ru(self) -> Optional[str]:
        """Get Russian name of the tag"""
        for translation in self.translations:
            if translation.language == "ru":
                return translation.name
        return None

    @property
    def name(self) -> dict:
        """Get all translations as a dictionary"""
        result = {}
        for translation in self.translations:
            result[translation.language] = translation.name
        return result
