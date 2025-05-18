"""
Health check API endpoints
"""
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_db
from app.core.config import settings

router = APIRouter()


@router.get("/health", tags=["health"])
async def health_check(db: AsyncSession = Depends(get_db)):
    """
    Health check endpoint for the room service.
    
    Used for health checks in Kubernetes/Docker.
    Should return 200 OK if the service is healthy.
    """
    # Simple check to ensure the app is running
    service_health = {
        "status": "ok",
        "service": "room_service",
        "version": settings.VERSION,
        "environment": settings.ENV
    }
    
    # Database health check
    db_health = {}
    try:
        # Проверяем соединение с БД
        await db.execute("SELECT 1")
        db_health = {
            "status": "ok",
            "message": "Connected to database"
        }
    except Exception as e:
        db_health = {
            "status": "error",
            "message": f"Database connection error: {str(e)}"
        }
    
    # Combine health checks
    result = {
        **service_health,
        "database": db_health
    }
    
    return result 