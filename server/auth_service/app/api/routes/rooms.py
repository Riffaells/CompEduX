from typing import List
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from ...db.session import get_db
from ...models.associations import RoomModel
from ...models.user import UserModel
from ...schemas import RoomSchema
from ...services.auth import get_current_user

router = APIRouter()


@router.get("/", response_model=List[RoomSchema])
async def read_rooms(
        skip: int = 0,
        limit: int = 100,
        current_user: UserModel = Depends(get_current_user),
        db: AsyncSession = Depends(get_db)
):
    """Get a list of all rooms"""
    query = select(RoomModel).offset(skip).limit(limit)
    result = await db.execute(query)
    rooms = result.scalars().all()
    return rooms


@router.get("/{room_id}", response_model=RoomSchema)
async def read_room(
        room_id: UUID,
        current_user: UserModel = Depends(get_current_user),
        db: AsyncSession = Depends(get_db)
):
    """Get room information by ID"""
    query = select(RoomModel).filter(RoomModel.id == room_id)
    result = await db.execute(query)
    room = result.scalars().first()

    if room is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )

    return room


# Schema for creating a room
class RoomCreateSchema(BaseModel):
    """
    Schema for creating a new room.
    """
    name: str
    description: str = None


@router.post("/", response_model=RoomSchema, status_code=status.HTTP_201_CREATED)
async def create_room(
        room: RoomCreateSchema,
        current_user: UserModel = Depends(get_current_user),
        db: AsyncSession = Depends(get_db)
):
    """Create a new room"""
    db_room = RoomModel(
        name=room.name,
        description=room.description
    )

    db.add(db_room)
    await db.commit()
    await db.refresh(db_room)

    # Add the creator to the room
    current_user.rooms.append(db_room)
    await db.commit()

    return db_room
