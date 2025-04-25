"""
Base database functionality for all CompEduX services.

This module provides common database functionality that can be reused across
all microservices, including:
- Base engine and session creation
- Database initialization utilities
- Common database operation patterns
"""
import traceback
from contextlib import asynccontextmanager
from typing import AsyncGenerator, Dict, Any, Optional, Callable, List
from urllib.parse import urlparse, parse_qs, urlunparse, urlencode

import psycopg2
from sqlalchemy import inspect, text
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlalchemy.ext.declarative import declarative_base

from common.logger import get_logger

# Create base logger
logger = get_logger("common.db")

# Create declarative base model
Base = declarative_base()


class AsyncDatabaseManager:
    """
    Manager for async database operations.
    Provides unified interface for database connections.
    """

    def __init__(self, settings, service_name: str, required_tables: List[str] = None):
        """
        Initialize database manager.

        Args:
            settings: Application settings with SQLALCHEMY_DATABASE_URI
            service_name: Name of the service for logging
            required_tables: List of table names that must exist
        """
        self.settings = settings
        self.service_name = service_name
        self.logger = get_logger(f"{service_name}.db")
        self.required_tables = required_tables or []

        # Get DB_ECHO from settings or use default value (False)
        db_echo = getattr(settings, "DB_ECHO", False)

        # Ensure we're using the asyncpg driver for async operations
        db_url = settings.SQLALCHEMY_DATABASE_URI

        # Parse URL to clean parameters not supported by asyncpg
        parsed_url = urlparse(db_url)
        query_params = parse_qs(parsed_url.query)

        # Remove client_encoding from parameters
        if 'client_encoding' in query_params:
            del query_params['client_encoding']

        # Rebuild URL without the client_encoding parameter
        new_query = urlencode(query_params, doseq=True)
        parsed_url = parsed_url._replace(query=new_query)
        clean_db_url = urlunparse(parsed_url)

        # Replace postgresql:// with postgresql+asyncpg://
        if clean_db_url.startswith('postgresql://') and '+asyncpg' not in clean_db_url:
            clean_db_url = clean_db_url.replace('postgresql://', 'postgresql+asyncpg://')
            self.logger.info(f"Changed database URL to use asyncpg driver: {clean_db_url}")

        # Create async engine
        self.engine = create_async_engine(
            clean_db_url,
            echo=db_echo,
            pool_pre_ping=True,
            pool_recycle=3600,
        )

        # Create session factory
        self.AsyncSessionLocal = async_sessionmaker(
            autocommit=False,
            autoflush=False,
            bind=self.engine,
            expire_on_commit=False
        )

    @asynccontextmanager
    async def get_db(self) -> AsyncGenerator[AsyncSession, None]:
        """
        Get database session as async context manager

        Yields:
            AsyncSession: SQLAlchemy async session
        """
        session = self.AsyncSessionLocal()
        try:
            yield session
            await session.commit()
        except Exception as e:
            await session.rollback()
            self.logger.error(f"Session error: {str(e)}")
            raise
        finally:
            await session.close()

    async def test_connection(self) -> bool:
        """
        Test database connection

        Returns:
            bool: True if connection successful
        """
        try:
            async with self.engine.begin() as conn:
                await conn.execute(text("SELECT 1"))
            return True
        except Exception as e:
            self.logger.error(f"Database connection test failed: {str(e)}")
            return False

    async def init_db(self, init_data_func: Optional[Callable[[AsyncSession], Any]] = None) -> bool:
        """
        Initialize the database with tables and optional initial data

        Args:
            init_data_func: Optional function to initialize data after tables created

        Returns:
            bool: True if initialization successful
        """
        try:
            self.logger.info(f"Initializing database for {self.service_name}...")

            # Test connection
            async with self.engine.begin() as conn:
                await conn.execute(text("SELECT 1"))
            self.logger.info("Database connection test successful")

            # For inspection, we need to use run_sync with a connection
            async with self.engine.begin() as conn:
                # Use run_sync to perform inspection in a sync context
                def inspect_tables(connection):
                    inspector = inspect(connection)
                    return inspector.get_table_names()

                existing_tables = await conn.run_sync(inspect_tables)

                # Check if tables need to be created
                if not self.required_tables or not all(table in existing_tables for table in self.required_tables):
                    # Create tables if they don't exist
                    await conn.run_sync(Base.metadata.create_all)
                    self.logger.info("Database tables created successfully")
                else:
                    self.logger.info("All required tables already exist")

            # Initialize data if function provided
            if init_data_func:
                self.logger.info("Initializing test data...")
                async with self.AsyncSessionLocal() as session:
                    await init_data_func(session)
                self.logger.info("Test data initialized")

            return True
        except Exception as e:
            self.logger.error(f"Unknown error initializing database: {str(e)}")
            self.logger.error(f"Error type: {type(e).__name__}")
            self.logger.error(f"Traceback: {traceback.format_exc()}")
            return False

    async def _create_user_and_database(self) -> bool:
        """
        Create PostgreSQL user and database if they don't exist.
        This requires a connection with superuser privileges.

        Returns:
            bool: True if successful, False otherwise
        """
        self.logger.info("Attempting to create database user and database...")

        # Connect to default postgres database as superuser
        try:
            # Use psycopg2 for admin operations
            conn = psycopg2.connect(
                host=self.settings.POSTGRES_HOST,
                port=self.settings.POSTGRES_PORT,
                user="postgres",  # Superuser
                password=self.settings.POSTGRES_SUPERUSER_PASSWORD,
                dbname="postgres"  # Default database
            )
            conn.autocommit = True
            cursor = conn.cursor()

            # Create user if not exists
            cursor.execute(f"SELECT 1 FROM pg_roles WHERE rolname = '{self.settings.POSTGRES_USER}'")
            if not cursor.fetchone():
                self.logger.info(f"Creating database user: {self.settings.POSTGRES_USER}")
                cursor.execute(
                    f"CREATE USER {self.settings.POSTGRES_USER} WITH PASSWORD '{self.settings.POSTGRES_PASSWORD}'"
                )
                self.logger.info(f"User {self.settings.POSTGRES_USER} created successfully")
            else:
                self.logger.info(f"User {self.settings.POSTGRES_USER} already exists")

            # Create database if not exists
            cursor.execute(f"SELECT 1 FROM pg_database WHERE datname = '{self.settings.POSTGRES_DB}'")
            if not cursor.fetchone():
                self.logger.info(f"Creating database: {self.settings.POSTGRES_DB}")
                cursor.execute(f"CREATE DATABASE {self.settings.POSTGRES_DB} OWNER {self.settings.POSTGRES_USER}")
                self.logger.info(f"Database {self.settings.POSTGRES_DB} created successfully")
            else:
                self.logger.info(f"Database {self.settings.POSTGRES_DB} already exists")

            # Grant privileges
            cursor.execute(
                f"GRANT ALL PRIVILEGES ON DATABASE {self.settings.POSTGRES_DB} TO {self.settings.POSTGRES_USER}"
            )
            self.logger.info(f"Granted privileges to {self.settings.POSTGRES_USER}")

            cursor.close()
            conn.close()

            # Try to initialize again
            self.logger.info("Retrying database initialization...")
            return await self.init_db()

        except Exception as e:
            self.logger.error(f"Failed to create user and database: {str(e)}")
            self.logger.error(traceback.format_exc())
            return False


# Utility functions
def table_exists(engine, table_name: str) -> bool:
    """Check if a table exists in the database"""
    inspector = inspect(engine)
    return table_name in inspector.get_table_names()


def get_db_info(engine) -> Dict[str, Any]:
    """Get database information such as tables, schema, etc."""
    inspector = inspect(engine)
    tables = inspector.get_table_names()

    result = {
        "tables": {},
        "dialect": engine.dialect.name,
        "driver": engine.dialect.driver,
    }

    for table in tables:
        columns = []
        for column in inspector.get_columns(table):
            columns.append({
                "name": column["name"],
                "type": str(column["type"]),
                "nullable": column["nullable"]
            })

        result["tables"][table] = {
            "columns": columns,
            "primary_key": inspector.get_pk_constraint(table).get("constrained_columns", []),
            "foreign_keys": inspector.get_foreign_keys(table),
        }

    return result
