"""
Course service API routes
"""
from fastapi import APIRouter

from common.logger import initialize_logging
from app.api.routes import courses, enrollments

# Initialize logger
logger = initialize_logging("course_service.api")

# Create main router
router = APIRouter()

# Include specific route modules
router.include_router(courses.router, prefix="/courses", tags=["courses"])
router.include_router(enrollments.router, prefix="/enrollments", tags=["enrollments"])

@router.get("/status")
async def status():
    """API status endpoint"""
    logger.info("API status endpoint called")
    return {"status": "operational"}
