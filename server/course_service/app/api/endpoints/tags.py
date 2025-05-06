"""
API endpoints for tags
"""
import uuid
from typing import List, Optional

from app.api.deps import get_db, verify_token
from app.crud.tag import tag_crud
from app.schemas.tag import Tag, TagCreate, TagUpdate
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger

# Set up logger
logger = get_logger("course_service.api.tags")

router = APIRouter()


@router.get("/", response_model=List[Tag])
async def get_tags(
        skip: int = Query(0, ge=0, description="Number of records to skip"),
        limit: int = Query(100, ge=1, le=1000, description="Maximum number of records to return"),
        search: Optional[str] = Query(None, description="Search string for filtering tags by name"),
        db: AsyncSession = Depends(get_db)
):
    """
    Get all tags with optional filtering
    """
    logger.info(f"Request to list tags with params: skip={skip}, limit={limit}, search={search}")

    if search:
        return await tag_crud.search_by_name(db, search, skip=skip, limit=limit)
    return await tag_crud.get_multi(db, skip=skip, limit=limit)


@router.get("/{tag_id}", response_model=Tag)
async def get_tag(
        tag_id: uuid.UUID,
        db: AsyncSession = Depends(get_db)
):
    """
    Get a specific tag by ID
    """
    logger.info(f"Request to get tag with ID: {tag_id}")

    tag = await tag_crud.get(db, tag_id)
    if not tag:
        logger.warning(f"Tag with ID {tag_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tag with ID {tag_id} not found"
        )

    return tag


@router.post("/", response_model=Tag, status_code=status.HTTP_201_CREATED)
async def create_tag(
        tag: TagCreate,
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Create a new tag

    Authentication required.
    """
    logger.info(f"Request to create new tag")

    # Check if a tag with the same name (in any language) already exists
    existing_tag = await tag_crud.get_by_name(db, tag.name.get("en", ""))
    if existing_tag:
        logger.warning(f"Tag with name '{tag.name.get('en', '')}' already exists")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Tag with name '{tag.name.get('en', '')}' already exists"
        )

    return await tag_crud.create(db, obj_in=tag)


@router.put("/{tag_id}", response_model=Tag)
async def update_tag(
        tag_id: uuid.UUID,
        tag: TagUpdate,
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Update a tag

    Authentication required.
    """
    logger.info(f"Request to update tag with ID: {tag_id}")

    # Get the existing tag
    existing_tag = await tag_crud.get(db, tag_id)
    if not existing_tag:
        logger.warning(f"Tag with ID {tag_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tag with ID {tag_id} not found"
        )

    # Check if the update would create a duplicate
    if tag.name and "en" in tag.name:
        duplicate = await tag_crud.get_by_name(db, tag.name["en"])
        if duplicate and duplicate.id != tag_id:
            logger.warning(f"Tag with name '{tag.name['en']}' already exists")
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Tag with name '{tag.name['en']}' already exists"
            )

    # Update the tag
    updated_tag = await tag_crud.update(db, db_obj=existing_tag, obj_in=tag)

    return updated_tag


@router.delete("/{tag_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_tag(
        tag_id: uuid.UUID,
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Delete a tag

    Authentication required.
    """
    logger.info(f"Request to delete tag with ID: {tag_id}")

    # Check if tag exists
    existing_tag = await tag_crud.get(db, tag_id)
    if not existing_tag:
        logger.warning(f"Tag with ID {tag_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tag with ID {tag_id} not found"
        )

    # Check if tag is used by any courses
    if await tag_crud.is_tag_in_use(db, tag_id):
        logger.warning(f"Cannot delete tag with ID {tag_id} as it is in use")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Cannot delete tag as it is in use by one or more courses"
        )

    # Delete the tag
    await tag_crud.remove(db, id=tag_id)

    return None


@router.get("/name/{name}", response_model=Tag)
async def get_tag_by_name(
        name: str,
        db: AsyncSession = Depends(get_db)
):
    """
    Get a tag by its name
    """
    logger.info(f"Request to get tag with name: {name}")

    tag = await tag_crud.get_by_name(db, name)
    if not tag:
        logger.warning(f"Tag with name '{name}' not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tag with name '{name}' not found"
        )

    return tag
