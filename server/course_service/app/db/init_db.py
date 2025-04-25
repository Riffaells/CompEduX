# -*- coding: utf-8 -*-
"""
Инициализация базы данных для course_service
"""
import sys

from sqlalchemy import text
from sqlalchemy.exc import OperationalError

from common.logger import get_logger
from .session import engine
from ..core.config import settings

logger = get_logger("course_service")


def reset_db():
    """
    Полный сброс и пересоздание базы данных.

    Удаляет базу данных и пользователя, затем создает их заново.
    Работает только в режиме разработки.

    Returns:
        bool: True если сброс успешен, False в противном случае.
    """
    # Проверяем режим разработки
    if settings.ENV != "development":
        logger.error(f"Сброс базы данных доступен только в режиме разработки (ENV=development)")
        logger.error(f"Текущий режим: ENV={settings.ENV}")
        logger.error(f"Измените значение ENV в файле .env для сброса базы данных")
        return False

    logger.info(f"Начинаю сброс базы данных {settings.POSTGRES_DB} (режим разработки: {settings.ENV})")

    try:
        # Импортируем psycopg2 здесь для лучшей изоляции
        import psycopg2

        # Подключение к базе данных postgres
        conn = psycopg2.connect(
            host=settings.POSTGRES_HOST,
            port=settings.POSTGRES_PORT,
            user="postgres",  # Используем пользователя postgres для администрирования
            password=settings.POSTGRES_ADMIN_PASSWORD,  # Пароль администратора из настроек
            dbname="postgres"
        )
        conn.autocommit = True
        cursor = conn.cursor()

        # Закрываем все активные соединения к базе
        logger.info("Закрываем все активные соединения к базе данных")
        sql_query = f"""
        SELECT pg_terminate_backend(pg_stat_activity.pid)
        FROM pg_stat_activity
        WHERE pg_stat_activity.datname = '{settings.POSTGRES_DB}'
        AND pid <> pg_backend_pid();
        """
        cursor.execute(sql_query)

        # Удаляем базу данных
        logger.info(f"Удаляем базу данных {settings.POSTGRES_DB}")
        sql_query = f"""
        DROP DATABASE IF EXISTS {settings.POSTGRES_DB};
        """
        cursor.execute(sql_query)

        # Удаляем пользователя
        logger.info(f"Удаляем пользователя {settings.POSTGRES_USER}")
        sql_query = f"""
        DROP ROLE IF EXISTS {settings.POSTGRES_USER};
        """
        cursor.execute(sql_query)

        # Создаем пользователя заново
        logger.info(f"Создаем пользователя {settings.POSTGRES_USER}")
        sql_query = f"""
        CREATE USER {settings.POSTGRES_USER} WITH PASSWORD '{settings.POSTGRES_PASSWORD}';
        """
        cursor.execute(sql_query)

        # Создаем базу данных
        logger.info(f"Создаем базу данных {settings.POSTGRES_DB}")
        sql_query = f"""
        CREATE DATABASE {settings.POSTGRES_DB} OWNER {settings.POSTGRES_USER};
        """
        cursor.execute(sql_query)

        # Предоставляем права пользователю
        logger.info(f"Предоставляем права пользователю {settings.POSTGRES_USER}")
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

        logger.info(f"База данных {settings.POSTGRES_DB} успешно сброшена и пересоздана")

        # После сброса базы, инициализируем ее
        init_result = init_db()
        if init_result:
            logger.info("База данных успешно инициализирована после сброса")
        else:
            logger.error("Ошибка при инициализации базы данных после сброса")
            return False

        return True

    except Exception as e:
        logger.error(f"Ошибка при сбросе базы данных: {str(e)}")
        import traceback
        logger.error(traceback.format_exc())
        return False


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

        # Здесь может быть добавление начальных данных для курсов, если это необходимо
        if settings.ENV == "development":
            # Инициализация тестовых данных (если нужно)
            pass

        return True
    except OperationalError as e:
        logger.error(f"Ошибка подключения к базе данных: {e}")

        # Если ошибка связана с аутентификацией, попробуем создать пользователя
        if "password authentication failed" in str(e) or "role" in str(e) and "does not exist" in str(e):
            logger.warning("Проблема с аутентификацией. Попытка создать пользователя...")
            logger.warning(
                f"Текущие настройки: POSTGRES_USER={settings.POSTGRES_USER}, POSTGRES_DB={settings.POSTGRES_DB}")
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

                logger.info(
                    f"Пользователь {settings.POSTGRES_USER} и база данных {settings.POSTGRES_DB} успешно созданы")

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
    # Проверяем, вызывается ли скрипт с аргументом reset
    if len(sys.argv) > 1 and sys.argv[1] == "reset":
        logger.info("Запуск полного сброса базы данных")
        success = reset_db()
        if success:
            logger.info("Полный сброс базы данных успешно выполнен")
            sys.exit(0)
        else:
            logger.error("Полный сброс базы данных не удался")
            sys.exit(1)
    else:
        # Просто инициализируем базу данных
        init_db()
