"""
Database package initialization

This package provides asynchronous database access using SQLAlchemy 2.0
"""
from app.db.database import get_database, connect_to_db, disconnect_from_db
# Import asynchronous database components
from app.db.init_db import init_db
from app.db.session import get_db, AsyncSessionLocal, engine, db_manager
from app.models.base import Base

__all__ = [
    # Base components
    "Base",
    "init_db",

    # Async SQLAlchemy components
    "AsyncSessionLocal",
    "engine",
    "get_db",
    "db_manager",

    # Additional async components
    "get_database",
    "connect_to_db",
    "disconnect_from_db"
]
