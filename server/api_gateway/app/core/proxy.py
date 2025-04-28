"""
Универсальный прокси-модуль для API Gateway

Модуль содержит функции для проксирования запросов к микросервисам,
проверки их доступности и управления кэшем состояния.
"""
import asyncio
import json
import time
from datetime import UTC
from typing import Dict, Tuple, Optional

import httpx
from app.core.config import SERVICE_ROUTES, settings
from fastapi import Request, Response, HTTPException

from common.logger import get_logger

# Настраиваем логгер
logger = get_logger("proxy")

# Глобальный клиент для повторного использования соединений
http_client: Optional[httpx.AsyncClient] = None

# Кэш результатов проверки здоровья сервисов
# Структура: {service_name: (timestamp, is_healthy)}
_service_health_cache: Dict[str, Tuple[float, bool]] = {}
_HEALTH_CHECK_INTERVAL = 30  # секунды между проверками


async def get_http_client() -> httpx.AsyncClient:
    """
    Возвращает глобальный HTTP-клиент или создает новый,
    если он еще не создан.
    """
    global http_client
    if http_client is None or http_client.is_closed:
        # Для Windows увеличиваем таймаут соединения
        timeout = 15.0 if settings.IS_WINDOWS else 10.0

        # Создаем клиент с настройками
        limits = httpx.Limits(max_connections=100, max_keepalive_connections=20)
        http_client = httpx.AsyncClient(
            timeout=timeout,
            limits=limits,
            http2=False  # Отключаем HTTP/2 для большей совместимости
        )
        logger.debug("Создан новый HTTP клиент")
    return http_client


async def proxy_request(base_url: str, path: str, request: Request, use_request_params: bool = True) -> Response:
    """
    Проксирует запрос к указанному сервису и возвращает ответ.
    Обеспечивает передачу всех заголовков, включая Authorization.

    Args:
        base_url: Базовый URL сервиса
        path: Путь к API (может содержать параметры запроса)
        request: Оригинальный FastAPI запрос
        use_request_params: Использовать ли параметры запроса из Request.
                         Если False, будут использоваться только параметры из path.

    Returns:
        Response: Ответ от сервиса
    """
    # Разделяем путь и строку запроса, если они есть в path
    target_path = path
    query_string = ""

    if '?' in path:
        target_path, query_string = path.split('?', 1)

    # Формируем URL для запроса к сервису
    target_url = f"{base_url.rstrip('/')}/{target_path.lstrip('/')}"

    # Добавляем параметры запроса
    params = {}
    if use_request_params and request is not None:
        # Получаем параметры из запроса
        params.update(dict(request.query_params))

    # Если в path есть параметры запроса, добавляем их или заменяем параметры из request
    if query_string:
        # Используем стандартную библиотеку для парсинга параметров
        from urllib.parse import parse_qsl
        path_params = dict(parse_qsl(query_string))
        for key, value in path_params.items():
            params[key] = value

    logger.debug(f"Proxy request - Target URL: {target_url}")
    logger.debug(f"Proxy request - Parameters: {params}")

    # Получаем метод запроса
    method = request.method if request else "GET"

    # Получаем заголовки запроса
    headers = dict(request.headers) if request else {}

    # Удаляем заголовки, которые могут вызвать проблемы при проксировании
    headers.pop('host', None)
    headers.pop('content-length', None)

    # Получаем тело запроса
    body = await request.body() if request else b""

    # Выполняем запрос к сервису
    client = await get_http_client()
    try:
        response = await client.request(
            method=method,
            url=target_url,
            params=params,
            headers=headers,
            content=body,
            timeout=10.0
        )
        logger.debug(f"Получен ответ от {target_url}: статус={response.status_code}")

        # Если сервис вернул ошибку, логируем подробности
        if response.status_code >= 400:
            # Пытаемся получить подробности ошибки из тела ответа
            try:
                error_details = response.json()
                logger.error(f"Сервис вернул ошибку {response.status_code}: {error_details}")
            except Exception:
                logger.error(f"Сервис вернул ошибку {response.status_code}, тело: {response.text[:200]}")

        # Создаем ответ FastAPI
        return Response(
            content=response.content,
            status_code=response.status_code,
            headers=dict(response.headers),
            media_type=response.headers.get('content-type')
        )
    except httpx.TimeoutException as e:
        logger.error(f"Таймаут при запросе к {target_url}: {str(e)}")
        return Response(
            content=json.dumps({"detail": f"Сервис недоступен: превышено время ожидания"}),
            status_code=504,  # Gateway Timeout
            media_type="application/json"
        )
    except httpx.ConnectError as e:
        logger.error(f"Ошибка соединения с {target_url}: {str(e)}")
        return Response(
            content=json.dumps({"detail": f"Сервис недоступен: ошибка подключения"}),
            status_code=503,  # Service Unavailable
            media_type="application/json"
        )
    except Exception as e:
        logger.error(f"Ошибка при проксировании запроса к {target_url}: {str(e)}", exc_info=True)
        return Response(
            content=json.dumps({"detail": f"Сервис недоступен: {str(e)}"}),
            status_code=503,
            media_type="application/json"
        )


