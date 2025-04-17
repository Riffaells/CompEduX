"""
Database initialization for course_service
"""
import sys
import os
import logging
from sqlalchemy import text
from sqlalchemy.exc import OperationalError

# Получаем абсолютный путь к корневой директории проекта
root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../.."))
if root_dir not in sys.path:
    sys.path.insert(0, root_dir)

from .session import db_manager, engine, SessionLocal
from app.core.config import settings

# Импортируем все модели для регистрации в SQLAlchemy
from app.models.base import Base
from app.models.course import Course
from app.models.tag import Tag, TagTranslation
from app.models.localization import Localization
from app.models.technology_tree import TechnologyTree

from ..db.test_data import create_test_courses

logger = logging.getLogger(__name__)

def init_db():
    """
    Инициализация базы данных для course_service.

    Проверяет подключение к базе данных и создает необходимые таблицы.
    В режиме разработки удаляет существующие таблицы и создает их заново.
    Также инициализирует тестовые данные, если находится в режиме разработки.

    Returns:
        bool: Успех или неудача инициализации
    """
    try:
        # Проверка подключения к базе данных
        with engine.connect() as conn:
            conn.execute(text("SELECT 1"))
        logger.info("Подключение к базе данных успешно установлено")

        # В режиме разработки сбрасываем все таблицы и создаем их заново
        if settings.ENV == "development":
            logger.info("Режим разработки: сброс и пересоздание всех таблиц")
            Base.metadata.drop_all(bind=engine)
            logger.info("Таблицы успешно удалены")

        # Создание таблиц
        Base.metadata.create_all(bind=engine)
        logger.info("Таблицы базы данных успешно созданы")

        # Создаем тестовые данные, если в режиме разработки
        if settings.ENV == "development":
            db = SessionLocal()
            try:
                create_test_courses(db)
                logger.info("Тестовые данные успешно созданы")
            except Exception as e:
                logger.error(f"Ошибка при создании тестовых данных: {e}")
            finally:
                db.close()

        return True
    except OperationalError as e:
        logger.error(f"Ошибка подключения к базе данных: {e}")

        # Если ошибка связана с аутентификацией, попробуем создать пользователя
        if "password authentication failed" in str(e) or "role" in str(e) and "does not exist" in str(e):
            logger.warning("Проблема с аутентификацией. Попытка создать пользователя...")
            logger.warning(f"Текущие настройки: POSTGRES_USER={settings.POSTGRES_USER}, POSTGRES_DB={settings.POSTGRES_DB}")
            try:
                # Подключение к PostgreSQL как администратор
                import psycopg2

                # Подключение к базе данных postgres
                conn = psycopg2.connect(
                    host=settings.POSTGRES_HOST,
                    port=settings.POSTGRES_PORT,
                    user="postgres",  # Используем пользователя postgres для создания нового пользователя
                    password=settings.POSTGRES_ADMIN_PASSWORD,  # Пароль администратора из настроек
                    dbname="postgres"
                )
                conn.autocommit = True
                cursor = conn.cursor()

                # Создание пользователя, если он не существует
                logger.info(f"Попытка создать пользователя: {settings.POSTGRES_USER}")
                sql_query = f"""
                DO
                $$
                BEGIN
                    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '{settings.POSTGRES_USER}') THEN
                        CREATE USER {settings.POSTGRES_USER} WITH PASSWORD '{settings.POSTGRES_PASSWORD}';
                    ELSE
                        ALTER USER {settings.POSTGRES_USER} WITH PASSWORD '{settings.POSTGRES_PASSWORD}';
                    END IF;
                END
                $$;
                """
                logger.info(f"SQL запрос: {sql_query}")
                cursor.execute(sql_query)

                # Создание базы данных, если она не существует
                sql_query = f"""
                SELECT 'CREATE DATABASE {settings.POSTGRES_DB} OWNER {settings.POSTGRES_USER}'
                WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '{settings.POSTGRES_DB}')
                """
                logger.info(f"SQL запрос для создания базы данных: {sql_query}")
                cursor.execute(sql_query)

                result = cursor.fetchone()
                if result:
                    logger.info(f"Выполнение запроса: {result[0]}")
                    cursor.execute(result[0])

                # Предоставление прав
                cursor.execute(f"GRANT ALL PRIVILEGES ON DATABASE {settings.POSTGRES_DB} TO {settings.POSTGRES_USER}")

                conn.close()

                # Подключение к созданной базе данных для настройки прав на схему
                conn = psycopg2.connect(
                    host=settings.POSTGRES_HOST,
                    port=settings.POSTGRES_PORT,
                    user="postgres",
                    password=settings.POSTGRES_ADMIN_PASSWORD,
                    dbname=settings.POSTGRES_DB
                )
                conn.autocommit = True
                cursor = conn.cursor()

                # Предоставление прав на схему public
                cursor.execute(f"GRANT ALL PRIVILEGES ON SCHEMA public TO {settings.POSTGRES_USER}")
                cursor.execute(f"GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO {settings.POSTGRES_USER}")
                cursor.execute(f"GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO {settings.POSTGRES_USER}")

                conn.close()

                logger.info(f"Пользователь {settings.POSTGRES_USER} и база данных {settings.POSTGRES_DB} успешно созданы")

                # Повторная попытка создания таблиц
                if settings.ENV == "development":
                    Base.metadata.drop_all(bind=engine)
                Base.metadata.create_all(bind=engine)
                logger.info("Таблицы базы данных успешно созданы после исправления проблемы с пользователем")

                return True
            except Exception as ex:
                logger.error(f"Не удалось создать пользователя базы данных: {ex}")
                return False
        return False
    except Exception as e:
        logger.error(f"Неизвестная ошибка при инициализации базы данных: {e}")
        return False


if __name__ == "__main__":
    init_db()
