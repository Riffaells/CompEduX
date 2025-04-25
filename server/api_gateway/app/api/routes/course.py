import json
from typing import Optional

from app.core.config import settings
from app.core.proxy import check_service_health, proxy_request, proxy_docs_request, get_http_client
from fastapi import APIRouter, Request, Response, Depends, Path as PathParam, Query
from fastapi.responses import RedirectResponse

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
async def proxy_request_to_course(path: str, request: Request) -> Response:
    """
    Проксирует запрос к сервису курсов и возвращает ответ.
    """
    course_service_url = settings.COURSE_SERVICE_URL
    return await proxy_request(course_service_url, path, request)


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
    "/",
    include_in_schema=True,
    summary="Получение списка курсов",
    description="Возвращает список курсов с пагинацией и возможностью фильтрации",
    response_description="Список курсов с информацией о пагинации"
)
async def get_courses(
        request: Request,
        page: Optional[int] = Query(0, description="Номер страницы (начиная с 0)"),
        size: Optional[int] = Query(10, description="Размер страницы (от 1 до 100)"),
        search: Optional[str] = Query(None, description="Поисковый запрос"),
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение списка курсов"""
    return await proxy_request_to_course("", request)


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
    """Получение информации о курсе по ID"""
    return await proxy_request_to_course(f"{course_id}", request)


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
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Поиск курсов по тегу"""
    return await proxy_request_to_course(f"tag/{tag}", request)


@router.get(
    "/author/{author_id}",
    include_in_schema=True,
    summary="Получение курсов по автору",
    description="Возвращает список курсов, созданных указанным автором",
    response_description="Список курсов автора"
)
async def get_courses_by_author(
        author_id: str = PathParam(..., description="ID автора"),
        request: Request = None,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """Получение курсов по автору"""
    return await proxy_request_to_course(f"author/{author_id}", request)


# Добавляем универсальный маршрут для других эндпоинтов course_service
@router.api_route(
    "/{path:path}",
    methods=["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"],
    include_in_schema=False  # Скрываем из документации чтобы не создавать путаницу
)
async def proxy_course_requests(
        path: str,
        request: Request,
        _: bool = Depends(check_course_service_health)
) -> Response:
    """
    Проксирует любые другие запросы к сервису курсов.
    Это позволяет API Gateway перенаправлять любые запросы без явного определения всех эндпоинтов.

    Parameters:
    - **path**: Путь для проксирования на сервис курсов
    """
    logger.info(f"Proxying generic course request: {path}")
    return await proxy_request_to_course(path, request)
