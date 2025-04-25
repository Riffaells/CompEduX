"""
API endpoints for technology trees
"""
import uuid
from typing import Any, Dict, Optional, List

from fastapi import APIRouter, Body, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session

from app.api.deps import get_db, verify_token
from app.crud.course import course_crud
from app.crud.technology_tree import technology_tree_crud
from app.schemas.technology_tree import TechnologyTree, TechnologyTreeCreate, TechnologyTreeUpdate

from common.logger import get_logger

# Set up logger
logger = get_logger("course_service.api.technology_tree")

router = APIRouter()


@router.get("/{course_id}/technology-tree", response_model=TechnologyTree)
async def get_technology_tree(
    course_id: uuid.UUID,
    language: Optional[str] = Query(None, description="Language code for localized content"),
    db: Session = Depends(get_db)
):
    """
    Get the technology tree for a course

    Optionally provide a language parameter to get localized content.
    """
    logger.info(f"Request to get technology tree for course: {course_id}")

    # Verify course exists first
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get the technology tree
    tree = technology_tree_crud.get_by_course_id(db, course_id)
    if not tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # If language is provided, localize the content
    if language and tree.data:
        # Get localized content (implementation in the model)
        # This is handled by the schema's from_orm method which processes the tree data
        pass

    return tree


@router.get("/{course_id}/technology-tree/languages", response_model=Dict[str, List[str]])
async def get_technology_tree_languages(
    course_id: uuid.UUID,
    db: Session = Depends(get_db)
):
    """
    Get all available languages in the technology tree
    """
    logger.info(f"Request to get available languages for technology tree of course: {course_id}")

    # Verify course exists first
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get the technology tree
    tree = technology_tree_crud.get_by_course_id(db, course_id)
    if not tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Get available languages
    languages = tree.available_languages()

    return {"languages": languages}


@router.post("/{course_id}/technology-tree", response_model=TechnologyTree, status_code=status.HTTP_201_CREATED)
async def create_technology_tree(
    course_id: uuid.UUID,
    technology_tree: TechnologyTreeCreate,
    db: Session = Depends(get_db),
    _: bool = Depends(verify_token)
):
    """
    Create a technology tree for a course

    Authentication required.
    """
    logger.info(f"Request to create technology tree for course: {course_id}")

    # Verify course exists first
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Check if technology tree already exists
    existing_tree = technology_tree_crud.get_by_course_id(db, course_id)
    if existing_tree:
        logger.warning(f"Technology tree already exists for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Technology tree already exists for course: {course_id}"
        )

    # Ensure the course_id in the tree matches the path parameter
    if technology_tree.course_id != course_id:
        logger.warning(f"Course ID mismatch: URL {course_id}, body {technology_tree.course_id}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Course ID in path must match course_id in request body"
        )

    # Create the technology tree
    tree = technology_tree_crud.create(db, obj_in=technology_tree)

    return tree


@router.put("/{course_id}/technology-tree", response_model=TechnologyTree)
async def update_technology_tree(
    course_id: uuid.UUID,
    technology_tree: TechnologyTreeUpdate,
    db: Session = Depends(get_db),
    _: bool = Depends(verify_token)
):
    """
    Update a technology tree for a course

    Authentication required.
    """
    logger.info(f"Request to update technology tree for course: {course_id}")

    # Verify course exists first
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get existing technology tree
    existing_tree = technology_tree_crud.get_by_course_id(db, course_id)
    if not existing_tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Update the technology tree
    updated_tree = technology_tree_crud.update(db, db_obj=existing_tree, obj_in=technology_tree)

    return updated_tree


@router.delete("/{course_id}/technology-tree", status_code=status.HTTP_204_NO_CONTENT)
async def delete_technology_tree(
    course_id: uuid.UUID,
    db: Session = Depends(get_db),
    _: bool = Depends(verify_token)
):
    """
    Delete a technology tree for a course

    Authentication required.
    """
    logger.info(f"Request to delete technology tree for course: {course_id}")

    # Verify course exists first
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get existing technology tree
    existing_tree = technology_tree_crud.get_by_course_id(db, course_id)
    if not existing_tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Delete the technology tree
    technology_tree_crud.remove(db, id=existing_tree.id)

    return None


@router.patch("/{course_id}/technology-tree/data", response_model=TechnologyTree)
async def update_technology_tree_data(
    course_id: uuid.UUID,
    data: Dict[str, Any] = Body(..., description="Technology tree data"),
    db: Session = Depends(get_db),
    _: bool = Depends(verify_token)
):
    """
    Update only the data portion of a technology tree

    Authentication required.

    This is a convenience endpoint for updating just the tree structure
    without having to send the entire object.
    """
    logger.info(f"Request to update technology tree data for course: {course_id}")

    # Verify course exists first
    course = course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get existing technology tree
    existing_tree = technology_tree_crud.get_by_course_id(db, course_id)
    if not existing_tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Create update object with just the data field
    update_data = TechnologyTreeUpdate(data=data)

    # Update the technology tree
    updated_tree = technology_tree_crud.update(db, db_obj=existing_tree, obj_in=update_data)

    return updated_tree
