"""
TechnologyTree API endpoints
"""
from typing import Dict, Any, Optional, List
import uuid
from fastapi import APIRouter, HTTPException, status, Depends, Body
from sqlalchemy.orm import Session
from sqlalchemy.exc import SQLAlchemyError

from app.db.session import get_db
from app.schemas.technology_tree import TechnologyTree as TechnologyTreeSchema
from app.schemas.technology_tree import TechnologyTreeCreate, TechnologyTreeUpdate, TechnologyTreeNodeCreate, TechnologyTreeNodeUpdate
from app.schemas.technology_tree import TechnologyTreeConnectionCreate, TechnologyTreeConnectionUpdate
from app.crud import technology_tree as technology_tree_crud
from common.logger import initialize_logging

# Initialize logger
logger = initialize_logging("course_service.api.technology_tree")

router = APIRouter()

@router.get("/{technology_tree_id}", response_model=TechnologyTreeSchema)
async def get_technology_tree(technology_tree_id: uuid.UUID, db: Session = Depends(get_db)):
    """
    Get a technology tree by ID
    """
    logger.info(f"Request to get technology tree with ID: {technology_tree_id}")
    technology_tree = technology_tree_crud.get_technology_tree(db, technology_tree_id)
    if technology_tree is None:
        logger.warning(f"Technology tree with ID {technology_tree_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree with ID {technology_tree_id} not found"
        )
    return technology_tree

@router.get("/course/{course_id}", response_model=TechnologyTreeSchema)
async def get_technology_tree_by_course(course_id: uuid.UUID, db: Session = Depends(get_db)):
    """
    Get a technology tree for a specific course
    """
    logger.info(f"Request to get technology tree for course ID: {course_id}")
    technology_tree = technology_tree_crud.get_technology_tree_by_course(db, course_id)
    if technology_tree is None:
        logger.warning(f"Technology tree for course ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree for course ID {course_id} not found"
        )
    return technology_tree

@router.post("/", response_model=TechnologyTreeSchema, status_code=status.HTTP_201_CREATED)
async def create_technology_tree(tech_tree: TechnologyTreeCreate, db: Session = Depends(get_db)):
    """
    Create a new technology tree
    """
    logger.info(f"Request to create a new technology tree for course: {tech_tree.course_id}")
    try:
        return technology_tree_crud.create_technology_tree(db, tech_tree.course_id, tech_tree.data)
    except ValueError as e:
        logger.error(f"Error creating technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except SQLAlchemyError as e:
        logger.error(f"Database error creating technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while creating technology tree"
        )

@router.put("/{technology_tree_id}", response_model=TechnologyTreeSchema)
async def update_technology_tree(
    technology_tree_id: uuid.UUID,
    tech_tree: TechnologyTreeUpdate,
    db: Session = Depends(get_db)
):
    """
    Update a technology tree
    """
    logger.info(f"Request to update technology tree with ID: {technology_tree_id}")
    if tech_tree.data is None:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No data provided for update"
        )

    try:
        updated_tree = technology_tree_crud.update_technology_tree(db, technology_tree_id, tech_tree.data)
        if updated_tree is None:
            logger.warning(f"Technology tree with ID {technology_tree_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Technology tree with ID {technology_tree_id} not found"
            )
        return updated_tree
    except SQLAlchemyError as e:
        logger.error(f"Database error updating technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while updating technology tree"
        )

