# -*- coding: utf-8 -*-
"""
Database initialization for auth_service
"""
from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger
from .session import db_manager
from ..core.config import settings
from ..models.enums import BeveragePreference

logger = get_logger("auth_service.db.init_db")


async def create_test_data(db: AsyncSession) -> None:
    """
    Create test data for development environment.

    Args:
        db: Async database session
    """
    from ..models.user import UserModel, UserProfileModel, UserPreferencesModel, UserRatingModel
    from ..services.auth import get_password_hash, get_user_by_email

    # Check if test user already exists
    test_user = await get_user_by_email(db, "test@example.com")

    if not test_user:
        logger.info("Creating test user for development")
        test_user = UserModel(
            email="test@example.com",
            username="test",
            hashed_password=get_password_hash("test123"),
            is_verified=True
        )

        # Create profile
        test_user.profile = UserProfileModel(
            first_name="Test",
            last_name="User",
            bio="This is a test user for development purposes",
            location="Test Location"
        )

        # Create preferences
        test_user.preferences = UserPreferencesModel(
            beverage_preference=BeveragePreference.COFFEE,
            theme="dark"
        )

        # Create ratings
        test_user.ratings = UserRatingModel(
            contribution_rating=4.5,
            bot_score=0.1,
            expertise_rating=3.8,
            competition_rating=4.2
        )

        # Используем контекстный менеджер для транзакции
        try:
            async with db.begin():
                db.add(test_user)
                logger.info("Test user transaction committed")
        except Exception as e:
            logger.error(f"Error creating test user: {str(e)}")
            raise

        # Обновление объекта после завершения транзакции
        await db.refresh(test_user)
        logger.info(f"Test user created: {test_user.email} (ID: {test_user.id})")


async def init_db() -> bool:
    """
    Initialize the database.

    This function:
    1. Checks database connection
    2. Creates all tables
    3. Loads initial test data for development

    Returns:
        bool: Success or failure
    """
    logger.info("Initializing auth service database...")

    # Use the common AsyncDatabaseManager to initialize
    success = await db_manager.init_db(create_test_data if settings.ENV == "development" else None)

    if success:
        logger.info("Auth service database initialized successfully")
    else:
        logger.error("Failed to initialize auth service database")

    return success


if __name__ == "__main__":
    import asyncio

    asyncio.run(init_db())
