#!/usr/bin/env python3
"""
Скрипт запуска Room Service

Этот скрипт является основной точкой входа для запуска Room Service,
особенно в контейнеризованной среде. Он правильно настраивает пути,
загружает переменные окружения и запускает FastAPI приложение с uvicorn.
"""
import os
import socket
import sys
import time
from pathlib import Path

import uvicorn
from dotenv import load_dotenv

# Добавляем путь к корневой директории проекта
ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.dirname(ROOT_DIR)  # Папка server, где находится common
if PROJECT_ROOT not in sys.path:
    sys.path.insert(0, PROJECT_ROOT)  # Добавляем папку server в sys.path

# Печатаем для диагностики
print(f"Adding root directory to PYTHONPATH: {PROJECT_ROOT}")

# Создаем директорию для логов, если её нет
log_dir = os.path.join(ROOT_DIR, "logs")
if not os.path.exists(log_dir):
    try:
        os.makedirs(log_dir)
        print(f"Created log directory: {log_dir}")
    except Exception as e:
        print(f"Error creating log directory: {e}")

# Загружаем переменные из .env файла с корректным путем
env_path = Path(ROOT_DIR) / '.env'
print(f"Loading environment from: {env_path}")
load_dotenv(dotenv_path=env_path)

# Установка переменной окружения для предотвращения буферизации
os.environ["PYTHONUNBUFFERED"] = "1"

# Импортируем логгер после настройки путей
from common.logger import initialize_logging, setup_logging_interceptors

# Настраиваем перехватчики логов
interceptors = setup_logging_interceptors(
    intercept_uvicorn=True,
    intercept_fastapi=True,
    intercept_sql=True
)

# Инициализируем единый логгер
logger = initialize_logging("room_service", log_file="logs/room_service.log")

# Проверяем пути Python для отладки
logger.info(f"Python paths: {sys.path}")
logger.info(f"PROJECT_ROOT: {PROJECT_ROOT}")
logger.info(f"Checking common module: {os.path.exists(os.path.join(PROJECT_ROOT, 'common'))}")


def check_port_available(host, port):
    """Проверяет, доступен ли порт для использования"""
    try:
        # Пытаемся привязаться к порту для проверки доступности
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
            s.bind((host, port))
            return True, None
    except OSError as e:
        return False, e
    except Exception as e:
        return False, e


def find_available_port(start_port, end_port=None, host='127.0.0.1'):
    """Находит доступный порт в заданном диапазоне"""
    if end_port is None:
        end_port = start_port + 100  # Ищем в диапазоне 100 портов по умолчанию

    for port in range(start_port, end_port + 1):
        available, _ = check_port_available(host, port)
        if available:
            return port

    return None  # Не нашли свободный порт


def main():
    """
    Основная функция запуска сервиса.
    """
    logger.info("Starting Room Service")

    # Получаем настройки из переменных окружения
    host = os.environ.get("HOST", "0.0.0.0")
    port = int(os.environ.get("PORT", 8002))
    env = os.environ.get("ENV", "development")
    debug = os.environ.get("DEBUG", "false").lower() == "true"

    # Выводим информацию о конфигурации
    logger.info(f"Environment: {env}, Debug mode: {debug}")
    logger.info(f"Service configuration: host={host}, port={port}")

    # Проверяем доступность порта
    port_available, error = check_port_available(host, port)

    # Если порт не доступен и ошибка связана с правами доступа,
    # пробуем использовать localhost вместо 0.0.0.0
    if not port_available:
        logger.warning(f"Port {port} is not available on {host}: {error}")

        if "10013" in str(error) and host == "0.0.0.0":
            # Пробуем привязаться к localhost
            logger.info("Trying to bind to localhost instead of all interfaces")
            host = "127.0.0.1"
            port_available, error = check_port_available(host, port)

            if port_available:
                logger.info(f"Successfully bound to {host}:{port}")
            else:
                # Если и это не помогло, ищем свободный порт
                logger.warning(f"Cannot bind to {host}:{port}: {error}")
                new_port = find_available_port(port + 1, port + 1000, host)

                if new_port:
                    logger.info(f"Found available port: {new_port}")
                    port = new_port
                else:
                    logger.error("Could not find an available port. Exiting.")
                    sys.exit(1)

    logger.info(f"Final configuration: {host}:{port}")

    # Задержка для стабильности при запуске в контейнерах
    if os.environ.get("DOCKER_DELAY", "false").lower() == "true":
        delay = int(os.environ.get("DOCKER_STARTUP_DELAY_SEC", 2))
        logger.info(f"Docker mode detected. Waiting {delay} seconds before startup...")
        time.sleep(delay)

    try:
        # Запускаем uvicorn с экземпляром приложения
        logger.info("Starting uvicorn server")

        # Импортируем приложение из main.py
        from app.main import app

        # Запускаем uvicorn с минимальным уровнем логов
        uvicorn.run(
            app,  # Передаем экземпляр приложения напрямую
            host=host,
            port=port,
            log_level="critical",  # Минимальный уровень логов
            access_log=False,  # Отключаем access logs
            use_colors=False  # Отключаем цвета для чистоты логов
        )
    except Exception as e:
        logger.error(f"Failed to start Room Service: {str(e)}", exc_info=True)
        sys.exit(1)


if __name__ == "__main__":
    main()
