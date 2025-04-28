"""
Сервис для взаимодействия с API курсов
"""
from typing import Dict, Optional, Any
from uuid import UUID

import httpx
from app.core.config import settings
from app.schemas.course import CourseResponse, CourseListResponse
from fastapi import HTTPException, status


async def create_course(course_data: Dict[str, Any], auth_token: str) -> CourseResponse:
    """
    Создает новый курс через API курсов

    Args:
        course_data: Данные курса
        auth_token: Токен авторизации

    Returns:
        CourseResponse: Данные созданного курса

    Raises:
        HTTPException: Если произошла ошибка при создании курса
    """
    headers = {"Authorization": f"Bearer {auth_token}"}

    async with httpx.AsyncClient() as client:
        try:
            response = await client.post(
                f"{settings.COURSE_SERVICE_URL}/api/courses",
                json=course_data,
                headers=headers,
                timeout=10.0
            )

            if response.status_code >= 400:
                error_detail = response.json().get("detail", str(response.text))
                raise HTTPException(
                    status_code=response.status_code,
                    detail=f"Ошибка создания курса: {error_detail}"
                )

            return CourseResponse(**response.json())

        except httpx.RequestError as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"Ошибка связи с сервисом курсов: {str(exc)}"
            )


async def get_course(course_id: UUID, auth_token: Optional[str] = None) -> CourseResponse:
    """
    Получает информацию о курсе по ID через API курсов

    Args:
        course_id: ID курса
        auth_token: Токен авторизации (опционально)

    Returns:
        CourseResponse: Данные курса

    Raises:
        HTTPException: Если курс не найден или произошла ошибка
    """
    headers = {}
    if auth_token:
        headers["Authorization"] = f"Bearer {auth_token}"

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.COURSE_SERVICE_URL}/api/courses/{course_id}",
                headers=headers,
                timeout=10.0
            )

            if response.status_code == status.HTTP_404_NOT_FOUND:
                raise HTTPException(
                    status_code=status.HTTP_404_NOT_FOUND,
                    detail="Курс не найден"
                )

            if response.status_code >= 400:
                error_detail = response.json().get("detail", str(response.text))
                raise HTTPException(
                    status_code=response.status_code,
                    detail=f"Ошибка получения информации о курсе: {error_detail}"
                )

            return CourseResponse(**response.json())

        except httpx.RequestError as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"Ошибка связи с сервисом курсов: {str(exc)}"
            )


async def update_course(course_id: UUID, course_data: Dict[str, Any], auth_token: str) -> CourseResponse:
    """
    Обновляет существующий курс через API курсов

    Args:
        course_id: ID курса для обновления
        course_data: Данные для обновления
        auth_token: Токен авторизации

    Returns:
        CourseResponse: Обновленные данные курса

    Raises:
        HTTPException: Если произошла ошибка при обновлении курса
    """
    headers = {"Authorization": f"Bearer {auth_token}"}

    async with httpx.AsyncClient() as client:
        try:
            response = await client.patch(
                f"{settings.COURSE_SERVICE_URL}/api/courses/{course_id}",
                json=course_data,
                headers=headers,
                timeout=10.0
            )

            if response.status_code == status.HTTP_404_NOT_FOUND:
                raise HTTPException(
                    status_code=status.HTTP_404_NOT_FOUND,
                    detail="Курс не найден"
                )

            if response.status_code >= 400:
                error_detail = response.json().get("detail", str(response.text))
                raise HTTPException(
                    status_code=response.status_code,
                    detail=f"Ошибка обновления курса: {error_detail}"
                )

            return CourseResponse(**response.json())

        except httpx.RequestError as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"Ошибка связи с сервисом курсов: {str(exc)}"
            )


async def delete_course(course_id: UUID, auth_token: str) -> Dict[str, Any]:
    """
    Удаляет курс по ID через API курсов

    Args:
        course_id: ID курса для удаления
        auth_token: Токен авторизации

    Returns:
        Dict[str, Any]: Сообщение об успешном удалении

    Raises:
        HTTPException: Если курс не найден или произошла ошибка
    """
    headers = {"Authorization": f"Bearer {auth_token}"}

    async with httpx.AsyncClient() as client:
        try:
            response = await client.delete(
                f"{settings.COURSE_SERVICE_URL}/api/courses/{course_id}",
                headers=headers,
                timeout=10.0
            )

            if response.status_code == status.HTTP_404_NOT_FOUND:
                raise HTTPException(
                    status_code=status.HTTP_404_NOT_FOUND,
                    detail="Курс не найден"
                )

            if response.status_code >= 400:
                error_detail = response.json().get("detail", str(response.text))
                raise HTTPException(
                    status_code=response.status_code,
                    detail=f"Ошибка удаления курса: {error_detail}"
                )

            return response.json()

        except httpx.RequestError as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"Ошибка связи с сервисом курсов: {str(exc)}"
            )


async def get_courses(
        skip: int = 0,
        limit: int = 10,
        subject: Optional[str] = None,
        level: Optional[str] = None,
        search: Optional[str] = None,
        auth_token: Optional[str] = None
) -> CourseListResponse:
    """
    Получает список курсов с возможностью фильтрации и пагинации

    Args:
        skip: Количество пропускаемых курсов (для пагинации)
        limit: Максимальное количество возвращаемых курсов
        subject: Фильтр по предмету
        level: Фильтр по уровню сложности
        search: Строка поиска по названию и описанию
        auth_token: Токен авторизации (опционально)

    Returns:
        CourseListResponse: Список курсов и общее количество

    Raises:
        HTTPException: Если произошла ошибка при получении списка курсов
    """
    headers = {}
    if auth_token:
        headers["Authorization"] = f"Bearer {auth_token}"

    params = {
        "skip": skip,
        "limit": limit
    }

    if subject:
        params["subject"] = subject
    if level:
        params["level"] = level
    if search:
        params["search"] = search

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.COURSE_SERVICE_URL}/api/courses",
                params=params,
                headers=headers,
                timeout=10.0
            )

            if response.status_code >= 400:
                error_detail = response.json().get("detail", str(response.text))
                raise HTTPException(
                    status_code=response.status_code,
                    detail=f"Ошибка получения списка курсов: {error_detail}"
                )

            return CourseListResponse(**response.json())

        except httpx.RequestError as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"Ошибка связи с сервисом курсов: {str(exc)}"
            )


async def get_teacher_courses(
        teacher_id: UUID,
        auth_token: str,
        skip: int = 0,
        limit: int = 10
) -> CourseListResponse:
    """
    Получает список курсов конкретного преподавателя

    Args:
        teacher_id: ID преподавателя
        auth_token: Токен авторизации
        skip: Количество пропускаемых курсов (для пагинации)
        limit: Максимальное количество возвращаемых курсов

    Returns:
        CourseListResponse: Список курсов и общее количество

    Raises:
        HTTPException: Если произошла ошибка при получении списка курсов
    """
    headers = {"Authorization": f"Bearer {auth_token}"}
    params = {
        "skip": skip,
        "limit": limit
    }

    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{settings.COURSE_SERVICE_URL}/api/teachers/{teacher_id}/courses",
                params=params,
                headers=headers,
                timeout=10.0
            )

            if response.status_code >= 400:
                error_detail = response.json().get("detail", str(response.text))
                raise HTTPException(
                    status_code=response.status_code,
                    detail=f"Ошибка получения курсов преподавателя: {error_detail}"
                )

            return CourseListResponse(**response.json())

        except httpx.RequestError as exc:
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail=f"Ошибка связи с сервисом курсов: {str(exc)}"
            )
