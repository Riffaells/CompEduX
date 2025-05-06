import uuid
from datetime import datetime, timezone

from app.models.base import Base
from sqlalchemy import Column, String, Text, Integer, Boolean, ForeignKey, DateTime, UniqueConstraint, Index, Table
from sqlalchemy.dialects.postgresql import UUID, JSONB, ARRAY
from sqlalchemy.orm import relationship

# Связующая таблица между уроками и статьями
lesson_article = Table(
    "lesson_article",
    Base.metadata,
    Column("lesson_id", UUID(as_uuid=True), ForeignKey("lessons.id", ondelete="CASCADE"), primary_key=True),
    Column("article_id", UUID(as_uuid=True), ForeignKey("articles.id", ondelete="CASCADE"), primary_key=True)
)

class Lesson(Base):
    """Database model for lessons in courses."""
    __tablename__ = "lessons"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4, index=True)
    course_id = Column(UUID(as_uuid=True), ForeignKey("courses.id", ondelete="CASCADE"), nullable=False, index=True)
    slug = Column(String(100), nullable=False, index=True)
    language = Column(String(5), nullable=False, index=True)
    title = Column(String(200), nullable=False)
    description = Column(String(500), nullable=True)

    # The main content of the lesson in Markdown or structured format
    content = Column(Text, nullable=False)

    # Additional data for interactive content, videos, attachments, etc.
    lesson_metadata = Column(JSONB, nullable=True)

    # ID ссылки на узел в дереве навыков (если урок привязан к конкретному узлу дерева)
    tree_node_id = Column(String(100), nullable=True)

    # Sorting order within a course
    order = Column(Integer, nullable=False, default=0)

    # Duration in minutes (estimated)
    duration = Column(Integer, nullable=True)

    # Whether this lesson is published and visible to students
    is_published = Column(Boolean, nullable=False, default=False)

    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # Relationship with Course
    course = relationship("Course", back_populates="lessons")

    # Relationships with Articles
    articles = relationship("Article", secondary=lesson_article, backref="lessons")

    __table_args__ = (
        # Ensure unique combination of course_id, slug, and language
        UniqueConstraint('course_id', 'slug', 'language', name='uq_lesson_course_slug_lang'),
        # Add index for faster filtering by is_published
        Index('ix_lessons_is_published', 'is_published'),
        # Add index for tree_node_id for faster lookups
        Index('ix_lessons_tree_node_id', 'tree_node_id')
    )

    def __repr__(self):
        return f"<Lesson id={self.id}, course_id={self.course_id}, slug={self.slug}, language={self.language}>"

    @property
    def article_ids(self):
        """Return list of article IDs associated with this lesson"""
        return [article.id for article in self.articles] if self.articles else []
