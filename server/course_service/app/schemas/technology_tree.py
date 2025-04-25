"""
Schemas for technology tree
"""
from datetime import datetime
from typing import Dict, Any, Optional, List
from uuid import UUID
from pydantic import BaseModel, Field


class TechnologyTreeBase(BaseModel):
    """Base schema for technology tree data"""
    course_id: UUID
    data: Optional[Dict[str, Any]] = Field(default=None, description="Technology tree structure")


class TechnologyTreeCreate(TechnologyTreeBase):
    """Schema for creating a new technology tree"""
    pass


class TechnologyTreeUpdate(BaseModel):
    """Schema for updating an existing technology tree"""
    data: Optional[Dict[str, Any]] = Field(None, description="Technology tree structure")


class TechnologyTree(TechnologyTreeBase):
    """Schema for a complete technology tree with metadata"""
    id: UUID
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

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


class TechnologyTreeWithLocalizedContent(TechnologyTree):
    """Technology tree with content localized for a specific language"""
    localized_data: Optional[Dict[str, Any]] = None

    class Config:
        from_attributes = True

    def __init__(self, **data):
        super().__init__(**data)
        language = data.get('language', 'en')
        self.localized_data = self.get_localized_content(language)


class TechnologyTreeLanguages(BaseModel):
    """Schema for technology tree languages response"""
    languages: List[str] = Field(..., description="List of available languages in the technology tree")
