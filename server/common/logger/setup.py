"""
Module for setting up logging in microservices
"""

import asyncio
import logging
import os
from contextlib import asynccontextmanager
from functools import lru_cache, wraps
from typing import Optional, Dict, Any, Callable, TypeVar

from rich.console import Console
from rich.logging import RichHandler

from common.logger.config import get_default_theme, format_sql_error

# Type variables for function annotations
T = TypeVar('T')
F = TypeVar('F', bound=Callable[..., Any])

# Global console instance
console = Console(theme=get_default_theme())

# Cache for loggers to avoid recreation
_loggers: Dict[str, logging.Logger] = {}


class SqlFormattingLogger(logging.Logger):
    """Logger that formats SQL errors for better readability"""

    def error(self, msg: Any, *args, **kwargs):
        """Format SQL error messages before logging"""
        if isinstance(msg, str) and ('SQL:' in msg or 'sqlalchemy' in msg.lower()):
            msg = format_sql_error(msg)
        super().error(msg, *args, **kwargs)

    def critical(self, msg: Any, *args, **kwargs):
        """Format SQL error messages before logging"""
        if isinstance(msg, str) and ('SQL:' in msg or 'sqlalchemy' in msg.lower()):
            msg = format_sql_error(msg)
        super().critical(msg, *args, **kwargs)


# Register our custom logger class
logging.setLoggerClass(SqlFormattingLogger)


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

    # Cast to SqlFormattingLogger to help type checking
    if isinstance(logger, SqlFormattingLogger):
        sql_logger = logger
    else:
        # Если стандартный логгер уже был создан до регистрации нашего класса,
        # мы не можем изменить его тип, но можем подменить методы
        original_error = logger.error
        original_critical = logger.critical

        @wraps(original_error)
        def wrapped_error(msg, *args, **kwargs):
            if isinstance(msg, str) and ('SQL:' in msg or 'sqlalchemy' in msg.lower()):
                msg = format_sql_error(msg)
            return original_error(msg, *args, **kwargs)

        @wraps(original_critical)
        def wrapped_critical(msg, *args, **kwargs):
            if isinstance(msg, str) and ('SQL:' in msg or 'sqlalchemy' in msg.lower()):
                msg = format_sql_error(msg)
            return original_critical(msg, *args, **kwargs)

        logger.error = wrapped_error
        logger.critical = wrapped_critical
        sql_logger = logger

    _loggers[name] = sql_logger
    return sql_logger


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
    rich_kwargs = {
        'console': console,
        'show_time': True,
        'show_path': False,
        'show_level': True,
        'markup': True,
        'rich_tracebacks': True,
        'tracebacks_show_locals': False,
        'omit_repeated_times': False,
        'log_time_format': "%d.%m.%Y %H:%M:%S",
    }

    # Проверяем поддержку дополнительных параметров
    # для совместимости с разными версиями rich
    import inspect

    # Получаем сигнатуру конструктора RichHandler
    rich_params = inspect.signature(RichHandler.__init__).parameters

    # Проверяем наличие дополнительных параметров в сигнатуре
    if 'highlighter' in rich_params:
        rich_kwargs['highlighter'] = None

    if 'enable_link_path' in rich_params:
        rich_kwargs['enable_link_path'] = False

    if 'word_wrap' in rich_params:
        rich_kwargs['word_wrap'] = True

    # Создаем handler с поддерживаемыми параметрами
    rich_handler = RichHandler(**rich_kwargs)

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
        logger.info(f"[bold green]{app_name} starting up...[/bold green]")
        yield
    except asyncio.CancelledError:
        logger.info(f"[yellow]{app_name} shutdown initiated (cancel)[/yellow]")
    except Exception as e:
        logger.error(f"[bold red]{app_name} error: {str(e)}[/bold red]")
    finally:
        logger.info(f"[bold red]{app_name} shutting down...[/bold red]")
