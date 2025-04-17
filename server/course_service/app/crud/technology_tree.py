"""
CRUD operations for technology tree model
"""
import logging
from typing import Dict, List, Any, Optional, Union, Tuple
from uuid import UUID
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session

from app.models.technology_tree import TechnologyTree
from app.models.course import Course

logger = logging.getLogger(__name__)


def get_technology_tree(db: Session, technology_tree_id: UUID) -> Optional[TechnologyTree]:
    """
    Get technology tree by id

    Args:
        db: Database session
        technology_tree_id: UUID of the technology tree

    Returns:
        TechnologyTree object if found, None otherwise
    """
    return db.query(TechnologyTree).filter(TechnologyTree.id == technology_tree_id).first()


def get_technology_tree_by_course(db: Session, course_id: UUID) -> Optional[TechnologyTree]:
    """
    Get technology tree for a specific course

    Args:
        db: Database session
        course_id: UUID of the course

    Returns:
        TechnologyTree object if found, None otherwise
    """
    return db.query(TechnologyTree).filter(TechnologyTree.course_id == course_id).first()


def create_technology_tree(db: Session, course_id: UUID, data: Dict = None) -> TechnologyTree:
    """
    Create a new technology tree for a course

    Args:
        db: Database session
        course_id: UUID of the course
        data: Technology tree data as a dictionary

    Returns:
        Created TechnologyTree object

    Raises:
        ValueError: If course does not exist or already has a technology tree
        SQLAlchemyError: On database error
    """
    try:
        # Check if the course exists
        course = db.query(Course).filter(Course.id == course_id).first()
        if not course:
            raise ValueError(f"Course with id {course_id} does not exist")

        # Check if the course already has a technology tree
        existing = get_technology_tree_by_course(db, course_id)
        if existing:
            raise ValueError(f"Course with id {course_id} already has a technology tree")

        tree_data = data or {}
        technology_tree = TechnologyTree(course_id=course_id, data=tree_data)

        db.add(technology_tree)
        db.commit()
        db.refresh(technology_tree)
        logger.info(f"Created technology tree for course {course_id}")
        return technology_tree

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Database error creating technology tree: {str(e)}")
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"Error creating technology tree: {str(e)}")
        raise


def update_technology_tree(
    db: Session, technology_tree_id: UUID, data: Dict[str, Any]
) -> Optional[TechnologyTree]:
    """
    Update technology tree data

    Args:
        db: Database session
        technology_tree_id: UUID of the technology tree to update
        data: New technology tree data

    Returns:
        Updated TechnologyTree object, or None if not found

    Raises:
        SQLAlchemyError: On database error
    """
    try:
        technology_tree = get_technology_tree(db, technology_tree_id)
        if not technology_tree:
            return None

        technology_tree.data = data

        db.commit()
        db.refresh(technology_tree)
        logger.info(f"Updated technology tree {technology_tree_id}")
        return technology_tree

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Database error updating technology tree: {str(e)}")
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"Error updating technology tree: {str(e)}")
        raise


def delete_technology_tree(db: Session, technology_tree_id: UUID) -> bool:
    """
    Delete technology tree

    Args:
        db: Database session
        technology_tree_id: UUID of the technology tree to delete

    Returns:
        True if deleted successfully, False if not found

    Raises:
        SQLAlchemyError: On database error
    """
    try:
        technology_tree = get_technology_tree(db, technology_tree_id)
        if not technology_tree:
            return False

        db.delete(technology_tree)
        db.commit()
        logger.info(f"Deleted technology tree {technology_tree_id}")
        return True

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Database error deleting technology tree: {str(e)}")
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"Error deleting technology tree: {str(e)}")
        raise


