"""
Technology Tree model for course learning paths
"""
import uuid
from datetime import datetime, UTC
from typing import Dict, List, Optional, Any

from sqlalchemy import Column, DateTime, ForeignKey, String, JSON
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.models.base import Base


class TechnologyTree(Base):
    """
    Model for storing structured technology trees (learning/skill paths) for courses
    """
    __tablename__ = "technology_trees"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    course_id = Column(
        UUID(as_uuid=True),
        ForeignKey("courses.id", ondelete="CASCADE"),
        unique=True,
        nullable=False,
        index=True
    )
    # JSON structure contains nodes, connections, and metadata
    data = Column(JSON, nullable=True)

    # Timestamps
    created_at = Column(DateTime, default=lambda: datetime.now(UTC))
    updated_at = Column(DateTime, default=lambda: datetime.now(UTC), onupdate=lambda: datetime.now(UTC))

    # Relationship - переделываем на правильное отношение с Course
    course = relationship("Course", back_populates="technology_tree")

    def __repr__(self) -> str:
        return f"<TechnologyTree id={self.id}, course_id={self.course_id}>"