async def retry_with_backoff(func, *args, max_retries=3, base_delay=0.5, **kwargs):
    """
    Выполняет функцию с повторными попытками и экспоненциальной задержкой.

    Args:
        func: Асинхронная функция для выполнения
        *args: Аргументы для функции
        max_retries: Максимальное количество повторных попыток
        base_delay: Базовая задержка между попытками (в секундах)
        **kwargs: Ключевые аргументы для функции

    Returns:
        Результат функции

    Raises:
        Exception: Последнее исключение от функции после всех попыток
    """
    last_exception = None

    for attempt in range(max_retries):
        try:
            return await func(*args, **kwargs)
        except (httpx.ConnectError, httpx.ConnectTimeout, ConnectionRefusedError, OSError) as e:
            last_exception = e

            # Увеличиваем задержку экспоненциально с небольшим случайным фактором
            import random
            delay = base_delay * (2 ** attempt) * (0.5 + random.random())

            logger.warning(
                f"Попытка {attempt + 1}/{max_retries} не удалась: {str(e)}. "
                f"Повторная попытка через {delay:.2f} секунд"
            )

            await asyncio.sleep(delay)
        except Exception as e:
            # Другие исключения не обрабатываем повторно
            raise e

    # Если все попытки не удались
    if last_exception:
        logger.error(f"Все попытки подключения ({max_retries}) не удались: {str(last_exception)}")
        raise last_exception


