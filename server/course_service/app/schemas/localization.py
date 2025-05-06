"""
Pydantic schemas for localization-related data
"""
from datetime import datetime
from typing import Dict, Optional, List
from uuid import UUID

from pydantic import BaseModel, Field


class LocalizationBase(BaseModel):
    """Base schema for localization data"""
    namespace: str = Field(..., min_length=1, max_length=100)
    translations: Dict[str, Dict[str, str]] = Field(
        ...,
        description="Dictionary with language codes as keys and dictionaries of text keys and translations as values"
    )
    description: Optional[str] = Field(None, description="Optional description of this localization namespace")


class LocalizationCreate(LocalizationBase):
    """Schema for creating a localization"""
    pass


class LocalizationUpdate(BaseModel):
    """Schema for updating a localization"""
    namespace: Optional[str] = Field(None, min_length=1, max_length=100)
    translations: Optional[Dict[str, Dict[str, str]]] = Field(
        None,
        description="Dictionary with language codes as keys and dictionaries of text keys and translations as values"
    )
    description: Optional[str] = Field(None, description="Optional description of this localization namespace")


class LocalizationRead(LocalizationBase):
    """Schema for localization response"""
    id: UUID
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True


class TranslationStatistics(BaseModel):
    """Schema for translation statistics"""
    language: str
    total_keys: int
    translated_keys: int
    completion_percentage: float


class LocalizationWithStatistics(LocalizationRead):
    """Schema for localization with translation statistics"""
    statistics: List[TranslationStatistics]


class TranslationResponse(BaseModel):
    """Schema for translation response"""
    key: str
    text: str
    lang: str


class LanguageList(BaseModel):
    """Schema for list of available languages"""
    languages: list[str]
