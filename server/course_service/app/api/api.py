from fastapi import APIRouter

from app.api.endpoints import (
    courses,
    articles,
    lessons,
    technology_tree,
    localization,
    tags,
    health
)

api_router = APIRouter()

# Health check
api_router.include_router(health.router, prefix="/health", tags=["health"])

# Course management
api_router.include_router(courses.router, prefix="", tags=["courses"])

# Technology tree endpoints - using course_id in the path
api_router.include_router(technology_tree.router, prefix="/{course_id}/tree", tags=["technology_tree"])

# Article endpoints - directly at /articles level
api_router.include_router(articles.router, prefix="/articles", tags=["articles"])

# Lesson endpoints - directly at /lessons level
api_router.include_router(lessons.router, prefix="/lessons", tags=["lessons"])

# Localization endpoints
api_router.include_router(localization.router, prefix="/localization", tags=["localization"])

# Tags endpoints
api_router.include_router(tags.router, prefix="/tags", tags=["tags"])
