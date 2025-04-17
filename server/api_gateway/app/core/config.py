from typing import List, Optional, Dict
import sys
import os

from pydantic import AnyHttpUrl, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    # Общие настройки
    ENV: str = "development"
    DEBUG: bool = True

    # Настройки API Gateway
    API_GATEWAY_HOST: str = "0.0.0.0"
    API_GATEWAY_PORT: int = 8000

    # Настройки API
    API_V1_STR: str = "/api/v1"

    # Настройки безопасности
    AUTH_SECRET_KEY: str

    # JWT настройки
    JWT_ALGORITHM: str = "HS256"
    JWT_PUBLIC_KEY: str = ""  # В development используем для проверки тот же секретный ключ что и для подписи

    # Настройки CORS
    BACKEND_CORS_ORIGINS: List[str] = ["*"]

    @field_validator("BACKEND_CORS_ORIGINS", mode="before")
    def assemble_cors_origins(cls, v):
        if isinstance(v, str) and not v.startswith("["):
            return [i.strip() for i in v.split(",")]
        return v

    # URL сервисов - берутся напрямую из .env файла
    AUTH_SERVICE_URL: str
    ROOM_SERVICE_URL: Optional[str]
    COMPETITION_SERVICE_URL: Optional[str]
    ACHIEVEMENT_SERVICE_URL: Optional[str]
    COURSE_SERVICE_URL: Optional[str]


settings = Settings()

# Конфигурация маршрутов к микросервисам
SERVICE_ROUTES: Dict[str, Dict[str, str]] = {
    "auth": {
        "base_url": settings.AUTH_SERVICE_URL,
        "prefix": "/auth",
        "health_endpoint": "/health"
    },
    "room": {
        "base_url": settings.ROOM_SERVICE_URL,
        "prefix": "/rooms",
        "health_endpoint": "/health"
    },
    "competition": {
        "base_url": settings.COMPETITION_SERVICE_URL,
        "prefix": "/competitions",
        "health_endpoint": "/health"
    },
    "achievement": {
        "base_url": settings.ACHIEVEMENT_SERVICE_URL,
        "prefix": "/achievements",
        "health_endpoint": "/health"
    },
    "course": {
        "base_url": settings.COURSE_SERVICE_URL,
        "prefix": "/courses",
        "health_endpoint": "/health"
    }
}
