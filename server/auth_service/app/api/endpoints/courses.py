"""
API эндпоинты для работы с курсами
"""
from typing import Optional
from uuid import UUID

from app.api.dependencies.auth import get_current_user
from app.schemas.course import CourseCreate, CourseResponse
from app.schemas.user import UserResponse
from app.services.course_service import create_course, get_course
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer

router = APIRouter(
    prefix="/courses",
    tags=["courses"],
    responses={404: {"description": "Курс не найден"}},
)

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="auth/token")


@router.post("", response_model=CourseResponse, status_code=status.HTTP_201_CREATED)
async def create_course_endpoint(
        course_data: CourseCreate,
        current_user: UserResponse = Depends(get_current_user)
):
    """
    Создать новый курс

    - **title**: Название курса
    - **description**: Описание курса
    - **price**: Цена курса (опционально)
    - **tags**: Список тегов курса (опционально)
    """
    # Получаем токен авторизации из текущего пользователя
    auth_token = current_user.auth_token

    try:
        new_course = await create_course(course_data.dict(), auth_token)
        return new_course
    except HTTPException as exc:
        raise exc
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Не удалось создать курс: {str(e)}"
        )


@router.get("/{course_id}", response_model=CourseResponse)
async def get_course_endpoint(
        course_id: UUID,
        current_user: Optional[UserResponse] = Depends(get_current_user)
):
    """
    Получить информацию о курсе по ID

    - **course_id**: Уникальный идентификатор курса
    """
    # Если пользователь авторизован, передаем его токен
    auth_token = current_user.auth_token if current_user else None

    try:
        course = await get_course(course_id, auth_token)
        return course
    except HTTPException as exc:
        raise exc
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Не удалось получить информацию о курсе: {str(e)}"
        )
