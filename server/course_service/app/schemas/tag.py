"""
Pydantic schemas for tags and translations
"""
from datetime import datetime
from typing import Dict, List, Optional
from pydantic import BaseModel, Field, validator


class TagTranslationBase(BaseModel):
    """Base schema for tag translation"""
    language: str = Field(..., min_length=2, max_length=5, description="ISO language code (e.g., 'en', 'ru')")
    name: str = Field(..., min_length=1, max_length=100, description="Tag name in the specified language")


class TagTranslationCreate(TagTranslationBase):
    """Schema for creating a new tag translation"""
    pass


class TagTranslationUpdate(TagTranslationBase):
    """Schema for updating an existing tag translation"""
    language: Optional[str] = Field(None, min_length=2, max_length=5)
    name: Optional[str] = Field(None, min_length=1, max_length=100)


class TagTranslation(TagTranslationBase):
    """Schema for tag translation response"""
    id: int
    tag_id: int

    class Config:
        from_attributes = True


class TagBase(BaseModel):
    """Base schema for tag"""
    translations: List[TagTranslationCreate] = Field(..., description="List of translations for the tag")


class TagCreate(TagBase):
    """Schema for creating a new tag"""
    pass


class TagUpdate(BaseModel):
    """Schema for updating an existing tag"""
    translations: Optional[List[TagTranslationCreate]] = Field(None, description="List of translations for the tag")


class Tag(BaseModel):
    """Schema for tag response"""
    id: int
    created_at: datetime
    updated_at: datetime
    translations: List[TagTranslation] = []

    class Config:
        from_attributes = True


class TagWithName(BaseModel):
    """
    Schema for tag response with name dictionary for convenience
    This is useful for API responses where we want to return a flat structure
    """
    id: int
    name: Dict[str, str] = Field(default_factory=dict, description="Dictionary of language codes to tag names")

    class Config:
        from_attributes = True

    @validator('name', pre=True)
    def extract_name_from_translations(cls, v, values):
        """Extract name dictionary from translations if needed"""
        if isinstance(v, dict):
            return v

        # If we have the whole Tag object with translations
        if 'translations' in values:
            translations = values.get('translations', [])
            return {t.language: t.name for t in translations}

        return {}
