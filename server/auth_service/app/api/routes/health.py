from typing import Dict, Any

from app.db.session import get_db
from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from sqlalchemy.sql import text

router = APIRouter()


@router.get("/", summary="Проверка состояния сервиса")
async def health_check(db: Session = Depends(get_db)) -> Dict[str, Any]:
    """
    Проверяет состояние сервиса авторизации и соединение с базой данных.
    """
    db_status = "error"
    db_message = "Could not connect to database"

    try:
        # Проверяем соединение с базой данных асинхронно
        result = await db.execute(text("SELECT 1"))
        if result.scalar() == 1:
            db_status = "ok"
            db_message = "Connected to database"
    except Exception as e:
        db_message = f"Database error: {str(e)}"

    return {
        "status": "ok" if db_status == "ok" else "error",
        "service": "auth_service",
        "version": "0.1.0",
        "database": {
            "status": db_status,
            "message": db_message
        }
    }
