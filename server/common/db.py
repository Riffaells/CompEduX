"""
Base database functionality for all CompEduX services.

This module provides common database functionality that can be reused across
all microservices, including:
- Base engine and session creation
- Database initialization utilities
- Common database operation patterns
"""
import sys
import traceback
import logging
from contextlib import contextmanager
from typing import Generator, Dict, Any, Optional, Callable
from urllib.parse import quote_plus, unquote

import psycopg2
from sqlalchemy import create_engine, text, inspect, MetaData
from sqlalchemy.exc import OperationalError
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session

from common.logger import get_logger

# Create base logger
logger = get_logger("common.db")

# Create declarative base model
Base = declarative_base()


class DatabaseManager:
    """
    Base database manager for SQLAlchemy integration.

    This class handles common database operations like:
    - Creating engine and session
    - Connection testing
    - Schema initialization
    - Database and user creation (if needed)
    """

    def __init__(self, settings, service_name: str):
        """
        Initialize database manager with service settings.

        Args:
            settings: Service configuration with database settings
            service_name: Name of the service for logging
        """
        self.settings = settings
        self.service_name = service_name
        self.logger = get_logger(f"{service_name}.db")

        # Log database connection info (masking password)
        connection_info = f"PostgreSQL connection info: host={settings.POSTGRES_HOST}, port={settings.POSTGRES_PORT}, " \
                         f"user={settings.POSTGRES_USER}, db={settings.POSTGRES_DB}, python={sys.version}"
        self.logger.info(connection_info)

        # Examine URI for any potential issues
        safe_uri = settings.SQLALCHEMY_DATABASE_URI.replace(settings.POSTGRES_PASSWORD, "****")
        self.logger.info(f"Database URI: {safe_uri}")

        # Manually construct URI with proper escaping to avoid encoding issues
        user = quote_plus(settings.POSTGRES_USER)
        password = quote_plus(settings.POSTGRES_PASSWORD)
        host = quote_plus(settings.POSTGRES_HOST)
        port = settings.POSTGRES_PORT
        db_name = quote_plus(settings.POSTGRES_DB)

        # Ensure proper escaping for special characters
        safe_uri_constructed = f"postgresql://{user}:****@{host}:{port}/{db_name}"
        self.logger.info(f"Constructed URI (safe): {safe_uri_constructed}")

        # Log encoding info
        self.logger.info(f"Default encoding: {sys.getdefaultencoding()}, "
                         f"Filesystem encoding: {sys.getfilesystemencoding()}")

        # Print encoding of each individual component for debugging
        self.logger.debug(f"User encoding: {settings.POSTGRES_USER!r}")
        self.logger.debug(f"Host encoding: {settings.POSTGRES_HOST!r}")
        self.logger.debug(f"DB name encoding: {settings.POSTGRES_DB!r}")

        # Create connection arguments with explicit encoding settings
        connect_args = {
            "options": "-c client_encoding=utf8",
            "client_encoding": "utf8"
        }
        self.logger.info(f"Connection arguments: {connect_args}")

        # Create engine with UTF-8 encoding settings to prevent encoding issues
        try:
            self.engine = create_engine(
                settings.SQLALCHEMY_DATABASE_URI,
                pool_pre_ping=True,
                connect_args=connect_args,
                echo=settings.DEBUG  # Enable SQLAlchemy logging in debug mode
            )
            self.logger.info("SQLAlchemy engine created successfully")
        except Exception as e:
            self.logger.error(f"Error creating SQLAlchemy engine: {str(e)}")
            self.logger.error(f"Engine traceback: {traceback.format_exc()}")
            # Re-raise to prevent initialization with a broken engine
            raise

        # Create session factory
        self.SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=self.engine)

    @contextmanager
    def get_db(self) -> Generator[Session, None, None]:
        """
        Dependency for obtaining a database session.

        Yields:
            Session: SQLAlchemy session for database operations
        """
        db = self.SessionLocal()
        try:
            yield db
        finally:
            db.close()

    def init_db(self, create_tables_func: Optional[Callable[[Session], None]] = None) -> bool:
        """
        Initialize database for the service.

        This function:
        1. Checks database connection
        2. Creates all tables
        3. Loads initial data if provided

        Args:
            create_tables_func: Optional function to create test/initial data

        Returns:
            bool: Success or failure
        """
        try:
            # Test direct psycopg2 connection first for detailed diagnostics
            self.logger.info("Testing direct psycopg2 connection...")
            try:
                # Try direct connection with psycopg2 first
                conn = psycopg2.connect(
                    host=self.settings.POSTGRES_HOST,
                    port=self.settings.POSTGRES_PORT,
                    user=self.settings.POSTGRES_USER,
                    password=self.settings.POSTGRES_PASSWORD,
                    dbname=self.settings.POSTGRES_DB,
                    client_encoding='utf8'
                )
                self.logger.info("Direct psycopg2 connection successful!")

                # Check server encoding
                cursor = conn.cursor()
                cursor.execute("SHOW server_encoding;")
                server_encoding = cursor.fetchone()[0]
                cursor.execute("SHOW client_encoding;")
                client_encoding = cursor.fetchone()[0]
                self.logger.info(f"PostgreSQL encodings - Server: {server_encoding}, Client: {client_encoding}")

                # Close direct connection
                cursor.close()
                conn.close()
            except Exception as e:
                self.logger.error(f"Direct psycopg2 connection failed: {str(e)}")
                self.logger.error(f"Connection traceback: {traceback.format_exc()}")
                # Continue with SQLAlchemy connection attempt

            # Check database connection using SQLAlchemy
            self.logger.info("Testing SQLAlchemy connection...")
            with self.engine.connect() as conn:
                result = conn.execute(text("SELECT 1"))
                value = result.scalar()
                self.logger.info(f"SQLAlchemy connection test result: {value}")
            self.logger.info("Database connection established successfully")

            # Import models and create tables
            self.logger.info("Preparing to create tables...")

            # Information about detected models
            inspector = inspect(self.engine)
            existing_tables = inspector.get_table_names()
            self.logger.info(f"Existing tables before creation: {existing_tables}")

            # Drop and recreate tables in development mode
            if self.settings.ENV == "development":
                self.logger.info("Development mode: dropping and recreating all tables")
                Base.metadata.drop_all(bind=self.engine)
                self.logger.info("Tables dropped successfully")

            # Create tables
            self.logger.info("Creating tables with Base.metadata.create_all...")
            Base.metadata.create_all(bind=self.engine)

            # Log tables after creation
            inspector = inspect(self.engine)
            tables_after = inspector.get_table_names()
            self.logger.info(f"Tables after creation: {tables_after}")
            self.logger.info("Database tables created successfully")

            # Create test data in development mode
            if create_tables_func and self.settings.ENV == "development":
                db = self.SessionLocal()
                try:
                    self.logger.info("Creating test data for development")
                    create_tables_func(db)
                    self.logger.info("Test data created successfully")
                finally:
                    db.close()

            return True

        except OperationalError as e:
            self.logger.error(f"Database connection error: {e}")

            # Provide more specific error information
            error_str = str(e)
            self.logger.error(f"Full error details: {error_str!r}")  # Use !r to see raw string with escapes

            # Log connection details for diagnosis
            db_details = (f"Connection attempted with: host={self.settings.POSTGRES_HOST}, "
                         f"port={self.settings.POSTGRES_PORT}, user={self.settings.POSTGRES_USER}, "
                         f"dbname={self.settings.POSTGRES_DB}")
            self.logger.error(f"Connection details: {db_details}")

            # Try to create user and database if authentication failed
            if "password authentication failed" in error_str or "role" in error_str and "does not exist" in error_str:
                self.logger.warning(f"Authentication problem. Trying to create user...")
                self.logger.warning(
                    f"Current settings: POSTGRES_USER={self.settings.POSTGRES_USER}, "
                    f"POSTGRES_DB={self.settings.POSTGRES_DB}"
                )

                return self._create_user_and_database()
            return False

        except Exception as e:
            # Log detailed error information
            exc_info = traceback.format_exc()
            self.logger.error(f"Unknown error initializing database: {e}")
            self.logger.error(f"Error type: {type(e).__name__}")
            self.logger.error(f"Traceback: {exc_info}")

            # For encoding errors, log the raw values
            if isinstance(e, UnicodeError):
                self.logger.error(f"Unicode error details: {e.reason}, object: {repr(e.object)}, "
                                 f"start: {e.start}, end: {e.end}")

                # Log all database connection parameters as raw bytes for inspection
                self.logger.error(f"Raw POSTGRES_USER: {repr(self.settings.POSTGRES_USER)}")
                self.logger.error(f"Raw POSTGRES_PASSWORD: {'*' * len(self.settings.POSTGRES_PASSWORD)}")
                self.logger.error(f"Raw POSTGRES_DB: {repr(self.settings.POSTGRES_DB)}")
                self.logger.error(f"Raw POSTGRES_HOST: {repr(self.settings.POSTGRES_HOST)}")
                self.logger.error(f"Raw POSTGRES_PORT: {repr(self.settings.POSTGRES_PORT)}")

                # Attempt to connect with explicitly encoded values
                self.logger.info("Attempting connection with explicitly encoded parameters...")
                try:
                    encoded_uri = (
                        f"postgresql://{self.settings.POSTGRES_USER.encode('ascii', 'replace').decode('ascii')}:"
                        f"{self.settings.POSTGRES_PASSWORD.encode('ascii', 'replace').decode('ascii')}@"
                        f"{self.settings.POSTGRES_HOST.encode('ascii', 'replace').decode('ascii')}:"
                        f"{self.settings.POSTGRES_PORT}/"
                        f"{self.settings.POSTGRES_DB.encode('ascii', 'replace').decode('ascii')}"
                    )
                    self.logger.info(f"Encoded URI (safe): {encoded_uri.replace(self.settings.POSTGRES_PASSWORD.encode('ascii', 'replace').decode('ascii'), '****')}")

                    test_engine = create_engine(
                        encoded_uri,
                        connect_args={"client_encoding": "utf8"}
                    )
                    with test_engine.connect() as conn:
                        conn.execute(text("SELECT 1"))
                    self.logger.info("Connection successful with encoded parameters!")
                except Exception as inner_e:
                    self.logger.error(f"Encoded parameters connection failed: {inner_e}")

            return False

    def _create_user_and_database(self) -> bool:
        """
        Create PostgreSQL user and database as administrator.

        Returns:
            bool: Success or failure
        """
        try:
            # Connect to PostgreSQL as administrator
            self.logger.info(f"Connecting to PostgreSQL as administrator")
            self.logger.info(f"Admin connection params: host={self.settings.POSTGRES_HOST}, "
                           f"port={self.settings.POSTGRES_PORT}, user=postgres")

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

            # Check server encoding
            cursor.execute("SHOW server_encoding;")
            server_encoding = cursor.fetchone()[0]
            cursor.execute("SHOW client_encoding;")
            client_encoding = cursor.fetchone()[0]
            self.logger.info(f"Admin connection encodings - Server: {server_encoding}, Client: {client_encoding}")

            # Create user if not exists
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

            # Create database if not exists
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

            # Grant privileges
            self.logger.info(f"Granting privileges on database to user {self.settings.POSTGRES_USER}")
            cursor.execute(f"GRANT ALL PRIVILEGES ON DATABASE {self.settings.POSTGRES_DB} TO {self.settings.POSTGRES_USER}")
            conn.close()

            # Connect to the created database to set up schema privileges
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

            # Grant schema privileges
            self.logger.info("Granting schema privileges")
            cursor.execute(f"GRANT ALL PRIVILEGES ON SCHEMA public TO {self.settings.POSTGRES_USER}")
            cursor.execute(f"GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO {self.settings.POSTGRES_USER}")
            cursor.execute(f"GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO {self.settings.POSTGRES_USER}")
            conn.close()

            self.logger.info(f"User {self.settings.POSTGRES_USER} and database {self.settings.POSTGRES_DB} created successfully")

            # Try to locate the Base model from the app
            try:
                self.logger.info("Importing app-specific Base model for table creation")
                # Prefer absolute imports when dealing with app models
                try:
                    from app.models.base import Base as AppBase
                    self.logger.info("Successfully imported Base model using absolute import")
                except ImportError:
                    self.logger.warning("Absolute import failed, trying relative import")
                    # Fallback to relative import if absolute fails
                    from ..models.base import Base as AppBase
                    self.logger.info("Successfully imported Base model using relative import")

                # Retry creating tables
                if self.settings.ENV == "development":
                    self.logger.info("Dropping existing tables before recreation")
                    AppBase.metadata.drop_all(bind=self.engine)

                self.logger.info("Creating tables")
                AppBase.metadata.create_all(bind=self.engine)
                self.logger.info("Database tables created successfully after user setup")
            except ImportError as import_err:
                self.logger.error(f"Error importing Base model: {import_err}")
                # Try with regular Base as fallback
                self.logger.info("Attempting to create tables with common Base model as fallback")
                Base.metadata.create_all(bind=self.engine)
                self.logger.info("Tables created with fallback Base model")
            except Exception as table_err:
                self.logger.error(f"Error creating tables: {table_err}")
                self.logger.error(f"Table creation traceback: {traceback.format_exc()}")
                # Continue anyway as we have created the user and database, which might be enough
                # for the application to attempt its own schema creation

            return True

        except Exception as ex:
            self.logger.error(f"Failed to create database user: {ex}")
            self.logger.error(f"Traceback: {traceback.format_exc()}")
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
