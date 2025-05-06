import uuid
from datetime import datetime, timezone

from app.models.base import Base
from sqlalchemy import Column, String, Text, Integer, Boolean, ForeignKey, DateTime, UniqueConstraint, Index
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship


class Article(Base):
    """Database model for article in courses."""
    __tablename__ = "articles"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    course_id = Column(UUID(as_uuid=True), ForeignKey("courses.id", ondelete="CASCADE"), nullable=False, index=True)
    slug = Column(String(100), nullable=False, index=True)
    language = Column(String(5), nullable=False, index=True)
    title = Column(String(200), nullable=False)
    description = Column(String(500), nullable=True)
    content = Column(Text, nullable=False)
    order = Column(Integer, nullable=False, default=0)
    is_published = Column(Boolean, nullable=False, default=False)
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # Relationship with Course
    course = relationship("Course", back_populates="articles")

    __table_args__ = (
        # Ensure unique combination of course_id, slug, and language
        UniqueConstraint('course_id', 'slug', 'language', name='uq_article_course_slug_lang'),
        # Add index for faster filtering by is_published
        Index('ix_articles_is_published', 'is_published')
    )

    def __repr__(self):
        return f"<Article id={self.id}, course_id={self.course_id}, slug={self.slug}, language={self.language}>"
