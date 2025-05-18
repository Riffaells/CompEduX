from typing import List, Optional, Tuple, Dict, Any, Union
from uuid import UUID
from sqlalchemy import select, func, or_, and_
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.models.course import Course, CourseVisibility
from app.models.tag import Tag
from app.schemas.course import CourseSearchParams

class CourseRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_course(self, course_id: UUID) -> Optional[Course]:
        """
        Get a course by ID.

        Args:
            course_id: UUID of the course

        Returns:
            Course object if found, None otherwise
        """
        query = select(Course).where(Course.id == course_id).options(
            selectinload(Course.tags),
            selectinload(Course.technology_tree)
        )
        result = await self.db.execute(query)
        return result.scalars().first()

    async def get_by_slug(self, slug: str) -> Optional[Course]:
        """
        Get a course by slug.

        Args:
            slug: Course slug

        Returns:
            Course object if found, None otherwise
        """
        query = select(Course).where(Course.slug == slug).options(
            selectinload(Course.tags),
            selectinload(Course.technology_tree)
        )
        result = await self.db.execute(query)
        return result.scalars().first()

    async def search_courses(self,
                            params: CourseSearchParams,
                            skip: int = 0,
                            limit: int = 100) -> Tuple[List[Course], int]:
        """
        Search courses with filtering, sorting, and pagination.

        Args:
            params: Search parameters
            skip: Number of records to skip
            limit: Maximum number of records to return

        Returns:
            Tuple of (list of courses, total count)
        """
        # Base query with joins and loading options
        query = select(Course).options(
            selectinload(Course.tags),
            selectinload(Course.technology_tree)
        )

        # Apply filters
        filters = []

        # Filter by author
        if params.author_id:
            filters.append(Course.author_id == params.author_id)

        # Filter by publication status
        if params.is_published is not None:
            filters.append(Course.is_published == params.is_published)

        # Filter by date range
        if params.from_date:
            filters.append(Course.created_at >= params.from_date)
        if params.to_date:
            filters.append(Course.created_at <= params.to_date)

        # Apply search term
        if params.search and params.language:
            # Search in specific language
            search_filter = or_(
                Course.title[params.language].astext.ilike(f"%{params.search}%"),
                Course.description[params.language].astext.ilike(f"%{params.search}%") if Course.description else False
            )
            filters.append(search_filter)
        elif params.search:
            # Search in all languages
            search_filter = or_(
                Course.title.astext.ilike(f"%{params.search}%"),
                Course.description.astext.ilike(f"%{params.search}%") if Course.description else False
            )
            filters.append(search_filter)

        # Apply tag filter if specified
        if params.tags:
            query = query.join(Course.tags).filter(Tag.id.in_(params.tags))

        # Apply all filters
        if filters:
            query = query.filter(and_(*filters))

        # Count total before pagination
        count_query = select(func.count()).select_from(query.subquery())
        total = await self.db.execute(count_query)
        total = total.scalar() or 0

        # Apply sorting
        if params.sort_by:
            sort_column = getattr(Course, params.sort_by)
            if params.sort_order and params.sort_order.lower() == "desc":
                query = query.order_by(sort_column.desc())
            else:
                query = query.order_by(sort_column)

        # Apply pagination
        query = query.offset(skip).limit(limit)

        # Execute the query
        result = await self.db.execute(query)
        courses = result.scalars().all()

        return list(courses), total

    async def update_course_language(self,
                                    course_id: UUID,
                                    language: str,
                                    title: str,
                                    description: Optional[str] = None) -> Optional[Course]:
        """
        Add or update a language version of course content.

        Args:
            course_id: UUID of the course
            language: Language code
            title: Title in the specified language
            description: Optional description in the specified language

        Returns:
            Updated Course object if successful, None otherwise
        """
        course = await self.get_course(course_id)
        if not course:
            return None

        # Add language
        course.add_language(language, title, description)

        # Save changes
        self.db.add(course)
        await self.db.commit()
        await self.db.refresh(course)

        return course
