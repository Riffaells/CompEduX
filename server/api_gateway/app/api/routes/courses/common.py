import json
from typing import Dict, Optional
from urllib.parse import parse_qsl

from app.core.config import settings
from app.core.proxy import check_service_health, proxy_request, get_http_client
from fastapi import Request, Response, Depends
from common.logger import get_logger

# Настройка логирования
logger = get_logger("course_proxy")


# Создаем зависимость для проверки здоровья сервиса с кэшированием
async def check_course_service_health():
    """Проверяет доступность сервиса курсов"""
    await check_service_health(
        service_name="course",
        force=False
    )
    return True


# Функция-helper для выполнения запросов к сервису курсов
async def proxy_request_to_course(path: str, request: Request, extra_params: Dict = None) -> Response:
    """
    Проксирует запрос к сервису курсов и возвращает ответ.

    Args:
        path: Путь к API, который следует добавить к базовому URL сервиса
        request: Объект Request с параметрами запроса
        extra_params: Дополнительные параметры запроса

    Returns:
        Response: Ответ от сервиса курсов
    """
    course_service_url = settings.COURSE_SERVICE_URL
    original_path = path

    # Если нам нужно добавить дополнительные параметры
    if extra_params:
        # Получаем текущие параметры из path
        if '?' in path:
            base_path, query = path.split('?', 1)
            params = dict(parse_qsl(query))
            # Добавляем новые параметры
            params.update(extra_params)
            # Строим новый путь
            path = f"{base_path}?{'&'.join(f'{k}={v}' for k, v in params.items())}"
        else:
            # Если в path нет параметров, добавляем их
            path = f"{path}?{'&'.join(f'{k}={v}' for k, v in extra_params.items())}"

        # Логируем изменение пути
        logger.debug(f"Добавлены параметры {extra_params} к пути, итоговый путь: {path}")

    # Собираем все параметры для логирования
    all_params = {}
    if request and hasattr(request, "query_params"):
        all_params.update(dict(request.query_params))
    if extra_params:
        all_params.update(extra_params)

    logger.debug(f"Проксирование запроса к сервису курсов:")
    logger.debug(f"  URL: {course_service_url}")
    logger.debug(f"  Путь: {path}")
    logger.debug(f"  Метод: {request.method if request else 'GET'}")
    logger.debug(f"  Параметры: {all_params}")

    return await proxy_request(course_service_url, path, request)


# Функция для обработки списковых параметров
def prepare_list_param(param_name: str, values: list) -> Dict:
    """
    Подготавливает параметры в формате списка для передачи в запрос.

    Args:
        param_name: Имя параметра
        values: Список значений

    Returns:
        Словарь параметров в формате {param_name: value1, param_name: value2, ...}
    """
    if not values:
        return {}

    result = {}
    for value in values:
        # Если параметр с таким именем уже есть, добавляем еще один
        if param_name in result:
            if isinstance(result[param_name], list):
                result[param_name].append(str(value))
            else:
                result[param_name] = [result[param_name], str(value)]
        else:
            result[param_name] = str(value)

    return result


# Прямой запрос к проверке здоровья сервиса
async def direct_health_check() -> Response:
    """
    Выполняет прямой запрос к эндпоинту здоровья сервиса курсов

    Returns:
        Response: Ответ от сервиса курсов
    """
    course_service_url = settings.COURSE_SERVICE_URL
    client = await get_http_client()
    health_url = f"{course_service_url.rstrip('/')}/health"

    try:
        logger.debug(f"Прямой запрос к health URL: {health_url}")
        response = await client.get(health_url, timeout=5.0)

        # Создаем ответ FastAPI из ответа httpx
        return Response(
            content=response.content,
            status_code=response.status_code,
            headers=dict(response.headers)
        )
    except Exception as e:
        logger.error(f"Ошибка при прямом запросе к {health_url}: {str(e)}")
        return Response(
            content=json.dumps({"detail": "Course service health check failed"}),
            status_code=503,
            media_type="application/json"
        )
