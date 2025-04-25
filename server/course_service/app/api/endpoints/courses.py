"""
API endpoints for courses
"""
import uuid
from datetime import datetime
from typing import Any, Dict, List, Optional

from fastapi import APIRouter, Body, Depends, HTTPException, Path, Query, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session

from app.api.deps import get_db, verify_token
from app.crud.course import course_crud
from app.models.course import CourseVisibility
from app.schemas.course import (
    Course, CourseCreate, CourseList, CourseResponse, CourseUpdate,
    CourseVisibilityUpdate, CourseLanguageUpdate
)

from common.logger import get_logger

# Set up logger
logger = get_logger("course_service.api.courses")

router = APIRouter()


@router.get("/", response_model=CourseList)
async def get_courses(
        page: int = Query(0, ge=0, description="Page number (starting from 0)"),
        size: int = Query(30, ge=1, le=100, description="Page size"),
        search: Optional[str] = Query(None, description="Search query for filtering by title and description"),
        language: Optional[str] = Query(None, description="Language code for search (e.g., 'en', 'ru')"),
        sort_by: Optional[str] = Query("created_at", description="Field to sort by"),
        sort_order: Optional[str] = Query("desc", description="Sort order: asc or desc"),
        author_id: Optional[uuid.UUID] = Query(None, description="Filter by author ID"),
        tag_ids: Optional[List[str]] = Query(None, description="List of tag IDs to filter by"),
        from_date: Optional[datetime] = Query(None, description="Filter courses created after this date"),
        to_date: Optional[datetime] = Query(None, description="Filter courses created before this date"),
        id: Optional[uuid.UUID] = Query(None, description="Get course by ID"),
        slug: Optional[str] = Query(None, description="Get course by slug"),
        db: Session = Depends(get_db)
):
    """
    Get a list of courses with filtering and pagination
    """
    logger.info(f"Request to list courses with params: page={page}, size={size}, search={search}")

    # If we have a direct ID or slug lookup, use that instead of pagination
    if id is not None:
        course = course_crud.get_course(db, id)
        if course is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID {id} not found"
            )
        return CourseList(items=[course], total=1, page=0, size=1, pages=1)

    if slug is not None:
        course = course_crud.get_by_slug(db, slug)
        if course is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with slug '{slug}' not found"
            )
        return CourseList(items=[course], total=1, page=0, size=1, pages=1)

    # Build search params for more complex queries
    from app.schemas.course import CourseSearchParams
    search_params = CourseSearchParams(
        search=search,
        language=language,
        tags=tag_ids,
        author_id=author_id,
        sort_by=sort_by,
        sort_order=sort_order,
        from_date=from_date,
        to_date=to_date
    )

    # Get courses with pagination
    courses, total = course_crud.search_courses(db, search_params, skip=page*size, limit=size)
    pages = (total + size - 1) // size  # Calculate total pages

    return CourseList(
        items=courses,
        total=total,
        page=page,
        size=size,
        pages=pages
    )


@router.get("/course", response_model=CourseResponse)
async def get_course_by_params(
        id: Optional[uuid.UUID] = Query(None, description="Course ID"),
        slug: Optional[str] = Query(None, description="Course slug"),
        db: Session = Depends(get_db)
):
    """
    Get information about a course by ID or slug using query parameters

    Use this endpoint when you want to fetch a course using query parameters
    instead of path parameters.

    Either id or slug must be provided.
    """
    if id is None and slug is None:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Either 'id' or 'slug' query parameter must be provided"
        )

    if id is not None:
        logger.info(f"Request to get course with ID: {id}")
        course = course_crud.get_course(db, id)
        if course is None:
            logger.warning(f"Course with ID {id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID {id} not found"
            )
        return course

    # If we're here, slug must be provided
    logger.info(f"Request to get course with slug: {slug}")
    course = course_crud.get_by_slug(db, slug)
    if course is None:
        logger.warning(f"Course with slug '{slug}' not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with slug '{slug}' not found"
        )
    return course


