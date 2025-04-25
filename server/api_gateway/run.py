#!/usr/bin/env python3
"""
API Gateway service runner script
"""
import os
import sys

# Add path to the root directory
ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(ROOT_DIR)
if PROJECT_ROOT not in sys.path:
    sys.path.insert(0, PROJECT_ROOT)

# Set environment variable to prevent buffering
os.environ["PYTHONUNBUFFERED"] = "1"

# Initialize unified logger
from common.logger import initialize_logging

logger = initialize_logging("api_gateway", log_file="logs/api_gateway.log")

# Import app modules
from api_gateway.app.core.config import settings


def main():
    """
    Main function to run the API Gateway service.
    Sets up the environment and launches the FastAPI app with uvicorn.
    """
    # Load environment variables from .env file
    from dotenv import load_dotenv
    env_path = os.path.join(ROOT_DIR, '.env')
    load_dotenv(dotenv_path=env_path)

    # Use settings from config
    host = settings.API_GATEWAY_HOST
    port = settings.API_GATEWAY_PORT

    logger.info(f"[bold green]Starting API Gateway on {host}:{port}[/bold green]")
    logger.info(f"Environment: {settings.ENV}, Debug mode: {settings.DEBUG}")

    # Print service URLs for reference
    logger.info(f"Auth service URL: {settings.AUTH_SERVICE_URL}")
    if settings.COURSE_SERVICE_URL:
        logger.info(f"Course service URL: {settings.COURSE_SERVICE_URL}")

    # Run the API Gateway with uvicorn
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host=host,
        port=port,
        log_level="error",  # Minimal uvicorn log level
        access_log=False,  # Disable uvicorn access logs
        use_colors=False  # Disable uvicorn colors for Rich compatibility
    )


if __name__ == "__main__":
    main()