@router.delete("/{technology_tree_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_technology_tree(technology_tree_id: uuid.UUID, db: Session = Depends(get_db)):
    """
    Delete a technology tree
    """
    logger.info(f"Request to delete technology tree with ID: {technology_tree_id}")
    try:
        success = technology_tree_crud.delete_technology_tree(db, technology_tree_id)
        if not success:
            logger.warning(f"Technology tree with ID {technology_tree_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Technology tree with ID {technology_tree_id} not found"
            )
        return None
    except SQLAlchemyError as e:
        logger.error(f"Database error deleting technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while deleting technology tree"
        )

@router.post("/{technology_tree_id}/nodes", response_model=TechnologyTreeSchema)
async def add_node_to_tree(
    technology_tree_id: uuid.UUID,
    node_data: TechnologyTreeNodeCreate,
    db: Session = Depends(get_db)
):
    """
    Add a new node to a technology tree
    """
    logger.info(f"Request to add node to technology tree ID: {technology_tree_id}")
    try:
        result = technology_tree_crud.add_tree_node(db, technology_tree_id, node_data.dict())
        if result[0] is None:
            logger.warning(f"Technology tree with ID {technology_tree_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Technology tree with ID {technology_tree_id} not found"
            )
        return result[0]
    except ValueError as e:
        logger.error(f"Error adding node to technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except SQLAlchemyError as e:
        logger.error(f"Database error adding node to technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while adding node to technology tree"
        )

@router.put("/{technology_tree_id}/nodes/{node_id}", response_model=TechnologyTreeSchema)
async def update_tree_node(
    technology_tree_id: uuid.UUID,
    node_id: str,
    node_data: TechnologyTreeNodeUpdate,
    db: Session = Depends(get_db)
):
    """
    Update a node in a technology tree
    """
    logger.info(f"Request to update node {node_id} in technology tree ID: {technology_tree_id}")
    try:
        updated_tree = technology_tree_crud.update_tree_node(db, technology_tree_id, node_id, node_data.dict(exclude_unset=True))
        if updated_tree is None:
            logger.warning(f"Technology tree with ID {technology_tree_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Technology tree with ID {technology_tree_id} not found"
            )
        return updated_tree
    except ValueError as e:
        logger.error(f"Error updating node in technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except SQLAlchemyError as e:
        logger.error(f"Database error updating node in technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while updating node in technology tree"
        )

@router.delete("/{technology_tree_id}/nodes/{node_id}", response_model=TechnologyTreeSchema)
async def delete_tree_node(
    technology_tree_id: uuid.UUID,
    node_id: str,
    db: Session = Depends(get_db)
):
    """
    Delete a node from the technology tree and all its connections
    """
    logger.info(f"Request to delete node {node_id} from technology tree ID: {technology_tree_id}")
    try:
        updated_tree = technology_tree_crud.delete_tree_node(db, technology_tree_id, node_id)
        if updated_tree is None:
            logger.warning(f"Technology tree with ID {technology_tree_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Technology tree with ID {technology_tree_id} not found"
            )
        return updated_tree
    except ValueError as e:
        logger.error(f"Error deleting node from technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except SQLAlchemyError as e:
        logger.error(f"Database error deleting node from technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while deleting node from technology tree"
        )

@router.post("/{technology_tree_id}/connections", response_model=TechnologyTreeSchema)
async def add_connection_to_tree(
    technology_tree_id: uuid.UUID,
    connection_data: TechnologyTreeConnectionCreate,
    db: Session = Depends(get_db)
):
    """
    Add a new connection between nodes in the technology tree
    """
    logger.info(f"Request to add connection to technology tree ID: {technology_tree_id}")
    try:
        result = technology_tree_crud.add_tree_connection(db, technology_tree_id, connection_data.dict())
        if result[0] is None:
            logger.warning(f"Technology tree with ID {technology_tree_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Technology tree with ID {technology_tree_id} not found"
            )
        return result[0]
    except ValueError as e:
        logger.error(f"Error adding connection to technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except SQLAlchemyError as e:
        logger.error(f"Database error adding connection to technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while adding connection to technology tree"
        )

@router.put("/{technology_tree_id}/connections/{connection_id}", response_model=TechnologyTreeSchema)
async def update_tree_connection(
    technology_tree_id: uuid.UUID,
    connection_id: str,
    connection_data: TechnologyTreeConnectionUpdate,
    db: Session = Depends(get_db)
):
    """
    Update a connection in the technology tree
    """
    logger.info(f"Request to update connection {connection_id} in technology tree ID: {technology_tree_id}")
    try:
        updated_tree = technology_tree_crud.update_tree_connection(
            db, technology_tree_id, connection_id, connection_data.dict(exclude_unset=True)
        )
        if updated_tree is None:
            logger.warning(f"Technology tree with ID {technology_tree_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Technology tree with ID {technology_tree_id} not found"
            )
        return updated_tree
    except ValueError as e:
        logger.error(f"Error updating connection in technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except SQLAlchemyError as e:
        logger.error(f"Database error updating connection in technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while updating connection in technology tree"
        )

@router.delete("/{technology_tree_id}/connections/{connection_id}", response_model=TechnologyTreeSchema)
async def delete_tree_connection(
    technology_tree_id: uuid.UUID,
    connection_id: str,
    db: Session = Depends(get_db)
):
    """
    Delete a connection from the technology tree
    """
    logger.info(f"Request to delete connection {connection_id} from technology tree ID: {technology_tree_id}")
    try:
        updated_tree = technology_tree_crud.delete_tree_connection(db, technology_tree_id, connection_id)
        if updated_tree is None:
            logger.warning(f"Technology tree with ID {technology_tree_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Technology tree with ID {technology_tree_id} not found"
            )
        return updated_tree
    except ValueError as e:
        logger.error(f"Error deleting connection from technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except SQLAlchemyError as e:
        logger.error(f"Database error deleting connection from technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while deleting connection from technology tree"
        )
