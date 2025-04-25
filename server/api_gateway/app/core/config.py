import sys
from typing import Optional, Dict

from common.config import BaseServiceSettings


class Settings(BaseServiceSettings):
    # API Gateway specific settings
    API_GATEWAY_HOST: str = "0.0.0.0"
    API_GATEWAY_PORT: int = 8000
    IS_WINDOWS: bool = sys.platform.startswith("win")

    # Security settings
    AUTH_SECRET_KEY: str

    # JWT settings
    JWT_ALGORITHM: str = "HS256"
    JWT_PUBLIC_KEY: str = ""  # In development, we use the same secret key for verification

    # Service URLs - taken directly from .env file
    AUTH_SERVICE_URL: str
    ROOM_SERVICE_URL: Optional[str] = None
    COMPETITION_SERVICE_URL: Optional[str] = None
    ACHIEVEMENT_SERVICE_URL: Optional[str] = None
    COURSE_SERVICE_URL: Optional[str] = None


settings = Settings()

# Configuration of routes to microservices
SERVICE_ROUTES: Dict[str, Dict[str, str]] = {
    "auth": {
        "base_url": settings.AUTH_SERVICE_URL,
        "prefix": "/auth",
        "health_endpoint": "/health"
    },
    # "room": {
    #     "base_url": settings.ROOM_SERVICE_URL,
    #     "prefix": "/rooms",
    #     "health_endpoint": "/health"
    # },
    # "competition": {
    #     "base_url": settings.COMPETITION_SERVICE_URL,
    #     "prefix": "/competitions",
    #     "health_endpoint": "/health"
    # },
    # "achievement": {
    #     "base_url": settings.ACHIEVEMENT_SERVICE_URL,
    #     "prefix": "/achievements",
    #     "health_endpoint": "/health"
    # },
    "course": {
        "base_url": settings.COURSE_SERVICE_URL,
        "prefix": "/courses",
        "health_endpoint": "/health"
    }
}
