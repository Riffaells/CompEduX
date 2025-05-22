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
    #       "requirements": ["node2", "node3"],
    #       "content_id": "optional-uuid-of-article"
    #     }
    #   },
    #   "connections": [
    #     {
    #       "id": "conn1",
    #       "from": "node1",
    #       "to": "node2",
    #       "type": "required|recommended|optional"
    #     }
    #   ],
    #   "metadata": {
    #     "defaultLanguage": "en",
    #     "availableLanguages": ["en", "ru"],
    #     "layoutType": "tree|mesh|radial",
    #     "layoutDirection": "horizontal|vertical"
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

        Processes the tree JSON data and extracts content for the requested language.

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
            self.data = {
                "nodes": {}, 
                "connections": [],
                "metadata": {
                    "defaultLanguage": "en",
                    "availableLanguages": ["en"],
                    "layoutType": "tree",
                    "layoutDirection": "horizontal"
                }
            }

        if "nodes" not in self.data:
            self.data["nodes"] = {}

        self.data["nodes"][node_id] = node_data

        # Update metadata
        if "metadata" not in self.data:
            self.data["metadata"] = {}

        # Update available languages
        languages = set()
        if "availableLanguages" in self.data["metadata"]:
            languages.update(self.data["metadata"]["availableLanguages"])
        
        if "title" in node_data and isinstance(node_data["title"], dict):
            languages.update(node_data["title"].keys())
        
        if "description" in node_data and isinstance(node_data["description"], dict):
            languages.update(node_data["description"].keys())
        
        self.data["metadata"]["availableLanguages"] = sorted(list(languages))
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

        # Remove connections involving this node
        if "connections" in self.data:
            self.data["connections"] = [
                conn for conn in self.data["connections"] 
                if conn.get("from") != node_id and conn.get("to") != node_id
            ]

        # Update requirements in other nodes
        for existing_node_id, node_data in self.data["nodes"].items():
            if "requirements" in node_data and node_id in node_data["requirements"]:
                node_data["requirements"].remove(node_id)

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
        
    def add_connection(self, from_node_id: str, to_node_id: str, connection_type: str = "required") -> Optional[str]:
        """
        Add a connection between two nodes
        
        Args:
            from_node_id: ID of the source node
            to_node_id: ID of the target node
            connection_type: Type of connection (required, recommended, optional)
            
        Returns:
            ID of the created connection or None if nodes don't exist
        """
        if not self.data or "nodes" not in self.data:
            return None
            
        # Check if both nodes exist
        if from_node_id not in self.data["nodes"] or to_node_id not in self.data["nodes"]:
            return None
            
        # Initialize connections list if it doesn't exist
        if "connections" not in self.data:
            self.data["connections"] = []
            
        # Create a new connection ID
        connection_id = f"conn-{str(uuid.uuid4())}"
        
        # Create the connection
        connection = {
            "id": connection_id,
            "from": from_node_id,
            "to": to_node_id,
            "type": connection_type
        }
        
        # Add the connection
        self.data["connections"].append(connection)
        
        # Update metadata
        if "metadata" in self.data:
            self.data["metadata"]["updated_at"] = datetime.now(timezone.utc).isoformat()
            
        self.version += 1
        return connection_id
        
    def remove_connection(self, connection_id: str) -> bool:
        """
        Remove a connection from the tree
        
        Args:
            connection_id: ID of the connection to remove
            
        Returns:
            True if connection was removed, False otherwise
        """
        if not self.data or "connections" not in self.data:
            return False
            
        # Find and remove the connection
        initial_length = len(self.data["connections"])
        self.data["connections"] = [
            conn for conn in self.data["connections"] 
            if conn.get("id") != connection_id
        ]
        
        # Check if a connection was removed
        if len(self.data["connections"]) < initial_length:
            # Update metadata
            if "metadata" in self.data:
                self.data["metadata"]["updated_at"] = datetime.now(timezone.utc).isoformat()
                
            self.version += 1
            return True
            
        return False
