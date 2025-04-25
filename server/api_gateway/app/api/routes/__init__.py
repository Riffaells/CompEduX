# Routes Package

from fastapi import APIRouter

# Создаем основной роутер для API
api_router = APIRouter()

# Импортируем и добавляем специализированные роутеры
from app.api.routes.auth import router as auth_router
from app.api.routes.health import router as health_router
from app.api.routes.course import router as course_router

# Добавляем специализированные роутеры с явными тегами и описаниями
api_router.include_router(
    health_router,
    prefix="/health",
    tags=["health"]
)

api_router.include_router(
    auth_router,
    prefix="/auth",
    tags=["auth"]
)

api_router.include_router(
    course_router,
    prefix="/courses",
    tags=["courses"]
)

# Примечание: Все остальные сервисы должны быть добавлены вручную
# путем создания отдельных файлов в routes/ и включения их здесь
