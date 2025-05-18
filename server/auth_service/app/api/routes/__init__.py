"""
Routes package for auth_service

This module imports and registers all API route modules.
"""
from fastapi import APIRouter

# Import route modules
from . import auth, users, stats, rooms, health, friends

# Import routers from modules
from .auth import router as auth_router
from .users import router as users_router
from .stats import router as stats_router
from .rooms import router as rooms_router
from .health import router as health_router
from .friends import router as friends_router

# Create main router
router = APIRouter()

# Include all routers with appropriate prefixes and tags
router.include_router(auth_router, prefix="/auth", tags=["auth"])
router.include_router(users_router, prefix="/users", tags=["users"])
router.include_router(stats_router, prefix="/stats", tags=["stats"])
router.include_router(rooms_router, prefix="/rooms", tags=["rooms"])
router.include_router(health_router, prefix="/health", tags=["health"])
router.include_router(friends_router, prefix="/friends", tags=["friends"])

# Routes Package
