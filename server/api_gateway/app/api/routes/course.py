import json
import uuid
from typing import Optional
from urllib.parse import parse_qsl

from app.core.config import settings
from app.core.proxy import check_service_health, proxy_request, proxy_docs_request, get_http_client
from fastapi import APIRouter, Request, Response, Depends, Path as PathParam, Query

from common.logger import get_logger

# Настройка логирования
logger = get_logger("course_proxy")

router = APIRouter()


# Создаем зависимость для проверки здоровья сервиса с кэшированием
async def check_course_service_health():
    """Проверяет доступность сервиса курсов"""
    await check_service_health(
        service_name="course",
        force=False
    )
    return True


# Функция-helper для выполнения запросов к сервису курсов
async def proxy_request_to_course(path: str, request: Request, extra_params: dict = None) -> Response:
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
def prepare_list_param(param_name: str, values: list) -> dict:
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


######################################################################
# ДОКУМЕНТАЦИЯ API
######################################################################

@router.get(
    "/docs",
    include_in_schema=True,
    summary="Документация сервиса курсов",
    description="Проксирует Swagger UI документацию сервиса курсов"
)
async def course_docs(request: Request, _: bool = Depends(check_course_service_health)):
    """Проксирование документации сервиса курсов"""
    logger.info("Proxying course service docs")
    # Используем специальную функцию для проксирования документации с правильным путем
    return await proxy_docs_request(settings.COURSE_SERVICE_URL, "api/v1/docs", request)


@router.get(
    "/redoc",
    include_in_schema=True,
    summary="ReDoc документация сервиса курсов",
    description="Проксирует ReDoc документацию сервиса курсов"
)
async def course_redoc(request: Request, _: bool = Depends(check_course_service_health)):
    """Проксирование ReDoc документации сервиса курсов"""
    logger.info("Proxying course service redoc")
    # Используем специальную функцию для проксирования документации с правильным путем
    return await proxy_docs_request(settings.COURSE_SERVICE_URL, "api/v1/redoc", request)


@router.get(
    "/openapi.json",
    include_in_schema=True,
    summary="OpenAPI спецификация сервиса курсов",
    description="Возвращает OpenAPI JSON схему сервиса курсов"
)
async def course_openapi(request: Request, _: bool = Depends(check_course_service_health)):
    """Проксирование OpenAPI JSON схемы сервиса курсов"""
    logger.info("Proxying course service OpenAPI schema")
    # Используем специальную функцию для проксирования документации с правильным путем
    return await proxy_docs_request(settings.COURSE_SERVICE_URL, "api/v1/openapi.json", request)


######################################################################
# ПРОВЕРКА ЗДОРОВЬЯ СЕРВИСА
######################################################################

@router.get(
    "/health",
    include_in_schema=True,
    summary="Проверка здоровья сервиса курсов",
    description="Проверяет доступность и работоспособность сервиса курсов",
    response_description="Статус здоровья сервиса курсов"
)
async def course_health(request: Request, _: bool = Depends(check_course_service_health)) -> Response:
    """Проверка здоровья сервиса курсов"""
    logger.info("Proxying course health check request")
    # Обходим проблему с некорректным проксированием, делаем прямой запрос
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


######################################################################
# API КУРСОВ
######################################################################

