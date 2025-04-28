"""
Схемы данных для работы с курсами
"""
from datetime import datetime
from typing import List, Optional
from uuid import UUID

from pydantic import BaseModel, Field


class CourseBase(BaseModel):
    """Базовая модель курса"""
    title: str = Field(..., description="Название курса")
    description: str = Field(..., description="Описание курса")
    subject: str = Field(..., description="Предмет курса")
    level: str = Field(..., description="Уровень сложности курса")


class CourseCreate(CourseBase):
    """Модель для создания курса"""
    teacher_id: UUID = Field(..., description="ID преподавателя")


class CourseUpdate(BaseModel):
    """Модель для обновления курса"""
    title: Optional[str] = Field(None, description="Название курса")
    description: Optional[str] = Field(None, description="Описание курса")
    subject: Optional[str] = Field(None, description="Предмет курса")
    level: Optional[str] = Field(None, description="Уровень сложности курса")
    is_active: Optional[bool] = Field(None, description="Статус активности курса")


class CourseResponse(CourseBase):
    """Модель ответа с данными курса"""
    id: UUID = Field(..., description="Уникальный идентификатор курса")
    teacher_id: UUID = Field(..., description="ID преподавателя курса")
    is_active: bool = Field(..., description="Статус активности курса")
    created_at: datetime = Field(..., description="Дата создания курса")
    updated_at: Optional[datetime] = Field(None, description="Дата последнего обновления курса")

    class Config:
        orm_mode = True


class CourseListResponse(BaseModel):
    """Модель ответа со списком курсов"""
    total: int = Field(..., description="Общее количество курсов")
    courses: List[CourseResponse] = Field(..., description="Список курсов")
