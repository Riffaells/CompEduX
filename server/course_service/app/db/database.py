"""
PostgreSQL database module for async operations
"""
import databases

from app.core.config import settings
from common.logger import get_logger

# Get logger
logger = get_logger("course_service.db")

# Get database URL
db_url = settings.SQLALCHEMY_DATABASE_URI

# Create database instance for async PostgreSQL operations
database = databases.Database(db_url, force_rollback=settings.ENV == "testing")

logger.info(f"Initialized PostgreSQL database: {db_url.replace(settings.POSTGRES_PASSWORD, '****')}")
