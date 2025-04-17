#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт запуска Course Service
"""
import os
import sys
import signal
import uvicorn
import logging
from pathlib import Path

# Добавляем путь к корневой директории проекта - один раз и без лишних сообщений
ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(ROOT_DIR)
if PROJECT_ROOT not in sys.path:
    sys.path.insert(0, PROJECT_ROOT)

# Флаг для контроля вывода PYTHONPATH
os.environ["PYTHONPATH_PRINTED"] = "1"

# Создаем директорию для логов, если её нет
log_dir = os.path.join(ROOT_DIR, "logs")
if not os.path.exists(log_dir):
    try:
        os.makedirs(log_dir)
        print(f"Created log directory: {log_dir}")
    except Exception as e:
        print(f"Error creating log directory: {e}")

# Установка переменной окружения для предотвращения буферизации
os.environ["PYTHONUNBUFFERED"] = "1"

# Полностью отключаем логи uvicorn для режима reload
loggers_to_silence = [
    "uvicorn.watchgram.watcher",
    "uvicorn.reload",
    "uvicorn.statreload",
    "watchfiles",
    "watchfiles.main",
    "watchgules",
    "watchgules.main"
]

for logger_name in loggers_to_silence:
    logger = logging.getLogger(logger_name)
    logger.handlers = []
    logger.propagate = False
    logger.setLevel(logging.CRITICAL)
    logger.addHandler(logging.NullHandler())

# Для полного отключения логов
logging.getLogger("watchfiles.main").disabled = True

# Инициализируем единый логгер
from common.logger import initialize_logging
logger = initialize_logging("course_service", log_file="logs/course_service.log")

def handle_signal(signum, frame):
    """Handle termination signals"""
    signals = {
        signal.SIGINT: "SIGINT",
        signal.SIGTERM: "SIGTERM"
    }
    signal_name = signals.get(signum, f"SIGNAL_{signum}")
    logger.info(f"Received {signal_name}, shutting down Course Service")
    sys.exit(0)

def main():
    # Регистрируем обработчики сигналов
    signal.signal(signal.SIGINT, handle_signal)
    signal.signal(signal.SIGTERM, handle_signal)

    logger.info("Starting Course Service")

    # Получаем порт из переменных окружения или используем порт по умолчанию
    port = int(os.getenv("PORT", 8002))
    host = os.getenv("HOST", "0.0.0.0")

    logger.info(f"Service configured to run on {host}:{port}")

    # Запускаем uvicorn с отключенным собственным логированием
    uvicorn.run(
        "app.main:app",
        host=host,
        port=port,
        #reload=True,
        log_level="error",
        access_log=False,
        use_colors=False
    )

if __name__ == "__main__":
    main()
