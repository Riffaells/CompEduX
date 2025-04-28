from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from ...db.session import get_db
from ...models.user import UserModel
from ...models.enums import UserRole
from ...services.auth import get_current_user
from ...services.stats import get_platform_stats, get_os_stats, get_app_version_stats

router = APIRouter()

@router.get("/platform")
async def platform_stats(
    db: AsyncSession = Depends(get_db),
    current_user: UserModel = Depends(get_current_user)
):
    """
    Статистика использования по платформам
    """
    # Проверка прав администратора
    if current_user.role != UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )

    stats = await get_platform_stats(db)
    return {"stats": stats}

@router.get("/os")
async def os_stats(
    db: AsyncSession = Depends(get_db),
    current_user: UserModel = Depends(get_current_user)
):
    """
    Статистика использования по операционным системам
    """
    # Проверка прав администратора
    if current_user.role != UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )

    stats = await get_os_stats(db)
    return {"stats": stats}

@router.get("/app_version")
async def app_version_stats(
    db: AsyncSession = Depends(get_db),
    current_user: UserModel = Depends(get_current_user)
):
    """
    Статистика использования по версиям приложения
    """
    # Проверка прав администратора
    if current_user.role != UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )

    stats = await get_app_version_stats(db)
    return {"stats": stats}
