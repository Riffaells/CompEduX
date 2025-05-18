from typing import List, Dict, Any

from fastapi import APIRouter, Depends, HTTPException, status

from ...models.user import UserModel
from ...services.auth import get_current_user

router = APIRouter()


@router.get("/", response_model=Dict[str, Any])
async def read_rooms(current_user: UserModel = Depends(get_current_user)):
    """Get a list of all rooms"""
    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="Room functionality has been moved to the room_service microservice"
    )


@router.get("/{room_id}", response_model=Dict[str, Any])
async def read_room(room_id: str, current_user: UserModel = Depends(get_current_user)):
    """Get room information by ID"""
    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="Room functionality has been moved to the room_service microservice"
    )


@router.post("/", response_model=Dict[str, Any], status_code=status.HTTP_201_CREATED)
async def create_room(room: Dict[str, Any], current_user: UserModel = Depends(get_current_user)):
    """Create a new room"""
    raise HTTPException(
        status_code=status.HTTP_501_NOT_IMPLEMENTED,
        detail="Room functionality has been moved to the room_service microservice"
    )
