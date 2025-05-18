from fastapi import APIRouter

from app.api.endpoints import (
    rooms,
    health
)

api_router = APIRouter()

# Health check
api_router.include_router(health.router, prefix="/health", tags=["health"])

# Room management
api_router.include_router(rooms.router, prefix="", tags=["rooms"])