@router.get(
    "",
    include_in_schema=True,
    summary="Получение списка курсов или отдельного курса",
    description="Возвращает список курсов с пагинацией и возможностью фильтрации или информацию об одном курсе по UUID или slug",
    response_description="Список курсов с информацией о пагинации или подробная информация о курсе"
)
async def get_courses(
        request: Request,
        uuid: Optional[str] = Query(None, description="UUID курса для получения конкретного курса"),
        slug: Optional[str] = Query(None, description="Slug курса для получения конкретного курса"),
        language: Optional[str] = Query(None, description="Код языка для поиска (например, 'en', 'ru')"),
        page: Optional[int] = Query(0, description="Номер страницы (начиная с 0)"),
        size: Optional[int] = Query(10, description="Размер страницы (от 1 до 100)"),
        search: Optional[str] = Query(None, description="Поисковый запрос"),
        sort_by: Optional[str] = Query("created_at", description="Поле для сортировки"),
        sort_order: Optional[str] = Query("desc", description="Порядок сортировки: asc или desc"),
        author_id: Optional[str] = Query(None, description="Фильтр по ID автора"),
        tag_ids: Optional[list[str]] = Query(None, description="Список ID тегов для фильтрации"),
        from_date: Optional[str] = Query(None, description="Фильтр курсов, созданных после этой даты (формат ISO)"),
        to_date: Optional[str] = Query(None, description="Фильтр курсов, созданных до этой даты (формат ISO)"),
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение списка курсов или отдельного курса"""
    # Подготавливаем дополнительные параметры, которые могут быть пропущены
    extra_params = {}

    # Явно обрабатываем специальные параметры
    if uuid:
        extra_params["uuid"] = uuid
    if slug:
        extra_params["slug"] = slug
    if language:
        extra_params["language"] = language
    if tag_ids:
        # Для списковых параметров нужна особая обработка
        for tag_id in tag_ids:
            if "tag_ids" in extra_params:
                if isinstance(extra_params["tag_ids"], list):
                    extra_params["tag_ids"].append(tag_id)
                else:
                    extra_params["tag_ids"] = [extra_params["tag_ids"], tag_id]
            else:
                extra_params["tag_ids"] = tag_id

    logger.debug(f"Запрос на получение курсов с параметрами: {dict(request.query_params)}")
    return await proxy_request_to_course("", request, extra_params)


@router.get(
    "/{course_id}",
    include_in_schema=True,
    summary="Получение информации о курсе",
    description="Возвращает подробную информацию о курсе по его ID или slug",
    response_description="Детальная информация о курсе"
)
async def get_course(
        course_id: str = PathParam(..., description="ID курса или slug"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение информации о курсе по ID или slug"""
    # Для получения курса используем единый эндпоинт со строкой запроса
    try:
        # Проверяем, является ли это UUID
        uuid.UUID(course_id)
        return await proxy_request_to_course("", request, {"uuid": course_id})
    except ValueError:
        # Если не UUID, считаем что это slug
        return await proxy_request_to_course("", request, {"slug": course_id})


@router.post(
    "",
    include_in_schema=True,
    summary="Создание нового курса",
    description="Создает новый курс. Требует авторизации",
    response_description="Созданный курс с ID",
    status_code=201
)
async def create_course(
        request: Request,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Создание нового курса"""
    return await proxy_request_to_course("", request)


@router.put(
    "/{course_id}",
    include_in_schema=True,
    summary="Обновление информации о курсе",
    description="Обновляет информацию о существующем курсе. Требует авторизации",
    response_description="Обновленная информация о курсе"
)
async def update_course(
        course_id: str = PathParam(..., description="ID курса"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Обновление информации о курсе"""
    return await proxy_request_to_course(f"{course_id}", request)


@router.delete(
    "/{course_id}",
    include_in_schema=True,
    summary="Удаление курса",
    description="Удаляет существующий курс. Требует авторизации",
    response_description="Статус успеха операции",
    status_code=204
)
async def delete_course(
        course_id: str = PathParam(..., description="ID курса"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Удаление курса"""
    return await proxy_request_to_course(f"{course_id}", request)


@router.get(
    "/tag/{tag}",
    include_in_schema=True,
    summary="Поиск курсов по тегу",
    description="Возвращает список курсов, содержащих указанный тег",
    response_description="Список курсов с указанным тегом"
)
async def search_by_tag(
        tag: str = PathParam(..., description="Название тега"),
        page: Optional[int] = Query(0, description="Номер страницы (начиная с 0)"),
        size: Optional[int] = Query(10, description="Размер страницы (от 1 до 100)"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Поиск курсов по тегу"""
    return await proxy_request_to_course("", request, {"tag_ids": tag})


@router.get(
    "/author/{author_id}",
    include_in_schema=True,
    summary="Получение курсов по автору",
    description="Возвращает список курсов, созданных указанным автором",
    response_description="Список курсов автора"
)
async def get_courses_by_author(
        author_id: str = PathParam(..., description="ID автора"),
        page: Optional[int] = Query(0, description="Номер страницы (начиная с 0)"),
        size: Optional[int] = Query(10, description="Размер страницы (от 1 до 100)"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение курсов по автору"""
    return await proxy_request_to_course("", request, {"author_id": author_id})
