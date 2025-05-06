from typing import Any, List, Optional
import uuid

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.api import deps
from app.crud.localization import localization_crud
from app.models.localization import Localization
from app.schemas.localization import (
    LocalizationCreate,
    LocalizationRead,
    LocalizationUpdate,
    LocalizationWithStatistics,
)

router = APIRouter()


@router.get("/", response_model=List[LocalizationRead])
async def get_localizations(
    db: AsyncSession = Depends(deps.get_db),
    skip: int = 0,
    limit: int = 100,
    namespace: Optional[str] = None,
) -> Any:
    """
    Retrieve localizations.
    """
    if namespace:
        localization = await localization_crud.get_by_namespace(db, namespace=namespace)
        return [localization] if localization else []

    return await localization_crud.get_multi(db, skip=skip, limit=limit)


@router.get("/statistics", response_model=List[LocalizationWithStatistics])
async def get_localizations_with_statistics(
    db: AsyncSession = Depends(deps.get_db),
    skip: int = 0,
    limit: int = 100,
) -> Any:
    """
    Retrieve localizations with statistics.
    """
    localizations = await localization_crud.get_multi(db, skip=skip, limit=limit)
    result = []

    for loc in localizations:
        stats = loc.get_translation_statistics()
        result.append(
            LocalizationWithStatistics(
                id=loc.id,
                namespace=loc.namespace,
                description=loc.description,
                translations=loc.translations,
                statistics=stats
            )
        )

    return result


@router.get("/languages", response_model=List[str])
async def get_available_languages(
    db: AsyncSession = Depends(deps.get_db),
    namespace: Optional[str] = None,
) -> Any:
    """
    Retrieve all available languages.
    """
    languages = await localization_crud.get_available_languages(db, namespace=namespace)
    return sorted(list(languages))


@router.post("/", response_model=LocalizationRead)
async def create_localization(
    *,
    db: AsyncSession = Depends(deps.get_db),
    localization_in: LocalizationCreate,
) -> Any:
    """
    Create new localization.
    """
    localization = await localization_crud.get_by_namespace(db, namespace=localization_in.namespace)
    if localization:
        raise HTTPException(
            status_code=400,
            detail=f"Localization with namespace '{localization_in.namespace}' already exists",
        )

    return await localization_crud.create(db, obj_in=localization_in)


@router.get("/{localization_id}", response_model=LocalizationRead)
async def get_localization(
    *,
    db: AsyncSession = Depends(deps.get_db),
    localization_id: uuid.UUID,
) -> Any:
    """
    Get localization by ID.
    """
    localization = await localization_crud.get(db, id=localization_id)
    if not localization:
        raise HTTPException(status_code=404, detail="Localization not found")
    return localization


@router.put("/{localization_id}", response_model=LocalizationRead)
async def update_localization(
    *,
    db: AsyncSession = Depends(deps.get_db),
    localization_id: uuid.UUID,
    localization_in: LocalizationUpdate,
) -> Any:
    """
    Update localization.
    """
    localization = await localization_crud.get(db, id=localization_id)
    if not localization:
        raise HTTPException(status_code=404, detail="Localization not found")

    # If namespace is being changed, verify it doesn't conflict
    if localization_in.namespace and localization_in.namespace != localization.namespace:
        existing = await localization_crud.get_by_namespace(db, namespace=localization_in.namespace)
        if existing and existing.id != localization_id:
            raise HTTPException(
                status_code=400,
                detail=f"Localization with namespace '{localization_in.namespace}' already exists",
            )

    localization = await localization_crud.update(db, db_obj=localization, obj_in=localization_in)
    return localization


@router.delete("/{localization_id}", response_model=LocalizationRead)
async def delete_localization(
    *,
    db: AsyncSession = Depends(deps.get_db),
    localization_id: uuid.UUID,
) -> Any:
    """
    Delete localization.
    """
    localization = await localization_crud.get(db, id=localization_id)
    if not localization:
        raise HTTPException(status_code=404, detail="Localization not found")

    localization = await localization_crud.remove(db, id=localization_id)
    return localization
