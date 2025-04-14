from typing import List, Dict, Any, Optional
from pydantic import BaseModel, Field

class PlatformStatSchema(BaseModel):
    """Схема для статистики по платформам"""
    platform: str = Field(..., description="Название платформы")
    user_count: int = Field(..., description="Количество уникальных пользователей")
    request_count: int = Field(..., description="Общее количество запросов")

class OSStatSchema(BaseModel):
    """Схема для статистики по операционным системам"""
    os: str = Field(..., description="Операционная система и версия")
    user_count: int = Field(..., description="Количество уникальных пользователей")
    request_count: int = Field(..., description="Общее количество запросов")

class AppVersionStatSchema(BaseModel):
    """Схема для статистики по версиям приложения"""
    app_version: str = Field(..., description="Версия приложения")
    client_version: str = Field(..., description="Версия клиента")
    user_count: int = Field(..., description="Количество уникальных пользователей")
    request_count: int = Field(..., description="Общее количество запросов")

class ClientStatsResponse(BaseModel):
    """Комбинированная схема для полной статистики"""
    platforms: List[PlatformStatSchema] = Field(
        default_factory=list,
        description="Статистика по платформам"
    )
    operating_systems: List[OSStatSchema] = Field(
        default_factory=list,
        description="Статистика по операционным системам"
    )
    app_versions: List[AppVersionStatSchema] = Field(
        default_factory=list,
        description="Статистика по версиям приложения"
    )
    total_users: int = Field(0, description="Общее количество уникальных пользователей")
    total_requests: int = Field(0, description="Общее количество запросов")
