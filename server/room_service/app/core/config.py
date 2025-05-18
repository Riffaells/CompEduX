# -*- coding: utf-8 -*-
"""
Configuration settings for Room Service
"""
import os

from common.config import BaseServiceSettings


class Settings(BaseServiceSettings):
    """
    Application settings for Room Service.
    Inherits base settings from BaseServiceSettings.
    """
    # Общие настройки
    PROJECT_NAME: str = "CompEduX Room Service"
    DESCRIPTION: str = "Room management service for CompEduX Learning Platform"
    VERSION: str = "0.1.0"

    # Настройки сервера
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8003"))

    # Настройки логирования SQL
    DB_ECHO: bool = os.getenv("DB_ECHO", "false").lower() == "true"

    # Настройки аутентификации
    AUTH_SECRET_KEY: str = os.getenv("AUTH_SECRET_KEY", "your-secret-key-for-jwt-tokens-must-be-set-in-production")
    JWT_ALGORITHM: str = os.getenv("JWT_ALGORITHM", "HS256")

    # База данных - переопределяем значения из базового класса с конкретными для этого сервиса
    POSTGRES_USER: str = os.getenv("POSTGRES_USER", "room_user")
    POSTGRES_PASSWORD: str = os.getenv("POSTGRES_PASSWORD", "roompassword123")
    POSTGRES_DB: str = os.getenv("POSTGRES_DB", "room_db")
    CONNECT_TIMEOUT: int = int(os.getenv("CONNECT_TIMEOUT", "10"))  # Database connection timeout in seconds
    # Настройки драйвера базы данных
    DB_DRIVER: str = os.getenv("DB_DRIVER", "postgresql")
    ASYNC_DB_DRIVER: str = os.getenv("ASYNC_DB_DRIVER", "postgresql+asyncpg")

    # Redis для кеширования
    REDIS_ENABLED: bool = False  # Redis currently disabled
    REDIS_HOST: str = os.getenv("REDIS_HOST", "localhost")
    REDIS_PORT: int = int(os.getenv("REDIS_PORT", "6379"))
    REDIS_PASSWORD: str = os.getenv("REDIS_PASSWORD", "")
    REDIS_DB: int = int(os.getenv("REDIS_DB", "0"))
    CACHE_EXPIRE_IN_SECONDS: int = int(os.getenv("CACHE_EXPIRE_IN_SECONDS", "300"))

    @property
    def SQLALCHEMY_DATABASE_URI(self) -> str:
        """
        Синхронное подключение к базе данных
        """
        return f"{self.DB_DRIVER}://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@{self.POSTGRES_HOST}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"

    @property
    def ASYNC_SQLALCHEMY_DATABASE_URI(self) -> str:
        """
        Асинхронное подключение к базе данных
        """
        return f"{self.ASYNC_DB_DRIVER}://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@{self.POSTGRES_HOST}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"


# Create settings instance
settings = Settings()
