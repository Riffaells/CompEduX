"""
Pydantic schemas for course-related data
"""
from datetime import datetime
from typing import Dict, List, Optional, Union, Any
from uuid import UUID

from pydantic import BaseModel, Field, validator

from app.schemas.technology_tree import TechnologyTree


# Schemas for Tags
class TagBase(BaseModel):
    """Base schema for tag data"""
    name: Dict[str, str]


class TagCreate(TagBase):
    """Schema for creating a new tag"""
    pass


class TagUpdate(TagBase):
    """Schema for updating an existing tag"""
    name: Optional[Dict[str, str]] = None


class Tag(TagBase):
    """Schema for tag response"""
    id: UUID

    class Config:
        from_attributes = True


# Schemas for Courses
class CourseBase(BaseModel):
    """Base schema for course data"""
    title: Dict[str, str]
    description: Optional[Dict[str, str]] = None
    instructor_id: UUID


class CourseCreate(CourseBase):
    """Schema for creating a new course"""
    tags: Optional[List[str]] = []  # List of tag names or IDs


class CourseUpdate(BaseModel):
    """Schema for updating an existing course"""
    title: Optional[Dict[str, str]] = None
    description: Optional[Dict[str, str]] = None
    instructor_id: Optional[UUID] = None
    tags: Optional[List[Union[str, UUID]]] = None


class CourseInDB(CourseBase):
    """Schema for course data as stored in DB"""
    id: UUID
    slug: str
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class Course(CourseInDB):
    """Schema for course response with tags"""
    tags: List[Tag] = []


class CourseResponse(Course):
    """Schema for complete course response, including optional technology tree"""
    technology_tree: Optional[TechnologyTree] = None

    class Config:
        from_attributes = True


# Schema for list of courses with pagination
class CourseList(BaseModel):
    """Schema for paginated list of courses"""
    items: List[Course]
    total: int
    page: int
    size: int
    pages: int


# Schema for development tree
class DevelopmentTree(BaseModel):
    """Schema for storing development tree data"""
    id: UUID
    course_id: UUID
    data: Dict[str, Any] = Field(..., description="JSON structure of the development tree")
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class DevelopmentTreeCreate(BaseModel):
    """Schema for creating a development tree"""
    course_id: UUID
    data: Dict[str, Any] = Field(..., description="JSON structure of the development tree")


class DevelopmentTreeUpdate(BaseModel):
    """Schema for updating a development tree"""
    data: Optional[Dict[str, Any]] = Field(None, description="JSON structure of the development tree")
