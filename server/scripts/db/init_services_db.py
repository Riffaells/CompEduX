#!/usr/bin/env python
"""
Database initialization script for CompEduX services.
This script creates necessary users, databases, and schemas for all services.
"""

import os
import sys
import logging
import psycopg2
from pathlib import Path
from typing import Dict, Any

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger("db_init")


def create_user_and_database(username, password, db_name, host="localhost", port=5432, admin_user="postgres", admin_password="secure_password"):
    """
    Create PostgreSQL user and database if they don't exist.
    This requires a connection with superuser privileges.

    Returns:
        bool: True if successful, False otherwise
    """
    logger.info(f"Attempting to create database user {username} and database {db_name}...")

    # Connect to default postgres database as superuser
    try:
        # Use psycopg2 for admin operations
        conn = psycopg2.connect(
            host=host,
            port=port,
            user=admin_user,
            password=admin_password,
            dbname="postgres"  # Default database
        )
        conn.autocommit = True
        cursor = conn.cursor()

        # Create user if not exists
        cursor.execute(f"SELECT 1 FROM pg_roles WHERE rolname = '{username}'")
        if not cursor.fetchone():
            logger.info(f"Creating database user: {username}")
            cursor.execute(
                f"CREATE USER {username} WITH PASSWORD '{password}'"
            )
            logger.info(f"User {username} created successfully")
        else:
            logger.info(f"User {username} already exists")

        # Create database if not exists
        cursor.execute(f"SELECT 1 FROM pg_database WHERE datname = '{db_name}'")
        if not cursor.fetchone():
            logger.info(f"Creating database: {db_name}")
            cursor.execute(f"CREATE DATABASE {db_name} OWNER {username}")
            logger.info(f"Database {db_name} created successfully")
        else:
            logger.info(f"Database {db_name} already exists")

        # Grant privileges
        cursor.execute(
            f"GRANT ALL PRIVILEGES ON DATABASE {db_name} TO {username}"
        )
        logger.info(f"Granted privileges to {username}")

        cursor.close()
        conn.close()
        return True

    except Exception as e:
        logger.error(f"Failed to create user and database: {str(e)}")
        import traceback
        logger.error(traceback.format_exc())
        return False


def init_service_db(service_name, username, password, db_name):
    """Initialize database for a specific service"""
    logger.info(f"Initializing database for {service_name} service")

    # Get connection parameters from environment
    host = os.getenv("POSTGRES_HOST", "localhost")
    port = int(os.getenv("POSTGRES_PORT", 5432))
    admin_user = os.getenv("POSTGRES_ADMIN_USER", "postgres")
    admin_password = os.getenv("POSTGRES_ADMIN_PASSWORD", "secure_password")

    # Create database user and database
    db_created = create_user_and_database(
        username=username,
        password=password,
        db_name=db_name,
        host=host,
        port=port,
        admin_user=admin_user,
        admin_password=admin_password
    )
    
    if not db_created:
        logger.error(f"Failed to create database for {service_name} service")
        return False
    
    # Test connection with service credentials
    try:
        conn = psycopg2.connect(
            host=host,
            port=port,
            user=username,
            password=password,
            dbname=db_name
        )
        conn.close()
        logger.info(f"Successfully connected to {db_name} database with service user")
        return True
    except Exception as e:
        logger.error(f"Failed to connect to {db_name} database with service user: {e}")
        return False


def main():
    """Initialize all service databases"""
    # Load environment variables
    try:
        from dotenv import load_dotenv
        env_path = Path(__file__).parent / ".env"
        load_dotenv(dotenv_path=env_path)
        logger.info(f"Loaded environment variables from {env_path}")
    except ImportError:
        logger.warning("python-dotenv not installed. Using environment variables as is.")

    logger.info("Starting database initialization process for all services")

    # Initialize auth service database
    auth_success = init_service_db(
        "auth",
        os.getenv("AUTH_DB_USER"),
        os.getenv("AUTH_DB_PASSWORD"),
        os.getenv("AUTH_DB_NAME"),
    )

    # Initialize course service database
    course_success = init_service_db(
        "course",
        os.getenv("COURSE_DB_USER"),
        os.getenv("COURSE_DB_PASSWORD"),
        os.getenv("COURSE_DB_NAME"),
    )

    # Initialize room service database
    room_success = init_service_db(
        "room",
        os.getenv("ROOM_DB_USER"),
        os.getenv("ROOM_DB_PASSWORD"),
        os.getenv("ROOM_DB_NAME"),
    )

    if auth_success and course_success and room_success:
        logger.info("All service databases initialized successfully")
        return 0
    else:
        logger.error("Failed to initialize one or more service databases")
        return 1


if __name__ == "__main__":
    sys.exit(main())
