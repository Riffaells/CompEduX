"""
API endpoints for technology trees
"""
import uuid
from typing import Any, Dict, Optional, List
from datetime import datetime, timezone

from app.api.deps import get_db, verify_token
from app.crud.course import course_crud
from app.crud.technology_tree import technology_tree_crud
from app.schemas.technology_tree import (
    TechnologyTree, TechnologyTreeCreate, TechnologyTreeUpdate,
    TechnologyTreeLanguages, NodeAddRequest, NodeUpdateRequest,
    NodeResponse, TreeExportFormat, TreeImportRequest
)
from fastapi import APIRouter, Body, Depends, HTTPException, status, Query, Path
from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger

# Set up logger
logger = get_logger("course_service.api.technology_tree")

router = APIRouter()


@router.get("/", response_model=TechnologyTree)
async def get_technology_tree(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        language: Optional[str] = Query(None, description="Language code for localized content"),
        db: AsyncSession = Depends(get_db)
):
    """
    Get the technology tree for a course

    Optionally provide a language parameter to get localized content.
    """
    logger.info(f"Request to get technology tree for course: {course_id}")

    # Verify course exists first
    course = await course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get the technology tree
    tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
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


@router.get("/languages", response_model=TechnologyTreeLanguages)
async def get_technology_tree_languages(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        db: AsyncSession = Depends(get_db)
):
    """
    Get all available languages in the technology tree
    """
    logger.info(f"Request to get available languages for technology tree of course: {course_id}")

    # Verify course exists first
    course = await course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get the technology tree
    tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
    if not tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Get available languages
    languages = tree.available_languages()

    return {"languages": languages}


