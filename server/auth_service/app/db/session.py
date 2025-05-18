"""
Database session module that extends common/db.py functionality
"""
from typing import AsyncGenerator

from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker

from common.db import AsyncDatabaseManager
from common.logger import get_logger
from ..core.config import settings
from ..models.base import Base

# Get logger
logger = get_logger(__name__)

# Создаем экземпляр менеджера базы данных
db_manager = AsyncDatabaseManager(
    settings=settings,
    service_name="auth_service",
    required_tables=["users", "friendships", "user_profiles", "user_preferences", "user_ratings", "user_privacy"]
)

# Используем AsyncSessionLocal из db_manager
async_session_factory = db_manager.AsyncSessionLocal

# Export common SQLAlchemy objects
engine = db_manager.engine
# Set expire_on_commit=False to prevent the MissingGreenlet error during async operations
AsyncSessionLocal = db_manager.AsyncSessionLocal


# Dependency function for getting async DB session
async def get_db() -> AsyncSession:
    """
    Dependency для получения сессии базы данных.
    
    Yields:
        AsyncSession: Сессия SQLAlchemy для асинхронной работы с базой данных
    """
    async with async_session_factory() as session:
        yield session


# Function for testing database connection
async def test_db_connection() -> bool:
    """Test database connection and return status"""
    return await db_manager.test_connection()
