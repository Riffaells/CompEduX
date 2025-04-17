"""
CRUD operations for Course model
"""
import logging
import uuid
from typing import Any, Dict, List, Optional, Union

from fastapi.encoders import jsonable_encoder
from sqlalchemy.orm import Session, joinedload

from app.models.course import Course, generate_slug
from app.models.tag import Tag
from app.schemas.course import CourseCreate, CourseUpdate

logger = logging.getLogger(__name__)


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

    def update(
        self, db: Session, *, db_obj: Course, obj_in: Union[CourseUpdate, Dict[str, Any]]
    ) -> Course:
        """
        Update a course

        Args:
            db: Database session
            db_obj: Course object to update
            obj_in: Updated course data

        Returns:
            Updated Course object
        """
        if isinstance(obj_in, dict):
            update_data = obj_in
        else:
            update_data = obj_in.dict(exclude_unset=True)

        # Handle tags separately
        tags_data = update_data.pop("tags", None)

        # Update other fields
        for field in update_data:
            if hasattr(db_obj, field) and field != "tags":
                # Handle multilingual fields (title, description)
                if field in ["title", "description"] and getattr(db_obj, field) and update_data[field]:
                    # Merge existing data with new data
                    current_value = getattr(db_obj, field)
                    if isinstance(current_value, dict) and isinstance(update_data[field], dict):
                        current_value.update(update_data[field])
                        setattr(db_obj, field, current_value)
                    else:
                        setattr(db_obj, field, update_data[field])
                else:
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

    def get_courses(self, db: Session, skip: int = 0, limit: int = 100, order_by: str = "created_at", order_desc: bool = True) -> List[Course]:
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
        return results[skip:skip+limit]

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


course_crud = CRUDCourse()
