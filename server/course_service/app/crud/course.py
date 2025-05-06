"""
CRUD operations for Course model
"""
import uuid
from datetime import datetime, timezone
from typing import Any, Dict, List, Optional, Union, Tuple

import sqlalchemy as sa
from app.models.article import Article
from app.models.course import Course, generate_slug, course_tag, CourseVisibility
from app.models.tag import Tag
from app.schemas.course import CourseCreate, CourseUpdate, CourseSearchParams
from fastapi.encoders import jsonable_encoder
from sqlalchemy import desc, asc, or_, text
from sqlalchemy.orm import Session, joinedload
from sqlalchemy.sql import func
from sqlalchemy.sql import select

from common.logger import get_logger

# Создаем логгер для этого модуля
logger = get_logger("course_service.crud.course")


class CRUDCourse:
    """CRUD operations for Course model"""

    async def get(self, db: Session, id: uuid.UUID) -> Optional[Course]:
        """
        Get a course by ID with related technology tree and tags

        Args:
            db: Database session
            id: UUID of the course

        Returns:
            Course if found, None otherwise
        """
        stmt = select(Course).options(
            joinedload(Course.technology_tree),
            joinedload(Course.tags)
        ).where(Course.id == id)
        result = await db.execute(stmt)
        return result.unique().scalar_one_or_none()

    async def get_course(self, db: Session, course_id: uuid.UUID) -> Optional[Course]:
        """
        Get a course by ID with related technology tree

        Args:
            db: Database session
            course_id: UUID of the course

        Returns:
            Course if found, None otherwise
        """
        return await self.get(db, course_id)

    async def get_by_slug(self, db: Session, slug: str) -> Optional[Course]:
        """
        Get course by slug with related technology tree and tags

        Args:
            db: Database session
            slug: Course slug

        Returns:
            Course object or None if not found
        """
        stmt = (
            select(Course)
            .options(
                joinedload(Course.technology_tree),
                joinedload(Course.tags)
            )
            .where(Course.slug == slug)
        )
        result = await db.execute(stmt)
        return result.unique().scalar_one_or_none()

    async def get_by_title(self, db: Session, title: str, language: str = 'en') -> Optional[Course]:
        """
        Get course by title with related technology tree and tags

        Args:
            db: Database session
            title: Course title
            language: Language code to search for (default: 'en')

        Returns:
            Course object or None if not found
        """
        stmt = (
            select(Course)
            .options(
                joinedload(Course.technology_tree),
                joinedload(Course.tags)
            )
            .where(text(f"title->'{language}' = :title"))
            .params(title=title)
        )
        result = await db.execute(stmt)
        return result.unique().scalar_one_or_none()

    async def get_multi(
            self, db: Session, *, skip: int = 0, limit: int = 100
    ) -> List[Course]:
        """
        Get multiple courses with pagination and related technology trees

        Args:
            db: Database session
            skip: Number of records to skip (offset)
            limit: Maximum number of records to return

        Returns:
            List of courses
        """
        logger.info(f"Getting all courses with skip={skip}, limit={limit}")
        try:
            stmt = select(Course).options(
                joinedload(Course.technology_tree),
                joinedload(Course.tags)
            ).offset(skip).limit(limit).order_by(desc(Course.created_at))

            result = await db.execute(stmt)
            courses = result.unique().scalars().all()
            logger.info(f"Successfully retrieved {len(courses)} courses")
            return courses
        except Exception as e:
            logger.error(f"Error getting courses: {str(e)}", exc_info=True)
            return []

    async def search_courses(
            self,
            db: Session,
            params: CourseSearchParams,
            skip: int = 0,
            limit: int = 10
    ) -> Tuple[List[Course], int]:
        """
        Search and filter courses with advanced parameters

        Args:
            db: Database session
            params: Search parameters object
            skip: Number of records to skip (offset)
            limit: Maximum number of records to return

        Returns:
            Tuple of (list of courses, total count)
        """
        # Построим базовый запрос
        stmt = select(Course).options(
            joinedload(Course.technology_tree),
            joinedload(Course.tags)
        )

        # Логируем параметры поиска
        logger.info(f"Searching courses with params: {params}")

        # Apply search filter if provided
        if params.search:
            # Search in title and description for the specified language
            lang = params.language or 'en'

            # Using PostgreSQL specific JSON operators to search in the fields
            title_filter = text(f"title->'{lang}' ILIKE :search")
            desc_filter = text(f"description->'{lang}' ILIKE :search")

            search_term = f'%{params.search}%'
            stmt = stmt.where(
                or_(
                    title_filter,
                    desc_filter
                )
            ).params(search=search_term)

        # Filter by author if provided
        if params.author_id:
            stmt = stmt.where(Course.author_id == params.author_id)

        # Filter by publication status if provided
        if params.is_published is not None:
            stmt = stmt.where(Course.is_published == params.is_published)

        # Filter by visibility level if provided
        if params.visibility:
            stmt = stmt.where(Course.visibility == params.visibility)

        # Filter by organization if provided
        if params.organization_id:
            stmt = stmt.where(Course.organization_id == params.organization_id)

        # Filter by date range if provided
        if params.from_date:
            stmt = stmt.where(Course.created_at >= params.from_date)

        if params.to_date:
            stmt = stmt.where(Course.created_at <= params.to_date)

        # Filter by tags if provided
        if params.tags and len(params.tags) > 0:
            # Get courses that have any of the specified tags
            tag_ids = [uuid.UUID(tag_id) for tag_id in params.tags]
            stmt = stmt.join(Course.tags).where(Tag.id.in_(tag_ids)).distinct()

        # Логируем запрос
        logger.info(f"SQL query: {stmt}")

        # Get total count before applying pagination
        count_stmt = select(sa.func.count()).select_from(stmt.subquery())
        total_result = await db.execute(count_stmt)
        total = total_result.scalar_one()

        logger.info(f"Total found: {total}")

        # Apply sorting
        if params.sort_by:
            # Sort by a direct column if it exists on the Course model
            if hasattr(Course, params.sort_by):
                sort_column = getattr(Course, params.sort_by)

                # Apply sort direction
                if params.sort_order == 'desc':
                    stmt = stmt.order_by(desc(sort_column))
                else:
                    stmt = stmt.order_by(asc(sort_column))
            # Sort by JSON field if it's a title or description in a specific language
            elif params.sort_by in ['title', 'description'] and params.language:
                # Using PostgreSQL's jsonb_extract_path_text for sorting
                lang = params.language
                sort_expr = text(f"{params.sort_by}->'{lang}'")

                if params.sort_order == 'desc':
                    stmt = stmt.order_by(desc(sort_expr))
                else:
                    stmt = stmt.order_by(asc(sort_expr))
            else:
                # Default sort by created_at if invalid sort field
                if params.sort_order == 'desc':
                    stmt = stmt.order_by(desc(Course.created_at))
                else:
                    stmt = stmt.order_by(asc(Course.created_at))
        else:
            # Default sort
            stmt = stmt.order_by(desc(Course.created_at))

        # Apply pagination
        stmt = stmt.offset(skip).limit(limit)
        results = await db.execute(stmt)

        # Добавляем unique() для результатов, содержащих eager-загруженные коллекции
        courses = results.unique().scalars().all()

        # Логируем количество найденных курсов
        logger.info(f"Found {len(courses)} courses after applying pagination")

        return courses, total

    async def create(self, db: Session, *, obj_in: CourseCreate) -> Course:
        """
        Create a new course

        Args:
            db: Database session
            obj_in: Course data

        Returns:
            Created Course object

        Raises:
            ValueError: If author_id is not provided
        """
        # Ensure author_id is provided
        if not obj_in.author_id:
            raise ValueError("author_id is required to create a course")

        # Generate a unique ID
        course_id = uuid.uuid4()

        # Generate a unique slug
        slug = generate_slug()
        while await self.get_by_slug(db, slug):
            slug = generate_slug()

        # Convert input data to dict and add id and slug
        obj_data = jsonable_encoder(obj_in)

        # Process tags
        tags_data = obj_data.pop("tags", [])

        # Create the course
        db_obj = Course(
            id=course_id,
            slug=slug,
            **obj_data
        )

        # Add tags if provided
        if tags_data:
            tags = []
            for tag_name in tags_data:
                # Check if tag already exists (by name)
                tag = await self._get_or_create_tag(db, tag_name)
                tags.append(tag)

            db_obj.tags = tags

        db.add(db_obj)
        await db.commit()
        await db.refresh(db_obj)

        # Загружаем полноценный объект со всеми связанными данными для возврата
        return await self.get(db, db_obj.id)

    async def update(self, db: Session, *, db_obj: Course, obj_in: Union[CourseUpdate, Dict[str, Any]]) -> Course:
        """
        Update a course

        Args:
            db: Database session
            db_obj: Existing Course object
            obj_in: Updated course data

        Returns:
            Updated Course object
        """
        obj_data = jsonable_encoder(db_obj)
        if isinstance(obj_in, dict):
            update_data = obj_in
        else:
            update_data = obj_in.dict(exclude_unset=True)

        # Update timestamp
        update_data["updated_at"] = datetime.now(timezone.utc)

        # Extract tags if present
        tags_data = update_data.pop("tags", None)

        # Update course fields
        for field in obj_data:
            if field in update_data:
                setattr(db_obj, field, update_data[field])

        # Update tags if provided
        if tags_data is not None:
            tags = []
            for tag_data in tags_data:
                # Data could be a tag id (UUID) or name (str)
                if isinstance(tag_data, uuid.UUID) or (isinstance(tag_data, str) and len(tag_data) == 36):
                    # Если это UUID или строка в формате UUID
                    try:
                        tag_id = tag_data if isinstance(tag_data, uuid.UUID) else uuid.UUID(tag_data)
                        stmt = select(Tag).where(Tag.id == tag_id)
                        result = await db.execute(stmt)
                        tag = result.scalar_one_or_none()
                        if tag:
                            tags.append(tag)
                    except ValueError:
                        # Если не удалось преобразовать в UUID, считаем это именем тега
                        tag = await self._get_or_create_tag(db, tag_data)
                        tags.append(tag)
                else:
                    tag = await self._get_or_create_tag(db, tag_data)
                    tags.append(tag)

            db_obj.tags = tags

        db.add(db_obj)
        await db.commit()
        await db.refresh(db_obj)
        return db_obj

    async def remove(self, db: Session, *, id: uuid.UUID) -> Course:
        """
        Remove a course

        Args:
            db: Database session
            id: UUID of the course to remove

        Returns:
            Removed Course object
        """
        stmt = select(Course).where(Course.id == id)
        result = await db.execute(stmt)
        obj = result.scalar_one_or_none()
        if obj:
            await db.delete(obj)
            await db.commit()
        return obj

    async def _get_or_create_tag(self, db: Session, tag_name: str) -> Tag:
        """
        Get existing tag by name or create a new one

        Args:
            db: Database session
            tag_name: Tag name (assumed to be in English)

        Returns:
            Tag object (existing or newly created)
        """
        # First check if tag already exists with this name in 'en' field
        stmt = select(Tag).where(Tag.name['en'].astext == tag_name)
        result = await db.execute(stmt)
        tag = result.scalar_one_or_none()

        if tag:
            return tag

        # Create new tag with the name as English value
        tag = Tag(name={"en": tag_name})
        db.add(tag)
        await db.commit()
        await db.refresh(tag)
        return tag

    async def get_courses(self, db: Session, skip: int = 0, limit: int = 100, order_by: str = "created_at",
                          order_desc: bool = True) -> List[Course]:
        """
        Get courses with sorting, pagination and related technology trees

        Args:
            db: Database session
            skip: Number of records to skip
            limit: Maximum number of records to return
            order_by: Field to sort by
            order_desc: Sort in descending order if True

        Returns:
            List of courses
        """
        stmt = select(Course).options(
            joinedload(Course.technology_tree),
            joinedload(Course.tags)
        )

        # Handle ordering
        if hasattr(Course, order_by):
            order_field = getattr(Course, order_by)
            if order_desc:
                stmt = stmt.order_by(order_field.desc())
            else:
                stmt = stmt.order_by(order_field)
        else:
            # Default to created_at if field not found
            if order_desc:
                stmt = stmt.order_by(Course.created_at.desc())
            else:
                stmt = stmt.order_by(Course.created_at)

        stmt = stmt.offset(skip).limit(limit)
        result = await db.execute(stmt)
        return result.unique().scalars().all()

    async def search_courses_by_tag(self, db: Session, tag: str, skip: int = 0, limit: int = 100) -> List[Course]:
        """
        Search courses by tag name with related technology trees

        Args:
            db: Database session
            tag: Tag name to search for
            skip: Number of records to skip
            limit: Maximum number of records to return

        Returns:
            List of courses with matching tag
        """
        # First find the tag by name
        tag_query = select(Tag).where(func.lower(Tag.name['en'].astext) == tag.lower())
        tag_result = await db.execute(tag_query)
        tag_obj = tag_result.scalars().first()

        if not tag_obj:
            return []

        # Query courses with the matching tag and load technology tree
        stmt = (
            select(Course)
            .join(course_tag)
            .where(course_tag.c.tag_id == tag_obj.id)
            .options(
                joinedload(Course.technology_tree),
                joinedload(Course.tags)
            )
            .offset(skip)
            .limit(limit)
        )

        result = await db.execute(stmt)
        return result.unique().scalars().all()

    async def update_course(self, db: Session, course_id: uuid.UUID, course_in: CourseUpdate) -> Optional[Course]:
        """
        Update a course by ID

        Args:
            db: Database session
            course_id: UUID of the course to update
            course_in: Updated course data

        Returns:
            Updated Course object, or None if course not found
        """
        db_obj = await self.get(db, course_id)
        if not db_obj:
            return None
        return await self.update(db, db_obj=db_obj, obj_in=course_in)

    async def delete_course(self, db: Session, course_id: uuid.UUID) -> bool:
        """
        Delete a course by ID

        Args:
            db: Database session
            course_id: UUID of the course to delete

        Returns:
            True if successful, False if course not found
        """
        obj = await self.get(db, course_id)
        if not obj:
            return False
        await self.remove(db, id=course_id)
        return True

    async def update_metadata(self, db: Session, course_id: uuid.UUID, metadata: Dict[str, Any]) -> Optional[Course]:
        """
        Update specific metadata fields for a course

        Args:
            db: Database session
            course_id: UUID of the course
            metadata: Dictionary of fields to update

        Returns:
            Updated Course object, or None if course not found
        """
        # Get the course
        course = await self.get(db, course_id)
        if not course:
            return None

        # Update only the specified fields
        for field, value in metadata.items():
            if hasattr(course, field):
                setattr(course, field, value)

        # Update timestamp
        course.updated_at = datetime.now(timezone.utc)

        # Save changes
        db.add(course)
        await db.commit()
        await db.refresh(course)

        return course

    async def update_course_language(
            self,
            db: Session,
            course_id: uuid.UUID,
            language: str,
            title: str,
            description: Optional[str] = None
    ) -> Optional[Course]:
        """
        Add or update a language version of a course

        Args:
            db: Database session
            course_id: UUID of the course
            language: Language code (e.g., 'en', 'ru')
            title: Course title in the specified language
            description: Optional course description in the specified language

        Returns:
            Updated Course object, or None if course not found
        """
        # Get the course
        course = await self.get(db, course_id)
        if not course:
            return None

        # Add or update the language version
        if not isinstance(course.title, dict):
            course.title = {}

        if course.description is None:
            course.description = {}
        elif not isinstance(course.description, dict):
            course.description = {}

        # Update the title
        course.title[language] = title

        # Update the description if provided
        if description is not None:
            course.description[language] = description

        # Save changes
        course.updated_at = datetime.now(timezone.utc)
        db.add(course)
        await db.commit()
        await db.refresh(course)

        return course

    async def remove_course_language(
            self,
            db: Session,
            course_id: uuid.UUID,
            language: str
    ) -> Optional[Course]:
        """
        Remove a language version from a course's content

        Args:
            db: Database session
            course_id: UUID of the course
            language: Language code to remove

        Returns:
            Updated course if found and language was removed, None otherwise
        """
        course = await self.get_course(db, course_id)
        if not course:
            return None

        # Remove the language
        course.remove_language(language)

        # Update the database
        db.add(course)
        await db.commit()
        await db.refresh(course)

        return course

    async def get_course_tree(
            self,
            db: Session,
            course_id: uuid.UUID,
            language: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        Get the complete tree structure for a course

        Args:
            db: Database session
            course_id: UUID of the course
            language: Optional language code for localized content

        Returns:
            Dictionary with the course tree structure
        """
        course = await self.get_course(db, course_id)
        if not course:
            return {"error": "Course not found"}

        # If the course has a technology tree, return it
        if course.technology_tree:
            tree_data = course.technology_tree.data

            # If a language is specified and the tree has localized content,
            # filter the tree to only include content in that language
            if language and isinstance(tree_data, dict) and "content" in tree_data:
                # This is a simplified example. In a real implementation,
                # you would recursively process the tree to extract content
                # in the specified language.
                pass

            return {
                "course_id": str(course_id),
                "tree": tree_data
            }

        # If the course doesn't have a technology tree, return an empty tree
        return {
            "course_id": str(course_id),
            "tree": {}
        }

    async def get_by_id(self, db: Session, course_id: int) -> Optional[Course]:
        """
        Get a course by ID

        Args:
            db: Database session
            course_id: Course ID

        Returns:
            Course object or None if not found
        """
        stmt = select(Course).where(Course.id == course_id)
        result = await db.execute(stmt)
        return result.scalar_one_or_none()


course_crud = CRUDCourse()
