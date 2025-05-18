# Routes Package

from fastapi import APIRouter

# Создаем основной роутер для API
api_router = APIRouter()

# Импортируем и добавляем специализированные роутеры
from app.api.routes.auth import router as auth_router
from app.api.routes.health import router as health_router
from app.api.routes.course import router as course_router
from app.api.routes.room import router as room_router
from app.api.routes.friends import router as friends_router
from app.api.routes.users import router as users_router

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
    friends_router,
    prefix="/friends",
    tags=["friends"]
)

api_router.include_router(
    users_router,
    prefix="/users",
    tags=["users"]
)

api_router.include_router(
    course_router,
    prefix="/courses",
    tags=["courses"]
)

api_router.include_router(
    room_router,
    prefix="/rooms",
    tags=["rooms"]
)

# Примечание: Все остальные сервисы должны быть добавлены вручную
# путем создания отдельных файлов в routes/ и включения их здесь
