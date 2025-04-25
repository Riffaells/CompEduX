"""
CRUD operations for Course model
"""
import uuid
from datetime import datetime
from typing import Any, Dict, List, Optional, Union, Tuple

from app.models.course import Course, generate_slug, course_tag, CourseVisibility
from app.models.article import Article
from app.models.tag import Tag
from app.schemas.course import CourseCreate, CourseUpdate, CourseSearchParams
from fastapi.encoders import jsonable_encoder
from sqlalchemy import desc, asc, or_, text
from sqlalchemy.orm import Session, joinedload

from common.logger import get_logger

# Создаем логгер для этого модуля
logger = get_logger("course_service.crud.course")


class CRUDCourse:
    """CRUD operations for Course model"""

    def get(self, db: Session, id: uuid.UUID) -> Optional[Course]:
        """
        Get a course by ID with related technology tree

        Args:
            db: Database session
            id: UUID of the course

        Returns:
            Course if found, None otherwise
        """
        return db.query(Course).options(joinedload(Course.technology_tree)).filter(Course.id == id).first()

    def get_course(self, db: Session, course_id: uuid.UUID) -> Optional[Course]:
        """
        Get a course by ID with related technology tree

        Args:
            db: Database session
            course_id: UUID of the course

        Returns:
            Course if found, None otherwise
        """
        return self.get(db, course_id)

    def get_by_slug(self, db: Session, slug: str) -> Optional[Course]:
        """
        Get a course by slug with related technology tree

        Args:
            db: Database session
            slug: Slug of the course

        Returns:
            Course if found, None otherwise
        """
        return db.query(Course).options(joinedload(Course.technology_tree)).filter(Course.slug == slug).first()

    def get_by_title(self, db: Session, title: str, lang: str = "en") -> Optional[Course]:
        """
        Get a course by title (exact match on specified language) with related technology tree

        Args:
            db: Database session
            title: Title of the course
            lang: Language code (default: "en")

        Returns:
            Course if found, None otherwise
        """
        courses = db.query(Course).options(joinedload(Course.technology_tree)).all()
        for course in courses:
            if isinstance(course.title, dict) and course.title.get(lang) == title:
                return course
        return None

    def get_multi(
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
        return db.query(Course).options(joinedload(Course.technology_tree)).offset(skip).limit(limit).all()

    def search_courses(
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
        query = db.query(Course).options(joinedload(Course.technology_tree))

        # Apply search filter if provided
        if params.search:
            # Search in title and description for the specified language
            lang = params.language or 'en'

            # Using PostgreSQL specific JSON operators to search in the fields
            title_filter = text(f"title->'{lang}' ILIKE :search")
            desc_filter = text(f"description->'{lang}' ILIKE :search")

            search_term = f'%{params.search}%'
            query = query.filter(
                or_(
                    title_filter,
                    desc_filter
                )
            ).params(search=search_term)

        # Filter by author if provided
        if params.author_id:
            query = query.filter(Course.author_id == params.author_id)

        # Filter by publication status if provided
        if params.is_published is not None:
            query = query.filter(Course.is_published == params.is_published)

        # Filter by visibility level if provided
        if params.visibility:
            query = query.filter(Course.visibility == params.visibility)

        # Filter by organization if provided
        if params.organization_id:
            query = query.filter(Course.organization_id == params.organization_id)

        # Filter by date range if provided
        if params.from_date:
            query = query.filter(Course.created_at >= params.from_date)

        if params.to_date:
            query = query.filter(Course.created_at <= params.to_date)

        # Filter by tags if provided
        if params.tags and len(params.tags) > 0:
            # Get courses that have any of the specified tags
            query = query.join(Course.tags).filter(
                Tag.id.in_([uuid.UUID(tag_id) for tag_id in params.tags])
            ).distinct()

        # Get total count before applying pagination
        total = query.count()

        # Apply sorting
        if params.sort_by:
            # Sort by a direct column if it exists on the Course model
            if hasattr(Course, params.sort_by):
                sort_column = getattr(Course, params.sort_by)

                # Apply sort direction
                if params.sort_order == 'desc':
                    query = query.order_by(desc(sort_column))
                else:
                    query = query.order_by(asc(sort_column))
            # Sort by JSON field if it's a title or description in a specific language
            elif params.sort_by in ['title', 'description'] and params.language:
                # Using PostgreSQL's jsonb_extract_path_text for sorting
                lang = params.language
                sort_expr = text(f"{params.sort_by}->'{lang}'")

                if params.sort_order == 'desc':
                    query = query.order_by(desc(sort_expr))
                else:
                    query = query.order_by(asc(sort_expr))
            else:
                # Default sort by created_at if invalid sort field
                if params.sort_order == 'desc':
                    query = query.order_by(desc(Course.created_at))
                else:
                    query = query.order_by(asc(Course.created_at))
        else:
            # Default sort
            query = query.order_by(desc(Course.created_at))

        # Apply pagination
        results = query.offset(skip).limit(limit).all()

        return results, total

    def create(self, db: Session, *, obj_in: CourseCreate) -> Course:
        """
        Create a new course

        Args:
            db: Database session
            obj_in: Course data

        Returns:
            Created Course object
        """
        # Generate a unique ID
        course_id = uuid.uuid4()

        # Generate a unique slug
        slug = generate_slug()
        while self.get_by_slug(db, slug):
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
                tag = self._get_or_create_tag(db, tag_name)
                tags.append(tag)

            db_obj.tags = tags

        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def update(self, db: Session, *, db_obj: Course, obj_in: Union[CourseUpdate, Dict[str, Any]]) -> Course:
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
        update_data["updated_at"] = datetime.utcnow()

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
                        tag = db.query(Tag).get(tag_id)
                        if tag:
                            tags.append(tag)
                    except ValueError:
                        # Если не удалось преобразовать в UUID, считаем это именем тега
                        tag = self._get_or_create_tag(db, tag_data)
                        tags.append(tag)
                else:
                    tag = self._get_or_create_tag(db, tag_data)
                    tags.append(tag)

            db_obj.tags = tags

        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def remove(self, db: Session, *, id: uuid.UUID) -> Course:
        """
        Remove a course

        Args:
            db: Database session
            id: UUID of the course to remove

        Returns:
            Removed Course object
        """
        obj = db.query(Course).get(id)
        db.delete(obj)
        db.commit()
        return obj

    def _get_or_create_tag(self, db: Session, tag_name: str) -> Tag:
        """
        Get existing tag by name or create a new one

        Args:
            db: Database session
            tag_name: Tag name (assumed to be in English)

        Returns:
            Tag object (existing or newly created)
        """
        # First check if tag already exists with this name in 'en' field
        tags = db.query(Tag).all()
        for tag in tags:
            if isinstance(tag.name, dict) and tag.name.get("en") == tag_name:
                return tag

        # Create new tag with the name as English value
        tag = Tag(name={"en": tag_name})
        db.add(tag)
        db.commit()
        db.refresh(tag)
        return tag

    def get_courses(self, db: Session, skip: int = 0, limit: int = 100, order_by: str = "created_at",
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
        query = db.query(Course).options(joinedload(Course.technology_tree))

        # Handle ordering
        if hasattr(Course, order_by):
            order_field = getattr(Course, order_by)
            if order_desc:
                query = query.order_by(order_field.desc())
            else:
                query = query.order_by(order_field)
        else:
            # Default to created_at if field not found
            if order_desc:
                query = query.order_by(Course.created_at.desc())
            else:
                query = query.order_by(Course.created_at)

        return query.offset(skip).limit(limit).all()

    def search_courses_by_tag(self, db: Session, tag: str, skip: int = 0, limit: int = 100) -> List[Course]:
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
        # Get all courses with tags and technology trees
        courses = db.query(Course).options(joinedload(Course.technology_tree)).all()

        # Filter by tag name
        results = []
        for course in courses:
            for course_tag in course.tags:
                if course_tag.name.get("en", "").lower() == tag.lower():
                    results.append(course)
                    break

        # Apply pagination
        return results[skip:skip + limit]

    def update_course(self, db: Session, course_id: uuid.UUID, course_in: CourseUpdate) -> Optional[Course]:
        """
        Update a course by ID

        Args:
            db: Database session
            course_id: UUID of the course to update
            course_in: Updated course data

        Returns:
            Updated Course object, or None if course not found
        """
        db_obj = self.get(db, course_id)
        if not db_obj:
            return None
        return self.update(db, db_obj=db_obj, obj_in=course_in)

    def delete_course(self, db: Session, course_id: uuid.UUID) -> bool:
        """
        Delete a course by ID

        Args:
            db: Database session
            course_id: UUID of the course to delete

        Returns:
            True if successful, False if course not found
        """
        obj = self.get(db, course_id)
        if not obj:
            return False
        db.delete(obj)
        db.commit()
        return True

    def update_metadata(self, db: Session, course_id: uuid.UUID, metadata: Dict[str, Any]) -> Optional[Course]:
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
        course = self.get(db, course_id)
        if not course:
            return None

        # Update only the specified fields
        for field, value in metadata.items():
            if hasattr(course, field):
                setattr(course, field, value)

        # Update timestamp
        course.updated_at = datetime.utcnow()

        # Save changes
        db.add(course)
        db.commit()
        db.refresh(course)

        return course

    def update_course_language(
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
        course = self.get(db, course_id)
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
        course.updated_at = datetime.utcnow()
        db.add(course)
        db.commit()
        db.refresh(course)

        return course

    def remove_course_language(
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
        course = self.get_course(db, course_id)
        if not course:
            return None

        # Remove the language
        course.remove_language(language)

        # Update the database
        db.add(course)
        db.commit()
        db.refresh(course)

        return course

    def get_course_tree(
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
        course = self.get_course(db, course_id)
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


course_crud = CRUDCourse()
