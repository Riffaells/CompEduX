import logging
import os
from sqlalchemy import create_engine, text
from sqlalchemy.exc import ProgrammingError

# Настройка логирования
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def init_db():
    """
    Инициализация баз данных для всех сервисов
    """
    # Получаем параметры подключения из переменных окружения
    user = os.getenv("POSTGRES_USER", "postgres")
    password = os.getenv("POSTGRES_PASSWORD", "secure_password")
    host = os.getenv("POSTGRES_HOST", "localhost")
    port = os.getenv("POSTGRES_PORT", "5432")

    # Подключаемся к основной базе данных postgres
    postgres_url = f"postgresql://{user}:{password}@{host}:{port}/postgres"
    engine = create_engine(postgres_url)

    # Список баз данных для создания
    databases = ["auth_db", "room_db", "competition_db", "achievement_db"]

    # Создаем базы данных, если они не существуют
    with engine.connect() as conn:
        conn.execution_options(isolation_level="AUTOCOMMIT")

        for db_name in databases:
            try:
                logger.info(f"Проверка существования базы данных {db_name}...")
                # Проверяем, существует ли база данных
                result = conn.execute(text(f"SELECT 1 FROM pg_database WHERE datname = '{db_name}'"))
                if result.fetchone() is None:
                    logger.info(f"Создание базы данных {db_name}...")
                    conn.execute(text(f"CREATE DATABASE {db_name}"))
                    logger.info(f"База данных {db_name} успешно создана")
                else:
                    logger.info(f"База данных {db_name} уже существует")
            except Exception as e:
                logger.error(f"Ошибка при создании базы данных {db_name}: {e}")

    logger.info("Инициализация баз данных завершена")


if __name__ == "__main__":
    init_db()
