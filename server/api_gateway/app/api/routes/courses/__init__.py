from fastapi import APIRouter

from .courses import router as courses_router
from .technology_tree import router as technology_tree_router
from .articles import router as articles_router
from .lessons import router as lessons_router

# Создаем основной роутер для всех API, связанных с курсами
router = APIRouter()

# Подключаем роутеры для различных компонентов
router.include_router(courses_router)
router.include_router(technology_tree_router)
router.include_router(articles_router)
router.include_router(lessons_router)
