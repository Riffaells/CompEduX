from datetime import datetime
from typing import Optional, List
from uuid import UUID
from pydantic import BaseModel, Field


# Base Article Schema
class ArticleBase(BaseModel):
    """Base schema for Article data."""
    slug: str = Field(..., min_length=1, max_length=100)
    language: str = Field(..., min_length=2, max_length=5)
    title: str = Field(..., min_length=1, max_length=200)
    description: Optional[str] = Field(None, max_length=500)
    content: str
    order: int = Field(0, ge=0)
    is_published: bool = Field(False)


# Schema for creating an article
class ArticleCreate(ArticleBase):
    """Schema for creating a new article."""
    pass


# Schema for updating an article
class ArticleUpdate(BaseModel):
    """Schema for updating an existing article."""
    slug: Optional[str] = Field(None, min_length=1, max_length=100)
    language: Optional[str] = Field(None, min_length=2, max_length=5)
    title: Optional[str] = Field(None, min_length=1, max_length=200)
    description: Optional[str] = Field(None, max_length=500)
    content: Optional[str] = None
    order: Optional[int] = Field(None, ge=0)
    is_published: Optional[bool] = None


# Schema for article response
class ArticleResponse(ArticleBase):
    """Schema for article responses."""
    id: UUID
    course_id: UUID
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


# Schema for listing articles
class ArticleListResponse(BaseModel):
    """Schema for a paginated list of articles."""
    items: List[ArticleResponse]
    total: int

    class Config:
        from_attributes = True
