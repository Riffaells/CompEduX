#!/usr/bin/env python3
"""
Скрипт запуска Auth Service
"""
import os
import sys
import signal
import uvicorn
import logging

# Добавляем путь к корневой директории проекта
ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.dirname(ROOT_DIR))

# Установка переменной окружения для предотвращения буферизации
os.environ["PYTHONUNBUFFERED"] = "1"

# Полностью отключаем логи uvicorn для режима reload
# Отключаем старые и новые варианты логгеров
loggers_to_silence = [
    "uvicorn.watchgram.watcher",     # Старая версия
    "uvicorn.reload",                # Новая версия
    "uvicorn.statreload",            # Для изменений файлов
    "watchfiles",                    # Основная библиотека для отслеживания файлов
    "watchfiles.main",               # Конкретно модуль main формирует сообщения
    "watchgules",                    # Возможные вариации названия
    "watchgules.main"
]

for logger_name in loggers_to_silence:
    logger = logging.getLogger(logger_name)
    logger.handlers = []
    logger.propagate = False
    logger.setLevel(logging.CRITICAL)  # Только критические ошибки
    logger.addHandler(logging.NullHandler())

# Для полного отключения логов можно также напрямую отключить весь модуль
logging.getLogger("watchfiles.main").disabled = True

# Инициализируем единый логгер
from common.logger import initialize_logging
logger = initialize_logging("auth_service", log_file="logs/auth_service.log")

def main():
    logger.info("[bold green]Starting Auth Service[/bold green]")

    # Запускаем uvicorn с отключенным собственным логированием
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8001,
        # reload=True,
        log_level="error",       # Минимальный уровень логов uvicorn
        access_log=False,        # Отключаем логи доступа uvicorn
        use_colors=False         # Отключаем цвета uvicorn, чтобы Rich работал
    )

if __name__ == "__main__":
    main()
