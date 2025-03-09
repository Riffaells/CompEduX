from pydantic import BaseModel, EmailStr
from typing import Optional, List
from datetime import datetime
from enum import Enum

class UserRole(str, Enum):
    USER = "user"
    MODERATOR = "moderator"
    ADMIN = "admin"

class AuthType(str, Enum):
    GOOGLE = "google"
    GITHUB = "github"
    EMAIL = "email"

class FriendStatus(str, Enum):
    REQUESTED = "requested"
    ACCEPTED = "accepted"
    BLOCKED = "blocked"

class AuthProviderBase(BaseModel):
    provider: AuthType
    provider_id: str

class AuthProviderCreate(AuthProviderBase):
    pass

class AuthProvider(AuthProviderBase):
    id: int
    user_id: int
    created_at: datetime

    class Config:
        orm_mode = True

class AchievementBase(BaseModel):
    title: str
    description: str
    points: int

class AchievementCreate(AchievementBase):
    pass

class Achievement(AchievementBase):
    id: int

    class Config:
        orm_mode = True

class UserAchievementBase(BaseModel):
    achievement_id: int

class UserAchievement(UserAchievementBase):
    id: int
    user_id: int
    received_at: datetime
    achievement: Achievement

    class Config:
        orm_mode = True

class FriendshipBase(BaseModel):
    friend_id: int
    status: FriendStatus = FriendStatus.REQUESTED

class FriendshipCreate(FriendshipBase):
    pass

class FriendshipUpdate(BaseModel):
    status: FriendStatus

class Friendship(FriendshipBase):
    id: int
    user_id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        orm_mode = True

class UserBase(BaseModel):
    username: str
    email: Optional[EmailStr] = None

class UserCreate(UserBase):
    password: Optional[str] = None
    auth_provider: Optional[AuthProviderCreate] = None

class UserUpdate(BaseModel):
    username: Optional[str] = None
    email: Optional[EmailStr] = None
    password: Optional[str] = None
    avatar_url: Optional[str] = None
    role: Optional[UserRole] = None

class User(UserBase):
    id: int
    is_active: bool
    rating: int
    avatar_url: Optional[str] = None
    role: UserRole
    created_at: datetime
    updated_at: Optional[datetime] = None
    auth_providers: List[AuthProvider] = []
    achievements: List[UserAchievement] = []

    class Config:
        orm_mode = True

class UserDetail(User):
    pass

class Token(BaseModel):
    access_token: str
    refresh_token: Optional[str] = None
    token_type: str

class TokenData(BaseModel):
    username: Optional[str] = None
    user_id: Optional[int] = None
    role: Optional[UserRole] = None
