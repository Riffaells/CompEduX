"""
Course-related API endpoints
"""
import uuid
from datetime import datetime
from typing import List, Optional, Dict, Any

from app.core.config import settings
from app.crud import course as course_crud
from app.db.session import get_db
from ..models.course import Course
from ..schemas.course import (
    CourseCreate, CourseUpdate, CourseResponse, CourseList,
    CourseLanguageUpdate, CourseSearchParams, CourseVisibilityUpdate,
    # TODO: Реализовать в будущем
    # CourseRatingUpdate, CourseUsageUpdate
    ArticleCreate, ArticleUpdate, ArticleResponse, ArticleList
)
from fastapi import APIRouter, HTTPException, status, Depends, Query, Header, Body, Path
from sqlalchemy.orm import Session

from common.logger import initialize_logging

# Initialize logger
logger = initialize_logging("course_service.api.courses")

router = APIRouter()


# Add authorization token verification function (similar to auth_service)
async def verify_token(authorization: Optional[str] = Header(None)):
    """
    Verify authorization token

    Used for protected endpoints that require authentication.
    """
    if not authorization:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # In a real application, this would validate the token through the auth service
    # For simplicity, we're just checking for the presence of a Bearer token
    if not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authentication credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # In a real application, this would include token validation code
    # and retrieval of user information
    return True


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
    Get a list of courses with pagination, search and sorting.

    Can also be used to get a single course by specifying id or slug query parameter.

    Parameters:
    - **page**: Page number (starting from 0)
    - **size**: Number of items per page
    - **search**: Search string to filter by title and description
    - **language**: Language code for search
    - **sort_by**: Field to sort by (created_at, title, etc.)
    - **sort_order**: Sort order (asc or desc)
    - **author_id**: Author ID for filtering
    - **tag_ids**: List of tag IDs for filtering
    - **from_date**: Filter courses created after this date
    - **to_date**: Filter courses created before this date
    - **id**: Get specific course by ID
    - **slug**: Get specific course by slug
    """
    # If ID or slug is provided, return a single course instead of a list
    if id is not None:
        logger.info(f"Request to get course with ID via query parameter: {id}")
        course = course_crud.get_course(db, id)
        if course is None:
            logger.warning(f"Course with ID {id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID {id} not found"
            )
        return CourseList(
            items=[course],
            total=1,
            page=0,
            size=1,
            pages=1
        )

    if slug is not None:
        logger.info(f"Request to get course with slug via query parameter: {slug}")
        course = course_crud.get_by_slug(db, slug)
        if course is None:
            logger.warning(f"Course with slug '{slug}' not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with slug '{slug}' not found"
            )
        return CourseList(
            items=[course],
            total=1,
            page=0,
            size=1,
            pages=1
        )

    # Standard list operation
    logger.info(
        f"Request to get courses with params: page={page}, size={size}, "
        f"search={search}, language={language}, sort_by={sort_by}, "
        f"sort_order={sort_order}, author_id={author_id}, tag_ids={tag_ids}, "
        f"from_date={from_date}, to_date={to_date}"
    )

    # Create search parameters object
    params = CourseSearchParams(
        search=search,
        language=language,
        tags=tag_ids,
        author_id=author_id,
        sort_by=sort_by,
        sort_order=sort_order,
        from_date=from_date,
        to_date=to_date
    )

    # Get courses with applied parameters
    courses, total = course_crud.search_courses(
        db,
        params=params,
        skip=page * size,
        limit=size
    )

    # Calculate total number of pages
    total_pages = (total + size - 1) // size

    # Form response in CourseList format
    response = CourseList(
        items=courses,
        total=total,
        page=page,
        size=size,
        pages=total_pages
    )

    return response


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
    logger.info(f"Request to create a new course: {course.model_dump()}")
    return course_crud.create_course(db, course)


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
    Update course metadata (partial update)

    Authentication required.

    This endpoint allows updating specific course fields without
    providing the entire course object.
    """
    logger.info(f"Request to update metadata for course ID: {course_id}")

    # Get the course
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Update metadata
    updated_course = course_crud.update_metadata(db, course_id, metadata)
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

    Allows setting detailed visibility options for a course:
    - PUBLIC: Available to everyone
    - PRIVATE: Only available to the author
    - FRIENDS: Available to friends of the author
    - LINK: Available only via direct link
    - PAID: Available after payment
    - ORGANIZATION: Available to members of specific organization
    - ENROLLED: Available only to enrolled users

    When setting visibility to ORGANIZATION, the organization_id field is required.
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

    # Prepare metadata update
    metadata = {
        "visibility": visibility_data.visibility
    }

    # Add organization_id if provided
    if visibility_data.organization_id:
        metadata["organization_id"] = visibility_data.organization_id

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

    Returns a summary of deleted and not found courses.
    """
    logger.info(f"Request to bulk delete {len(course_ids)} courses")

    deleted_ids = []
    not_found_ids = []

    for course_id in course_ids:
        success = course_crud.delete_course(db, course_id)
        if success:
            deleted_ids.append(str(course_id))
        else:
            not_found_ids.append(str(course_id))

    return {
        "deleted_count": len(deleted_ids),
        "deleted_ids": deleted_ids,
        "not_found_count": len(not_found_ids),
        "not_found_ids": not_found_ids
    }


@router.get("/tag/{tag}", response_model=List[CourseResponse])
async def search_by_tag(
        tag: str,
        skip: int = Query(0, ge=0, description="Number of records to skip"),
        limit: int = Query(100, ge=1, le=100, description="Maximum number of records to return"),
        db: Session = Depends(get_db)
):
    """
    Search courses by tag
    """
    logger.info(f"Request to search courses by tag: {tag}")
    courses = course_crud.search_courses_by_tag(db, tag, skip=skip, limit=limit)
    return courses


@router.get("/author/{author_id}", response_model=List[CourseResponse])
async def get_courses_by_author(
        author_id: uuid.UUID,
        skip: int = Query(0, ge=0, description="Number of records to skip"),
        limit: int = Query(100, ge=1, le=100, description="Maximum number of records to return"),
        db: Session = Depends(get_db)
):
    """
    Get all courses by a specific author
    """
    logger.info(f"Request to get courses by author: {author_id}")

    # Create search parameters with author filter
    params = CourseSearchParams(
        author_id=author_id,
        sort_by="created_at",
        sort_order="desc"
    )

    # Get courses with author filter applied
    courses, _ = course_crud.search_courses(
        db,
        params=params,
        skip=skip,
        limit=limit
    )

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
    """
    logger.info(f"Request to update language '{language}' for course ID: {course_id}")

    # Check that the language code in the path matches the one in the data
    if language != language_data.language:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Language code in path doesn't match the one in request body"
        )

    # Get the course
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Add or update the language version
    updated_course = course_crud.update_course_language(
        db,
        course_id,
        language_data.language,
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
    Delete a language version of a course

    Authentication required.
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

    # Delete the language version
    updated_course = course_crud.remove_course_language(db, course_id, language)

    # Check that at least one language remains
    if not updated_course.title:
        logger.warning(f"Cannot remove the last language version for course ID: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Cannot remove the last language version. A course must have at least one language."
        )

    return updated_course


@router.get("/{course_id}/languages", response_model=Dict[str, List[str]])
async def get_course_languages(
        course_id: uuid.UUID,
        db: Session = Depends(get_db)
):
    """
    Get the list of available languages for a course
    """
    logger.info(f"Request to get available languages for course ID: {course_id}")

    # Get the course
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get language lists for title and description
    title_languages = list(course.title.keys()) if isinstance(course.title, dict) else []
    description_languages = list(course.description.keys()) if isinstance(course.description, dict) else []

    # Form the response
    languages = {
        "available_languages": sorted(list(set(title_languages + description_languages))),
        "title_languages": sorted(title_languages),
        "description_languages": sorted(description_languages)
    }

    return languages


@router.get("/{id_or_slug}/tree", response_model=Dict[str, Any])
async def get_course_tree(
        id_or_slug: str,
        language: Optional[str] = Query(None, description="Language code for content"),
        db: Session = Depends(get_db)
):
    """
    Get the complete tree structure of a course

    This endpoint returns the detailed tree structure of the course,
    including all modules, sections, and lessons.

    Parameters:
    - **id_or_slug**: Course ID or slug
    - **language**: Language code for localized content (optional)
    """
    logger.info(f"Request to get course tree for ID or slug: {id_or_slug} with language: {language}")

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
    except ValueError:
        # Not a valid UUID, try as slug
        course = course_crud.get_by_slug(db, id_or_slug)
        if course is None:
            logger.warning(f"Course with slug '{id_or_slug}' not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID or slug '{id_or_slug}' not found"
            )

    # Get the course tree structure
    # This would be a separate CRUD function that fetches the full tree structure
    # For now, we'll assume this function exists and returns the tree
    tree = course_crud.get_course_tree(db, course.id, language)

    return tree


# TODO: Реализовать в будущем
# @router.post("/{course_id}/rating", response_model=CourseResponse)
# async def rate_course(
#         course_id: uuid.UUID,
#         rating_data: CourseRatingUpdate,
#         db: Session = Depends(get_db),
#         _: bool = Depends(verify_token)
# ):
#     """
#     Rate a course
#
#     This endpoint allows users to submit ratings for courses.
#     The rating is a value between 0 and 5.
#
#     Authentication required.
#     """
#     logger.info(f"Request to rate course {course_id} with rating {rating_data.rating}")
#
#     # Get the course
#     course = course_crud.get_course(db, course_id)
#     if not course:
#         logger.warning(f"Course with ID {course_id} not found")
#         raise HTTPException(
#             status_code=status.HTTP_404_NOT_FOUND,
#             detail=f"Course with ID {course_id} not found"
#         )
#
#     # Update the rating
#     updated_course = course_crud.add_course_rating(
#         db,
#         course_id,
#         rating_data.user_id,
#         rating_data.rating
#     )
#
#     return updated_course


# TODO: Реализовать в будущем
# @router.put("/{course_id}/usage", response_model=CourseResponse)
# async def update_course_usage(
#         course_id: uuid.UUID,
#         usage_data: CourseUsageUpdate,
#         db: Session = Depends(get_db),
#         _: bool = Depends(verify_token)
# ):
#     """
#     Update course usage metrics
#
#     This endpoint allows updating usage metrics for a course,
#     such as the number of groups using it, number of students,
#     or course completions.
#
#     Authentication required.
#     """
#     logger.info(f"Request to update usage metrics for course {course_id}")
#
#     # Get the course
#     course = course_crud.get_course(db, course_id)
#     if not course:
#         logger.warning(f"Course with ID {course_id} not found")
#         raise HTTPException(
#             status_code=status.HTTP_404_NOT_FOUND,
#             detail=f"Course with ID {course_id} not found"
#         )
#
#     # Prepare the usage update
#     usage_update = {}
#     if usage_data.groups is not None:
#         usage_update["groups"] = usage_data.groups
#     if usage_data.students is not None:
#         usage_update["students"] = usage_data.students
#     if usage_data.completions is not None:
#         usage_update["completions"] = usage_data.completions
#
#     # Update the usage metrics
#     updated_course = course_crud.update_course_usage(db, course_id, usage_update)
#
#     return updated_course


# TODO: Реализовать в будущем
# @router.get("/{course_id}/ratings", response_model=Dict[str, Any])
# async def get_course_ratings(
#         course_id: uuid.UUID,
#         db: Session = Depends(get_db)
# ):
#     """
#     Get detailed rating information for a course
#
#     Returns information about the course ratings, including
#     average rating, number of ratings, and distribution of ratings.
#     """
#     logger.info(f"Request to get rating information for course {course_id}")
#
#     # Get the course
#     course = course_crud.get_course(db, course_id)
#     if not course:
#         logger.warning(f"Course with ID {course_id} not found")
#         raise HTTPException(
#             status_code=status.HTTP_404_NOT_FOUND,
#             detail=f"Course with ID {course_id} not found"
#         )
#
#     # Get the rating details
#     rating_details = course_crud.get_course_rating_details(db, course_id)
#
#     return rating_details


@router.get("/{id_or_slug}/articles", response_model=ArticleList)
async def get_course_articles(
        id_or_slug: str,
        language: Optional[str] = Query(None, description="Filter by language (e.g. 'en', 'ru')"),
        page: int = Query(0, ge=0, description="Page number (starting from 0)"),
        size: int = Query(30, ge=1, le=100, description="Page size"),
        db: Session = Depends(get_db)
):
    """
    Get articles for a course with pagination

    Parameters:
    - **id_or_slug**: Course ID or slug
    - **language**: Optional language code to filter articles
    - **page**: Page number (starting from 0)
    - **size**: Number of items per page
    """
    logger.info(f"Request to get articles for course {id_or_slug} with language={language}, page={page}, size={size}")

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
    except ValueError:
        # Not a valid UUID, try as slug
        course = course_crud.get_by_slug(db, id_or_slug)
        if course is None:
            logger.warning(f"Course with slug '{id_or_slug}' not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID or slug '{id_or_slug}' not found"
            )

    # Get articles with pagination
    articles, total = course_crud.get_course_articles(
        db,
        course.id,
        language=language,
        skip=page * size,
        limit=size
    )

    # Calculate total pages
    total_pages = (total + size - 1) // size if total > 0 else 0

    return ArticleList(
        items=articles,
        total=total,
        page=page,
        size=size,
        pages=total_pages
    )


@router.get("/{id_or_slug}/articles/{article_slug}", response_model=ArticleResponse)
async def get_course_article(
        id_or_slug: str,
        article_slug: str,
        language: str = Query(..., min_length=2, max_length=5, description="Language code (e.g. 'en', 'ru')"),
        db: Session = Depends(get_db)
):
    """
    Get a specific article from a course

    Parameters:
    - **id_or_slug**: Course ID or slug
    - **article_slug**: Article slug
    - **language**: Language code of the article
    """
    logger.info(f"Request to get article {article_slug} in language {language} for course {id_or_slug}")

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
    except ValueError:
        # Not a valid UUID, try as slug
        course = course_crud.get_by_slug(db, id_or_slug)
        if course is None:
            logger.warning(f"Course with slug '{id_or_slug}' not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID or slug '{id_or_slug}' not found"
            )

    # Get article
    article = course_crud.get_article(db, course.id, article_slug, language)
    if not article:
        logger.warning(f"Article {article_slug} in language {language} not found for course {course.id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Article with slug '{article_slug}' in language '{language}' not found for this course"
        )

    return article


@router.post("/{id_or_slug}/articles", response_model=ArticleResponse, status_code=status.HTTP_201_CREATED)
async def create_course_article(
        id_or_slug: str,
        article: ArticleCreate,
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Create a new article for a course

    Authentication required.

    Parameters:
    - **id_or_slug**: Course ID or slug
    - **article**: Article data
    """
    logger.info(f"Request to create a new article for course {id_or_slug}")

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
    except ValueError:
        # Not a valid UUID, try as slug
        course = course_crud.get_by_slug(db, id_or_slug)
        if course is None:
            logger.warning(f"Course with slug '{id_or_slug}' not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID or slug '{id_or_slug}' not found"
            )

    # Update the course_id in the article data to ensure it matches the course
    article_data = article.dict()
    article_data["course_id"] = course.id

    # Check if article with this slug and language already exists
    existing_article = course_crud.get_article(db, course.id, article.slug, article.language)
    if existing_article:
        logger.warning(f"Article with slug {article.slug} and language {article.language} already exists for course {course.id}")
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail=f"Article with slug '{article.slug}' and language '{article.language}' already exists for this course"
        )

    # Create article
    new_article = course_crud.create_article(db, article_data)

    return new_article


@router.put("/{id_or_slug}/articles/{article_slug}/{language}", response_model=ArticleResponse)
async def update_course_article(
        id_or_slug: str,
        article_slug: str,
        language: str,
        article_update: ArticleUpdate,
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Update an existing article for a course

    Authentication required.

    Parameters:
    - **id_or_slug**: Course ID or slug
    - **article_slug**: Article slug
    - **language**: Language code of the article
    - **article_update**: Updated article data
    """
    logger.info(f"Request to update article {article_slug} in language {language} for course {id_or_slug}")

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
    except ValueError:
        # Not a valid UUID, try as slug
        course = course_crud.get_by_slug(db, id_or_slug)
        if course is None:
            logger.warning(f"Course with slug '{id_or_slug}' not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID or slug '{id_or_slug}' not found"
            )

    # Update article
    updated_article = course_crud.update_article(
        db,
        course.id,
        article_slug,
        language,
        article_update.dict(exclude_unset=True)
    )

    if not updated_article:
        logger.warning(f"Article {article_slug} in language {language} not found for course {course.id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Article with slug '{article_slug}' in language '{language}' not found for this course"
        )

    return updated_article


@router.delete("/{id_or_slug}/articles/{article_slug}/{language}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_course_article(
        id_or_slug: str,
        article_slug: str,
        language: str,
        db: Session = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Delete an article from a course

    Authentication required.

    Parameters:
    - **id_or_slug**: Course ID or slug
    - **article_slug**: Article slug
    - **language**: Language code of the article
    """
    logger.info(f"Request to delete article {article_slug} in language {language} from course {id_or_slug}")

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
    except ValueError:
        # Not a valid UUID, try as slug
        course = course_crud.get_by_slug(db, id_or_slug)
        if course is None:
            logger.warning(f"Course with slug '{id_or_slug}' not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID or slug '{id_or_slug}' not found"
            )

    # Delete article
    success = course_crud.delete_article(db, course.id, article_slug, language)

    if not success:
        logger.warning(f"Article {article_slug} in language {language} not found for course {course.id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Article with slug '{article_slug}' in language '{language}' not found for this course"
        )

    return None
