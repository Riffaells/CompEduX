"""
Pydantic schemas for course-related data
"""
from datetime import datetime
from typing import Dict, List, Optional, Union, Any
from uuid import UUID

from pydantic import BaseModel, Field, field_validator

from ..models.course import CourseVisibility


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

    model_config = {"from_attributes": True}


# Schemas for Courses
class CourseBase(BaseModel):
    """Base schema for course data"""
    title: Dict[str, str]
    description: Optional[Dict[str, str]] = None
    author_id: Optional[UUID] = None
    visibility: Optional[CourseVisibility] = Field(
        default=CourseVisibility.PRIVATE,
        description="Course visibility level"
    )
    organization_id: Optional[UUID] = Field(
        default=None,
        description="Organization ID (required if visibility is ORGANIZATION)"
    )
    is_published: Optional[bool] = Field(
        default=False,
        description="Whether the course is published"
    )

    @field_validator('organization_id')
    @classmethod
    def validate_organization_id(cls, v, values):
        """Validate that organization_id is provided if visibility is ORGANIZATION"""
        if values.data.get('visibility') == CourseVisibility.ORGANIZATION and not v:
            raise ValueError(
                "organization_id is required when visibility is set to ORGANIZATION"
            )
        return v


class CourseCreate(CourseBase):
    """Schema for creating a new course"""
    tags: Optional[List[str]] = []


class CourseUpdate(BaseModel):
    """Schema for updating an existing course"""
    title: Optional[Dict[str, str]] = None
    description: Optional[Dict[str, str]] = None
    author_id: Optional[UUID] = None
    tags: Optional[List[Union[str, UUID]]] = None
    visibility: Optional[CourseVisibility] = None
    organization_id: Optional[UUID] = None
    is_published: Optional[bool] = None

    @field_validator('organization_id')
    @classmethod
    def validate_organization_id(cls, v, values):
        """Validate that organization_id is provided if visibility is ORGANIZATION"""
        if values.data.get('visibility') == CourseVisibility.ORGANIZATION and v is None:
            raise ValueError(
                "organization_id is required when visibility is set to ORGANIZATION"
            )
        return v


class CourseVisibilityUpdate(BaseModel):
    """Schema for updating course visibility"""
    visibility: CourseVisibility = Field(..., description="Course visibility level")
    organization_id: Optional[UUID] = Field(
        default=None,
        description="Organization ID (required if visibility is ORGANIZATION)"
    )

    @field_validator('organization_id')
    @classmethod
    def validate_organization_id(cls, v, values):
        """Validate that organization_id is provided if visibility is ORGANIZATION"""
        if values.data.get('visibility') == CourseVisibility.ORGANIZATION and not v:
            raise ValueError(
                "organization_id is required when visibility is set to ORGANIZATION"
            )
        return v


class CourseInDB(CourseBase):
    """Schema for course data as stored in DB"""
    id: UUID
    slug: str
    created_at: datetime
    updated_at: datetime

    # TODO: Реализовать в будущем
    # rating_avg: Dict[str, Any] = Field(
    #     default={"value": 0.0, "count": 0},
    #     description="Average rating information"
    # )
    # usage_count: Dict[str, Any] = Field(
    #     default={"groups": 0, "students": 0, "completions": 0},
    #     description="Usage metrics for the course"
    # )

    class Config:
        from_attributes = True


class Course(CourseInDB):
    """Schema for course response with tags"""
    tags: List[Tag] = []


class CourseWithLanguageUtils(Course):
    """Schema for course response with language utility methods"""

    def get_title(self, language: str = 'en', fallback: bool = True) -> str:
        """
        Get title in specific language

        Args:
            language: ISO language code (e.g., 'en', 'ru', 'fr')
            fallback: If True and requested language not found, returns first available
                      title in any language

        Returns:
            Title in requested language or fallback
        """
        if not self.title:
            return ""

        if isinstance(self.title, dict):
            if language in self.title:
                return self.title[language]
            if fallback and self.title:
                return next(iter(self.title.values()), "")
        return str(self.title)

    def get_description(self, language: str = 'en', fallback: bool = True) -> Optional[str]:
        """
        Get description in specific language

        Args:
            language: ISO language code (e.g., 'en', 'ru', 'fr')
            fallback: If True and requested language not found, returns first available
                      description in any language

        Returns:
            Description in requested language or fallback
        """
        if not self.description:
            return None

        if isinstance(self.description, dict):
            if language in self.description:
                return self.description[language]
            if fallback and self.description:
                return next(iter(self.description.values()), None)
        return str(self.description)

    def available_languages(self) -> List[str]:
        """
        Get list of all languages available for this course

        Returns:
            List of ISO language codes
        """
        languages = set()

        if isinstance(self.title, dict):
            languages.update(self.title.keys())

        if isinstance(self.description, dict):
            languages.update(self.description.keys())

        return sorted(list(languages))


