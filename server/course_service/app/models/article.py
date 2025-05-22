import uuid
from datetime import datetime, timezone
from typing import Dict, Any, Optional, List

from app.models.base import Base
from sqlalchemy import Column, String, Text, Integer, Boolean, ForeignKey, DateTime, UniqueConstraint, Index
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import relationship


class Article(Base):
    """Database model for article in courses."""
    __tablename__ = "articles"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    course_id = Column(UUID(as_uuid=True), ForeignKey("courses.id", ondelete="CASCADE"), nullable=False, index=True)
    slug = Column(String(100), nullable=False, index=True)
    
    # Multilingual fields in JSON format: {"en": "Title", "ru": "Заголовок"}
    title = Column(JSONB, nullable=False)
    description = Column(JSONB, nullable=True)
    content = Column(JSONB, nullable=False)
    
    order = Column(Integer, nullable=False, default=0)
    is_published = Column(Boolean, nullable=False, default=False)
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # Relationship with Course
    course = relationship("Course", back_populates="articles")

    __table_args__ = (
        # Ensure unique combination of course_id and slug
        UniqueConstraint('course_id', 'slug', name='uq_article_course_slug'),
        # Add index for faster filtering by is_published
        Index('ix_articles_is_published', 'is_published')
    )

    def __repr__(self):
        return f"<Article id={self.id}, course_id={self.course_id}, slug={self.slug}>"
    
    def get_title(self, language: str = 'en', fallback: bool = True) -> str:
        """
        Get title in specific language

        Args:
            language: ISO language code (e.g., 'en', 'ru')
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
            language: ISO language code (e.g., 'en', 'ru')
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
    
    def get_content(self, language: str = 'en', fallback: bool = True) -> str:
        """
        Get content in specific language

        Args:
            language: ISO language code (e.g., 'en', 'ru')
            fallback: If True and requested language not found, returns first available
                      content in any language

        Returns:
            Content in requested language or fallback
        """
        if not self.content:
            return ""

        if isinstance(self.content, dict):
            if language in self.content:
                return self.content[language]
            if fallback and self.content:
                return next(iter(self.content.values()), "")
        return str(self.content)
    
    def get_localized_version(self, language: str = 'en', fallback: bool = True) -> Dict[str, Any]:
        """
        Get a localized version of the article

        Args:
            language: ISO language code (e.g., 'en', 'ru')
            fallback: If True and requested language not found, returns first available
                      content in any language

        Returns:
            Dictionary with localized title, description and content
        """
        return {
            "id": self.id,
            "course_id": self.course_id,
            "slug": self.slug,
            "title": self.get_title(language, fallback),
            "description": self.get_description(language, fallback),
            "content": self.get_content(language, fallback),
            "order": self.order,
            "is_published": self.is_published,
            "created_at": self.created_at,
            "updated_at": self.updated_at
        }
        
    def available_languages(self) -> List[str]:
        """
        Get list of all languages available in this article

        Returns:
            List of language codes found in the article fields
        """
        languages = set()

        # Check languages in title
        if isinstance(self.title, dict):
            languages.update(self.title.keys())

        # Check languages in description
        if isinstance(self.description, dict):
            languages.update(self.description.keys())
            
        # Check languages in content
        if isinstance(self.content, dict):
            languages.update(self.content.keys())

        return sorted(list(languages))
