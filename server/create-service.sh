#!/bin/bash

# Скрипт для создания нового микросервиса

# Цвета для вывода
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Проверка наличия аргумента
if [ $# -eq 0 ]; then
    echo -e "${RED}Ошибка: Не указано имя сервиса.${NC}"
    echo -e "Использование: $0 <service_name>"
    exit 1
fi

SERVICE_NAME=$1
SERVICE_DIR="${SERVICE_NAME}_service"

# Проверка существования директории
if [ -d "$SERVICE_DIR" ]; then
    echo -e "${RED}Ошибка: Директория '$SERVICE_DIR' уже существует.${NC}"
    exit 1
fi

echo -e "${YELLOW}Создание нового микросервиса '$SERVICE_NAME'...${NC}"

# Создание директории сервиса
mkdir -p $SERVICE_DIR

# Создание базовых файлов
echo -e "${YELLOW}Создание базовых файлов...${NC}"

# database.py
cat > $SERVICE_DIR/database.py << EOF
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import os

DATABASE_URL = os.getenv("DATABASE_URL", "postgresql://postgres:postgres@postgres:5432/competition_platform")

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

# Dependency
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
EOF

# models.py
cat > $SERVICE_DIR/models.py << EOF
from sqlalchemy import Boolean, Column, Integer, String, DateTime, ForeignKey, Enum, Text
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
import enum
from database import Base

# Определите ваши модели здесь
class ExampleModel(Base):
    __tablename__ = "examples"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    description = Column(Text, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())
EOF

# schemas.py
cat > $SERVICE_DIR/schemas.py << EOF
from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime

class ExampleBase(BaseModel):
    name: str
    description: Optional[str] = None

class ExampleCreate(ExampleBase):
    pass

class Example(ExampleBase):
    id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        orm_mode = True
EOF

# main.py
cat > $SERVICE_DIR/main.py << EOF
from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
import sys
import os

# Добавляем путь к общим модулям
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db, engine
import models
import schemas
from common.auth import get_current_user, get_current_user_id, check_admin_role, check_moderator_role

# Create tables
models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="${SERVICE_NAME} Service")

# Environment variables
AUTH_SERVICE_URL = os.getenv("AUTH_SERVICE_URL", "http://auth_service:8000")

@app.get("/examples/", response_model=List[schemas.Example])
async def get_examples(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    examples = db.query(models.ExampleModel).offset(skip).limit(limit).all()
    return examples

@app.post("/examples/", response_model=schemas.Example, status_code=status.HTTP_201_CREATED)
async def create_example(
    example: schemas.ExampleCreate,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    db_example = models.ExampleModel(**example.dict())
    db.add(db_example)
    db.commit()
    db.refresh(db_example)
    return db_example

# Health check endpoint
@app.get("/health")
def health_check():
    return {"status": "ok"}
EOF

# requirements.txt
cat > $SERVICE_DIR/requirements.txt << EOF
fastapi==0.95.0
uvicorn==0.21.1
sqlalchemy==2.0.7
psycopg2-binary==2.9.5
pydantic==1.10.7
httpx==0.24.0
python-jose==3.3.0
python-multipart==0.0.6
pytest==7.3.1
pytest-cov==4.1.0
EOF

# base.Dockerfile
cat > $SERVICE_DIR/base.Dockerfile << EOF
FROM python:3.13

WORKDIR /app

# Установка зависимостей
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Переменная окружения для указания режима работы
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
EOF

# development.Dockerfile
cat > $SERVICE_DIR/development.Dockerfile << EOF
FROM ${SERVICE_NAME}_service-base

# Установка дополнительных инструментов для разработки
RUN pip install --no-cache-dir watchdog[watchmedo] pytest pytest-cov

# Не копируем код, так как он будет монтироваться как volume

# Запуск с автоматическим перезапуском при изменении файлов
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--reload", "--reload-dir", "/app", "--reload-dir", "/app/common"]
EOF

# production.Dockerfile
cat > $SERVICE_DIR/production.Dockerfile << EOF
FROM ${SERVICE_NAME}_service-base

# Копируем код приложения
COPY . .

# Запуск без режима автоматической перезагрузки
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
EOF

echo -e "${GREEN}Микросервис '$SERVICE_NAME' успешно создан в директории '$SERVICE_DIR'.${NC}"
echo -e "${YELLOW}Теперь вам нужно добавить сервис в docker-compose.yml.${NC}"
echo -e "Пример конфигурации для docker-compose.yml:"
echo -e "${YELLOW}
  ${SERVICE_NAME}_service-base:
    build:
      context: ./${SERVICE_DIR}
      dockerfile: base.Dockerfile
    image: ${SERVICE_NAME}_service-base
    command: echo \"Base image built\"

  ${SERVICE_NAME}_service:
    build:
      context: ./${SERVICE_DIR}
      dockerfile: \${ENV:-production}.Dockerfile
    environment:
      - DATABASE_URL=postgresql://\${POSTGRES_USER:-postgres}:\${POSTGRES_PASSWORD:-postgres}@postgres:5432/\${POSTGRES_DB:-competition_platform}
      - AUTH_SERVICE_URL=\${AUTH_SERVICE_URL:-http://auth_service:8000}
      - ENV=\${ENV:-production}
    volumes:
      - ./${SERVICE_DIR}:/app
      - ./common:/app/common
    depends_on:
      postgres:
        condition: service_healthy
      auth_service:
        condition: service_started
      ${SERVICE_NAME}_service-base:
        condition: service_completed_successfully
    networks:
      - app_network
    restart: unless-stopped
${NC}"

echo -e "${YELLOW}Также добавьте сервис в docker-compose.override.yml:${NC}"
echo -e "${YELLOW}
  ${SERVICE_NAME}_service:
    environment:
      - ENV=development
      - DEBUG=true
    volumes:
      - ./${SERVICE_DIR}:/app:delegated
      - ./common:/app/common:delegated
    ports:
      - \"8005:8000\"  # Выберите свободный порт
${NC}"
