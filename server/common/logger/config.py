"""
Config module for logger setup
"""

import logging
from dataclasses import dataclass
from datetime import timezone, timedelta, datetime
from pathlib import Path
from typing import List, Optional, Any

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


def format_sql_error(error_msg: str) -> str:
    """
    Format SQL error messages to make them more readable.

    Args:
        error_msg: Original error message

    Returns:
        Formatted error message
    """
    # Если это не строка или пустая строка, возвращаем как есть
    if not isinstance(error_msg, str) or not error_msg:
        return str(error_msg)

    # Заменяем множественные пробелы на один
    msg = ' '.join(error_msg.split())

    # Если сообщение слишком длинное, форматируем его
    if len(msg) > 100:
        # Находим SQL запрос
        sql_start = msg.find('[SQL:')
        if sql_start > 0:
            # Разбиваем на части: сообщение об ошибке и SQL запрос
            error_part = msg[:sql_start].strip()
            sql_part = msg[sql_start:].strip()

            # Форматируем SQL запрос
            sql_part = sql_part.replace('[SQL:', '\n[SQL:')
            sql_part = sql_part.replace('VALUES', '\nVALUES')
            sql_part = sql_part.replace('FROM', '\nFROM')
            sql_part = sql_part.replace('WHERE', '\nWHERE')
            sql_part = sql_part.replace('JOIN', '\nJOIN')
            sql_part = sql_part.replace('GROUP BY', '\nGROUP BY')
            sql_part = sql_part.replace('ORDER BY', '\nORDER BY')

            # Если есть параметры, добавляем их на новой строке
            params_start = sql_part.find('[parameters:')
            if params_start > 0:
                params_part = sql_part[params_start:].strip()
                sql_part = sql_part[:params_start].strip() + '\n' + params_part

            # Если есть ссылка на документацию, добавляем её на новой строке
            background_start = msg.find('(Background on this error at:')
            if background_start > 0:
                background_part = '\n' + msg[background_start:].strip()
                if params_start > 0 and background_start > params_start:
                    # Ссылка находится в части параметров
                    pass
                else:
                    # Добавляем ссылку отдельно если она не в параметрах
                    sql_part = sql_part.replace(msg[background_start:].strip(), background_part)

            return f"{error_part}\n{sql_part}"

    return msg
