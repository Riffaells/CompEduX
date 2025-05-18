"""
API routes for the room service
"""

from fastapi import APIRouter

from app.api.endpoints import health, rooms

api_router = APIRouter()

# Include the health check endpoint
api_router.include_router(health.router, tags=["health"])

# Include room endpoints
api_router.include_router(rooms.router, prefix="/rooms", tags=["rooms"])
