"""
Database session module that extends common/db.py functionality
"""
from typing import AsyncGenerator

from sqlalchemy.ext.asyncio import AsyncSession

from common.db import AsyncDatabaseManager
from common.logger import get_logger
from ..core.config import settings

# Get logger
logger = get_logger(__name__)

# Create async database manager
db_manager = AsyncDatabaseManager(settings, "auth_service")

# Export common SQLAlchemy objects
engine = db_manager.engine
AsyncSessionLocal = db_manager.AsyncSessionLocal


# Dependency function for getting async DB session
async def get_db() -> AsyncGenerator[AsyncSession, None]:
    """
    Dependency function for obtaining an async database session.
    This is a wrapper around db_manager.get_db for compatibility.

    Yields:
        AsyncSession: SQLAlchemy async session for database operations
    """
    async with db_manager.get_db() as db:
        yield db


# Function for testing database connection
async def test_db_connection() -> bool:
    """Test database connection and return status"""
    return await db_manager.test_connection()
