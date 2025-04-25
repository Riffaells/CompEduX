"""
Утилиты для работы с базой данных
"""
from contextlib import asynccontextmanager
from typing import AsyncGenerator, TypeVar, Generic

from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger

T = TypeVar('T')
logger = get_logger(__name__)


@asynccontextmanager
async def db_transaction(db: AsyncSession) -> AsyncGenerator[AsyncSession, None]:
    """
    Контекстный менеджер для транзакций SQLAlchemy.

    Автоматически делает commit при успешном выполнении операции
    и rollback при возникновении исключения.

    Args:
        db: SQLAlchemy сессия

    Yields:
        SQLAlchemy сессия

    Example:
        async with db_transaction(db) as session:
            user = UserModel(email="test@example.com")
            session.add(user)
            # Автоматический commit после выхода из контекста
    """
    try:
        yield db
        await db.commit()
    except Exception as e:
        await db.rollback()
        logger.error(f"Database transaction failed: {str(e)}")
        raise


async def execute_with_retry(db: AsyncSession, operation, max_retries: int = 3):
    """
    Выполняет операцию с базой данных с повторными попытками при сбоях.

    Args:
        db: SQLAlchemy сессия
        operation: Функция для выполнения
        max_retries: Максимальное количество попыток

    Returns:
        Результат выполнения функции

    Raises:
        Exception: Если все попытки завершились неудачно
    """
    last_error = None

    for attempt in range(max_retries):
        try:
            async with db_transaction(db) as session:
                result = await operation(session)
                return result
        except Exception as e:
            last_error = e
            logger.warning(f"Database operation failed (attempt {attempt+1}/{max_retries}): {str(e)}")

    # Все попытки неудачны
    logger.error(f"All database operation attempts failed: {str(last_error)}")
    raise last_error