@router.get("/{id_or_slug}", response_model=CourseResponse)
async def get_course_flexible(
        id_or_slug: str,
        db: Session = Depends(get_db)
):
    """
    Get information about a course by its ID or slug

    This endpoint automatically detects whether you're providing a UUID or a slug
    and routes the request accordingly.
    """
    logger.info(f"Request to get course with ID or slug: {id_or_slug}")

    # Try to parse as UUID first
    try:
        course_id = uuid.UUID(id_or_slug)
        course = course_crud.get_course(db, course_id)
        if course is None:
            logger.warning(f"Course with ID {course_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID {course_id} not found"
            )
        return course
    except ValueError:
        # Not a valid UUID, try as slug
        course = course_crud.get_by_slug(db, id_or_slug)
        if course is None:
            logger.warning(f"Course with slug '{id_or_slug}' not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID or slug '{id_or_slug}' not found"
            )
        return course


@router.post("/", response_model=CourseResponse, status_code=status.HTTP_201_CREATED)
async def create_course(
        course: CourseCreate,
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Create a new course

    Authentication required.
    """
    logger.info(f"Request to create a new course")
    return course_crud.create(db, obj_in=course)


@router.put("/{course_id}", response_model=CourseResponse)
async def update_course(
        course_id: uuid.UUID,
        course: CourseUpdate,
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Update an existing course

    Authentication required.
    """
    logger.info(f"Request to update course with ID: {course_id}")
    db_course = course_crud.update_course(db, course_id, course)
    if db_course is None:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )
    return db_course


