import logging
import os
from sqlalchemy import create_engine, text
from sqlalchemy.exc import ProgrammingError

# Настройка логирования
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def init_db():
    """
    Инициализация базы данных для auth_service
    """
    # Получаем параметры подключения из переменных окружения
    user = os.getenv("POSTGRES_USER", "auth_user")
    password = os.getenv("POSTGRES_PASSWORD", "auth_password")
    host = os.getenv("POSTGRES_HOST", "localhost")
    port = os.getenv("POSTGRES_PORT", "5432")
    db = os.getenv("POSTGRES_DB", "auth_db")

    # Подключаемся к базе данных
    db_url = f"postgresql://{user}:{password}@{host}:{port}/{db}"
    logger.info(f"Подключение к базе данных {db} с пользователем {user}")

    try:
        engine = create_engine(db_url)
        with engine.connect() as conn:
            logger.info("Успешное подключение к базе данных")
            # Здесь можно выполнить дополнительные операции инициализации,
            # например, создание таблиц или заполнение начальными данными
    except Exception as e:
        logger.error(f"Ошибка при подключении к базе данных: {e}")
        raise

    logger.info("Инициализация базы данных завершена")


if __name__ == "__main__":
    init_db()
