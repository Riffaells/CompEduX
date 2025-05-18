"""
Async PostgreSQL database module using SQLAlchemy 2.0
"""
from typing import AsyncGenerator
from urllib.parse import urlparse, parse_qs, urlunparse, urlencode

from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger
from .session import db_manager, async_session_factory
from ..core.config import settings

# Get logger
logger = get_logger(__name__)

# Database URL - удаляем client_encoding из строки подключения
parsed_url = urlparse(settings.SQLALCHEMY_DATABASE_URI)
query_params = parse_qs(parsed_url.query)

# Удаляем client_encoding из параметров запроса
if 'client_encoding' in query_params:
    del query_params['client_encoding']

# Собираем URL обратно
new_query = urlencode(query_params, doseq=True)
parsed_url = parsed_url._replace(query=new_query)
clean_uri = urlunparse(parsed_url)

# Заменяем postgresql:// на postgresql+asyncpg://
db_url = clean_uri.replace('postgresql://', 'postgresql+asyncpg://')
logger.info(f"Initialized async PostgreSQL database: {db_url}")


# Async context manager for database connection
async def get_database() -> AsyncGenerator[AsyncSession, None]:
    """
    Dependency for getting async database connection.

    Sets expire_on_commit=False to prevent greenlet_spawn errors in async contexts
    when accessing ORM attributes after commit.

    Yields:
        AsyncSession: Async database session
    """
    session = async_session_factory()
    try:
        yield session
        await session.commit()
    except Exception as e:
        await session.rollback()
        logger.error(f"Database session error: {str(e)}")
        raise
    finally:
        await session.close()


# Startup and shutdown event handlers
async def connect_to_db():
    """Connect to database on startup"""
    logger.info("Connecting to async database...")
    try:
        # Test connection
        if await db_manager.test_connection():
            logger.info("Connected to async database successfully")
        else:
            logger.error("Failed to connect to async database")
            raise Exception("Database connection test failed")
    except Exception as e:
        logger.error(f"Error connecting to async database: {str(e)}")
        raise


async def disconnect_from_db():
    """Disconnect from database on shutdown"""
    logger.info("Disconnecting from async database...")
    try:
        # SQLAlchemy 2.0 async engine handles connection pooling automatically
        # Just log the event for now
        logger.info("Disconnected from async database successfully")
    except Exception as e:
        logger.error(f"Error disconnecting from async database: {str(e)}")