@router.patch("/{course_id}/metadata", response_model=CourseResponse)
async def update_course_metadata(
        course_id: uuid.UUID,
        metadata: Dict[str, Any] = Body(..., description="Course metadata to update"),
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Update specific course metadata fields

    Authentication required.

    This endpoint allows updating specific course fields without sending the entire course object.
    """
    logger.info(f"Request to update metadata for course ID: {course_id}")

    # Update metadata
    updated_course = course_crud.update_metadata(db, course_id, metadata)
    if updated_course is None:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    return updated_course


@router.patch("/{course_id}/status", response_model=CourseResponse)
async def update_course_status(
        course_id: uuid.UUID,
        is_published: bool = Body(..., description="Course publication status"),
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Update course publication status

    Authentication required.

    Simple endpoint to quickly toggle course publication status.
    """
    logger.info(f"Request to update status for course ID: {course_id} to is_published={is_published}")

    # Get the course
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Update status
    metadata = {"is_published": is_published}
    updated_course = course_crud.update_metadata(db, course_id, metadata)
    return updated_course


@router.patch("/{course_id}/visibility", response_model=CourseResponse)
async def update_course_visibility(
        course_id: uuid.UUID,
        visibility_data: CourseVisibilityUpdate,
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Update course visibility settings

    Authentication required.

    This endpoint allows changing the visibility level of a course and related settings.
    """
    logger.info(f"Request to update visibility for course ID: {course_id} to {visibility_data.visibility}")

    # Get the course
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Validation for organization visibility
    if visibility_data.visibility == CourseVisibility.ORGANIZATION and not visibility_data.organization_id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="organization_id is required when visibility is set to ORGANIZATION"
        )

    # Prepare data for update
    metadata = {
        "visibility": visibility_data.visibility,
    }

    # Only update organization_id if the visibility is ORGANIZATION
    if visibility_data.visibility == CourseVisibility.ORGANIZATION:
        metadata["organization_id"] = visibility_data.organization_id

    # Remove organization_id when switching to other visibility levels
    elif course.organization_id is not None:
        metadata["organization_id"] = None

    # Update visibility settings
    updated_course = course_crud.update_metadata(db, course_id, metadata)
    return updated_course


@router.delete("/{course_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_course(
        course_id: uuid.UUID,
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Delete a course

    Authentication required.
    """
    logger.info(f"Request to delete course with ID: {course_id}")
    success = course_crud.delete_course(db, course_id)
    if not success:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )
    return None


@router.post("/bulk-delete", status_code=status.HTTP_200_OK)
async def bulk_delete_courses(
        course_ids: List[uuid.UUID] = Body(..., description="List of course IDs to delete"),
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Delete multiple courses at once

    Authentication required.

    Returns statistics about the deletion operation.
    """
    logger.info(f"Request to bulk delete {len(course_ids)} courses")

    results = {
        "total": len(course_ids),
        "deleted": 0,
        "failed": 0,
        "not_found": 0,
        "failed_ids": []
    }

    for course_id in course_ids:
        try:
            success = course_crud.delete_course(db, course_id)
            if success:
                results["deleted"] += 1
            else:
                results["not_found"] += 1
        except Exception as e:
            logger.error(f"Error deleting course {course_id}: {str(e)}")
            results["failed"] += 1
            results["failed_ids"].append(str(course_id))

    return results


@router.get("/tag/{tag}", response_model=List[CourseResponse])
async def search_by_tag(
        tag: str,
        skip: int = Query(0, ge=0, description="Number of records to skip"),
        limit: int = Query(100, ge=1, le=100, description="Maximum number of records to return"),
        db: Session = Depends(get_db)
):
    """
    Get courses that have a specific tag
    """
    logger.info(f"Request to search courses by tag: {tag}")
    return course_crud.search_courses_by_tag(db, tag, skip=skip, limit=limit)


@router.get("/author/{author_id}", response_model=List[CourseResponse])
async def get_courses_by_author(
        author_id: uuid.UUID,
        skip: int = Query(0, ge=0, description="Number of records to skip"),
        limit: int = Query(100, ge=1, le=100, description="Maximum number of records to return"),
        db: Session = Depends(get_db)
):
    """
    Get all courses created by a specific author
    """
    logger.info(f"Request to get courses by author: {author_id}")

    # Build search params
    from app.schemas.course import CourseSearchParams
    search_params = CourseSearchParams(
        author_id=author_id,
        sort_by="created_at",
        sort_order="desc"
    )

    # Get courses with filter
    courses, _ = course_crud.search_courses(db, search_params, skip=skip, limit=limit)
    return courses


@router.put("/{course_id}/languages/{language}", response_model=CourseResponse)
async def update_course_language(
        course_id: uuid.UUID,
        language: str = Path(..., min_length=2, description="Language code (e.g., 'en', 'ru')"),
        language_data: CourseLanguageUpdate = Body(..., description="Data for language version update"),
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Add or update a language version of a course

    Authentication required.

    This endpoint allows adding or updating course content in a specific language.
    """
    logger.info(f"Request to update language '{language}' for course ID: {course_id}")

    # Validate that provided language code matches the path parameter
    if language_data.language != language:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Language code in path ({language}) does not match language in request body ({language_data.language})"
        )

    # Get the course
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Update the language
    updated_course = course_crud.update_course_language(
        db,
        course_id,
        language,
        language_data.title,
        language_data.description
    )

    return updated_course


@router.delete("/{course_id}/languages/{language}", response_model=CourseResponse)
async def delete_course_language(
        course_id: uuid.UUID,
        language: str = Path(..., min_length=2, description="Language code (e.g., 'en', 'ru')"),
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Remove a language version from a course

    Authentication required.

    This endpoint allows removing course content in a specific language.
    """
    logger.info(f"Request to delete language '{language}' for course ID: {course_id}")

    # Get the course
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Check if language exists
    if language not in course.available_languages():
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Language '{language}' not found for course"
        )

    # Remove the language
    updated_course = course_crud.remove_course_language(db, course_id, language)

    return updated_course


@router.get("/{course_id}/languages", response_model=Dict[str, List[str]])
async def get_course_languages(
        course_id: uuid.UUID,
        db: Session = Depends(get_db)
):
    """
    Get all available languages for a course
    """
    logger.info(f"Request to get languages for course ID: {course_id}")

    # Get the course
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get available languages
    languages = course.available_languages()

    return {"languages": languages}


@router.get("/{id_or_slug}/tree", response_model=Dict[str, Any])
async def get_course_tree(
        id_or_slug: str,
        language: Optional[str] = Query(None, description="Language code for content"),
        db: Session = Depends(get_db)
):
    """
    Get the complete tree structure for a course

    This includes the technology tree, sections, lessons, and other structured content.
    """
    logger.info(f"Request to get course tree for ID or slug: {id_or_slug}")

    # First, get the course
    try:
        # Try as UUID
        course_id = uuid.UUID(id_or_slug)
    except ValueError:
        # Try as slug
        course = course_crud.get_by_slug(db, id_or_slug)
        if not course:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with slug '{id_or_slug}' not found"
            )
        course_id = course.id
    else:
        # We have a valid UUID, get the course
        course = course_crud.get_course(db, course_id)
        if not course:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID {course_id} not found"
            )

    # Get the course tree
    tree = course_crud.get_course_tree(db, course_id, language)

    return tree
