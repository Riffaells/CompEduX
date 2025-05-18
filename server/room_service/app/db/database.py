"""
Модуль для работы с PostgreSQL через asyncpg
"""
import databases

from common.logger import get_logger
from ..core.config import settings

# Получаем логгер
logger = get_logger(__name__)

# Получаем URL подключения
db_url = settings.SQLALCHEMY_DATABASE_URI

# Создаем экземпляр базы данных для асинхронной работы с PostgreSQL
database = databases.Database(db_url, force_rollback=settings.ENV == "testing")

logger.info(f"Инициализирована PostgreSQL база данных: {db_url}")