def update_tree_node(
    db: Session, technology_tree_id: UUID, node_id: str, node_data: Dict[str, Any]
) -> Optional[TechnologyTree]:
    """
    Update a specific node in the technology tree

    Args:
        db: Database session
        technology_tree_id: UUID of the technology tree
        node_id: ID of the node to update
        node_data: New node data

    Returns:
        Updated TechnologyTree object, or None if not found

    Raises:
        ValueError: If node not found
        SQLAlchemyError: On database error
    """
    try:
        technology_tree = get_technology_tree(db, technology_tree_id)
        if not technology_tree or not technology_tree.data:
            return None

        # Deep copy the tree data to avoid modifying it directly
        tree_data = dict(technology_tree.data)

        # Find and update the node
        nodes = tree_data.get("nodes", [])
        node_updated = False

        for i, node in enumerate(nodes):
            if node.get("id") == node_id:
                # Update the node with new data, preserving existing fields not in node_data
                nodes[i] = {**node, **node_data}
                node_updated = True
                break

        if not node_updated:
            raise ValueError(f"Node with id {node_id} not found in technology tree {technology_tree_id}")

        # Update nodes list in tree data
        tree_data["nodes"] = nodes

        # Save changes
        technology_tree.data = tree_data
        db.commit()
        db.refresh(technology_tree)
        logger.info(f"Updated node {node_id} in technology tree {technology_tree_id}")
        return technology_tree

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Database error updating tree node: {str(e)}")
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"Error updating tree node: {str(e)}")
        raise


def add_tree_node(
    db: Session, technology_tree_id: UUID, node_data: Dict[str, Any]
) -> Tuple[Optional[TechnologyTree], str]:
    """
    Add a new node to the technology tree

    Args:
        db: Database session
        technology_tree_id: UUID of the technology tree
        node_data: Node data. Must contain at least an 'id' field

    Returns:
        Tuple of (Updated TechnologyTree object or None if not found, ID of the new node)

    Raises:
        ValueError: If node data is invalid or node with same ID already exists
        SQLAlchemyError: On database error
    """
    node_id = node_data.get("id")
    if not node_id:
        # Generate a random ID if not provided
        import uuid
        node_id = f"node_{str(uuid.uuid4())[:8]}"
        node_data["id"] = node_id

    try:
        technology_tree = get_technology_tree(db, technology_tree_id)
        if not technology_tree:
            return None, ""

        # Deep copy the tree data to avoid modifying it directly
        tree_data = dict(technology_tree.data) if technology_tree.data else {"nodes": [], "connections": []}

        # Check if node with same ID already exists
        nodes = tree_data.get("nodes", [])
        for node in nodes:
            if node.get("id") == node_id:
                raise ValueError(f"Node with id {node_id} already exists in technology tree {technology_tree_id}")

        # Add the new node
        nodes.append(node_data)

        # Update nodes list in tree data
        tree_data["nodes"] = nodes

        # Save changes
        technology_tree.data = tree_data
        db.commit()
        db.refresh(technology_tree)
        logger.info(f"Added node {node_id} to technology tree {technology_tree_id}")
        return technology_tree, node_id

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Database error adding tree node: {str(e)}")
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"Error adding tree node: {str(e)}")
        raise


def get_by_course_id(db: Session, course_id: UUID) -> Optional[TechnologyTree]:
    """
    Alias for get_technology_tree_by_course for consistency with other crud modules

    Args:
        db: Database session
        course_id: UUID of the course

    Returns:
        TechnologyTree object if found, None otherwise
    """
    return get_technology_tree_by_course(db, course_id)


