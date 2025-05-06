"""
API endpoints for courses
"""
import uuid
from datetime import datetime
from typing import Any, Dict, List, Optional, Union

from app.api.deps import get_db, verify_token, get_current_user_id
from app.crud.course import course_crud
from app.models.course import CourseVisibility
from app.schemas.course import (
    Course, CourseCreate, CourseList, CourseResponse, CourseUpdate,
    CourseLanguageUpdate
)
from fastapi import APIRouter, Body, Depends, HTTPException, Path, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger

# Set up logger
logger = get_logger("course_service.api.courses")

router = APIRouter()

# Список зарезервированных slug, которые нельзя использовать для курсов
RESERVED_SLUGS = [
    "docs", "redoc", "openapi.json", "tag", "tags", "tree", "article", "articles",
    "lesson", "lessons", "api", "admin", "auth", "login", "logout", "register",
    "user", "users", "settings", "profile", "dashboard", "search"
]


@router.get("/", response_model=Union[CourseList, CourseResponse])
async def get_courses(
        uuid: Optional[uuid.UUID] = Query(None, description="Get course by UUID"),
        slug: Optional[str] = Query(None, description="Get course by slug"),
        page: int = Query(0, ge=0, description="Page number (starting from 0)"),
        size: int = Query(31, ge=1, le=100, description="Page size"),
        search: Optional[str] = Query(None, description="Search query for filtering by title and description"),
        language: Optional[str] = Query(None, description="Language code for search (e.g., 'en', 'ru')"),
        sort_by: Optional[str] = Query("created_at", description="Field to sort by"),
        sort_order: Optional[str] = Query("desc", description="Sort order: asc or desc"),
        author_id: Optional[uuid.UUID] = Query(None, description="Filter by author ID"),
        tag_ids: Optional[List[str]] = Query(None, description="List of tag IDs to filter by"),
        from_date: Optional[datetime] = Query(None, description="Filter courses created after this date"),
        to_date: Optional[datetime] = Query(None, description="Filter courses created before this date"),
        db: AsyncSession = Depends(get_db)
):
    """
    Универсальный эндпоинт для работы с курсами

    ## Режимы работы:

    ### 1. Получение отдельного курса:
    - По UUID: `GET /?uuid=550e8400-e29b-41d4-a716-446655440000`
    - По slug: `GET /?slug=introduction-to-programming`

    При указании uuid или slug эндпоинт вернет подробную информацию об одном курсе
    в формате CourseResponse.

    ### 2. Получение списка курсов с фильтрацией и пагинацией:
    - Базовый запрос: `GET /`
    - С пагинацией: `GET /?page=0&size=10`
    - С поиском: `GET /?search=programming&language=en`
    - По автору: `GET /?author_id=550e8400-e29b-41d4-a716-446655440000`
    - По тегам: `GET /?tag_ids=tag1&tag_ids=tag2`

    При отсутствии параметров uuid и slug эндпоинт вернет список курсов
    с учетом фильтров в формате CourseList с информацией о пагинации.
    """
    logger.info(
        f"Request to get courses with params: uuid={uuid}, slug={slug}, page={page}, size={size}, search={search}")

    # Если указан UUID или slug, возвращаем один курс
    if uuid is not None:
        course = await course_crud.get_course(db, uuid)
        if course is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with UUID {uuid} not found"
            )
        return course  # Возвращаем один курс (CourseResponse)

    if slug is not None:
        course = await course_crud.get_by_slug(db, slug)
        if course is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with slug '{slug}' not found"
            )
        return course  # Возвращаем один курс (CourseResponse)

    # Если не указаны uuid и slug, возвращаем список курсов с фильтрацией
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
        to_date=to_date,
        # Добавляем параметр is_published, если он не указан, выводим все курсы
        is_published=None
    )

    # Get courses with pagination
    courses, total = await course_crud.search_courses(db, search_params, skip=page * size, limit=size)

    # Логируем результаты поиска
    logger.info(f"Found {total} courses matching search criteria")

    # Если курсы не найдены, возвращаем пустой список
    if total == 0:
        # Возвращаем пустой список с пагинацией
        return CourseList(
            items=[],
            total=0,
            page=page,
            size=size,
            pages=0
        )

    pages = (total + size - 1) // size  # Calculate total pages

    return CourseList(
        items=courses,
        total=total,
        page=page,
        size=size,
        pages=pages
    )


@router.get("/{id_or_slug}", response_model=CourseResponse)
async def get_course_flexible(
        id_or_slug: str,
        db: AsyncSession = Depends(get_db)
):
    """
    Get information about a course by its ID or slug

    This endpoint automatically detects whether you're providing a UUID or a slug
    and routes the request accordingly.
    """
    # Проверяем, не является ли slug зарезервированным словом
    if id_or_slug.lower() in RESERVED_SLUGS:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Not found. Reserved slug cannot be used as course identifier."
        )

    logger.info(f"Request to get course with ID or slug: {id_or_slug}")

    # Try to parse as UUID first
    try:
        course_id = uuid.UUID(id_or_slug)
        course = await course_crud.get_course(db, course_id)
        if course is None:
            logger.warning(f"Course with ID {course_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID {course_id} not found"
            )
        return course
    except ValueError:
        # Not a valid UUID, try as slug
        course = await course_crud.get_by_slug(db, id_or_slug)
        if course is None:
            logger.warning(f"Course with slug '{id_or_slug}' not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID or slug '{id_or_slug}' not found"
            )
        return course


