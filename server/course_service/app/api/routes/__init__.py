"""
Инициализация маршрутов API
"""
from fastapi import APIRouter

from . import courses, enrollments, technology_tree, tags  # Используем относительные импорты

# Создаем главный роутер для экспорта
router = APIRouter()

# Подключаем роутеры из модулей
router.include_router(courses.router, prefix="/courses", tags=["courses"])
router.include_router(enrollments.router, prefix="/enrollments", tags=["enrollments"])
router.include_router(technology_tree.router, prefix="/technology-trees", tags=["technology_trees"])
router.include_router(tags.router, prefix="/tags", tags=["tags"])

# Экспортируем все модули маршрутов и основной роутер
__all__ = ["courses", "enrollments", "technology_tree", "tags", "router"]
