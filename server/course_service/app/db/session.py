"""
SQLAlchemy session configuration for Course Service
"""
from common.db import DatabaseManager
from app.core.config import settings

# Create database manager instance
db_manager = DatabaseManager(settings, "course_service")

# Get engine and SessionLocal from manager
engine = db_manager.engine
SessionLocal = db_manager.SessionLocal

# Create a database dependency
get_db = db_manager.get_db
