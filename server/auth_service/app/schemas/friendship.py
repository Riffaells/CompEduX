from datetime import datetime
from enum import Enum
from typing import Dict, List, Optional, Any, Union
from uuid import UUID

from pydantic import BaseModel, Field, EmailStr


class FriendshipStatusEnum(str, Enum):
    """Enum for friendship status"""
    PENDING = "PENDING"
    ACCEPTED = "ACCEPTED"
    REJECTED = "REJECTED"
    BLOCKED = "BLOCKED"


class FriendshipBase(BaseModel):
    """Base schema for friendship operations"""
    user_id: UUID
    friend_id: UUID


class FriendshipCreate(BaseModel):
    """Schema for creating a new friendship request"""
    friend_id: UUID


class FriendshipUpdate(BaseModel):
    """Schema for updating a friendship status"""
    status: FriendshipStatusEnum


class FriendshipResponse(BaseModel):
    """Schema for friendship response"""
    id: UUID
    user_id: UUID
    friend_id: UUID
    status: FriendshipStatusEnum
    requested_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class FriendMinimalSchema(BaseModel):
    """Minimal user info for friend lists"""
    id: UUID
    username: str
    email: Optional[str] = None
    
    class Config:
        from_attributes = True


class FriendDetailSchema(BaseModel):
    """Detailed user info with profile data for friend lists"""
    id: UUID
    username: str
    email: Optional[str] = None
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    avatar_url: Optional[str] = None
    
    class Config:
        from_attributes = True


class FriendWithStatusSchema(FriendDetailSchema):
    """User with friendship status"""
    friendship_id: UUID
    status: FriendshipStatusEnum
    requested_at: datetime
    direction: str  # "incoming" or "outgoing"
    
    class Config:
        from_attributes = True


class FriendListResponse(BaseModel):
    """Response for friend list endpoints"""
    items: List[FriendWithStatusSchema]
    total: int
    page: int
    size: int
    pages: int 