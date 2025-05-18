"""
Асинхронный менеджер базы данных для course_service
"""
import asyncio
import concurrent.futures
import sys
import traceback
from contextlib import asynccontextmanager
from typing import AsyncGenerator, Any

import psycopg2
from databases import Database
from sqlalchemy import text, inspect, create_engine
from sqlalchemy.exc import OperationalError
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.orm import sessionmaker

from common.logger import get_logger
from ..core.config import settings
from ..models.base import Base

# Create base logger
logger = get_logger("room_service.db")

# Используем асинхронный URL из настроек
logger.info(f"Используем асинхронный URL для подключения к БД: {settings.ASYNC_SQLALCHEMY_DATABASE_URI}")

# Async engine and session factory
engine = create_async_engine(
    settings.ASYNC_SQLALCHEMY_DATABASE_URI,
    echo=settings.DB_ECHO,
    pool_pre_ping=True,
    pool_recycle=3600,
)

async_session_factory = async_sessionmaker(
    bind=engine,
    expire_on_commit=False,
    autocommit=False,
    autoflush=False,
)

# Database instance for SQLAlchemy 2.0 style
database = engine


async def get_async_session() -> AsyncGenerator[AsyncSession, Any]:
    """Get async database session"""
    async with async_session_factory() as session:
        yield session


# Initialize the database
async def init_db():
    """Initialize the database with tables"""
    try:
        # Test connection
        async with engine.begin() as conn:
            await conn.execute(text("SELECT 1"))

        # Create tables
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)

        return True
    except Exception as e:
        logger.error(f"Database initialization error: {e}")
        return False


