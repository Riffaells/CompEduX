#!/usr/bin/env python
"""
Database initialization script for CompEduX services.
This script creates necessary users, databases, and schemas for all services.
"""

import os
import sys
from pathlib import Path

# Add the parent directory to sys.path to import common modules
sys.path.append(str(Path(__file__).parent.parent.parent))

from common.db import DatabaseManager
from dotenv import load_dotenv

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s",
)
logger = logging.getLogger("db_init")


def init_service_db(service_name, username, password, db_name):
    """Initialize database for a specific service"""
    logger.info(f"Initializing database for {service_name} service")

    # Create database manager with admin credentials
    db_manager = DatabaseManager(
        host=os.getenv("POSTGRES_HOST", "localhost"),
        port=int(os.getenv("POSTGRES_PORT", 5432)),
        user=os.getenv("POSTGRES_ADMIN_USER", "postgres"),
        password=os.getenv("POSTGRES_ADMIN_PASSWORD", "secure_password"),
        database="postgres",  # Connect to default postgres database as admin
    )

    # Create service user and database
    db_manager._create_user_and_database(username, password, db_name)
    logger.info(f"Created user '{username}' and database '{db_name}'")

    # Close admin connection
    db_manager.engine.dispose()
    logger.info(f"Disposed admin connection")

    # Initialize service database schema
    service_db_manager = DatabaseManager(
        host=os.getenv("POSTGRES_HOST", "localhost"),
        port=int(os.getenv("POSTGRES_PORT", 5432)),
        user=username,
        password=password,
        database=db_name,
    )

    # Test connection with service credentials
    if service_db_manager.test_connection():
        logger.info(f"Successfully connected to {db_name} database with service user")
    else:
        logger.error(f"Failed to connect to {db_name} database with service user")
        return False

    logger.info(f"Completed {service_name} service database initialization")
    return True


def main():
    """Initialize all service databases"""
    # Load environment variables
    env_path = Path(__file__).parent / ".env"
    load_dotenv(dotenv_path=env_path)

    logger.info("Starting database initialization process for all services")

    # Initialize auth service database
    auth_success = init_service_db(
        "auth",
        os.getenv("AUTH_SERVICE_USER"),
        os.getenv("AUTH_SERVICE_PASSWORD"),
        os.getenv("AUTH_SERVICE_DB"),
    )

    # Initialize course service database
    course_success = init_service_db(
        "course",
        os.getenv("COURSE_SERVICE_USER"),
        os.getenv("COURSE_SERVICE_PASSWORD"),
        os.getenv("COURSE_SERVICE_DB"),
    )

    if auth_success and course_success:
        logger.info("All service databases initialized successfully")
        return 0
    else:
        logger.error("Failed to initialize one or more service databases")
        return 1


if __name__ == "__main__":
    sys.exit(main())
