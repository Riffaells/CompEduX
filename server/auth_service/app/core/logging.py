"""
Logging redirects to common logger module
"""

from common.logger import get_logger, initialize_logging

# Create service logger
logger = get_logger("auth_service.core.logging")


def setup_logging(level: str = "info", enable_file_logging: bool = True, config_path: str = None) -> None:
    """
    Initialize logging for the auth service using common logger.

    Args:
        level: Logging level (debug, info, warning, error, critical)
        enable_file_logging: Enable file logging
        config_path: Path to logging config file
    """
    log_file = "logs/auth_service.log" if enable_file_logging else None
    return initialize_logging("auth_service", log_file=log_file)


def get_logger(name: str):
    """
    Get a logger from the common logger module.

    Args:
        name: Name of the module

    Returns:
        Logger instance
    """
    # Keep service prefix
    if not name.startswith("auth_service."):
        name = f"auth_service.{name}"

    return get_logger(name)


# Function for request logging
def log_request_info(request_id: str, method: str, path: str, **kwargs) -> None:
    """
    Log request information.

    Args:
        request_id: Unique request ID
        method: HTTP method
        path: Request path
        kwargs: Additional parameters for logging
    """
    req_logger = get_logger("auth_service.request")
    log_data = {
        "request_id": request_id,
        "method": method,
        "path": path,
        **kwargs
    }
    req_logger.info(f"Request: {log_data}")


# Function for response logging
def log_response_info(request_id: str, status_code: int, duration_ms: float, **kwargs) -> None:
    """
    Log response information.

    Args:
        request_id: Unique request ID
        status_code: Response status code
        duration_ms: Processing duration in milliseconds
        kwargs: Additional parameters for logging
    """
    resp_logger = get_logger("auth_service.response")
    log_data = {
        "request_id": request_id,
        "status_code": status_code,
        "duration_ms": duration_ms,
        **kwargs
    }

    if status_code >= 500:
        resp_logger.error(f"Response: {log_data}")
    elif status_code >= 400:
        resp_logger.warning(f"Response: {log_data}")
    else:
        resp_logger.info(f"Response: {log_data}")
