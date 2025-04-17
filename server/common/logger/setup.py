"""
Module for setting up logging in microservices
"""

import logging
import sys
import os
from functools import lru_cache
from typing import Optional, Dict, Any
from pathlib import Path
import asyncio
from contextlib import asynccontextmanager

from rich.console import Console
from rich.logging import RichHandler
from rich.theme import Theme

from common.logger.config import LoggerConfig, get_default_theme, format_log_time


# Global console instance
console = Console(theme=get_default_theme())

# Cache for loggers to avoid recreation
_loggers: Dict[str, logging.Logger] = {}


def get_logger(name: str) -> logging.Logger:
    """
    Get an existing logger by name.
    If the logger doesn't exist yet, creates a new one.

    Args:
        name: Logger name

    Returns:
        Logger instance
    """
    if name in _loggers:
        return _loggers[name]

    # If logger is not in cache, create a new one
    logger = logging.getLogger(name)
    _loggers[name] = logger
    return logger


@lru_cache
def setup_rich_logger(
    service_name: str,
    log_level: int = logging.INFO,
    log_file: Optional[str] = None,
    **kwargs
) -> logging.Logger:
    """
    Set up a Rich logger with the specified parameters.

    Args:
        service_name: Service/logger name
        log_level: Logging level (default INFO)
        log_file: Path to log file (optional)
        **kwargs: Additional parameters

    Returns:
        Configured logger instance
    """
    if service_name in _loggers:
        return _loggers[service_name]

    # Create log directory if needed
    if log_file:
        os.makedirs(os.path.dirname(log_file), exist_ok=True)

    # Configure Rich console with theme
    console = Console(theme=get_default_theme())

    # Disable logging for uvicorn and other libraries
    for name in ["uvicorn", "uvicorn.access", "uvicorn.error",
                "fastapi", "httpx", "asyncio"]:
        logger = logging.getLogger(name)
        logger.handlers = []
        logger.propagate = False
        logger.addHandler(logging.NullHandler())

    # Configure Rich handler for beautiful console output
    rich_handler = RichHandler(
        console=console,
        show_time=True,
        show_path=False,
        show_level=True,
        markup=True,
        rich_tracebacks=True,
        tracebacks_show_locals=False,
        omit_repeated_times=False,
        log_time_format="%H:%M:%S"
    )

    # Level for console output
    rich_handler.setLevel(log_level)

    # Configure basic logger configuration
    handlers = [rich_handler]

    # Add file handler if log path is specified
    if log_file:
        file_handler = logging.FileHandler(log_file, encoding='utf-8')
        file_handler.setLevel(log_level)
        file_formatter = logging.Formatter(
            '%(asctime)s [%(levelname)s] %(name)s: %(message)s',
            datefmt='%Y-%m-%d %H:%M:%S'
        )
        file_handler.setFormatter(file_formatter)
        handlers.append(file_handler)

    # Clear previous log handlers
    logging.getLogger().handlers.clear()

    # Configure root logger
    logging.basicConfig(
        level=log_level,
        format="%(message)s",
        handlers=handlers,
        force=True
    )

    # Create and configure logger for the service
    logger = logging.getLogger(service_name)
    logger.setLevel(log_level)

    # Remove any previously created handlers
    logger.handlers.clear()
    # Don't duplicate messages to root logger
    logger.propagate = False
    # Add handlers directly
    for handler in handlers:
        logger.addHandler(handler)

    # Save logger in cache
    _loggers[service_name] = logger

    return logger


# Async context manager for FastAPI lifespan function
@asynccontextmanager
async def log_service_lifecycle(app_name: str, logger: logging.Logger):
    """
    Log service lifecycle events.

    Args:
        app_name: Application name
        logger: Logger instance
    """
    try:
        logger.info(f"[{format_log_time()}] [bold green]{app_name} starting up...[/bold green]")
        yield
    except asyncio.CancelledError:
        logger.info(f"[{format_log_time()}] [yellow]{app_name} shutdown initiated (cancel)[/yellow]")
    except Exception as e:
        logger.error(f"[{format_log_time()}] [bold red]{app_name} error: {str(e)}[/bold red]")
    finally:
        logger.info(f"[{format_log_time()}] [bold red]{app_name} shutting down...[/bold red]")
