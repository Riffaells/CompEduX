"""
Pytest configuration for room service tests
"""
import asyncio
import os
from typing import AsyncGenerator

import pytest
import pytest_asyncio
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import NullPool

from app.db.db import Base, get_db
from app.main import app
from fastapi.testclient import TestClient
from fastapi import FastAPI

# Настройка тестовой базы данных
TEST_DB_URL = os.environ.get("TEST_DATABASE_URL", "postgresql+asyncpg://postgres:postgres@localhost/test_room_db")


# Создаем тестовый движок базы данных
test_engine = create_async_engine(
    TEST_DB_URL,
    poolclass=NullPool,
    echo=False
)

# Создаем фабрику сессий
TestingSessionLocal = sessionmaker(
    autocommit=False,
    autoflush=False,
    bind=test_engine,
    class_=AsyncSession,
    expire_on_commit=False,
)


# Переопределяем зависимость получения БД для тестов
async def override_get_db() -> AsyncGenerator[AsyncSession, None]:
    async with TestingSessionLocal() as session:
        yield session


# Переопределяем зависимость для аутентификации в тестах
async def override_get_current_user():
    return {"id": "00000000-0000-0000-0000-000000000000", "email": "test@example.com"}


async def override_get_current_active_user():
    return {"id": "00000000-0000-0000-0000-000000000000", "email": "test@example.com", "is_active": True}


@pytest.fixture(scope="session")
def event_loop():
    """Create an instance of the default event loop for each test case."""
    loop = asyncio.get_event_loop_policy().new_event_loop()
    yield loop
    loop.close()


@pytest_asyncio.fixture(scope="function")
async def test_db():
    """Create test database tables before tests and drop them after."""
    # Создаем таблицы
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    
    yield
    
    # Удаляем таблицы
    async with test_engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)


@pytest.fixture(scope="function")
def test_app(test_db) -> FastAPI:
    """Get test app with overridden dependencies."""
    # Переопределяем зависимости
    app.dependency_overrides[get_db] = override_get_db
    
    return app


@pytest.fixture(scope="function")
def test_client(test_app) -> TestClient:
    """Get test client."""
    with TestClient(app) as client:
        yield client 