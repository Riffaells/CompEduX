from typing import List, Optional

from pydantic import AnyHttpUrl, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    # Общие настройки
    ENV: str = "development"
    DEBUG: bool = True

    # Настройки API
    API_V1_STR: str = "/api/v1"

    # Настройки безопасности
    AUTH_SECRET_KEY: str

    # Настройки CORS
    BACKEND_CORS_ORIGINS: List[str] = ["*"]

    @field_validator("BACKEND_CORS_ORIGINS", mode="before")
    def assemble_cors_origins(cls, v):
        if isinstance(v, str) and not v.startswith("["):
            return [i.strip() for i in v.split(",")]
        return v

    # URL сервисов
    AUTH_SERVICE_URL: str = "http://auth_service:8000"
    ROOM_SERVICE_URL: Optional[str] = "http://room_service:8000"
    COMPETITION_SERVICE_URL: Optional[str] = "http://competition_service:8000"
    ACHIEVEMENT_SERVICE_URL: Optional[str] = "http://achievement_service:8000"


settings = Settings()