class CourseResponse(CourseWithLanguageUtils):
    """Schema for complete course response"""
    tags: Optional[List[Tag]] = Field(default_factory=list)

    model_config = {"from_attributes": True}


# Schema for list of courses with pagination
class CourseList(BaseModel):
    """Schema for paginated list of courses"""
    items: List[Course]
    total: int
    page: int
    size: int
    pages: int


# Extended schema for search/filtering parameters
class CourseSearchParams(BaseModel):
    """Schema for course search/filtering parameters"""
    search: Optional[str] = Field(None, description="Search term for course title and description")
    language: Optional[str] = Field('en', description="Language code for search (e.g., 'en', 'ru')")
    tags: Optional[List[str]] = Field(None, description="List of tag IDs or names to filter by")
    author_id: Optional[UUID] = Field(None, description="Filter by author ID")
    visibility: Optional[CourseVisibility] = Field(None, description="Filter by visibility level")
    is_published: Optional[bool] = Field(None, description="Filter by publication status")
    organization_id: Optional[UUID] = Field(None, description="Filter by organization ID")
    sort_by: Optional[str] = Field('created_at', description="Field to sort by (created_at, title, etc.)")
    sort_order: Optional[str] = Field('desc', description="Sort order: asc or desc")
    from_date: Optional[datetime] = Field(None, description="Filter courses created after this date")
    to_date: Optional[datetime] = Field(None, description="Filter courses created before this date")

    @field_validator('sort_order')
    @classmethod
    def validate_sort_order(cls, v):
        """Validate sort order"""
        if v is not None and v not in ('asc', 'desc'):
            raise ValueError("sort_order must be either 'asc' or 'desc'")
        return v

    @field_validator('language')
    @classmethod
    def validate_language(cls, v):
        """Validate language code"""
        if v is not None and len(v) < 2:
            raise ValueError("language must be a valid ISO language code (e.g., 'en', 'ru')")
        return v

    class Config:
        """Pydantic configuration"""
        from_attributes = True
        json_schema_extra = {
            "example": {
                "search": "programming",
                "language": "en",
                "author_id": "550e8400-e29b-41d4-a716-446655440000",
                "sort_by": "created_at",
                "sort_order": "desc"
            }
        }


# Schema for language operation
class CourseLanguageUpdate(BaseModel):
    """Schema for adding or updating a language version of a course"""
    language: str = Field(..., min_length=2, description="ISO language code (e.g., 'en', 'ru')")
    title: str = Field(..., min_length=1, description="Course title in the specified language")
    description: Optional[str] = Field(None, description="Course description in the specified language")


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


# TODO: Реализовать в будущем
# class CourseRatingUpdate(BaseModel):
#     """Schema for updating course rating"""
#     rating: float = Field(..., ge=0, le=5, description="User rating (0-5)")
#     user_id: UUID = Field(..., description="ID of the user submitting the rating")


# TODO: Реализовать в будущем
# class CourseUsageUpdate(BaseModel):
#     """Schema for updating course usage metrics"""
#     groups: Optional[int] = Field(None, ge=0, description="Number of groups using the course")
#     students: Optional[int] = Field(None, ge=0, description="Number of students using the course")
#     completions: Optional[int] = Field(None, ge=0, description="Number of course completions")


# Schemas for Articles
class ArticleBase(BaseModel):
    """Base schema for article data"""
    slug: str = Field(..., min_length=1, description="Article identifier unique within a course")
    language: str = Field(..., min_length=2, max_length=5, description="ISO language code (e.g. 'en', 'ru')")
    title: str = Field(..., min_length=1, description="Article title")
    order: int = Field(0, ge=0, description="Order of the article within the course")
    content: str = Field(..., description="Article content in Markdown format")
    metadata: Optional[Dict[str, Any]] = Field(None, description="Additional metadata for the article")


class ArticleCreate(ArticleBase):
    """Schema for creating a new article"""
    course_id: UUID = Field(..., description="ID of the course this article belongs to")


class ArticleUpdate(BaseModel):
    """Schema for updating an existing article"""
    title: Optional[str] = Field(None, min_length=1, description="Article title")
    order: Optional[int] = Field(None, ge=0, description="Order of the article within the course")
    content: Optional[str] = Field(None, description="Article content in Markdown format")
    metadata: Optional[Dict[str, Any]] = Field(None, description="Additional metadata for the article")


class ArticleResponse(ArticleBase):
    """Schema for article response"""
    id: UUID
    course_id: UUID
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


# Schema for list of articles with pagination
class ArticleList(BaseModel):
    """Schema for paginated list of articles"""
    items: List[ArticleResponse]
    total: int
    page: int
    size: int
    pages: int
