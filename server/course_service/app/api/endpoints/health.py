"""
API endpoints for health check
"""
from datetime import datetime, timezone

from app.api.deps import get_db
from app.db.db import check_db_connection
from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger

# Set up logger
logger = get_logger("course_service.api.health")

router = APIRouter()


@router.get("/", summary="Health check")
async def health_check(db: AsyncSession = Depends(get_db)):
    """
    Check health of the course service.

    Returns information about:
    - API status
    - Database connection status
    """
    logger.info("Health check requested")

    # Check database connection
    db_status = await check_db_connection(db)

    return {
        "status": "healthy",
        "api": {
            "status": "operational"
        },
        "database": {
            "status": "connected" if db_status else "disconnected"
        },
        "message": "Course service is healthy",
        "timestamp": datetime.now(timezone.utc).isoformat()
    }
