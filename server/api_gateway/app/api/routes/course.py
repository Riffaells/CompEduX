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


######################################################################
# API ДРЕВА ТЕХНОЛОГИЙ (TECHNOLOGY TREE)
######################################################################

@router.get(
    "/{course_id}/tree",
    include_in_schema=True,
    summary="Получение древа технологий курса",
    description="Возвращает структуру древа технологий для указанного курса",
    response_description="Данные древа технологий"
)
async def get_technology_tree(
        course_id: str = PathParam(..., description="ID курса"),
        language: Optional[str] = Query(None, description="Код языка для локализованного контента"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение древа технологий для курса"""
    return await proxy_request_to_course(f"{course_id}/tree", request)


@router.post(
    "/{course_id}/tree",
    include_in_schema=True,
    summary="Создание древа технологий для курса",
    description="Создает новое древо технологий для курса. Требует авторизации",
    response_description="Созданное древо технологий",
    status_code=201
)
async def create_technology_tree(
        course_id: str = PathParam(..., description="ID курса"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Создание древа технологий для курса"""
    return await proxy_request_to_course(f"{course_id}/tree", request)


@router.put(
    "/{course_id}/tree",
    include_in_schema=True,
    summary="Обновление древа технологий",
    description="Обновляет существующее древо технологий для курса. Требует авторизации",
    response_description="Обновленное древо технологий"
)
async def update_technology_tree(
        course_id: str = PathParam(..., description="ID курса"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Обновление древа технологий для курса"""
    return await proxy_request_to_course(f"{course_id}/tree", request)


@router.delete(
    "/{course_id}/tree",
    include_in_schema=True,
    summary="Удаление древа технологий",
    description="Удаляет древо технологий для курса. Требует авторизации",
    response_description="",
    status_code=204
)
async def delete_technology_tree(
        course_id: str = PathParam(..., description="ID курса"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Удаление древа технологий для курса"""
    return await proxy_request_to_course(f"{course_id}/tree", request)


@router.patch(
    "/{course_id}/tree/data",
    include_in_schema=True,
    summary="Обновление данных древа технологий",
    description="Обновляет только данные древа технологий без изменения других полей. Требует авторизации",
    response_description="Обновленное древо технологий"
)
async def update_technology_tree_data(
        course_id: str = PathParam(..., description="ID курса"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Обновление данных древа технологий"""
    return await proxy_request_to_course(f"{course_id}/tree/data", request)


@router.post(
    "/{course_id}/tree/nodes",
    include_in_schema=True,
    summary="Добавление узла в древо технологий",
    description="Добавляет новый узел в древо технологий. Требует авторизации",
    response_description="Результат операции с добавленным узлом"
)
async def add_tree_node(
        course_id: str = PathParam(..., description="ID курса"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Добавление узла в древо технологий"""
    return await proxy_request_to_course(f"{course_id}/tree/nodes", request)


@router.get(
    "/{course_id}/tree/nodes/{node_id}",
    include_in_schema=True,
    summary="Получение информации об узле древа",
    description="Возвращает информацию о конкретном узле древа технологий",
    response_description="Данные узла древа технологий"
)
async def get_tree_node(
        course_id: str = PathParam(..., description="ID курса"),
        node_id: str = PathParam(..., description="ID узла"),
        language: Optional[str] = Query(None, description="Код языка для локализованного контента"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение информации об узле древа технологий"""
    return await proxy_request_to_course(f"{course_id}/tree/nodes/{node_id}", request)


@router.put(
    "/{course_id}/tree/nodes/{node_id}",
    include_in_schema=True,
    summary="Обновление узла древа технологий",
    description="Обновляет существующий узел древа технологий. Требует авторизации",
    response_description="Результат операции с обновленным узлом"
)
async def update_tree_node(
        course_id: str = PathParam(..., description="ID курса"),
        node_id: str = PathParam(..., description="ID узла"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Обновление узла древа технологий"""
    return await proxy_request_to_course(f"{course_id}/tree/nodes/{node_id}", request)


@router.delete(
    "/{course_id}/tree/nodes/{node_id}",
    include_in_schema=True,
    summary="Удаление узла древа технологий",
    description="Удаляет узел из древа технологий. Требует авторизации",
    response_description="Результат операции удаления узла"
)
async def delete_tree_node(
        course_id: str = PathParam(..., description="ID курса"),
        node_id: str = PathParam(..., description="ID узла"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Удаление узла древа технологий"""
    return await proxy_request_to_course(f"{course_id}/tree/nodes/{node_id}", request)


@router.patch(
    "/{course_id}/tree/publish",
    include_in_schema=True,
    summary="Публикация или отмена публикации древа технологий",
    description="Меняет статус публикации древа технологий. Требует авторизации",
    response_description="Обновленное древо технологий"
)
async def publish_technology_tree(
        course_id: str = PathParam(..., description="ID курса"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Публикация или отмена публикации древа технологий"""
    return await proxy_request_to_course(f"{course_id}/tree/publish", request)


@router.get(
    "/{course_id}/tree/export",
    include_in_schema=True,
    summary="Экспорт древа технологий",
    description="Экспортирует древо технологий в формат, подходящий для инструментов визуализации",
    response_description="Данные древа технологий в формате для экспорта"
)
async def export_technology_tree(
        course_id: str = PathParam(..., description="ID курса"),
        language: Optional[str] = Query(None, description="Код языка для локализованного контента"),
        localize: bool = Query(False, description="Включить локализованный контент узлов"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Экспорт древа технологий"""
    return await proxy_request_to_course(f"{course_id}/tree/export", request)


@router.post(
    "/{course_id}/tree/import",
    include_in_schema=True,
    summary="Импорт древа технологий",
    description="Импортирует древо технологий из внешних данных. Требует авторизации",
    response_description="Импортированное древо технологий"
)
async def import_technology_tree(
        course_id: str = PathParam(..., description="ID курса"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Импорт древа технологий"""
    return await proxy_request_to_course(f"{course_id}/tree/import", request)


######################################################################
# API СТАТЕЙ (ARTICLES)
######################################################################

@router.get(
    "/articles",
    include_in_schema=True,
    summary="Получение списка статей",
    description="Возвращает список статей с возможностью фильтрации и пагинации",
    response_description="Список статей с информацией о пагинации"
)
async def list_articles(
        course_id: Optional[str] = Query(..., description="ID курса"),
        skip: Optional[int] = Query(0, description="Количество записей для пропуска"),
        limit: Optional[int] = Query(100, description="Максимальное количество записей для возврата"),
        language: Optional[str] = Query(None, description="Фильтр по языку"),
        is_published: Optional[bool] = Query(None, description="Фильтр по статусу публикации"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение списка статей"""
    return await proxy_request_to_course("articles", request)


@router.get(
    "/articles/{article_id}",
    include_in_schema=True,
    summary="Получение информации о статье",
    description="Возвращает информацию о конкретной статье по её ID",
    response_description="Данные статьи"
)
async def get_article(
        article_id: str = PathParam(..., description="ID статьи"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение информации о статье"""
    return await proxy_request_to_course(f"articles/{article_id}", request)


@router.post(
    "/articles",
    include_in_schema=True,
    summary="Создание новой статьи",
    description="Создает новую статью для курса. Требует авторизации",
    response_description="Созданная статья",
    status_code=201
)
async def create_article(
        request: Request,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Создание новой статьи"""
    return await proxy_request_to_course("articles", request)


@router.put(
    "/articles/{article_id}",
    include_in_schema=True,
    summary="Обновление статьи",
    description="Обновляет существующую статью. Требует авторизации",
    response_description="Обновленная статья"
)
async def update_article(
        article_id: str = PathParam(..., description="ID статьи"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Обновление статьи"""
    return await proxy_request_to_course(f"articles/{article_id}", request)


@router.delete(
    "/articles/{article_id}",
    include_in_schema=True,
    summary="Удаление статьи",
    description="Удаляет существующую статью. Требует авторизации",
    response_description="",
    status_code=204
)
async def delete_article(
        article_id: str = PathParam(..., description="ID статьи"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Удаление статьи"""
    return await proxy_request_to_course(f"articles/{article_id}", request)


######################################################################
# API УРОКОВ (LESSONS)
######################################################################

@router.get(
    "/lessons",
    include_in_schema=True,
    summary="Получение списка уроков",
    description="Возвращает список уроков с возможностью фильтрации и пагинации",
    response_description="Список уроков с информацией о пагинации"
)
async def list_lessons(
        course_id: Optional[str] = Query(..., description="ID курса"),
        skip: Optional[int] = Query(0, description="Количество записей для пропуска"),
        limit: Optional[int] = Query(100, description="Максимальное количество записей для возврата"),
        language: Optional[str] = Query(None, description="Фильтр по языку"),
        is_published: Optional[bool] = Query(None, description="Фильтр по статусу публикации"),
        tree_node_id: Optional[str] = Query(None, description="Фильтр по ID узла дерева технологий"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение списка уроков"""
    return await proxy_request_to_course("lessons", request)


@router.get(
    "/lessons/{lesson_id}",
    include_in_schema=True,
    summary="Получение информации об уроке",
    description="Возвращает информацию о конкретном уроке по его ID",
    response_description="Данные урока"
)
async def get_lesson(
        lesson_id: str = PathParam(..., description="ID урока"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение информации об уроке"""
    return await proxy_request_to_course(f"lessons/{lesson_id}", request)


@router.get(
    "/lessons/{lesson_id}/content",
    include_in_schema=True,
    summary="Получение урока с контентом",
    description="Возвращает урок вместе со связанными статьями и материалами",
    response_description="Урок с полным контентом"
)
async def get_lesson_with_content(
        lesson_id: str = PathParam(..., description="ID урока"),
        language: Optional[str] = Query(None, description="Предпочтительный язык для контента"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение урока с контентом"""
    return await proxy_request_to_course(f"lessons/{lesson_id}/content", request)


@router.post(
    "/lessons",
    include_in_schema=True,
    summary="Создание нового урока",
    description="Создает новый урок для курса. Требует авторизации",
    response_description="Созданный урок",
    status_code=201
)
async def create_lesson(
        request: Request,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Создание нового урока"""
    return await proxy_request_to_course("lessons", request)


@router.put(
    "/lessons/{lesson_id}",
    include_in_schema=True,
    summary="Обновление урока",
    description="Обновляет существующий урок. Требует авторизации",
    response_description="Обновленный урок"
)
async def update_lesson(
        lesson_id: str = PathParam(..., description="ID урока"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Обновление урока"""
    return await proxy_request_to_course(f"lessons/{lesson_id}", request)


@router.delete(
    "/lessons/{lesson_id}",
    include_in_schema=True,
    summary="Удаление урока",
    description="Удаляет существующий урок. Требует авторизации",
    response_description="",
    status_code=204
)
async def delete_lesson(
        lesson_id: str = PathParam(..., description="ID урока"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Удаление урока"""
    return await proxy_request_to_course(f"lessons/{lesson_id}", request)


@router.post(
    "/lessons/{lesson_id}/articles/{article_id}",
    include_in_schema=True,
    summary="Добавление статьи к уроку",
    description="Связывает статью с уроком. Требует авторизации",
    response_description="Результат операции"
)
async def add_article_to_lesson(
        lesson_id: str = PathParam(..., description="ID урока"),
        article_id: str = PathParam(..., description="ID статьи"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Добавление статьи к уроку"""
    return await proxy_request_to_course(f"lessons/{lesson_id}/articles/{article_id}", request)


@router.delete(
    "/lessons/{lesson_id}/articles/{article_id}",
    include_in_schema=True,
    summary="Удаление статьи из урока",
    description="Удаляет связь между статьей и уроком. Требует авторизации",
    response_description="Результат операции"
)
async def remove_article_from_lesson(
        lesson_id: str = PathParam(..., description="ID урока"),
        article_id: str = PathParam(..., description="ID статьи"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Удаление статьи из урока"""
    return await proxy_request_to_course(f"lessons/{lesson_id}/articles/{article_id}", request)