async def check_service_health(
        service_name: str,
        service_url: Optional[str] = None,
        health_endpoint: Optional[str] = None,
        force: bool = False,
        timeout: float = 3.0
) -> bool:
    """
    Проверяет доступность микросервиса с кэшированием результатов.

    Функция сначала проверяет кэш, и если результат не кэширован или истек срок кэша,
    выполняет HTTP запрос к эндпоинту проверки здоровья сервиса.

    Args:
        service_name: Имя сервиса согласно SERVICE_ROUTES
        service_url: URL сервиса (если не указан, берется из SERVICE_ROUTES)
        health_endpoint: Эндпоинт для проверки (если не указан, берется из SERVICE_ROUTES)
        force: Принудительная проверка, игнорируя кэш
        timeout: Таймаут запроса в секундах

    Returns:
        True если сервис доступен

    Raises:
        HTTPException(503): Если сервис недоступен
        ValueError: Если сервис не найден в конфигурации и не указаны service_url и health_endpoint
    """
    global _service_health_cache

    # Получаем конфигурацию сервиса, если не указаны параметры
    if service_url is None or health_endpoint is None:
        if service_name not in SERVICE_ROUTES:
            raise ValueError(f"Сервис '{service_name}' не найден в конфигурации SERVICE_ROUTES")

        service_config = SERVICE_ROUTES[service_name]
        service_url = service_url or service_config.get("base_url")
        health_endpoint = health_endpoint or service_config.get("health_endpoint", "/health")

    # Добавляем подробное логирование
    logger.debug(f"Проверка здоровья сервиса {service_name}:")
    logger.debug(f"  - URL: {service_url}")
    logger.debug(f"  - Endpoint: {health_endpoint}")

    # Пропускаем проверку, если URL сервиса не указан
    if not service_url:
        logger.warning(f"Сервис {service_name} не сконфигурирован (URL не указан)")
        return False

    # Формируем ключ для кэша
    cache_key = f"{service_name}:{service_url}"
    current_time = time.time()

    # Проверяем кэш, если не требуется принудительная проверка
    if not force and cache_key in _service_health_cache:
        last_check, is_healthy = _service_health_cache[cache_key]
        if current_time - last_check < _HEALTH_CHECK_INTERVAL:
            if not is_healthy:
                logger.warning(f"Сервис {service_name} недоступен (из кэша)")
                raise HTTPException(
                    status_code=503,
                    detail=f"Сервис {service_name} временно недоступен"
                )
            return True

    # Нормализуем URL
    if service_url.endswith("/"):
        service_url = service_url[:-1]

    # Формируем URL для проверки здоровья
    if health_endpoint.startswith("/"):
        health_url = f"{service_url}{health_endpoint}"
    else:
        health_url = f"{service_url}/{health_endpoint}"

    logger.debug(f"Полный URL для проверки здоровья: {health_url}")

    try:
        # Получаем HTTP-клиент
        client = await get_http_client()

        # Для Windows используем повторные попытки подключения
        if settings.IS_WINDOWS:
            async def _check_health():
                logger.debug(f"Отправка GET-запроса на {health_url}")
                return await client.get(
                    health_url,
                    timeout=timeout,
                    headers={
                        "User-Agent": "API-Gateway-HealthCheck",
                        "Accept": "application/json"
                    }
                )

            # Пробуем подключиться с повторными попытками
            response = await retry_with_backoff(
                _check_health,
                max_retries=3 if force else 2,  # Меньше попыток, если это регулярная проверка
                base_delay=0.3
            )
        else:
            # Для других платформ делаем обычный запрос
            logger.debug(f"Отправка GET-запроса на {health_url}")
            response = await client.get(
                health_url,
                timeout=timeout,
                headers={
                    "User-Agent": "API-Gateway-HealthCheck",
                    "Accept": "application/json"
                }
            )

        logger.debug(f"Получен ответ от {health_url}, статус {response.status_code}")
        if response.status_code == 200:
            logger.debug(f"Сервис {service_name} доступен")
            _service_health_cache[cache_key] = (current_time, True)
            return True

        # Если неуспешный статус-код
        error_message = f"Проверка здоровья сервиса {service_name} вернула статус {response.status_code}"
        logger.warning(error_message)
        _service_health_cache[cache_key] = (current_time, False)

        raise HTTPException(
            status_code=503,
            detail=f"Сервис {service_name} вернул ошибку: HTTP {response.status_code}"
        )

    except httpx.TimeoutException:
        error_message = f"Таймаут при проверке здоровья сервиса {service_name}"
        logger.error(error_message)
        _service_health_cache[cache_key] = (current_time, False)

        raise HTTPException(
            status_code=503,
            detail=f"Сервис {service_name} не отвечает (таймаут)"
        )

    except (httpx.ConnectError, ConnectionRefusedError) as e:
        # Особая обработка для Windows - будем более детально показывать ошибку
        if settings.IS_WINDOWS:
            error_message = (
                f"Не удалось подключиться к сервису {service_name} по адресу {health_url}. "
                f"Ошибка: {e.__class__.__name__}: {str(e)}"
            )
            logger.error(error_message)

            # Проверим хост и порт на доступность
            import socket
            host, port = health_url.split("://")[1].split("/")[0].split(":")
            port = int(port)

            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.settimeout(1)
            try:
                result = sock.connect_ex((host, port))
                if result != 0:
                    logger.error(f"Порт {port} недоступен на хосте {host}. Код ошибки: {result}")
            except Exception as sock_err:
                logger.error(f"Ошибка проверки сокета: {str(sock_err)}")
            finally:
                sock.close()

            _service_health_cache[cache_key] = (current_time, False)

            raise HTTPException(
                status_code=503,
                detail=(
                    f"Не удалось подключиться к сервису {service_name}. "
                    f"Проверьте, запущен ли сервис на {host}:{port}."
                )
            )
        else:
            error_message = f"Ошибка связи при проверке здоровья сервиса {service_name}: {str(e)}"
            logger.error(error_message)
            _service_health_cache[cache_key] = (current_time, False)

            raise HTTPException(
                status_code=503,
                detail=f"Сервис {service_name} недоступен: ошибка сети"
            )

    except httpx.RequestError as e:
        error_message = f"Ошибка связи при проверке здоровья сервиса {service_name}: {str(e)}"
        logger.error(error_message)
        _service_health_cache[cache_key] = (current_time, False)

        raise HTTPException(
            status_code=503,
            detail=f"Сервис {service_name} недоступен: ошибка сети"
        )

    except Exception as e:
        error_message = f"Непредвиденная ошибка при проверке здоровья сервиса {service_name}: {str(e)}"
        logger.error(error_message, exc_info=True)
        _service_health_cache[cache_key] = (current_time, False)

        raise HTTPException(
            status_code=503,
            detail=f"Сервис {service_name} временно недоступен"
        )


