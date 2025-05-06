from datetime import datetime
from typing import Optional, List, Dict, Any
from uuid import UUID

from pydantic import BaseModel, Field, validator, root_validator, model_validator


# Base Lesson Schema
class LessonBase(BaseModel):
    """Base schema for Lesson data."""
    slug: str = Field(..., min_length=1, max_length=100)
    language: str = Field(..., min_length=2, max_length=5)
    title: str = Field(..., min_length=1, max_length=200)
    description: Optional[str] = Field(None, max_length=500)
    content: str
    order: int = Field(0, ge=0)
    duration: Optional[int] = Field(None, ge=0, description="Estimated duration in minutes")
    is_published: bool = Field(False)
    lesson_metadata: Optional[Dict[str, Any]] = Field(default_factory=dict)
    tree_node_id: Optional[UUID] = Field(None, description="ID of the technology tree node this lesson is associated with")


# Schema for creating a new lesson
class LessonCreate(LessonBase):
    """Schema for creating a new lesson."""
    course_id: UUID = Field(..., description="ID of the course this lesson belongs to")


# Schema for updating an existing lesson
class LessonUpdate(BaseModel):
    """Schema for updating an existing lesson."""
    title: Optional[str] = Field(None, min_length=1, max_length=200)
    description: Optional[str] = Field(None, max_length=500)
    content: Optional[str] = None
    order: Optional[int] = Field(None, ge=0)
    duration: Optional[int] = Field(None, ge=0)
    is_published: Optional[bool] = None
    lesson_metadata: Optional[Dict[str, Any]] = None
    tree_node_id: Optional[UUID] = Field(None, description="ID of the technology tree node this lesson is associated with")


# Schema for lesson response
class LessonResponse(LessonBase):
    """Schema for lesson responses."""
    id: UUID
    course_id: UUID
    created_at: datetime
    updated_at: datetime
    article_ids: List[UUID] = Field(default_factory=list, description="IDs of articles associated with this lesson")

    class Config:
        from_attributes = True


# Schema for article reference in lesson content
class ArticleReference(BaseModel):
    """Minimal article data for use in lesson content."""
    id: UUID
    title: str
    description: Optional[str] = None
    language: str
    slug: str


# Schema for tree node reference
class NodeReference(BaseModel):
    """Minimal tree node data for use in lesson content."""
    id: str
    title: Dict[str, str] = Field(..., description="Node title in different languages")
    description: Optional[Dict[str, str]] = Field(None, description="Node description in different languages")
    type: Optional[str] = Field(None, description="Node type")
    metadata: Optional[Dict[str, Any]] = Field(None, description="Node metadata")


# Schema for lesson with full content
class LessonWithContent(LessonResponse):
    """Schema for lesson with associated articles and materials."""
    articles: List[ArticleReference] = Field(default_factory=list, description="Articles associated with this lesson")
    node_info: Optional[NodeReference] = Field(None, description="Information about the associated tree node")


# Schema for listing lessons
class LessonListResponse(BaseModel):
    """Schema for a paginated list of lessons."""
    items: List[LessonResponse]
    total: int

    class Config:
        from_attributes = True
