from fastapi import APIRouter

from app.api.endpoints import (
    courses,
    technology_tree,
    tags,
    health,
    articles,
)

api_router = APIRouter()

api_router.include_router(health.router, prefix="/health", tags=["health"])
api_router.include_router(courses.router, prefix="/courses", tags=["courses"])
api_router.include_router(tags.router, prefix="/tags", tags=["tags"])
api_router.include_router(technology_tree.router, prefix="/courses", tags=["technology_tree"])
api_router.include_router(articles.router, prefix="/courses", tags=["articles"])
