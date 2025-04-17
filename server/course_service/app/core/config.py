# -*- coding: utf-8 -*-
"""
Configuration settings for Course Service
"""
from typing import Optional

from common.config import BaseServiceSettings


class Settings(BaseServiceSettings):
    """
    Application settings for Course Service.
    Inherits base settings from BaseServiceSettings.
    """
    # Service-specific settings
    PROJECT_NAME: str = "Course Service"
    DESCRIPTION: str = "Course Management Service API"

    # Database settings - with specific defaults for course service
    POSTGRES_USER: str = "course_user"
    POSTGRES_PASSWORD: str = "coursepassword123"
    POSTGRES_DB: str = "course_db"


# Create settings instance
settings = Settings()