async def get_all_services_health() -> Dict:
    """
    Возвращает статус здоровья всех сервисов

    Returns:
        Словарь с информацией о состоянии всех сервисов:
        {
            "status": "ok" | "degraded" | "critical",
            "timestamp": ISO-formatted datetime string,
            "api_gateway": {
                "version": string,
                "uptime": seconds
            },
            "services": {
                "service_name": {
                    "status": "ok" | "error" | "unknown",
                    "message": string,
                    "url": string (опционально),
                    "response_time_ms": int (опционально)
                },
                ...
            }
        }
    """
    services_status = {}
    overall_status = "ok"
    critical_services_down = 0

    # Время начала проверки
    check_start_time = time.time()

    # Проверяем каждый сервис из конфигурации
    for service_name, config in SERVICE_ROUTES.items():
        service_url = config.get("base_url")
        health_endpoint = config.get("health_endpoint", "/health")

        if not service_url:
            services_status[service_name] = {
                "status": "unknown",
                "message": "Сервис не сконфигурирован (URL не указан)"
            }
            continue

        # Определяем, является ли сервис критически важным
        # (считаем auth сервис и основные сервисы критически важными)
        is_critical = service_name == "auth" or service_name in ["user", "room"]

        try:
            # Фиксируем время начала проверки
            service_check_start = time.time()

            # Быстрая проверка с небольшим таймаутом
            await check_service_health(
                service_name=service_name,
                service_url=service_url,
                health_endpoint=health_endpoint,
                force=True,  # Всегда делаем реальную проверку, а не берём из кэша
                timeout=1.5  # Короткий таймаут для быстрого ответа
            )

            # Вычисляем время ответа
            response_time_ms = int((time.time() - service_check_start) * 1000)

            services_status[service_name] = {
                "status": "ok",
                "message": "Сервис доступен",
                "url": service_url,
                "response_time_ms": response_time_ms
            }
        except HTTPException as e:
            # Вычисляем время ответа, если проверка заняла какое-то время
            response_time_ms = int(
                (time.time() - service_check_start) * 1000) if 'service_check_start' in locals() else None

            services_status[service_name] = {
                "status": "error",
                "message": e.detail,
                "url": service_url
            }

            if response_time_ms is not None:
                services_status[service_name]["response_time_ms"] = response_time_ms

            # Обновляем общий статус
            if is_critical:
                critical_services_down += 1
                overall_status = "critical"
            else:
                overall_status = "degraded" if overall_status != "critical" else overall_status

        except Exception as e:
            services_status[service_name] = {
                "status": "error",
                "message": f"Непредвиденная ошибка: {str(e)}",
                "url": service_url
            }

            # Обновляем общий статус
            if is_critical:
                critical_services_down += 1
                overall_status = "critical"
            else:
                overall_status = "degraded" if overall_status != "critical" else overall_status

    # Получаем версию и время запуска API Gateway
    try:
        import os
        import psutil
        from datetime import datetime

        # Пытаемся получить информацию о процессе
        process = psutil.Process(os.getpid())
        process_start_time = datetime.fromtimestamp(process.create_time())
        uptime_seconds = (datetime.now() - process_start_time).total_seconds()

        # Пытаемся получить версию из переменных окружения или другого источника
        version = os.environ.get("API_GATEWAY_VERSION", "0.1.0")

        api_gateway_info = {
            "version": version,
            "uptime": int(uptime_seconds)
        }
    except ImportError:
        # psutil может быть не установлен
        api_gateway_info = {
            "version": "unknown",
            "uptime": "unknown"
        }
    except Exception as e:
        api_gateway_info = {
            "version": "unknown",
            "uptime": "unknown",
            "error": str(e)
        }

    # Вычисляем общее время проверки
    total_check_time_ms = int((time.time() - check_start_time) * 1000)

    return {
        "status": overall_status,
        "timestamp": datetime.now(UTC).isoformat(),
        "api_gateway": api_gateway_info,
        "services": services_status,
        "stats": {
            "total_services": len(SERVICE_ROUTES),
            "available_services": sum(1 for s in services_status.values() if s.get("status") == "ok"),
            "critical_services_down": critical_services_down,
            "total_check_time_ms": total_check_time_ms
        }
    }