def add_tree_connection(
    db: Session, technology_tree_id: UUID, connection_data: Dict[str, Any]
) -> Tuple[Optional[TechnologyTree], str]:
    """
    Add a new connection between nodes in the technology tree

    Args:
        db: Database session
        technology_tree_id: UUID of the technology tree
        connection_data: Connection data with from_node and to_node IDs

    Returns:
        Tuple of (Updated TechnologyTree object or None if not found, ID of the new connection)

    Raises:
        ValueError: If connection data is invalid or nodes don't exist
        SQLAlchemyError: On database error
    """
    connection_id = connection_data.get("id")
    if not connection_id:
        # Generate a random ID if not provided
        import uuid
        connection_id = f"conn_{str(uuid.uuid4())[:8]}"
        connection_data["id"] = connection_id

    # Rename from_node to 'from' if needed for consistency with frontend
    if "from_node" in connection_data and "from" not in connection_data:
        connection_data["from"] = connection_data.pop("from_node")

    # Rename to_node to 'to' if needed for consistency with frontend
    if "to_node" in connection_data and "to" not in connection_data:
        connection_data["to"] = connection_data.pop("to_node")

    # Ensure required fields are present
    if "from" not in connection_data or "to" not in connection_data:
        raise ValueError("Connection must have 'from' and 'to' node IDs")

    try:
        technology_tree = get_technology_tree(db, technology_tree_id)
        if not technology_tree:
            return None, ""

        # Deep copy the tree data to avoid modifying it directly
        tree_data = dict(technology_tree.data) if technology_tree.data else {"nodes": [], "connections": []}

        # Check if nodes exist
        nodes = tree_data.get("nodes", [])
        from_node_exists = False
        to_node_exists = False

        for node in nodes:
            if node.get("id") == connection_data["from"]:
                from_node_exists = True
            if node.get("id") == connection_data["to"]:
                to_node_exists = True

        if not from_node_exists:
            raise ValueError(f"Source node with id {connection_data['from']} does not exist")
        if not to_node_exists:
            raise ValueError(f"Target node with id {connection_data['to']} does not exist")

        # Check if connection with same ID already exists
        connections = tree_data.get("connections", [])
        for connection in connections:
            if connection.get("id") == connection_id:
                raise ValueError(f"Connection with id {connection_id} already exists")

        # Add the new connection
        connections.append(connection_data)

        # Update connections list in tree data
        tree_data["connections"] = connections

        # Save changes
        technology_tree.data = tree_data
        db.commit()
        db.refresh(technology_tree)
        logger.info(f"Added connection {connection_id} to technology tree {technology_tree_id}")
        return technology_tree, connection_id

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Database error adding tree connection: {str(e)}")
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"Error adding tree connection: {str(e)}")
        raise


def update_tree_connection(
    db: Session, technology_tree_id: UUID, connection_id: str, connection_data: Dict[str, Any]
) -> Optional[TechnologyTree]:
    """
    Update a connection in the technology tree

    Args:
        db: Database session
        technology_tree_id: UUID of the technology tree
        connection_id: ID of the connection to update
        connection_data: New connection data

    Returns:
        Updated TechnologyTree object, or None if not found

    Raises:
        ValueError: If connection not found
        SQLAlchemyError: On database error
    """
    try:
        technology_tree = get_technology_tree(db, technology_tree_id)
        if not technology_tree or not technology_tree.data:
            return None

        # Deep copy the tree data to avoid modifying it directly
        tree_data = dict(technology_tree.data)

        # Find and update the connection
        connections = tree_data.get("connections", [])
        connection_updated = False

        for i, connection in enumerate(connections):
            if connection.get("id") == connection_id:
                # Rename from_node to 'from' if needed for consistency with frontend
                if "from_node" in connection_data and "from" not in connection_data:
                    connection_data["from"] = connection_data.pop("from_node")

                # Rename to_node to 'to' if needed for consistency with frontend
                if "to_node" in connection_data and "to" not in connection_data:
                    connection_data["to"] = connection_data.pop("to_node")

                # Update the connection with new data, preserving existing fields not in connection_data
                connections[i] = {**connection, **connection_data}
                connection_updated = True
                break

        if not connection_updated:
            raise ValueError(f"Connection with id {connection_id} not found in technology tree {technology_tree_id}")

        # Update connections list in tree data
        tree_data["connections"] = connections

        # Save changes
        technology_tree.data = tree_data
        db.commit()
        db.refresh(technology_tree)
        logger.info(f"Updated connection {connection_id} in technology tree {technology_tree_id}")
        return technology_tree

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Database error updating tree connection: {str(e)}")
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"Error updating tree connection: {str(e)}")
        raise


