"""
Technology Tree model for course learning paths
"""
import uuid
from datetime import datetime, timezone
from typing import Dict, Any, Optional, List

from app.models.base import Base
from sqlalchemy import Column, DateTime, ForeignKey, JSON
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship


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
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # Relationship - переделываем на правильное отношение с Course
    course = relationship("Course", back_populates="technology_tree")

    def __repr__(self) -> str:
        return f"<TechnologyTree id={self.id}, course_id={self.course_id}>"

    def get_localized_content(self, language: str = 'en', fallback: bool = True) -> Dict[str, Any]:
        """
        Get technology tree content localized for a specific language

        Args:
            language: Language code (e.g., 'en', 'ru')
            fallback: Whether to fall back to another language if requested language not found

        Returns:
            Dictionary containing localized tree content
        """
        if not self.data:
            return {}

        # Clone the data to avoid modifying the original
        localized_data = dict(self.data)

        # Process nodes if they exist
        if 'nodes' in localized_data:
            for node_id, node in localized_data['nodes'].items():
                # Localize node titles
                if 'title' in node and isinstance(node['title'], dict):
                    if language in node['title']:
                        node['title_localized'] = node['title'][language]
                    elif fallback and node['title']:
                        # Fallback to first available language
                        node['title_localized'] = next(iter(node['title'].values()), "")
                    else:
                        node['title_localized'] = ""

                # Localize node descriptions
                if 'description' in node and isinstance(node['description'], dict):
                    if language in node['description']:
                        node['description_localized'] = node['description'][language]
                    elif fallback and node['description']:
                        # Fallback to first available language
                        node['description_localized'] = next(iter(node['description'].values()), "")
                    else:
                        node['description_localized'] = ""

        return localized_data

    def available_languages(self) -> List[str]:
        """
        Get list of all languages available in this technology tree

        Returns:
            List of language codes
        """
        languages = set()

        if not self.data or 'nodes' not in self.data:
            return []

        # Check languages in each node
        for node_id, node in self.data['nodes'].items():
            # Get languages from titles
            if 'title' in node and isinstance(node['title'], dict):
                languages.update(node['title'].keys())

            # Get languages from descriptions
            if 'description' in node and isinstance(node['description'], dict):
                languages.update(node['description'].keys())

        return sorted(list(languages))
