"""
Декораторы для функций сервиса аутентификации
"""
import functools
from typing import Any, Callable, TypeVar

from fastapi import HTTPException, status
from sqlalchemy.orm import Session

from common.logger import get_logger

# Типизированные переменные для функций
T = TypeVar('T')
logger = get_logger(__name__)


def handle_sqlalchemy_errors(func: Callable[..., T]) -> Callable[..., T]:
    """
    Декоратор для обработки ошибок SQLAlchemy.

    Заменяет многократные блоки try/except/rollback в функциях
    на единый обработчик ошибок с логированием и откатом транзакций.

    Args:
        func: Декорируемая функция

    Returns:
        Обёрнутая функция с обработкой ошибок
    """
    @functools.wraps(func)
    async def wrapper(*args, **kwargs):
        # Находим параметр db в аргументах
        db = None
        for arg in args:
            if isinstance(arg, Session):
                db = arg
                break

        if db is None:
            for key, value in kwargs.items():
                if isinstance(value, Session):
                    db = value
                    break

        if db is None:
            logger.warning(f"SQLAlchemy session not found in arguments for {func.__name__}")

        try:
            # Вызываем оригинальную функцию
            return await func(*args, **kwargs)
        except HTTPException:
            # Пропускаем HTTPException, они должны быть обработаны на уровне API
            if db is not None:
                try:
                    await db.rollback()
                except Exception as rollback_error:
                    logger.error(f"Rollback failed in {func.__name__}: {str(rollback_error)}")
            raise
        except Exception as e:
            # Логируем ошибку и откатываем транзакцию
            logger.error(f"Error in {func.__name__}: {str(e)}")

            if db is not None:
                try:
                    await db.rollback()
                except Exception as rollback_error:
                    logger.error(f"Rollback failed in {func.__name__}: {str(rollback_error)}")

            # Для функций создания/обновления возвращаем HTTP ошибку
            if func.__name__ in ('create_user', 'create_refresh_token', 'refresh_access_token'):
                raise HTTPException(
                    status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    detail=f"Database operation failed: {str(e)}"
                )

            # Для функций аутентификации и проверки возвращаем None
            elif func.__name__ in ('authenticate_user', 'get_user_by_email', 'get_user_by_username', 'get_user_by_id'):
                return None

            # Для функций отзыва токенов возвращаем False
            elif func.__name__ == 'revoke_refresh_token':
                return False

            # Для остальных функций пробрасываем исключение дальше
            else:
                raise

    return wrapper
