"""
Common logging package for CompEduX microservices.

This module provides unified logging functionality with Rich formatting
and optional file logging for all microservices in the CompEduX platform.
"""

# Then import config which may use get_logger
from common.logger.config import LoggerConfig, get_default_theme, format_log_time, format_sql_error
# Import the interceptor module
from common.logger.interceptor import (
    LogInterceptor,
    setup_logging_interceptors,
    create_uvicorn_interceptor,
    create_fastapi_interceptor,
    create_sql_interceptor
)
# First import setup which contains get_logger
from common.logger.setup import setup_rich_logger, get_logger
from common.logger.startup import initialize_logging

__all__ = [
    "setup_rich_logger",
    "get_logger",
    "LoggerConfig",
    "get_default_theme",
    "initialize_logging",
    "format_log_time",
    "format_sql_error",
    "LogInterceptor",
    "setup_logging_interceptors",
    "create_uvicorn_interceptor",
    "create_fastapi_interceptor",
    "create_sql_interceptor"
]
