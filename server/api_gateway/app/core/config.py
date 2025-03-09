import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    API_GATEWAY_PORT: int = 8000
    AUTH_SERVICE_URL: str = os.getenv("AUTH_SERVICE_URL", "http://auth_service:8000")
    ROOM_SERVICE_URL: str = os.getenv("ROOM_SERVICE_URL", "http://room_service:8000")
    COMPETITION_SERVICE_URL: str = os.getenv("COMPETITION_SERVICE_URL", "http://competition_service:8000")
    ACHIEVEMENT_SERVICE_URL: str = os.getenv("ACHIEVEMENT_SERVICE_URL", "http://achievement_service:8000")

settings = Settings()
