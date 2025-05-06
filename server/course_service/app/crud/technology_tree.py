"""
CRUD operations for technology tree model
"""
import uuid
from typing import Dict, Any, Optional, Tuple, List
from uuid import UUID

from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session

from common.logger import get_logger
from ..models.course import Course
from ..models.technology_tree import TechnologyTree
from ..schemas.technology_tree import TechnologyTreeCreate, TechnologyTreeUpdate

# Создаем логгер для этого модуля
logger = get_logger("course_service.crud.technology_tree")


class TechnologyTreeCRUD:
    """CRUD operations for technology tree model"""

    def get(self, db: Session, id: UUID) -> Optional[TechnologyTree]:
        """
        Get technology tree by id

        Args:
            db: Database session
            id: UUID of the technology tree

        Returns:
            TechnologyTree object if found, None otherwise
        """
        return db.query(TechnologyTree).filter(TechnologyTree.id == id).first()

    def get_by_course_id(self, db: Session, course_id: UUID) -> Optional[TechnologyTree]:
        """
        Get technology tree for a specific course

        Args:
            db: Database session
            course_id: UUID of the course

        Returns:
            TechnologyTree object if found, None otherwise
        """
        return db.query(TechnologyTree).filter(TechnologyTree.course_id == course_id).first()

    def create(self, db: Session, obj_in: TechnologyTreeCreate) -> TechnologyTree:
        """
        Create a new technology tree

        Args:
            db: Database session
            obj_in: Technology tree create schema with course_id and data

        Returns:
            Created TechnologyTree object

        Raises:
            ValueError: If course does not exist or already has a technology tree
            SQLAlchemyError: On database error
        """
        try:
            # Check if the course exists
            course = db.query(Course).filter(Course.id == obj_in.course_id).first()
            if not course:
                raise ValueError(f"Course with id {obj_in.course_id} does not exist")

            # Check if the course already has a technology tree
            existing = self.get_by_course_id(db, obj_in.course_id)
            if existing:
                raise ValueError(f"Course with id {obj_in.course_id} already has a technology tree")

            # Convert to dict and create model
            obj_data = obj_in.dict()
            db_obj = TechnologyTree(**obj_data)

            db.add(db_obj)
            db.commit()
            db.refresh(db_obj)
            logger.info(f"Created technology tree for course {obj_in.course_id}")
            return db_obj

        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"Database error creating technology tree: {str(e)}")
            raise
        except Exception as e:
            db.rollback()
            logger.error(f"Error creating technology tree: {str(e)}")
            raise

    def update(self, db: Session, db_obj: TechnologyTree, obj_in: TechnologyTreeUpdate | Dict[str, Any]) -> TechnologyTree:
        """
        Update technology tree

        Args:
            db: Database session
            db_obj: Existing TechnologyTree object
            obj_in: Technology tree update schema or dict with update data

        Returns:
            Updated TechnologyTree object

        Raises:
            SQLAlchemyError: On database error
        """
        try:
            update_data = obj_in if isinstance(obj_in, dict) else obj_in.dict(exclude_unset=True)

            for field, value in update_data.items():
                if value is not None:
                    setattr(db_obj, field, value)

            db.add(db_obj)
            db.commit()
            db.refresh(db_obj)
            logger.info(f"Updated technology tree {db_obj.id}")
            return db_obj

        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"Database error updating technology tree: {str(e)}")
            raise
        except Exception as e:
            db.rollback()
            logger.error(f"Error updating technology tree: {str(e)}")
            raise

    def remove(self, db: Session, id: UUID) -> bool:
        """
        Delete technology tree

        Args:
            db: Database session
            id: UUID of the technology tree to delete

        Returns:
            True if deleted successfully, False if not found

        Raises:
            SQLAlchemyError: On database error
        """
        try:
            db_obj = self.get(db, id)
            if not db_obj:
                return False

            db.delete(db_obj)
            db.commit()
            logger.info(f"Deleted technology tree {id}")
            return True

        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"Database error deleting technology tree: {str(e)}")
            raise
        except Exception as e:
            db.rollback()
            logger.error(f"Error deleting technology tree: {str(e)}")
            raise

    def update_tree_data(self, db: Session, tree_id: UUID, data: Dict[str, Any]) -> Optional[TechnologyTree]:
        """
        Update just the data portion of a technology tree

        Args:
            db: Database session
            tree_id: UUID of the technology tree
            data: New tree data

        Returns:
            Updated TechnologyTree object, or None if not found

        Raises:
            SQLAlchemyError: On database error
        """
        try:
            db_obj = self.get(db, tree_id)
            if not db_obj:
                return None

            # Increment version
            version = db_obj.version + 1 if hasattr(db_obj, "version") else 1

            # Update tree data and version
            db_obj.data = data
            db_obj.version = version

            db.add(db_obj)
            db.commit()
            db.refresh(db_obj)
            logger.info(f"Updated technology tree data for tree {tree_id}")
            return db_obj

        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"Database error updating technology tree data: {str(e)}")
            raise
        except Exception as e:
            db.rollback()
            logger.error(f"Error updating technology tree data: {str(e)}")
            raise

    def get_node(self, db: Session, tree_id: UUID, node_id: str) -> Optional[Dict[str, Any]]:
        """
        Get a specific node from a technology tree

        Args:
            db: Database session
            tree_id: UUID of the technology tree
            node_id: ID of the node to retrieve

        Returns:
            Node data if found, None otherwise
        """
        db_obj = self.get(db, tree_id)
        if not db_obj or not db_obj.data or "nodes" not in db_obj.data:
            return None

        return db_obj.data["nodes"].get(node_id)

    def add_node(self, db: Session, tree_id: UUID, node_id: str, node_data: Dict[str, Any]) -> Optional[TechnologyTree]:
        """
        Add a node to a technology tree

        Args:
            db: Database session
            tree_id: UUID of the technology tree
            node_id: ID for the new node
            node_data: Node data to add

        Returns:
            Updated TechnologyTree object, or None if tree not found

        Raises:
            ValueError: If node_id already exists
            SQLAlchemyError: On database error
        """
        try:
            db_obj = self.get(db, tree_id)
            if not db_obj:
                return None

            # Check if node already exists
            if db_obj.data and "nodes" in db_obj.data and node_id in db_obj.data["nodes"]:
                raise ValueError(f"Node with ID {node_id} already exists in tree {tree_id}")

            # Add node to tree
            db_obj.add_node(node_id, node_data)

            # Save changes
            db.add(db_obj)
            db.commit()
            db.refresh(db_obj)
            logger.info(f"Added node {node_id} to technology tree {tree_id}")
            return db_obj

        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"Database error adding node to technology tree: {str(e)}")
            raise
        except Exception as e:
            db.rollback()
            logger.error(f"Error adding node to technology tree: {str(e)}")
            raise

    def update_node(self, db: Session, tree_id: UUID, node_id: str, node_data: Dict[str, Any]) -> Optional[TechnologyTree]:
        """
        Update a node in a technology tree

        Args:
            db: Database session
            tree_id: UUID of the technology tree
            node_id: ID of the node to update
            node_data: New node data

        Returns:
            Updated TechnologyTree object, or None if tree or node not found

        Raises:
            SQLAlchemyError: On database error
        """
        try:
            db_obj = self.get(db, tree_id)
            if not db_obj or not db_obj.data or "nodes" not in db_obj.data or node_id not in db_obj.data["nodes"]:
                return None

            # Update node
            existing_node = db_obj.data["nodes"][node_id]
            updated_node = {**existing_node, **node_data}
            db_obj.data["nodes"][node_id] = updated_node

            # Update version
            db_obj.version += 1

            # Save changes
            db.add(db_obj)
            db.commit()
            db.refresh(db_obj)
            logger.info(f"Updated node {node_id} in technology tree {tree_id}")
            return db_obj

        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"Database error updating node in technology tree: {str(e)}")
            raise
        except Exception as e:
            db.rollback()
            logger.error(f"Error updating node in technology tree: {str(e)}")
            raise

    def remove_node(self, db: Session, tree_id: UUID, node_id: str) -> Optional[TechnologyTree]:
        """
        Remove a node from a technology tree

        Args:
            db: Database session
            tree_id: UUID of the technology tree
            node_id: ID of the node to remove

        Returns:
            Updated TechnologyTree object, or None if tree not found or node not removed

        Raises:
            SQLAlchemyError: On database error
        """
        try:
            db_obj = self.get(db, tree_id)
            if not db_obj:
                return None

            # Remove node
            if not db_obj.remove_node(node_id):
                return None

            # Save changes
            db.add(db_obj)
            db.commit()
            db.refresh(db_obj)
            logger.info(f"Removed node {node_id} from technology tree {tree_id}")
            return db_obj

        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"Database error removing node from technology tree: {str(e)}")
            raise
        except Exception as e:
            db.rollback()
            logger.error(f"Error removing node from technology tree: {str(e)}")
            raise

    def set_publish_status(self, db: Session, tree_id: UUID, is_published: bool) -> Optional[TechnologyTree]:
        """
        Set the publish status of a technology tree

        Args:
            db: Database session
            tree_id: UUID of the technology tree
            is_published: Whether the tree is published

        Returns:
            Updated TechnologyTree object, or None if not found

        Raises:
            SQLAlchemyError: On database error
        """
        try:
            db_obj = self.get(db, tree_id)
            if not db_obj:
                return None

            # Update publish status
            db_obj.is_published = is_published

            # Save changes
            db.add(db_obj)
            db.commit()
            db.refresh(db_obj)
            logger.info(f"Set publish status to {is_published} for technology tree {tree_id}")
            return db_obj

        except SQLAlchemyError as e:
            db.rollback()
            logger.error(f"Database error setting publish status for technology tree: {str(e)}")
            raise
        except Exception as e:
            db.rollback()
            logger.error(f"Error setting publish status for technology tree: {str(e)}")
            raise


# Create a singleton instance
technology_tree_crud = TechnologyTreeCRUD()
