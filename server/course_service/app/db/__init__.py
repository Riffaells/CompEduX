"""
Database package initialization
"""
# Импортируем Base
from app.models.base import Base  # noqa

# Импортируем все модели, чтобы они были корректно зарегистрированы
from app.models import Course, Tag, TagTranslation, Localization, TechnologyTree

# Экспортируем нужные компоненты
from app.db.session import get_db, SessionLocal, engine  # noqa
from app.db.init_db import init_db
from app.db.test_data import create_test_courses, reset_database

__all__ = [
    "Base",
    "init_db",
    "SessionLocal",
    "engine",
    "create_test_courses",
    "reset_database"
]
