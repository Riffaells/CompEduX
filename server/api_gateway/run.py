#!/usr/bin/env python3
"""
Скрипт запуска API Gateway сервиса
"""
import os
import sys
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
logger = initialize_logging("api_gateway", log_file="logs/api_gateway.log")

def main():
    # Загружаем настройки из .env файла
    from dotenv import load_dotenv
    env_path = os.path.join(ROOT_DIR, '.env')
    load_dotenv(dotenv_path=env_path)

    # Получаем настройки хоста и порта из .env
    host = os.getenv("API_GATEWAY_HOST", "0.0.0.0")
    port = int(os.getenv("API_GATEWAY_PORT", 8000))

    logger.info(f"[bold green]Starting API Gateway on {host}:{port}[/bold green]")

    try:
        # Запускаем uvicorn с отключенным собственным логированием
        uvicorn.run(
            "app.main:app",
            host=host,
            port=port,
            reload=True,
            log_level="error",       # Минимальный уровень логов uvicorn
            access_log=False,        # Отключаем логи доступа uvicorn
            use_colors=False         # Отключаем цвета uvicorn, чтобы Rich работал
        )
    except KeyboardInterrupt:
        logger.info("[yellow]Shutdown by keyboard interrupt[/yellow]")
    except Exception as e:
        logger.error(f"[bold red]Error during startup: {str(e)}[/bold red]", exc_info=True)

if __name__ == "__main__":
    main()
