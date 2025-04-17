"""
Config module for logger setup
"""

import logging
from dataclasses import dataclass
from pathlib import Path
from typing import List, Optional, Dict, Any
from datetime import timezone, timedelta, datetime

from rich.theme import Theme


# Application timezone (UTC+6)
APP_TIMEZONE = timezone(timedelta(hours=6))


@dataclass
class LoggerConfig:
    """
    Configuration class for logger setup
    """
    handlers: List[Any]
    level: int = logging.INFO
    format: Optional[str] = None
    date_format: Optional[str] = None
    console_width: int = 120
    markup: bool = True
    rich_tracebacks: bool = True
    tracebacks_show_locals: bool = True
    show_time: bool = False
    service_name: str = "service"
    log_file: Optional[Path] = None

    def __init__(
        self,
        service_name: str,
        level: str = "INFO",
        format_string: str = None,
        log_file: str = None,
        **kwargs
    ):
        """
        Initialize logger configuration.

        Args:
            service_name: Service name
            level: Logging level
            format_string: Custom format string
            log_file: Path to log file
            **kwargs: Additional parameters
        """
        self.service_name = service_name
        self.level = level
        self.format_string = format_string
        self.log_file = log_file
        self.extra_kwargs = kwargs


def get_default_theme() -> Theme:
    """
    Returns default color theme for rich logger.

    Returns:
        Theme: Rich Theme with color configuration
    """
    return Theme({
        "info": "green",
        "warning": "yellow",
        "error": "red",
        "critical": "red bold",
        "success": "green bold",
        "timestamp": "cyan",
        "name": "blue",
    })


def format_log_time() -> str:
    """
    Format current date and time as a string for logging.

    Returns:
        str: Formatted date and time in DD.MM.YYYY HH:MM:SS format
    """
    now = datetime.now()
    return now.strftime("%d.%m.%Y %H:%M:%S")