def delete_tree_connection(
    db: Session, technology_tree_id: UUID, connection_id: str
) -> Optional[TechnologyTree]:
    """
    Delete a connection from the technology tree

    Args:
        db: Database session
        technology_tree_id: UUID of the technology tree
        connection_id: ID of the connection to delete

    Returns:
        Updated TechnologyTree object, or None if not found

    Raises:
        ValueError: If connection not found
        SQLAlchemyError: On database error
    """
    try:
        technology_tree = get_technology_tree(db, technology_tree_id)
        if not technology_tree or not technology_tree.data:
            return None

        # Deep copy the tree data to avoid modifying it directly
        tree_data = dict(technology_tree.data)

        # Find and remove the connection
        connections = tree_data.get("connections", [])
        original_length = len(connections)

        connections = [conn for conn in connections if conn.get("id") != connection_id]

        if len(connections) == original_length:
            raise ValueError(f"Connection with id {connection_id} not found in technology tree {technology_tree_id}")

        # Update connections list in tree data
        tree_data["connections"] = connections

        # Save changes
        technology_tree.data = tree_data
        db.commit()
        db.refresh(technology_tree)
        logger.info(f"Deleted connection {connection_id} from technology tree {technology_tree_id}")
        return technology_tree

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Database error deleting tree connection: {str(e)}")
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"Error deleting tree connection: {str(e)}")
        raise


def delete_tree_node(
    db: Session, technology_tree_id: UUID, node_id: str
) -> Optional[TechnologyTree]:
    """
    Delete a node from the technology tree and all its connections

    Args:
        db: Database session
        technology_tree_id: UUID of the technology tree
        node_id: ID of the node to delete

    Returns:
        Updated TechnologyTree object, or None if not found

    Raises:
        ValueError: If node not found
        SQLAlchemyError: On database error
    """
    try:
        technology_tree = get_technology_tree(db, technology_tree_id)
        if not technology_tree or not technology_tree.data:
            return None

        # Deep copy the tree data to avoid modifying it directly
        tree_data = dict(technology_tree.data)

        # Find and remove the node
        nodes = tree_data.get("nodes", [])
        original_node_length = len(nodes)

        nodes = [node for node in nodes if node.get("id") != node_id]

        if len(nodes) == original_node_length:
            raise ValueError(f"Node with id {node_id} not found in technology tree {technology_tree_id}")

        # Update nodes list in tree data
        tree_data["nodes"] = nodes

        # Also remove any connections involving this node
        connections = tree_data.get("connections", [])
        connections = [
            conn for conn in connections
            if conn.get("from") != node_id and conn.get("to") != node_id
        ]

        # Update connections list in tree data
        tree_data["connections"] = connections

        # Save changes
        technology_tree.data = tree_data
        db.commit()
        db.refresh(technology_tree)
        logger.info(f"Deleted node {node_id} and its connections from technology tree {technology_tree_id}")
        return technology_tree

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Database error deleting tree node: {str(e)}")
        raise
    except Exception as e:
        db.rollback()
        logger.error(f"Error deleting tree node: {str(e)}")
        raise


# Update module-level singleton
technology_tree_crud = {
    "get": get_technology_tree,
    "get_by_course": get_technology_tree_by_course,
    "get_by_course_id": get_by_course_id,
    "create": create_technology_tree,
    "update": update_technology_tree,
    "delete": delete_technology_tree,
    "update_node": update_tree_node,
    "add_node": add_tree_node,
    "delete_node": delete_tree_node,
    "add_connection": add_tree_connection,
    "update_connection": update_tree_connection,
    "delete_connection": delete_tree_connection
}
