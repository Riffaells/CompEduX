"""
Tests for room service API
"""
import pytest
from httpx import AsyncClient
from fastapi import status
import uuid

# Импорт для тестирования
from app.main import app


@pytest.mark.asyncio
async def test_health_check():
    """Test health check endpoint"""
    async with AsyncClient(app=app, base_url="http://test") as client:
        response = await client.get("/health")
        assert response.status_code == status.HTTP_200_OK
        data = response.json()
        assert "status" in data
        assert data["status"] == "ok"


@pytest.mark.asyncio
async def test_create_room(mocker):
    """Test room creation"""
    # Mock authentication
    mocker.patch(
        "app.api.deps.get_current_active_user",
        return_value={"id": uuid.uuid4(), "email": "test@example.com"}
    )
    
    # Mock database
    mocker.patch(
        "app.crud.room.RoomCRUD.create",
        return_value={
            "id": uuid.uuid4(),
            "name": {"en": "Test Room"},
            "code": "ABC123",
            "created_at": "2023-01-01T00:00:00Z",
            "updated_at": "2023-01-01T00:00:00Z"
        }
    )
    
    async with AsyncClient(app=app, base_url="http://test") as client:
        response = await client.post(
            "/",
            json={
                "name": {"en": "Test Room"},
                "description": {"en": "Test Description"},
                "course_id": str(uuid.uuid4()),
                "status": "PENDING",
                "max_participants": 10
            }
        )
        assert response.status_code == status.HTTP_201_CREATED
        data = response.json()
        assert "id" in data
        assert "name" in data
        assert data["name"]["en"] == "Test Room"


@pytest.mark.asyncio
async def test_join_room_by_code(mocker):
    """Test joining room by code"""
    # Mock authentication
    mocker.patch(
        "app.api.deps.get_current_active_user",
        return_value={"id": uuid.uuid4(), "email": "test@example.com"}
    )
    
    # Mock room retrieval
    room_id = uuid.uuid4()
    mocker.patch(
        "app.crud.room.RoomCRUD.get_by_code",
        return_value={"id": room_id, "code": "ABC123"}
    )
    
    # Mock participant addition
    mocker.patch(
        "app.crud.room.RoomParticipantCRUD.create",
        return_value=True
    )
    
    async with AsyncClient(app=app, base_url="http://test") as client:
        response = await client.post(
            "/join",
            json={"code": "ABC123"}
        )
        assert response.status_code == status.HTTP_200_OK
        data = response.json()
        assert data["joined"] is True
        assert "room_id" in data 