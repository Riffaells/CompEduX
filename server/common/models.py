from sqlalchemy import Column, Integer, DateTime
from sqlalchemy.sql import func

# Базовый класс для всех моделей
class BaseModel:
    id = Column(Integer, primary_key=True, index=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
