import logging
import logging.config
import sys
from typing import Dict, Any
import os
from pathlib import Path

# Базовая директория для логов
LOG_DIR = Path("logs")
LOG_DIR.mkdir(exist_ok=True)

# Уровни логирования
LOG_LEVELS = {
    "debug": logging.DEBUG,
    "info": logging.INFO,
    "warning": logging.WARNING,
    "error": logging.ERROR,
    "critical": logging.CRITICAL,
}

# Форматтер для логов
LOG_FORMAT = "%(asctime)s [%(processName)s: %(process)d] [%(threadName)s: %(thread)d] [%(levelname)s] %(name)s: %(message)s"
SIMPLE_FORMAT = "%(asctime)s [%(levelname)s] %(name)s: %(message)s"

def setup_logging(level: str = "info", enable_file_logging: bool = True, config_path: str = None) -> None:
    """
    Настройка логирования для всего приложения.

    Args:
        level: Уровень логирования (debug, info, warning, error, critical)
        enable_file_logging: Включить логирование в файл
        config_path: Путь к файлу конфигурации логирования
    """
    # Если указан путь к файлу конфигурации и файл существует, используем его
    if config_path and os.path.exists(config_path):
        logging.config.fileConfig(config_path, disable_existing_loggers=False)
        app_logger = logging.getLogger("app")
        app_logger.info(f"Logging configured from file: {config_path}")
        return

    # Получаем числовой уровень логирования
    log_level = LOG_LEVELS.get(level.lower(), logging.INFO)

    # Настройка корневого логгера
    root_logger = logging.getLogger()
    root_logger.setLevel(log_level)
    root_logger.handlers = []  # Очищаем существующие обработчики

    # Создаем форматтер для логов
    formatter = logging.Formatter(LOG_FORMAT)
    simple_formatter = logging.Formatter(SIMPLE_FORMAT)

    # Консольный вывод
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setFormatter(simple_formatter)
    console_handler.setLevel(log_level)
    root_logger.addHandler(console_handler)

    # Файловый вывод
    if enable_file_logging:
        file_handler = logging.FileHandler(LOG_DIR / "app.log")
        file_handler.setFormatter(formatter)
        file_handler.setLevel(log_level)
        root_logger.addHandler(file_handler)

        # Отдельный файл для ошибок
        error_file_handler = logging.FileHandler(LOG_DIR / "error.log")
        error_file_handler.setFormatter(formatter)
        error_file_handler.setLevel(logging.ERROR)
        root_logger.addHandler(error_file_handler)

    # Отключаем стандартные логгеры библиотек
    uvicorn_logger = logging.getLogger("uvicorn")
    uvicorn_logger.handlers = []
    uvicorn_logger.propagate = True

    uvicorn_access_logger = logging.getLogger("uvicorn.access")
    uvicorn_access_logger.handlers = []
    uvicorn_access_logger.propagate = True

    # Настраиваем логгер приложения
    app_logger = logging.getLogger("app")
    app_logger.setLevel(log_level)

    # Логируем информацию о настройке
    app_logger.info(f"Logging configured with level {level}")

def get_logger(name: str) -> logging.Logger:
    """
    Получение настроенного логгера.

    Args:
        name: Имя модуля или компонента

    Returns:
        Настроенный логгер
    """
    # Добавляем префикс приложения, если его еще нет
    if not name.startswith("app."):
        name = f"app.{name}"

    return logging.getLogger(name)

# Функция для логирования запросов
def log_request_info(request_id: str, method: str, path: str, **kwargs) -> None:
    """
    Логирование информации о запросе.

    Args:
        request_id: Уникальный ID запроса
        method: HTTP метод
        path: Путь запроса
        kwargs: Дополнительные параметры для логирования
    """
    logger = get_logger("request")
    log_data = {
        "request_id": request_id,
        "method": method,
        "path": path,
        **kwargs
    }
    logger.info(f"Request: {log_data}")

# Функция для логирования ответов
def log_response_info(request_id: str, status_code: int, duration_ms: float, **kwargs) -> None:
    """
    Логирование информации об ответе.

    Args:
        request_id: Уникальный ID запроса
        status_code: Код ответа
        duration_ms: Длительность обработки в миллисекундах
        kwargs: Дополнительные параметры для логирования
    """
    logger = get_logger("response")
    log_data = {
        "request_id": request_id,
        "status_code": status_code,
        "duration_ms": duration_ms,
        **kwargs
    }

    if status_code >= 500:
        logger.error(f"Response: {log_data}")
    elif status_code >= 400:
        logger.warning(f"Response: {log_data}")
    else:
        logger.info(f"Response: {log_data}")
