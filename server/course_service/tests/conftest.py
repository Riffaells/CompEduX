"""
Конфигурация для pytest и настройка окружения тестирования
"""
import os
import sys
import pytest

# Добавляем корень проекта в sys.path для доступа к общим модулям
project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), '../..'))
if project_root not in sys.path:
    sys.path.insert(0, project_root)

# Добавляем директорию сервиса курсов в sys.path
course_service_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
if course_service_path not in sys.path:
    sys.path.insert(0, course_service_path)

# Здесь могут быть определены fixtures для тестов
@pytest.fixture
def api_base_url():
    """Возвращает базовый URL API для тестирования"""
    return "http://localhost:8000/api/v1"

@pytest.fixture
def test_headers():
    """Возвращает тестовые заголовки для запросов к API"""
    return {
        "Authorization": "Bearer test_token",
        "Content-Type": "application/json"
    }
