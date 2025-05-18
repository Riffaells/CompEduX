"""
Base module for database compatibility.
This file exists to maintain backward compatibility with code that imports Base from app.db.base.
For new code, import Base directly from app.models.base.
"""
from app.models.base import Base  # noqa

__all__ = ["Base"]
