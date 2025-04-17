"""
CRUD modules initialization
"""
from app.crud.course import course_crud
from app.crud.localization import localization_crud
from app.crud.technology_tree import technology_tree_crud

__all__ = ["course_crud", "technology_tree_crud", "localization_crud"]