async def close_http_client():
    """Закрывает HTTP-клиент при завершении работы"""
    global http_client
    if http_client and not http_client.is_closed:
        await http_client.aclose()
        http_client = None


async def proxy_docs_request(base_url: str, docs_path: str, request: Request) -> Response:
    """
    Проксирует запрос к документации сервиса.
    Специально оптимизирован для Swagger UI и ReDoc.

    Args:
        base_url: Базовый URL сервиса
        docs_path: Путь к документации (docs, redoc, openapi.json)
        request: Оригинальный FastAPI запрос

    Returns:
        Response: Ответ от сервиса
    """
    # Формируем URL для запроса к сервису
    target_url = f"{base_url.rstrip('/')}/{docs_path.lstrip('/')}"

    logger.debug(f"Proxying docs request to: {target_url}")

    # Получаем метод запроса
    method = request.method

    # Получаем заголовки запроса
    headers = dict(request.headers)
    # Удаляем заголовки, которые могут вызвать проблемы при проксировании
    headers.pop('host', None)
    headers.pop('content-length', None)

    # Для запросов к документации добавляем заголовок, чтобы сервер не сжимал ответ
    headers['Accept-Encoding'] = 'identity'

    # Получаем параметры запроса
    params = dict(request.query_params)

    # Получаем тело запроса
    body = await request.body()

    # Выполняем запрос к сервису
    async with httpx.AsyncClient(follow_redirects=True) as client:
        try:
            logger.debug(f"Отправка {method} запроса на {target_url}")
            response = await client.request(
                method=method,
                url=target_url,
                headers=headers,
                params=params,
                content=body,
                timeout=10.0  # Устанавливаем таймаут
            )
            logger.debug(f"Получен ответ от {target_url}: статус={response.status_code}")

            # Создаем ответ FastAPI
            return Response(
                content=response.content,
                status_code=response.status_code,
                headers=dict(response.headers),
                media_type=response.headers.get('content-type')
            )
        except Exception as e:
            # Логируем ошибку
            logger.error(f"Error proxying docs request to {target_url}: {str(e)}")
            # Возвращаем ошибку сервера
            return Response(
                content=json.dumps({"detail": f"Documentation unavailable: {str(e)}"}),
                status_code=503,
                media_type="application/json"
            )
