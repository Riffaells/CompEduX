from fastapi import APIRouter

# Temporarily commented out rooms import to troubleshoot database connection issues
from . import auth, users  # , rooms

router = APIRouter()

router.include_router(auth.router, prefix="/auth", tags=["auth"])
router.include_router(users.router, prefix="/users", tags=["users"])
# Temporarily commented out rooms router to troubleshoot database connection issues
# router.include_router(rooms.router, prefix="/rooms", tags=["rooms"])

# Routes Package
