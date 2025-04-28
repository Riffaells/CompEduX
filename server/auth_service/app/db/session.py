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
# Set expire_on_commit=False to prevent the MissingGreenlet error during async operations
AsyncSessionLocal = db_manager.AsyncSessionLocal


# Dependency function for getting async DB session
async def get_db() -> AsyncGenerator[AsyncSession, None]:
    """
    Dependency function for obtaining an async database session.
    This function provides a clean session without transaction management,
    letting the route handlers manage their own transactions.

    The session is configured with expire_on_commit=False to avoid the
    MissingGreenlet error when accessing lazy-loaded attributes
    in an async context.

    Yields:
        AsyncSession: SQLAlchemy async session for database operations
    """
    async with AsyncSessionLocal() as db:
        # Set expire_on_commit to False to avoid greenlet_spawn errors
        db.expire_on_commit = False
        # Просто предоставляем сессию без управления транзакциями
        try:
            yield db
        finally:
            # Закрываем сессию без коммита/роллбэка
            await db.close()


# Function for testing database connection
async def test_db_connection() -> bool:
    """Test database connection and return status"""
    return await db_manager.test_connection()