@router.post("/", response_model=CourseResponse, status_code=status.HTTP_201_CREATED)
async def create_course(
        course_data: Dict[str, Any] = Body(...),
        db: AsyncSession = Depends(get_db),
        user_id: uuid.UUID = Depends(get_current_user_id)
):
    """
    Create a new course

    Authentication required. Author ID is automatically extracted from the authentication token.
    """
    logger.info(f"Request to create a new course by user ID: {user_id}")
    logger.info(f"Request body: {course_data}")

    # Добавляем ID автора из токена
    course_data["author_id"] = str(user_id)

    try:
        # Создаем объект CourseCreate
        course = CourseCreate(**course_data)

        # Создаем курс
        return await course_crud.create(db, obj_in=course)
    except Exception as e:
        logger.error(f"Error creating course: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Error creating course: {str(e)}"
        )


@router.put("/{course_id}", response_model=CourseResponse)
async def update_course(
        course_id: uuid.UUID,
        course_update: Dict[str, Any] = Body(...),
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Update an existing course

    Authentication required.

    This is a universal endpoint for updating any part of a course, including:
    - Basic course information (title, description)
    - Course metadata
    - Publication status
    - Visibility settings

    Use the appropriate fields in your request body to update specific parts of the course.
    """
    logger.info(f"Request to update course with ID: {course_id}")
    logger.info(f"Update data: {course_update}")

    # Get the existing course
    course = await course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Special handling for visibility settings
    if "visibility" in course_update:
        visibility = course_update.get("visibility")

        # Validate organization_id when visibility is ORGANIZATION
        if visibility == CourseVisibility.ORGANIZATION and "organization_id" not in course_update:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="organization_id is required when visibility is set to ORGANIZATION"
            )

        # Remove organization_id when switching to other visibility levels
        elif visibility != CourseVisibility.ORGANIZATION and course.organization_id is not None:
            course_update["organization_id"] = None

    try:
        # Create CourseUpdate object if needed, or directly use the dictionary
        if all(key in CourseUpdate.__annotations__ for key in course_update.keys()):
            update_obj = CourseUpdate(**course_update)
            updated_course = await course_crud.update_course(db, course_id, update_obj)
        else:
            # For metadata or partial updates
            updated_course = await course_crud.update_metadata(db, course_id, course_update)

        if updated_course is None:
            logger.warning(f"Course with ID {course_id} not found during update")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID {course_id} not found"
            )

        return updated_course
    except Exception as e:
        logger.error(f"Error updating course: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Error updating course: {str(e)}"
        )


@router.delete("/{course_id}", status_code=status.HTTP_200_OK)
async def delete_course(
        course_id: uuid.UUID,
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Delete a course

    Authentication required.

    Returns:
        JSON with operation status and details
    """
    logger.info(f"Request to delete course with ID: {course_id}")
    success = await course_crud.delete_course(db, course_id)
    if not success:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )
    return {
        "status": "success",
        "message": f"Course with ID {course_id} successfully deleted",
        "deleted_id": str(course_id)
    }


@router.get("/tag/{tag}", response_model=List[CourseResponse])
async def search_by_tag(
        tag: str,
        skip: int = Query(0, ge=0, description="Number of records to skip"),
        limit: int = Query(100, ge=1, le=100, description="Maximum number of records to return"),
        db: AsyncSession = Depends(get_db)
):
    """
    Get courses that have a specific tag
    """
    logger.info(f"Request to search courses by tag: {tag}")
    return await course_crud.search_courses_by_tag(db, tag, skip=skip, limit=limit)


@router.get("/author/{author_id}", response_model=List[CourseResponse])
async def get_courses_by_author(
        author_id: uuid.UUID,
        skip: int = Query(0, ge=0, description="Number of records to skip"),
        limit: int = Query(100, ge=1, le=100, description="Maximum number of records to return"),
        db: AsyncSession = Depends(get_db)
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
        sort_order="desc",
        # Добавляем параметр is_published=None, чтобы получить все курсы автора
        is_published=None
    )

    # Get courses with filter
    courses, _ = await course_crud.search_courses(db, search_params, skip=skip, limit=limit)
    return courses


@router.put("/{course_id}/languages/{language}", response_model=CourseResponse)
async def update_course_language(
        course_id: uuid.UUID,
        language: str = Path(..., min_length=2, description="Language code (e.g., 'en', 'ru')"),
        language_data: CourseLanguageUpdate = Body(..., description="Data for language version update"),
        db: AsyncSession = Depends(get_db),
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
    course = await course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Update the language
    updated_course = await course_crud.update_course_language(
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
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Remove a language version from a course

    Authentication required.

    This endpoint allows removing course content in a specific language.
    """
    logger.info(f"Request to delete language '{language}' for course ID: {course_id}")

    # Get the course
    course = await course_crud.get_course(db, course_id)
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
    updated_course = await course_crud.remove_course_language(db, course_id, language)

    return updated_course


@router.get("/{course_id}/languages", response_model=Dict[str, List[str]])
async def get_course_languages(
        course_id: uuid.UUID,
        db: AsyncSession = Depends(get_db)
):
    """
    Get all available languages for a course
    """
    logger.info(f"Request to get languages for course ID: {course_id}")

    # Get the course
    course = await course_crud.get_course(db, course_id)
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
        db: AsyncSession = Depends(get_db)
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
        course = await course_crud.get_by_slug(db, id_or_slug)
        if not course:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with slug '{id_or_slug}' not found"
            )
        course_id = course.id
    else:
        # We have a valid UUID, get the course
        course = await course_crud.get_course(db, course_id)
        if not course:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Course with ID {course_id} not found"
            )

    # Get the course tree
    tree = await course_crud.get_course_tree(db, course_id, language)

    return tree
