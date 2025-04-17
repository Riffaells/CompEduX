# -*- coding: utf-8 -*-
"""
Configuration settings for Auth Service
"""
from typing import List, Optional

from pydantic import AnyHttpUrl, field_validator
from common.config import BaseServiceSettings


class Settings(BaseServiceSettings):
    """
    Application settings for Auth Service.
    Inherits base settings from BaseServiceSettings.
    """
    # Service-specific settings
    PROJECT_NAME: str = "Auth Service"
    DESCRIPTION: str = "Authentication and User Management Service"

    # Security settings
    AUTH_SECRET_KEY: str
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    ADMIN_API_KEY: str = "admin_secret_key"

    # Database settings - with specific defaults for auth service
    POSTGRES_USER: str = "auth_user"
    POSTGRES_PASSWORD: str = "authpassword123"
    POSTGRES_DB: str = "auth_db"

    # OAuth settings
    GOOGLE_CLIENT_ID: Optional[str] = None
    GOOGLE_CLIENT_SECRET: Optional[str] = None
    GOOGLE_REDIRECT_URI: Optional[str] = None

    GITHUB_CLIENT_ID: Optional[str] = None
    GITHUB_CLIENT_SECRET: Optional[str] = None
    GITHUB_REDIRECT_URI: Optional[str] = None

    # Email settings
    SMTP_HOST: Optional[str] = None
    SMTP_PORT: Optional[int] = 587
    SMTP_USER: Optional[str] = None
    SMTP_PASSWORD: Optional[str] = None
    EMAILS_FROM_EMAIL: Optional[str] = None
    EMAILS_FROM_NAME: Optional[str] = None


# Create settings instance
settings = Settings()
