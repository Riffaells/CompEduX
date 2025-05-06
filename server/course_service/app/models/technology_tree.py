"""
Technology Tree model for course learning paths
"""
import uuid
from datetime import datetime, timezone
from typing import Dict, Any, List, Optional

from app.models.base import Base
from sqlalchemy import Column, DateTime, ForeignKey, JSON, String, Boolean, Integer
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship


class TechnologyTree(Base):
    """
    Model for storing structured technology trees (learning/skill paths) for courses.

    Technology tree represents a visual representation of the course structure and learning path,
    showing dependencies between topics, modules and skills.
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
    # Format:
    # {
    #   "nodes": {
    #     "node1": {
    #       "id": "node1",
    #       "title": {"en": "Title in English", "ru": "Заголовок на русском"},
    #       "description": {"en": "Description in English", "ru": "Описание на русском"},
    #       "type": "topic|skill|module|article",
    #       "position": {"x": 100, "y": 200},
    #       "dependencies": ["node2", "node3"],
    #       "content_ref": "optional-uuid-of-article-or-other-content"
    #     }
    #   },
    #   "metadata": {
    #     "version": "1.0",
    #     "created_at": "ISO timestamp",
    #     "updated_at": "ISO timestamp"
    #   }
    # }
    data = Column(JSON, nullable=True)

    # Indicates if the tree is published and visible to students
    is_published = Column(Boolean, default=False, nullable=False)

    # Tree version for tracking changes
    version = Column(Integer, default=1, nullable=False)

    # Timestamps
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime(timezone=True), default=lambda: datetime.now(timezone.utc),
                        onupdate=lambda: datetime.now(timezone.utc))

    # Relationship with Course model
    course = relationship("Course", back_populates="technology_tree")

    def __repr__(self) -> str:
        return f"<TechnologyTree id={self.id}, course_id={self.course_id}, version={self.version}>"

    def get_localized_content(self, language: str = 'en', fallback: bool = True) -> Dict[str, Any]:
        """
        Get technology tree content localized for a specific language

        Processes the tree JSON data and adds localized versions of titles and descriptions
        based on the requested language.

        Args:
            language: Language code (e.g., 'en', 'ru')
            fallback: Whether to fall back to another language if requested language not found

        Returns:
            Dictionary containing localized tree content with additional 'title_localized'
            and 'description_localized' fields for each node
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
            List of language codes found in the tree nodes
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

    def add_node(self, node_id: str, node_data: Dict[str, Any]) -> None:
        """
        Add a new node to the technology tree

        Args:
            node_id: Unique identifier for the node
            node_data: Node data including title, description, position, etc.
        """
        if not self.data:
            self.data = {"nodes": {}, "metadata": {"version": 1}}

        if "nodes" not in self.data:
            self.data["nodes"] = {}

        self.data["nodes"][node_id] = node_data

        # Update metadata
        if "metadata" not in self.data:
            self.data["metadata"] = {}

        self.data["metadata"]["updated_at"] = datetime.now(timezone.utc).isoformat()
        self.version += 1

    def remove_node(self, node_id: str) -> bool:
        """
        Remove a node from the technology tree

        Args:
            node_id: ID of the node to remove

        Returns:
            True if node was removed, False if node not found
        """
        if not self.data or "nodes" not in self.data or node_id not in self.data["nodes"]:
            return False

        # Remove the node
        del self.data["nodes"][node_id]

        # Update dependencies in other nodes
        for existing_node_id, node_data in self.data["nodes"].items():
            if "dependencies" in node_data and node_id in node_data["dependencies"]:
                node_data["dependencies"].remove(node_id)

        # Update metadata
        if "metadata" in self.data:
            self.data["metadata"]["updated_at"] = datetime.now(timezone.utc).isoformat()

        self.version += 1
        return True

    def get_node(self, node_id: str) -> Optional[Dict[str, Any]]:
        """
        Get a specific node from the technology tree

        Args:
            node_id: ID of the node to retrieve

        Returns:
            Node data if found, None otherwise
        """
        if not self.data or "nodes" not in self.data or node_id not in self.data["nodes"]:
            return None

        return self.data["nodes"][node_id]
