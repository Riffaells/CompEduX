"""
Module for intercepting logs from external libraries like uvicorn, fastapi, etc.
This helps prevent duplicate logs in the console.
"""

import logging
import sys
from typing import List, Optional


class LogInterceptor:
    """Intercepts logs from specific loggers and prevents them from being output."""

    def __init__(self, logger_names: List[str]):
        """
        Initialize the log interceptor for the specified loggers.

        Args:
            logger_names: List of logger names to intercept
        """
        self.logger_names = logger_names
        self.original_handlers = {}

    def intercept(self):
        """Intercept logs from the specified loggers."""
        for name in self.logger_names:
            logger = logging.getLogger(name)
            # Save original handlers
            self.original_handlers[name] = list(logger.handlers)
            # Remove all handlers
            logger.handlers = []
            # Disable propagation to parent loggers
            logger.propagate = False
            # Set level to CRITICAL to minimize logging
            logger.setLevel(logging.CRITICAL)
            # Add null handler
            logger.addHandler(logging.NullHandler())
            # Completely disable logger
            logger.disabled = True

    def restore(self):
        """Restore original handlers for the intercepted loggers."""
        for name, handlers in self.original_handlers.items():
            logger = logging.getLogger(name)
            logger.handlers = handlers
            logger.propagate = True
            logger.disabled = False


def create_uvicorn_interceptor() -> LogInterceptor:
    """
    Create an interceptor for uvicorn loggers.

    Returns:
        LogInterceptor instance
    """
    return LogInterceptor([
        "uvicorn",
        "uvicorn.access",
        "uvicorn.error",
        "uvicorn.asgi",
        "uvicorn.lifespan",
        "uvicorn.reload",
        "uvicorn.statreload",
        "watchfiles",
        "watchfiles.main",
    ])


def create_fastapi_interceptor() -> LogInterceptor:
    """
    Create an interceptor for FastAPI loggers.

    Returns:
        LogInterceptor instance
    """
    return LogInterceptor([
        "fastapi",
        "starlette",
        "starlette.applications",
        "starlette.middleware",
        "starlette.routing",
    ])


def create_sql_interceptor() -> LogInterceptor:
    """
    Create an interceptor for SQLAlchemy loggers.

    Returns:
        LogInterceptor instance
    """
    return LogInterceptor([
        "sqlalchemy",
        "sqlalchemy.engine",
        "sqlalchemy.engine.base",
        "sqlalchemy.dialects",
        "sqlalchemy.pool",
        "sqlalchemy.orm",
        "sqlalchemy.engine.base.Engine",
    ])


def setup_logging_interceptors(
    intercept_uvicorn: bool = True,
    intercept_fastapi: bool = True,
    intercept_sql: bool = True,
    additional_loggers: Optional[List[str]] = None
) -> List[LogInterceptor]:
    """
    Set up log interceptors for various components.

    Args:
        intercept_uvicorn: Whether to intercept uvicorn logs
        intercept_fastapi: Whether to intercept FastAPI logs
        intercept_sql: Whether to intercept SQLAlchemy logs
        additional_loggers: Additional logger names to intercept

    Returns:
        List of created interceptors
    """
    interceptors = []

    if intercept_uvicorn:
        interceptor = create_uvicorn_interceptor()
        interceptor.intercept()
        interceptors.append(interceptor)

    if intercept_fastapi:
        interceptor = create_fastapi_interceptor()
        interceptor.intercept()
        interceptors.append(interceptor)

    if intercept_sql:
        interceptor = create_sql_interceptor()
        interceptor.intercept()
        interceptors.append(interceptor)

    if additional_loggers:
        interceptor = LogInterceptor(additional_loggers)
        interceptor.intercept()
        interceptors.append(interceptor)

    return interceptors