@router.post("/", response_model=TechnologyTree, status_code=status.HTTP_201_CREATED)
async def create_technology_tree(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        technology_tree: TechnologyTreeCreate = Body(...),
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Create a technology tree for a course

    Authentication required.
    """
    logger.info(f"Request to create technology tree for course: {course_id}")

    # Verify course exists first
    course = await course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Check if technology tree already exists
    existing_tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
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
    tree = await technology_tree_crud.create_async(db, obj_in=technology_tree)

    return tree


@router.put("/", response_model=TechnologyTree)
async def update_technology_tree(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        technology_tree: TechnologyTreeUpdate = Body(...),
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Update a technology tree for a course

    Authentication required.
    """
    logger.info(f"Request to update technology tree for course: {course_id}")

    # Verify course exists first
    course = await course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get existing technology tree
    existing_tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
    if not existing_tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Update the technology tree
    updated_tree = await technology_tree_crud.update_async(db, db_obj=existing_tree, obj_in=technology_tree)

    return updated_tree


@router.delete("/", status_code=status.HTTP_204_NO_CONTENT)
async def delete_technology_tree(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Delete a technology tree for a course

    Authentication required.
    """
    logger.info(f"Request to delete technology tree for course: {course_id}")

    # Verify course exists first
    course = await course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get existing technology tree
    existing_tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
    if not existing_tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Delete the technology tree
    await technology_tree_crud.remove_async(db, id=existing_tree.id)

    return None


@router.patch("/data", response_model=TechnologyTree)
async def update_technology_tree_data(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        data: Dict[str, Any] = Body(..., description="Technology tree data"),
        db: AsyncSession = Depends(get_db),
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
    course = await course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Get existing technology tree
    existing_tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
    if not existing_tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Update just the data
    updated_tree = await technology_tree_crud.update_tree_data_async(db, tree_id=existing_tree.id, data=data)

    return updated_tree


@router.post("/nodes", response_model=NodeResponse)
async def add_tree_node(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        node_request: NodeAddRequest = Body(...),
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Add a new node to the technology tree

    Authentication required.
    """
    logger.info(f"Request to add node {node_request.node_id} to technology tree for course: {course_id}")

    # Get existing technology tree
    tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
    if not tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Check if node ID already exists
    if tree.data and "nodes" in tree.data and node_request.node_id in tree.data["nodes"]:
        logger.warning(f"Node with ID {node_request.node_id} already exists in tree")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Node with ID {node_request.node_id} already exists"
        )

    # Add node to tree
    try:
        node_data = node_request.node_data.dict()
        tree.add_node(node_request.node_id, node_data)

        # Save updated tree
        updated_tree = await technology_tree_crud.update_async(db, db_obj=tree, obj_in={"data": tree.data, "version": tree.version})

        return {
            "success": True,
            "node_id": node_request.node_id,
            "message": "Node added successfully",
            "node": updated_tree.data["nodes"].get(node_request.node_id) if updated_tree.data and "nodes" in updated_tree.data else None
        }
    except Exception as e:
        logger.error(f"Error adding node: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error adding node: {str(e)}"
        )


@router.put("/nodes/{node_id}", response_model=NodeResponse)
async def update_tree_node(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        node_id: str = Path(..., description="ID of the node to update"),
        node_data: Dict[str, Any] = Body(..., description="Node data to update"),
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Update an existing node in the technology tree

    Authentication required.
    """
    logger.info(f"Request to update node {node_id} in technology tree for course: {course_id}")

    # Get existing technology tree
    tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
    if not tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Check if node exists
    if not tree.data or "nodes" not in tree.data or node_id not in tree.data["nodes"]:
        logger.warning(f"Node with ID {node_id} not found in tree")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Node with ID {node_id} not found"
        )

    # Update node
    try:
        existing_node = tree.data["nodes"][node_id]
        updated_node = {**existing_node, **node_data}
        tree.data["nodes"][node_id] = updated_node

        # Update metadata
        if "metadata" not in tree.data:
            tree.data["metadata"] = {}
        tree.data["metadata"]["updated_at"] = datetime.now(timezone.utc).isoformat()
        tree.version += 1

        # Save updated tree
        updated_tree = await technology_tree_crud.update_async(db, db_obj=tree, obj_in={"data": tree.data, "version": tree.version})

        return {
            "success": True,
            "node_id": node_id,
            "message": "Node updated successfully",
            "node": updated_tree.data["nodes"].get(node_id) if updated_tree.data and "nodes" in updated_tree.data else None
        }
    except Exception as e:
        logger.error(f"Error updating node: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error updating node: {str(e)}"
        )


@router.delete("/nodes/{node_id}", response_model=NodeResponse)
async def delete_tree_node(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        node_id: str = Path(..., description="ID of the node to delete"),
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Delete a node from the technology tree

    Authentication required.
    """
    logger.info(f"Request to delete node {node_id} from technology tree for course: {course_id}")

    # Get existing technology tree
    tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
    if not tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Delete node
    success = tree.remove_node(node_id)
    if not success:
        logger.warning(f"Node with ID {node_id} not found in tree")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Node with ID {node_id} not found"
        )

    # Save updated tree
    await technology_tree_crud.update_async(db, db_obj=tree, obj_in={"data": tree.data, "version": tree.version})

    return {
        "success": True,
        "node_id": node_id,
        "message": "Node deleted successfully"
    }


@router.get("/nodes/{node_id}", response_model=Dict[str, Any])
async def get_tree_node(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        node_id: str = Path(..., description="ID of the node to retrieve"),
        language: Optional[str] = Query(None, description="Language code for localized content"),
        db: AsyncSession = Depends(get_db)
):
    """
    Get a specific node from the technology tree

    Optionally provide a language parameter to get localized content.
    """
    logger.info(f"Request to get node {node_id} from technology tree for course: {course_id}")

    # Get existing technology tree
    tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
    if not tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Get node
    node = tree.get_node(node_id)
    if not node:
        logger.warning(f"Node with ID {node_id} not found in tree")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Node with ID {node_id} not found"
        )

    # Add localized content if language specified
    if language:
        if "title" in node and isinstance(node["title"], dict):
            if language in node["title"]:
                node["title_localized"] = node["title"][language]
            else:
                node["title_localized"] = next(iter(node["title"].values()), "")

        if "description" in node and isinstance(node["description"], dict):
            if language in node["description"]:
                node["description_localized"] = node["description"][language]
            else:
                node["description_localized"] = next(iter(node["description"].values()), "")

    return node


