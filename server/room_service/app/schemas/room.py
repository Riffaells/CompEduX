"""
Pydantic schemas for room service
"""
from datetime import datetime
from enum import Enum
from typing import Dict, List, Optional, Any, Union
from uuid import UUID

from pydantic import BaseModel, Field, field_validator, root_validator, model_validator

from app.models.room import RoomStatus, RoomParticipantRole


class RoomStatusEnum(str, Enum):
    """Enum for room status"""
    ACTIVE = "ACTIVE"
    ARCHIVED = "ARCHIVED"
    COMPLETED = "COMPLETED"
    PENDING = "PENDING"


class RoomParticipantRoleEnum(str, Enum):
    """Enum for participant roles in a room"""
    OWNER = "OWNER"
    TEACHER = "TEACHER"
    STUDENT = "STUDENT"
    OBSERVER = "OBSERVER"


class RoomBase(BaseModel):
    """Base schema for room operations"""
    name: Dict[str, str] = Field(..., description="Multilingual room name")
    description: Optional[Dict[str, str]] = Field(None, description="Multilingual room description")
    course_id: UUID
    owner_id: UUID
    status: RoomStatusEnum = RoomStatusEnum.PENDING
    max_participants: int = Field(0, ge=0, le=1000, description="Maximum number of participants (0 for unlimited)")
    settings: Optional[Dict[str, Any]] = Field(None, description="Room settings")
    
    @model_validator(mode='after')
    def validate_name_languages(self) -> 'RoomBase':
        """Validate that name contains at least one language"""
        if not self.name or len(self.name) == 0:
            raise ValueError("Name must contain at least one language")
        
        # Проверка на максимальную длину названий
        for lang, value in self.name.items():
            if len(value) > 200:  # Ограничение на 200 символов
                raise ValueError(f"Name in language {lang} exceeds maximum length (200 characters)")
        
        # Проверка максимальной длины описания
        if self.description:
            for lang, value in self.description.items():
                if len(value) > 5000:  # Ограничение на 5000 символов
                    raise ValueError(f"Description in language {lang} exceeds maximum length (5000 characters)")
        
        # Проверка размера settings
        if self.settings and len(str(self.settings)) > 10000:  # Ограничение на 10000 символов
            raise ValueError("Settings data is too large")
            
        return self


class RoomCreate(BaseModel):
    """Schema for creating a new room"""
    name: Dict[str, str] = Field(..., description="Multilingual room name")
    description: Optional[Dict[str, str]] = Field(None, description="Multilingual room description")
    course_id: UUID
    status: RoomStatusEnum = RoomStatusEnum.PENDING
    max_participants: int = Field(0, ge=0, le=1000, description="Maximum number of participants (0 for unlimited)")
    settings: Optional[Dict[str, Any]] = Field(None, description="Room settings")
    
    @model_validator(mode='after')
    def validate_name_languages(self) -> 'RoomCreate':
        """Validate that name contains at least one language"""
        if not self.name or len(self.name) == 0:
            raise ValueError("Name must contain at least one language")
        
        # Проверка на максимальную длину названий
        for lang, value in self.name.items():
            if len(value) > 200:  # Ограничение на 200 символов
                raise ValueError(f"Name in language {lang} exceeds maximum length (200 characters)")
        
        # Проверка максимальной длины описания
        if self.description:
            for lang, value in self.description.items():
                if len(value) > 5000:  # Ограничение на 5000 символов
                    raise ValueError(f"Description in language {lang} exceeds maximum length (5000 characters)")
        
        # Проверка размера settings
        if self.settings and len(str(self.settings)) > 10000:  # Ограничение на 10000 символов
            raise ValueError("Settings data is too large")
            
        return self


class RoomUpdate(BaseModel):
    """Schema for updating an existing room"""
    name: Optional[Dict[str, str]] = Field(None, description="Multilingual room name")
    description: Optional[Dict[str, str]] = Field(None, description="Multilingual room description")
    status: Optional[RoomStatusEnum] = None
    max_participants: Optional[int] = Field(None, ge=0, le=1000, description="Maximum number of participants (0 for unlimited)")
    settings: Optional[Dict[str, Any]] = Field(None, description="Room settings")
    
    @model_validator(mode='after')
    def validate_update_data(self) -> 'RoomUpdate':
        """Validate update data"""
        # Проверяем name, если оно указано
        if self.name is not None:
            if len(self.name) == 0:
                raise ValueError("If name is provided, it must contain at least one language")
            
            # Проверка на максимальную длину названий
            for lang, value in self.name.items():
                if len(value) > 200:
                    raise ValueError(f"Name in language {lang} exceeds maximum length (200 characters)")
        
        # Проверка максимальной длины описания
        if self.description is not None:
            for lang, value in self.description.items():
                if len(value) > 5000:
                    raise ValueError(f"Description in language {lang} exceeds maximum length (5000 characters)")
        
        # Проверка размера settings
        if self.settings is not None and len(str(self.settings)) > 10000:
            raise ValueError("Settings data is too large")
            
        return self


class RoomResponse(RoomBase):
    """Schema for room response"""
    id: UUID
    code: str
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class RoomDetailResponse(RoomResponse):
    """Schema for detailed room response"""
    # Можно добавить дополнительные поля, которые нужны только для детального просмотра
    pass


class RoomParticipantBase(BaseModel):
    """Base schema for room participant operations"""
    user_id: UUID
    role: RoomParticipantRoleEnum = RoomParticipantRoleEnum.STUDENT
    participant_metadata: Optional[Dict[str, Any]] = None


class RoomParticipantCreate(RoomParticipantBase):
    """Schema for adding a participant to a room"""
    pass


class RoomParticipantUpdate(BaseModel):
    """Schema for updating a room participant"""
    role: Optional[RoomParticipantRoleEnum] = None
    participant_metadata: Optional[Dict[str, Any]] = None


class RoomParticipantResponse(RoomParticipantBase):
    """Schema for room participant response"""
    room_id: UUID
    joined_at: datetime
    last_activity_at: datetime

    class Config:
        from_attributes = True


class RoomProgressBase(BaseModel):
    """Base schema for room progress operations"""
    user_id: UUID
    node_id: str
    status: str
    data: Optional[Dict[str, Any]] = None


class RoomProgressCreate(RoomProgressBase):
    """Schema for creating a new progress record"""
    pass


class RoomProgressUpdate(BaseModel):
    """Schema for updating a progress record"""
    status: Optional[str] = None
    data: Optional[Dict[str, Any]] = None


class RoomProgressResponse(RoomProgressBase):
    """Schema for progress record response"""
    id: UUID
    room_id: UUID
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class RoomCodeJoin(BaseModel):
    """Schema for joining a room by code"""
    code: str = Field(..., min_length=6, max_length=6)


class RoomJoinResponse(BaseModel):
    """Schema for room joining response"""
    room_id: UUID
    joined: bool
    message: str


class ParticipantList(BaseModel):
    """Schema for listing participants in a room"""
    participants: List[RoomParticipantResponse]
    total: int


class RoomList(BaseModel):
    """Schema for listing rooms"""
    items: List[RoomResponse]
    total: int
    page: int
    size: int
    pages: int