class AsyncDatabaseManager:
    """
    Асинхронный менеджер базы данных для SQLAlchemy и AsyncPG.

    Этот класс обрабатывает основные операции с базой данных:
    - Создание асинхронного engine и сессий
    - Тестирование подключения
    - Инициализация схемы
    - Создание базы данных и пользователя (при необходимости)
    - Полный сброс базы данных
    """

    def __init__(self, service_name: str = "room_service"):
        """
        Инициализация менеджера базы данных с настройками сервиса.

        Args:
            service_name: Имя сервиса для логирования
        """
        self.settings = settings
        self.service_name = service_name
        self.logger = get_logger(f"{service_name}.db")

        # Маскируем пароль для логирования
        connection_info = (f"PostgreSQL connection info: host={settings.POSTGRES_HOST}, "
                           f"port={settings.POSTGRES_PORT}, user={settings.POSTGRES_USER}, "
                           f"db={settings.POSTGRES_DB}, python={sys.version}")
        self.logger.info(connection_info)

        # Безопасный URI для логирования
        safe_uri = settings.SQLALCHEMY_DATABASE_URI.replace(settings.POSTGRES_PASSWORD, "****")
        self.logger.info(f"Database URI: {safe_uri}")

        # Безопасный асинхронный URI для логирования
        safe_async_uri = settings.ASYNC_SQLALCHEMY_DATABASE_URI.replace(settings.POSTGRES_PASSWORD, "****")
        self.logger.info(f"Async Database URI: {safe_async_uri}")

        # Создаем асинхронный engine
        try:
            # Используем асинхронный URL из настроек
            self.async_engine = create_async_engine(
                settings.ASYNC_SQLALCHEMY_DATABASE_URI,
                pool_pre_ping=True,
                echo=settings.DB_ECHO
            )
            self.logger.info("AsyncIO SQLAlchemy engine created successfully")
        except Exception as e:
            self.logger.error(f"Error creating async SQLAlchemy engine: {str(e)}")
            self.logger.error(f"Engine traceback: {traceback.format_exc()}")
            # Re-raise to prevent initialization with a broken engine
            raise

        # Создаем синхронный engine для случаев, когда нужен синхронный доступ
        try:
            sync_connect_args = {"options": "-c client_encoding=utf8"}
            self.logger.info(f"Sync connection arguments: {sync_connect_args}")

            self.sync_engine = create_engine(
                settings.SQLALCHEMY_DATABASE_URI,
                pool_pre_ping=True,
                connect_args=sync_connect_args,
                echo=settings.DB_ECHO
            )
            self.logger.info("Sync SQLAlchemy engine created successfully")
        except Exception as e:
            self.logger.error(f"Error creating sync SQLAlchemy engine: {str(e)}")
            self.logger.error(f"Sync engine traceback: {traceback.format_exc()}")
            self.sync_engine = None

        # Создаем фабрики сессий
        self.async_session_factory = async_sessionmaker(
            bind=self.async_engine,
            autocommit=False,
            autoflush=False,
            expire_on_commit=False
        )

        if self.sync_engine:
            self.sync_session_factory = sessionmaker(
                bind=self.sync_engine,
                autocommit=False,
                autoflush=False
            )
        else:
            self.sync_session_factory = None

        # Создаем объект Database для низкоуровневых запросов
        try:
            # Для асинхронного подключения через databases
            self.database = Database(
                settings.SQLALCHEMY_DATABASE_URI,
                force_rollback=settings.ENV == "testing"
            )
            self.logger.info(
                f"Database instance created with URL: {settings.SQLALCHEMY_DATABASE_URI.replace(settings.POSTGRES_PASSWORD, '****')}")
        except Exception as e:
            self.logger.error(f"Error creating Database instance: {str(e)}")
            self.logger.error(f"Database creation traceback: {traceback.format_exc()}")
            raise

    @asynccontextmanager
    async def get_db(self) -> AsyncGenerator[AsyncSession, None]:
        """
        Зависимость для получения асинхронной сессии базы данных.

        Yields:
            AsyncSession: Асинхронная сессия SQLAlchemy для операций с базой данных
        """
        session = self.async_session_factory()
        try:
            yield session
        finally:
            await session.close()

    async def connect(self) -> bool:
        """
        Подключение к базе данных.

        Returns:
            bool: Успех или неудача
        """
        try:
            await self.database.connect()
            self.logger.info("Database connection established successfully")
            return True
        except Exception as e:
            self.logger.error(f"Error connecting to database: {str(e)}")
            return False

    async def disconnect(self) -> bool:
        """
        Отключение от базы данных.

        Returns:
            bool: Успех или неудача
        """
        try:
            await self.database.disconnect()
            self.logger.info("Database connection closed successfully")
            return True
        except Exception as e:
            self.logger.error(f"Error disconnecting from database: {str(e)}")
            return False

    async def init_db(self) -> bool:
        """
        Инициализация базы данных для сервиса.

        Эта функция:
        1. Проверяет подключение к базе данных
        2. Создает все таблицы
        3. Загружает начальные данные, если они предоставлены

        Returns:
            bool: Успех или неудача
        """
        try:
            # Проверяем подключение через databases, которое уже работает
            self.logger.info("Testing database connection...")
            query = text("SELECT 1 as test")
            await self.database.connect()
            result = await self.database.fetch_one(query)
            if result and result['test'] == 1:
                self.logger.info("Database connection test successful")
            else:
                self.logger.error("Database connection test failed")
                return False

            # Импортируем модели и создаем таблицы
            self.logger.info("Preparing to create tables...")

            # Удаляем и пересоздаем таблицы в режиме разработки
            if self.settings.ENV == "development":
                self.logger.info("Development mode: dropping and recreating all tables")

                # Используем DDL запросы для управления таблицами, вместо SQLAlchemy ORM
                await self.database.execute(text("DROP SCHEMA public CASCADE;"))
                await self.database.execute(text("CREATE SCHEMA public;"))

                # Восстанавливаем стандартные права
                await self.database.execute(text(f"GRANT ALL ON SCHEMA public TO {self.settings.POSTGRES_USER};"))
                await self.database.execute(text("GRANT ALL ON SCHEMA public TO public;"))

                self.logger.info("Schema reset successfully")

            # Создаем таблицы используя синхронный SQLAlchemy engine
            if self.sync_engine:
                # Запускаем создание таблиц в отдельном потоке, чтобы не блокировать асинхронный код
                def create_tables():
                    try:
                        Base.metadata.create_all(bind=self.sync_engine)
                        return True
                    except Exception as e:
                        self.logger.error(f"Error creating tables: {e}")
                        return False

                # Ждем завершения создания таблиц
                tables_created = await asyncio.get_event_loop().run_in_executor(None, create_tables)

                if tables_created:
                    self.logger.info("Tables created successfully using sync engine")
                else:
                    self.logger.error("Failed to create tables")
                    return False

                # Проверяем созданные таблицы
                def get_table_names():
                    inspector = inspect(self.sync_engine)
                    return inspector.get_table_names()

                tables_after = await asyncio.get_event_loop().run_in_executor(None, get_table_names)
                self.logger.info(f"Tables after creation: {tables_after}")
            else:
                self.logger.warning("Sync engine not available, can't create tables using SQLAlchemy ORM")
                return False

            self.logger.info("Database tables created successfully")
            return True

        except OperationalError as e:
            self.logger.error(f"Database connection error: {e}")

            # Дополнительная информация об ошибке
            error_str = str(e)
            self.logger.error(f"Full error details: {error_str!r}")

            # Логируем детали подключения для диагностики
            db_details = (f"Connection attempted with: host={self.settings.POSTGRES_HOST}, "
                          f"port={self.settings.POSTGRES_PORT}, user={self.settings.POSTGRES_USER}, "
                          f"dbname={self.settings.POSTGRES_DB}")
            self.logger.error(f"Connection details: {db_details}")

            # Пытаемся создать пользователя и базу данных, если аутентификация не удалась
            if "password authentication failed" in error_str or "role" in error_str and "does not exist" in error_str:
                self.logger.warning("Authentication problem. Trying to create user...")
                return await self.create_user_and_database()
            return False

        except Exception as e:
            # Логируем детальную информацию об ошибке
            exc_info = traceback.format_exc()
            self.logger.error(f"Unknown error initializing database: {e}")
            self.logger.error(f"Error type: {type(e).__name__}")
            self.logger.error(f"Traceback: {exc_info}")
            return False
        finally:
            # Закрываем соединение databases если оно было открыто
            try:
                await self.database.disconnect()
            except:
                pass

    async def create_user_and_database(self) -> bool:
        """
        Создать пользователя и базу данных PostgreSQL с правами администратора.

        Returns:
            bool: Успех или неудача
        """
        try:
            # Логируем операцию
            self.logger.info(f"Connecting to PostgreSQL as administrator")
            self.logger.info(f"Admin connection params: host={self.settings.POSTGRES_HOST}, "
                             f"port={self.settings.POSTGRES_PORT}, user=postgres")

            # Подключаемся к PostgreSQL как администратор через psycopg2 (синхронно)
            conn = psycopg2.connect(
                host=self.settings.POSTGRES_HOST,
                port=self.settings.POSTGRES_PORT,
                user="postgres",
                password=self.settings.POSTGRES_ADMIN_PASSWORD,
                dbname="postgres",
                client_encoding='utf8'
            )
            conn.autocommit = True
            cursor = conn.cursor()

            # Проверяем кодировку сервера
            cursor.execute("SHOW server_encoding;")
            server_encoding = cursor.fetchone()[0]
            cursor.execute("SHOW client_encoding;")
            client_encoding = cursor.fetchone()[0]
            self.logger.info(f"Admin connection encodings - Server: {server_encoding}, Client: {client_encoding}")

            # Создаем пользователя, если он не существует
            self.logger.info(f"Attempting to create user: {self.settings.POSTGRES_USER}")
            sql_query = f"""
            DO $$
            BEGIN
                IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '{self.settings.POSTGRES_USER}') THEN
                    CREATE USER {self.settings.POSTGRES_USER} WITH PASSWORD '{self.settings.POSTGRES_PASSWORD}';
                ELSE
                    ALTER USER {self.settings.POSTGRES_USER} WITH PASSWORD '{self.settings.POSTGRES_PASSWORD}';
                END IF;
            END $$;
            """
            cursor.execute(sql_query)
            self.logger.info("User created/updated successfully")

            # Создаем базу данных, если она не существует
            self.logger.info(f"Attempting to create database: {self.settings.POSTGRES_DB}")
            sql_query = f"""
            SELECT 'CREATE DATABASE {self.settings.POSTGRES_DB} OWNER {self.settings.POSTGRES_USER} ENCODING ''UTF8'' LC_COLLATE ''C.UTF-8'' LC_CTYPE ''C.UTF-8'' TEMPLATE template0'
            WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '{self.settings.POSTGRES_DB}')
            """
            cursor.execute(sql_query)

            result = cursor.fetchone()
            if result:
                self.logger.info(f"Executing query: {result[0]}")
                cursor.execute(result[0])
                self.logger.info(f"Database {self.settings.POSTGRES_DB} created successfully")
            else:
                self.logger.info(f"Database {self.settings.POSTGRES_DB} already exists")

            # Предоставляем права
            self.logger.info(f"Granting privileges on database to user {self.settings.POSTGRES_USER}")
            cursor.execute(
                f"GRANT ALL PRIVILEGES ON DATABASE {self.settings.POSTGRES_DB} TO {self.settings.POSTGRES_USER}")
            conn.close()

            # Подключаемся к созданной базе данных для настройки прав на схему
            self.logger.info(f"Connecting to the created database {self.settings.POSTGRES_DB} to set privileges")
            conn = psycopg2.connect(
                host=self.settings.POSTGRES_HOST,
                port=self.settings.POSTGRES_PORT,
                user="postgres",
                password=self.settings.POSTGRES_ADMIN_PASSWORD,
                dbname=self.settings.POSTGRES_DB,
                client_encoding='utf8'
            )
            conn.autocommit = True
            cursor = conn.cursor()

            # Предоставляем права на схему
            self.logger.info("Granting schema privileges")
            cursor.execute(f"GRANT ALL PRIVILEGES ON SCHEMA public TO {self.settings.POSTGRES_USER}")
            cursor.execute(f"GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO {self.settings.POSTGRES_USER}")
            cursor.execute(f"GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO {self.settings.POSTGRES_USER}")
            conn.close()

            self.logger.info(
                f"User {self.settings.POSTGRES_USER} and database {self.settings.POSTGRES_DB} created successfully")

            # Если пользователь и база созданы успешно, пробуем создать таблицы
            if self.settings.ENV == "development":
                self.logger.info("Development mode: creating fresh tables")

                # Создаем синхронный движок SQLAlchemy для создания таблиц
                sync_engine = create_engine(
                    self.settings.SQLALCHEMY_DATABASE_URI,
                    echo=False  # Отключаем встроенное логирование SQLAlchemy
                )

                # Используем потоковый пул для создания таблиц в отдельном потоке
                with concurrent.futures.ThreadPoolExecutor() as pool:
                    # Пытаемся создать таблицы используя тот же подход как в init_db
                    self.logger.info("Creating tables after user and database setup")
                    try:
                        # Создаем таблицы
                        def create_tables():
                            try:
                                Base.metadata.create_all(bind=sync_engine)
                                return True
                            except Exception as e:
                                self.logger.error(f"Error creating tables: {e}")
                                return False

                        # Ждем завершения создания таблиц
                        tables_created = await asyncio.get_event_loop().run_in_executor(pool, create_tables)

                        if tables_created:
                            self.logger.info("Tables created successfully")
                        else:
                            self.logger.error("Failed to create tables")
                            # Продолжаем, так как мы уже создали пользователя и базу данных

                    except Exception as table_err:
                        self.logger.error(f"Error creating tables: {table_err}")
                        self.logger.error(f"Table creation traceback: {traceback.format_exc()}")
                        # Продолжаем, так как мы уже создали пользователя и базу данных,
                        # чего может быть достаточно для работы приложения

            return True

        except Exception as ex:
            self.logger.error(f"Failed to create database user: {ex}")
            self.logger.error(f"Traceback: {traceback.format_exc()}")
            return False

    async def reset_db(self) -> bool:
        """
        Полный сброс и пересоздание базы данных.

        Удаляет базу данных и пользователя, затем создает их заново.
        Работает только в режиме разработки.

        Returns:
            bool: True если сброс успешен, False в противном случае.
        """
        # Проверяем режим разработки
        if self.settings.ENV != "development":
            self.logger.error(f"Сброс базы данных доступен только в режиме разработки (ENV=development)")
            self.logger.error(f"Текущий режим: ENV={self.settings.ENV}")
            self.logger.error(f"Измените значение ENV в файле .env для сброса базы данных")
            return False

        self.logger.info(
            f"Начинаю сброс базы данных {self.settings.POSTGRES_DB} (режим разработки: {self.settings.ENV})")

        try:
            # Подключение к базе данных postgres
            conn = psycopg2.connect(
                host=self.settings.POSTGRES_HOST,
                port=self.settings.POSTGRES_PORT,
                user="postgres",  # Используем пользователя postgres для администрирования
                password=self.settings.POSTGRES_ADMIN_PASSWORD,  # Пароль администратора из настроек
                dbname="postgres"
            )
            conn.autocommit = True
            cursor = conn.cursor()

            # Закрываем все активные соединения к базе
            self.logger.info("Закрываем все активные соединения к базе данных")
            sql_query = f"""
            SELECT pg_terminate_backend(pg_stat_activity.pid)
            FROM pg_stat_activity
            WHERE pg_stat_activity.datname = '{self.settings.POSTGRES_DB}'
            AND pid <> pg_backend_pid();
            """
            cursor.execute(sql_query)

            # Удаляем базу данных
            self.logger.info(f"Удаляем базу данных {self.settings.POSTGRES_DB}")
            sql_query = f"""
            DROP DATABASE IF EXISTS {self.settings.POSTGRES_DB};
            """
            cursor.execute(sql_query)

            # Удаляем пользователя
            self.logger.info(f"Удаляем пользователя {self.settings.POSTGRES_USER}")
            sql_query = f"""
            DROP ROLE IF EXISTS {self.settings.POSTGRES_USER};
            """
            cursor.execute(sql_query)

            # Создаем пользователя заново
            self.logger.info(f"Создаем пользователя {self.settings.POSTGRES_USER}")
            sql_query = f"""
            CREATE USER {self.settings.POSTGRES_USER} WITH PASSWORD '{self.settings.POSTGRES_PASSWORD}';
            """
            cursor.execute(sql_query)

            # Создаем базу данных
            self.logger.info(f"Создаем базу данных {self.settings.POSTGRES_DB}")
            sql_query = f"""
            CREATE DATABASE {self.settings.POSTGRES_DB} OWNER {self.settings.POSTGRES_USER};
            """
            cursor.execute(sql_query)

            # Предоставляем права пользователю
            self.logger.info(f"Предоставляем права пользователю {self.settings.POSTGRES_USER}")
            cursor.execute(
                f"GRANT ALL PRIVILEGES ON DATABASE {self.settings.POSTGRES_DB} TO {self.settings.POSTGRES_USER}")

            conn.close()

            # Подключение к созданной базе данных для настройки прав на схему
            conn = psycopg2.connect(
                host=self.settings.POSTGRES_HOST,
                port=self.settings.POSTGRES_PORT,
                user="postgres",
                password=self.settings.POSTGRES_ADMIN_PASSWORD,
                dbname=self.settings.POSTGRES_DB
            )
            conn.autocommit = True
            cursor = conn.cursor()

            # Предоставление прав на схему public
            cursor.execute(f"GRANT ALL PRIVILEGES ON SCHEMA public TO {self.settings.POSTGRES_USER}")
            cursor.execute(f"GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO {self.settings.POSTGRES_USER}")
            cursor.execute(f"GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO {self.settings.POSTGRES_USER}")

            conn.close()

            self.logger.info(f"База данных {self.settings.POSTGRES_DB} успешно сброшена и пересоздана")

            # После сброса базы, инициализируем ее
            init_result = await self.init_db()
            if init_result:
                self.logger.info("База данных успешно инициализирована после сброса")
            else:
                self.logger.error("Ошибка при инициализации базы данных после сброса")
                return False

            return True

        except Exception as e:
            self.logger.error(f"Ошибка при сбросе базы данных: {str(e)}")
            import traceback
            self.logger.error(traceback.format_exc())
            return False


# Создаем экземпляр менеджера базы данных
db_manager = AsyncDatabaseManager()

# Экспортируем полезные объекты
database = db_manager.database
get_async_session = db_manager.get_db
init_db = db_manager.init_db
reset_db = db_manager.reset_db


async def check_db_connection(db: AsyncSession) -> bool:
    """
    Check database connection by running a simple query.

    Args:
        db: Database session

    Returns:
        True if database connection is working, False otherwise
    """
    try:
        # Execute a simple query to check connection
        result = await db.execute(text("SELECT 1"))
        return result.scalar() == 1
    except Exception:
        return False
