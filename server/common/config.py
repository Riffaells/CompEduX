"""
Base configuration settings for all CompEduX services
"""
import os
from typing import List, Optional, Dict, Any

from pydantic import field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class BaseServiceSettings(BaseSettings):
    """
    Base settings class that all service configurations should inherit from.

    Provides common configuration options used across all microservices.
    """
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    # Environment settings
    ENV: str = "development"
    DEBUG: bool = True
    VERSION: str = "0.1.0"

    # API settings
    API_V1_STR: str = "/api/v1"

    # CORS settings
    BACKEND_CORS_ORIGINS: List[str] = ["*"]

    @field_validator("BACKEND_CORS_ORIGINS", mode="before")
    def assemble_cors_origins(cls, v):
        """
        Parse CORS origins from string to list.
        """
        if isinstance(v, str) and not v.startswith("["):
            return [i.strip() for i in v.split(",")]
        return v

    # Database settings
    POSTGRES_USER: str = ""
    POSTGRES_PASSWORD: str = ""
    POSTGRES_DB: str = ""
    POSTGRES_HOST: str = "localhost"
    POSTGRES_PORT: str = "5432"
    POSTGRES_ADMIN_PASSWORD: str = "secure_password"

    @property
    def SQLALCHEMY_DATABASE_URI(self) -> str:
        """
        Database connection URI string with UTF-8 encoding
        """
        return f"postgresql://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@{self.POSTGRES_HOST}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}?client_encoding=utf8"

    # Service discovery and integration
    API_GATEWAY_URL: Optional[str] = "http://localhost:8000"
    AUTH_SERVICE_URL: Optional[str] = "http://localhost:8001"
    COURSE_SERVICE_URL: Optional[str] = "http://localhost:8002"

    # Rate limiting
    RATE_LIMIT_PER_MINUTE: int = 60

    def dict_with_protected_values(self) -> Dict[str, Any]:
        """
        Return settings as a dictionary with sensitive values masked
        """
        settings_dict = self.model_dump()
        # Mask sensitive values
        for key in ["POSTGRES_PASSWORD", "POSTGRES_ADMIN_PASSWORD"]:
            if key in settings_dict and settings_dict[key]:
                settings_dict[key] = "****"
        return settings_dict
