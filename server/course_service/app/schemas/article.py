from datetime import datetime
from typing import Optional, List, Dict
from uuid import UUID

from pydantic import BaseModel, Field


# Base Article Schema
class ArticleBase(BaseModel):
    """Base schema for Article data."""
    slug: str = Field(..., min_length=1, max_length=100)
    # Multilingual fields in JSON format: {"en": "Title", "ru": "Заголовок"}
    title: Dict[str, str] = Field(..., description="Multilingual title in format {language_code: title}")
    description: Optional[Dict[str, str]] = Field(None, description="Multilingual description in format {language_code: description}")
    content: Dict[str, str] = Field(..., description="Multilingual content in format {language_code: content}")
    order: int = Field(0, ge=0)
    is_published: bool = Field(False)


# Schema for creating an article
class ArticleCreate(ArticleBase):
    """Schema for creating a new article."""
    course_id: UUID = Field(..., description="ID of the course this article belongs to")


# Schema for updating an article
class ArticleUpdate(BaseModel):
    """Schema for updating an existing article."""
    slug: Optional[str] = Field(None, min_length=1, max_length=100)
    title: Optional[Dict[str, str]] = Field(None, description="Multilingual title in format {language_code: title}")
    description: Optional[Dict[str, str]] = Field(None, description="Multilingual description in format {language_code: description}")
    content: Optional[Dict[str, str]] = Field(None, description="Multilingual content in format {language_code: content}")
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


# Schema for localized article response
class ArticleLocalizedResponse(BaseModel):
    """Schema for localized article response."""
    id: UUID
    course_id: UUID
    slug: str
    title: str
    description: Optional[str] = None
    content: str
    order: int
    is_published: bool
    created_at: datetime
    updated_at: datetime
    language: str

    class Config:
        from_attributes = True


# Schema for article language information
class ArticleLanguagesResponse(BaseModel):
    """Schema for article languages response."""
    languages: List[str] = Field(..., description="List of available languages in the article")
