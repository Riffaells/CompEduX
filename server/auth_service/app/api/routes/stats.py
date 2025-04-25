from typing import List

from fastapi import APIRouter, Depends, HTTPException, status, Security
from fastapi.security import APIKeyHeader
from sqlalchemy import func, distinct
from sqlalchemy.orm import Session

from ...core.config import settings
from ...db.session import get_db
from ...models.stats import ClientStatModel
from ...schemas.stats import (
    PlatformStatSchema,
    OSStatSchema,
    AppVersionStatSchema,
    ClientStatsResponse
)
from ...services.stats import get_platform_stats, get_os_stats, get_app_version_stats

router = APIRouter()

# Защита API ключом для админского доступа
admin_key_header = APIKeyHeader(name="X-Admin-Key", auto_error=False)


async def verify_admin_access(api_key: str = Security(admin_key_header)):
    """Проверка прав администратора по API-ключу"""
    if not api_key or api_key != settings.ADMIN_API_KEY:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Доступ запрещен. Требуется административный ключ."
        )
    return True


@router.get("/platforms", response_model=List[PlatformStatSchema], dependencies=[Depends(verify_admin_access)])
async def read_platform_stats(db: Session = Depends(get_db)):
    """Получить статистику по платформам клиентов"""
    return get_platform_stats(db)


@router.get("/os", response_model=List[OSStatSchema], dependencies=[Depends(verify_admin_access)])
async def read_os_stats(db: Session = Depends(get_db)):
    """Получить статистику по операционным системам клиентов"""
    return get_os_stats(db)


@router.get("/versions", response_model=List[AppVersionStatSchema], dependencies=[Depends(verify_admin_access)])
async def read_app_version_stats(db: Session = Depends(get_db)):
    """Получить статистику по версиям приложения"""
    return get_app_version_stats(db)


@router.get("/summary", response_model=ClientStatsResponse, dependencies=[Depends(verify_admin_access)])
async def read_stats_summary(db: Session = Depends(get_db)):
    """Получить сводную статистику использования приложения"""
    # Получаем статистику по всем категориям
    platforms = get_platform_stats(db)
    os_stats = get_os_stats(db)
    app_versions = get_app_version_stats(db)

    # Считаем общие показатели
    total_users_query = db.query(func.count(distinct(ClientStatModel.user_id))).scalar()
    total_requests_query = db.query(func.count(ClientStatModel.id)).scalar()

    total_users = total_users_query or 0
    total_requests = total_requests_query or 0

    # Собираем полный ответ
    return ClientStatsResponse(
        platforms=platforms,
        operating_systems=os_stats,
        app_versions=app_versions,
        total_users=total_users,
        total_requests=total_requests
    )
