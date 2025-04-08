import os
from typing import List, Optional

from pydantic import AnyHttpUrl, PostgresDsn, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """
    Application settings.

    All settings can be overridden by environment variables.
    """
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    # General settings
    ENV: str = "development"
    DEBUG: bool = True
    PROJECT_NAME: str = "Auth Service"
    VERSION: str = "0.1.0"
    DESCRIPTION: str = "Authentication and User Management Service"

    # API settings
    API_V1_STR: str = "/api/v1"

    # Security settings
    AUTH_SECRET_KEY: str
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 30
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    ADMIN_API_KEY: str = "admin_secret_key"  # API ключ для административных эндпоинтов

    # CORS settings
    BACKEND_CORS_ORIGINS: List[str] = ["*"]

    @field_validator("BACKEND_CORS_ORIGINS", mode="before")
    def assemble_cors_origins(cls, v):
        """
        Parse CORS origins from string to list.

        Allows setting CORS origins as a comma-separated string in .env file.
        """
        if isinstance(v, str) and not v.startswith("["):
            return [i.strip() for i in v.split(",")]
        return v

    # Database settings
    POSTGRES_USER: str
    POSTGRES_PASSWORD: str
    POSTGRES_DB: str
    POSTGRES_HOST: str = "localhost"
    POSTGRES_PORT: str = "5432"
    POSTGRES_ADMIN_PASSWORD: str = "secure_password"  # Пароль администратора PostgreSQL

    @property
    def SQLALCHEMY_DATABASE_URI(self) -> str:
        """
        Database connection URI string.
        In development environment, this may return an empty string to use SQLite instead.
        In production, PostgreSQL will be used.
        """
        # If we're in development mode and PostgreSQL settings are not available,
        # we'll allow SQLite to be used by returning an empty string
        # (The main.py will set up the SQLite path if needed)
        if os.getenv("ENV") == "development":
            try:
                return self.construct_postgres_uri()
            except Exception:
                return ""

        # In production, always use PostgreSQL
        return self.construct_postgres_uri()

    def construct_postgres_uri(self) -> str:
        """Helper method to construct PostgreSQL URI"""
        return f"postgresql://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@{self.POSTGRES_HOST}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"

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

    # Rate limiting
    RATE_LIMIT_PER_MINUTE: int = 60


# Create settings instance
settings = Settings()
