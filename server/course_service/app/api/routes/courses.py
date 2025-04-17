"""
Course-related API endpoints
"""
from typing import List, Optional
import uuid
from fastapi import APIRouter, HTTPException, status, Depends, Query, Header
from sqlalchemy.orm import Session

from app.db.session import get_db
from app.models.course import Course
from app.schemas.course import CourseCreate, CourseUpdate, CourseResponse
from app.crud import course as course_crud
from common.logger import initialize_logging

# Добавляем функцию проверки токена авторизации (аналогично auth_service)
async def verify_token(authorization: Optional[str] = Header(None)):
    """
    Verify authorization token

    Проверка токена авторизации для защищенных эндпоинтов
    """
    if not authorization:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Здесь обычно должна быть проверка токена через сервис авторизации
    # Для упрощения просто проверяем наличие Bearer токена
    if not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authentication credentials",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # В реальном приложении здесь должен быть код проверки токена
    # и получения информации о пользователе
    return True

# Initialize logger
logger = initialize_logging("course_service.api.courses")

router = APIRouter()

@router.get("/", response_model=List[CourseResponse])
async def get_courses(
    skip: int = Query(0, ge=0, description="Number of records to skip"),
    limit: int = Query(100, ge=1, le=100, description="Maximum number of records to return"),
    order_by: str = Query("created_at", description="Field to sort by"),
    desc: bool = Query(True, description="Sort descending"),
    db: Session = Depends(get_db)
):
    """
    Get all courses with pagination and sorting
    """
    logger.info(f"Request to get courses with params: skip={skip}, limit={limit}, order_by={order_by}, desc={desc}")
    courses = course_crud.get_courses(db, skip=skip, limit=limit, order_by=order_by, order_desc=desc)
    return courses

@router.get("/{course_id}", response_model=CourseResponse)
async def get_course(course_id: uuid.UUID, db: Session = Depends(get_db)):
    """
    Get a specific course by ID
    """
    logger.info(f"Request to get course with ID: {course_id}")
    course = course_crud.get_course(db, course_id)
    if course is None:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
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

    Требуется аутентификация.
    """
    logger.info(f"Request to create a new course: {course.dict()}")
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

    Требуется аутентификация.
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

@router.delete("/{course_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_course(
    course_id: uuid.UUID,
    db: Session = Depends(get_db),
    _: bool = Depends(verify_token)
):
    """
    Delete a course

    Требуется аутентификация.
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
