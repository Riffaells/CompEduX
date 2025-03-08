from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from enum import Enum

class RoomType(str, Enum):
    PUBLIC = "public"
    PRIVATE = "private"
    INHERITED = "inherited"

class RoomBase(BaseModel):
    name: str
    description: str
    type: RoomType = RoomType.PUBLIC
    parent_id: Optional[int] = None

class RoomCreate(RoomBase):
    pass

class Room(RoomBase):
    id: int
    owner_id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        orm_mode = True

class RoomMemberBase(BaseModel):
    room_id: int
    user_id: int
    role: str = "member"

class RoomMemberCreate(RoomMemberBase):
    pass

class RoomMember(RoomMemberBase):
    id: int
    joined_at: datetime

    class Config:
        orm_mode = True

class InvitationBase(BaseModel):
    room_id: int
    user_id: int

class InvitationCreate(InvitationBase):
    expires_at: Optional[datetime] = None

class Invitation(InvitationBase):
    id: int
    inviter_id: int
    created_at: datetime
    expires_at: Optional[datetime] = None
    is_accepted: bool

    class Config:
        orm_mode = True