@router.patch("/publish", response_model=TechnologyTree)
async def publish_technology_tree(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        is_published: bool = Body(..., description="Whether to publish or unpublish the tree"),
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Publish or unpublish a technology tree

    Authentication required.
    """
    logger.info(f"Request to set technology tree publish status to {is_published} for course: {course_id}")

    # Get existing technology tree
    tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
    if not tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Update publish status
    updated_tree = await technology_tree_crud.update_async(db, db_obj=tree, obj_in={"is_published": is_published})

    return updated_tree


@router.get("/export", response_model=TreeExportFormat)
async def export_technology_tree(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        language: Optional[str] = Query(None, description="Language code for localized content"),
        localize: bool = Query(False, description="Whether to include localized node content"),
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Export a technology tree in a format suitable for visualization tools

    Authentication required.
    """
    logger.info(f"Request to export technology tree for course: {course_id}")

    # Get existing technology tree
    tree = await technology_tree_crud.get_by_course_id_async(db, course_id)
    if not tree:
        logger.warning(f"Technology tree not found for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Technology tree not found for course: {course_id}"
        )

    # Prepare export format
    if not tree.data:
        return {
            "nodes": {},
            "connections": [],
            "metadata": {
                "version": tree.version,
                "course_id": str(course_id),
                "is_published": tree.is_published
            }
        }

    # Add localized content if requested
    export_data = tree.data.copy() if tree.data else {"nodes": {}}

    if localize and language and "nodes" in export_data:
        for node_id, node in export_data["nodes"].items():
            # Localize node titles
            if "title" in node and isinstance(node["title"], dict):
                if language in node["title"]:
                    node["title_localized"] = node["title"][language]
                else:
                    node["title_localized"] = next(iter(node["title"].values()), "")

            # Localize node descriptions
            if "description" in node and isinstance(node["description"], dict):
                if language in node["description"]:
                    node["description_localized"] = node["description"][language]
                else:
                    node["description_localized"] = next(iter(node["description"].values()), "")

    # Build connections list from nodes dependencies
    connections = []
    if "nodes" in export_data:
        for node_id, node in export_data["nodes"].items():
            if "dependencies" in node and node["dependencies"]:
                for dep_id in node["dependencies"]:
                    connections.append({
                        "source": dep_id,
                        "target": node_id
                    })

    # Add metadata
    if "metadata" not in export_data:
        export_data["metadata"] = {}

    export_data["metadata"].update({
        "version": tree.version,
        "course_id": str(course_id),
        "is_published": tree.is_published,
        "created_at": tree.created_at.isoformat() if hasattr(tree, "created_at") else None,
        "updated_at": tree.updated_at.isoformat() if hasattr(tree, "updated_at") else None
    })

    return {
        "nodes": export_data.get("nodes", {}),
        "connections": connections,
        "metadata": export_data.get("metadata", {})
    }


@router.post("/import", response_model=TechnologyTree)
async def import_technology_tree(
        course_id: uuid.UUID = Path(..., description="ID of the course"),
        import_data: TreeImportRequest = Body(...),
        db: AsyncSession = Depends(get_db),
        _: bool = Depends(verify_token)
):
    """
    Import a technology tree from external data

    Authentication required.
    """
    logger.info(f"Request to import technology tree for course: {course_id}")

    # Verify course exists
    course = await course_crud.get_course(db, course_id)
    if not course:
        logger.warning(f"Course with ID {course_id} not found")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {course_id} not found"
        )

    # Check if the course_id in the import data matches the path parameter
    if import_data.course_id != course_id:
        logger.warning(f"Course ID mismatch: URL {course_id}, body {import_data.course_id}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Course ID in path must match course_id in request body"
        )

    # Check if a technology tree already exists for this course
    existing_tree = await technology_tree_crud.get_by_course_id_async(db, course_id)

    if existing_tree and not import_data.replace_existing:
        logger.warning(f"Technology tree already exists for course: {course_id}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Technology tree already exists for course: {course_id}. Set replace_existing=true to overwrite."
        )

    # Process the imported data
    try:
        # If replacing existing tree
        if existing_tree:
            tree_data = import_data.tree_data
            # Increment version
            version = existing_tree.version + 1 if hasattr(existing_tree, "version") else 1

            # Update the existing tree
            updated_tree = await technology_tree_crud.update_async(
                db,
                db_obj=existing_tree,
                obj_in={"data": tree_data, "version": version}
            )
            return updated_tree
        else:
            # Create a new tree
            new_tree = TechnologyTreeCreate(
                course_id=course_id,
                data=import_data.tree_data,
                version=1,
                is_published=False
            )
            created_tree = await technology_tree_crud.create_async(db, obj_in=new_tree)
            return created_tree
    except Exception as e:
        logger.error(f"Error importing technology tree: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error importing technology tree: {str(e)}"
        )
