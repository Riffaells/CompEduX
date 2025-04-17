# -*- coding: utf-8 -*-
"""
Инициализация базы данных для auth_service
"""
import logging
from sqlalchemy import text
from sqlalchemy.exc import OperationalError
from .session import engine, SessionLocal
from ..core.config import settings
from ..models.enums import BeveragePreference

logger = logging.getLogger(__name__)

def init_db():
    """
    Инициализация базы данных.

    Проверяет подключение к базе данных и создает необходимые таблицы.
    При обновлении структуры базы данных, существующие таблицы удаляются и создаются заново.
    """
    try:
        # Проверка подключения к базе данных
        with engine.connect() as conn:
            conn.execute(text("SELECT 1"))
        logger.info("Подключение к базе данных успешно установлено")

        # Удаление и создание таблиц
        from ..models.base import Base

        # В режиме разработки сбрасываем все таблицы и создаем их заново
        if settings.ENV == "development":
            logger.info("Режим разработки: сброс и пересоздание всех таблиц")
            Base.metadata.drop_all(bind=engine)
            logger.info("Таблицы успешно удалены")

        # Создание таблиц
        Base.metadata.create_all(bind=engine)
        logger.info("Таблицы базы данных успешно созданы")

        # Создаем тестового пользователя, если в режиме разработки
        if settings.ENV == "development":
            from ..models.user import UserModel, UserProfileModel, UserPreferencesModel, UserRatingModel
            from ..services.auth import get_password_hash, get_user_by_email

            db = SessionLocal()
            # Проверяем, есть ли уже тестовый пользователь
            test_user = get_user_by_email(db, "test@example.com")

            if not test_user:
                logger.info("Создание тестового пользователя для разработки")
                test_user = UserModel(
                    email="test@example.com",
                    username="test",
                    hashed_password=get_password_hash("test123"),
                    is_verified=True
                )

                # Создаем профиль
                test_user.profile = UserProfileModel(
                    first_name="Test",
                    last_name="User",
                    bio="This is a test user for development purposes",
                    location="Test Location"
                )

                # Создаем настройки
                test_user.preferences = UserPreferencesModel(
                    beverage_preference=BeveragePreference.COFFEE,
                    theme="dark"
                )

                # Создаем рейтинги
                test_user.ratings = UserRatingModel(
                    contribution_rating=4.5,
                    bot_score=0.1,
                    expertise_rating=3.8,
                    competition_rating=4.2
                )

                db.add(test_user)
                db.commit()
                logger.info(f"Тестовый пользователь создан: {test_user.email} (ID: {test_user.id})")

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
                from ..models.base import Base
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
