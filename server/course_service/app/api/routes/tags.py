"""
Tag API endpoints
"""
from typing import List, Dict, Optional
from fastapi import APIRouter, HTTPException, Depends, Query, Path, status
from sqlalchemy.orm import Session
from sqlalchemy.exc import SQLAlchemyError

from app.db.session import get_db
from app.schemas.tag import Tag, TagCreate, TagUpdate, TagWithName, TagTranslationCreate
from app.crud.tag import tag_crud
from common.logger import initialize_logging

# Initialize logger
logger = initialize_logging("course_service.api.tags")

router = APIRouter()


@router.get("/", response_model=List[TagWithName])
async def get_tags(
    skip: int = Query(0, ge=0, description="Number of records to skip"),
    limit: int = Query(100, ge=1, le=100, description="Maximum number of records to return"),
    db: Session = Depends(get_db)
):
    """
    Get all tags with pagination
    """
    logger.info(f"Request to get tags with params: skip={skip}, limit={limit}")
    tags = tag_crud["get_all"](db, skip=skip, limit=limit)

    # Convert to TagWithName schema
    result = []
    for tag in tags:
        name_dict = {t.language: t.name for t in tag.translations}
        result.append(TagWithName(id=tag.id, name=name_dict))

    return result


@router.get("/{tag_id}", response_model=Tag)
async def get_tag(
    tag_id: int = Path(..., ge=1, description="The ID of the tag"),
    db: Session = Depends(get_db)
):
    """
    Get a specific tag by ID
    """
    logger.info(f"Request to get tag with ID: {tag_id}")
    tag = tag_crud["get"](db, tag_id)
    if tag is None:
        logger.warning(f"Tag with ID {tag_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tag with ID {tag_id} not found"
        )
    return tag


@router.post("/", response_model=Tag, status_code=status.HTTP_201_CREATED)
async def create_tag(
    tag: TagCreate,
    db: Session = Depends(get_db)
):
    """
    Create a new tag with translations
    """
    logger.info(f"Request to create a new tag")
    try:
        return tag_crud["create"](db, tag)
    except SQLAlchemyError as e:
        logger.error(f"Database error creating tag: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while creating tag"
        )


@router.post("/simple", response_model=Tag, status_code=status.HTTP_201_CREATED)
async def create_tag_simple(
    translations: Dict[str, str],
    db: Session = Depends(get_db)
):
    """
    Create a new tag with translations specified as a dictionary
    Example: {"en": "Programming", "ru": "Программирование"}
    """
    logger.info(f"Request to create a new tag with translations: {translations}")
    try:
        return tag_crud["create_with_translations"](db, translations)
    except ValueError as e:
        logger.error(f"Error creating tag: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except SQLAlchemyError as e:
        logger.error(f"Database error creating tag: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while creating tag"
        )


@router.put("/{tag_id}", response_model=Tag)
async def update_tag(
    tag_id: int = Path(..., ge=1, description="The ID of the tag to update"),
    tag: TagUpdate = None,
    db: Session = Depends(get_db)
):
    """
    Update an existing tag with new translations
    """
    logger.info(f"Request to update tag with ID: {tag_id}")
    try:
        updated_tag = tag_crud["update"](db, tag_id, tag)
        if updated_tag is None:
            logger.warning(f"Tag with ID {tag_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Tag with ID {tag_id} not found"
            )
        return updated_tag
    except SQLAlchemyError as e:
        logger.error(f"Database error updating tag: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while updating tag"
        )


@router.delete("/{tag_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_tag(
    tag_id: int = Path(..., ge=1, description="The ID of the tag to delete"),
    db: Session = Depends(get_db)
):
    """
    Delete a tag
    """
    logger.info(f"Request to delete tag with ID: {tag_id}")
    try:
        success = tag_crud["delete"](db, tag_id)
        if not success:
            logger.warning(f"Tag with ID {tag_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Tag with ID {tag_id} not found"
            )
        return None
    except SQLAlchemyError as e:
        logger.error(f"Database error deleting tag: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while deleting tag"
        )


@router.put("/{tag_id}/translations/{language}", response_model=Tag)
async def update_tag_translation(
    tag_id: int = Path(..., ge=1, description="The ID of the tag"),
    language: str = Path(..., min_length=2, max_length=5, description="Language code (e.g., 'en', 'ru')"),
    name: str = Query(..., min_length=1, max_length=100, description="New tag name"),
    db: Session = Depends(get_db)
):
    """
    Update or add a specific translation for a tag
    """
    logger.info(f"Request to update translation for tag ID: {tag_id}, language: {language}")
    try:
        updated_tag = tag_crud["update_translation"](db, tag_id, language, name)
        if updated_tag is None:
            logger.warning(f"Tag with ID {tag_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Tag with ID {tag_id} not found"
            )
        return updated_tag
    except SQLAlchemyError as e:
        logger.error(f"Database error updating tag translation: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while updating tag translation"
        )


@router.delete("/{tag_id}/translations/{language}", response_model=Tag)
async def delete_tag_translation(
    tag_id: int = Path(..., ge=1, description="The ID of the tag"),
    language: str = Path(..., min_length=2, max_length=5, description="Language code (e.g., 'en', 'ru')"),
    db: Session = Depends(get_db)
):
    """
    Remove a specific translation from a tag
    """
    logger.info(f"Request to delete translation for tag ID: {tag_id}, language: {language}")
    try:
        updated_tag = tag_crud["remove_translation"](db, tag_id, language)
        if updated_tag is None:
            logger.warning(f"Tag with ID {tag_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Tag with ID {tag_id} not found"
            )
        return updated_tag
    except ValueError as e:
        logger.error(f"Error deleting tag translation: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except SQLAlchemyError as e:
        logger.error(f"Database error deleting tag translation: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Database error occurred while deleting tag translation"
        )
