import json

from app.core.config import settings
from app.core.proxy import check_service_health, proxy_request, proxy_docs_request, get_http_client
from fastapi import APIRouter, Request, Response, Depends
from fastapi.responses import RedirectResponse

from common.logger import get_logger

# Настройка логирования
logger = get_logger("auth_proxy")

router = APIRouter(
    responses={
        401: {"description": "Не авторизован"},
        403: {"description": "Доступ запрещен"},
        500: {"description": "Внутренняя ошибка сервера"},
        503: {"description": "Сервис авторизации недоступен"}
    }
)


# Создаем зависимость для проверки здоровья сервиса с кэшированием
async def check_auth_service_health():
    """Проверяет доступность сервиса авторизации"""
    await check_service_health(
        service_name="auth",
        force=False
    )
    return True


# Функция-helper для выполнения запросов к сервису авторизации
async def proxy_request_to_auth(path: str, request: Request) -> Response:
    """
    Проксирует запрос к сервису авторизации и возвращает ответ.
    """
    auth_service_url = settings.AUTH_SERVICE_URL
    return await proxy_request(auth_service_url, path, request)


######################################################################
# ДОКУМЕНТАЦИЯ API
######################################################################

@router.get(
    "/docs",
    include_in_schema=True,
    summary="Документация сервиса авторизации",
    description="Проксирует Swagger UI документацию сервиса авторизации"
)
async def auth_docs(request: Request, _: bool = Depends(check_auth_service_health)):
    """Проксирование документации сервиса авторизации"""
    logger.info("Proxying auth service docs")
    # Используем специальную функцию для проксирования документации с правильным путем
    return await proxy_docs_request(settings.AUTH_SERVICE_URL, "api/v1/docs", request)


@router.get(
    "/redoc",
    include_in_schema=True,
    summary="ReDoc документация сервиса авторизации",
    description="Проксирует ReDoc документацию сервиса авторизации"
)
async def auth_redoc(request: Request, _: bool = Depends(check_auth_service_health)):
    """Проксирование ReDoc документации сервиса авторизации"""
    logger.info("Proxying auth service redoc")
    # Используем специальную функцию для проксирования документации с правильным путем
    return await proxy_docs_request(settings.AUTH_SERVICE_URL, "api/v1/redoc", request)


@router.get(
    "/openapi.json",
    include_in_schema=True,
    summary="OpenAPI спецификация сервиса авторизации",
    description="Возвращает OpenAPI JSON схему сервиса авторизации"
)
async def auth_openapi(request: Request, _: bool = Depends(check_auth_service_health)):
    """Проксирование OpenAPI JSON схемы сервиса авторизации"""
    logger.info("Proxying auth service OpenAPI schema")
    # Используем специальную функцию для проксирования документации с правильным путем
    return await proxy_docs_request(settings.AUTH_SERVICE_URL, "api/v1/openapi.json", request)


######################################################################
# ПРОВЕРКА ЗДОРОВЬЯ СЕРВИСА
######################################################################

@router.get(
    "/health",
    include_in_schema=True,
    summary="Проверка здоровья сервиса авторизации",
    description="Проверяет доступность и работоспособность сервиса авторизации",
    response_description="Статус здоровья сервиса авторизации"
)
async def auth_health(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Проверка здоровья сервиса авторизации"""
    logger.info("Proxying auth health check request")
    # Обходим проблему с некорректным проксированием, делаем прямой запрос
    auth_service_url = settings.AUTH_SERVICE_URL
    client = await get_http_client()
    health_url = f"{auth_service_url.rstrip('/')}/health"

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
            content=json.dumps({"detail": "Auth service health check failed"}),
            status_code=503,
            media_type="application/json"
        )


######################################################################
# API АВТОРИЗАЦИИ
######################################################################

@router.post(
    "/register",
    include_in_schema=True,
    summary="Регистрация нового пользователя",
    description="Регистрация нового пользователя в системе",
    response_description="Информация о созданном пользователе и токены аутентификации",
    status_code=201
)
async def register(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Регистрация нового пользователя"""
    return await proxy_request_to_auth("register", request)


@router.post(
    "/login",
    include_in_schema=True,
    summary="Вход в систему",
    description="Аутентификация пользователя и получение токенов доступа",
    response_description="Токены аутентификации (access_token и refresh_token)"
)
async def login(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Вход в систему и получение токенов"""
    return await proxy_request_to_auth("login", request)


@router.post(
    "/refresh",
    include_in_schema=True,
    summary="Обновление токена доступа",
    description="Обновление истекшего access_token с помощью refresh_token",
    response_description="Новый access_token и текущий refresh_token"
)
async def refresh_token(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Обновление access token с помощью refresh token"""
    return await proxy_request_to_auth("refresh", request)


@router.post(
    "/logout",
    include_in_schema=True,
    summary="Выход из системы",
    description="Выход из системы и отзыв refresh_token",
    response_description="Подтверждение успешного выхода из системы"
)
async def logout(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Выход из системы (отзыв refresh token)"""
    return await proxy_request_to_auth("logout", request)


@router.get(
    "/me",
    include_in_schema=True,
    summary="Информация о текущем пользователе",
    description="Получение информации о текущем авторизованном пользователе",
    response_description="Данные текущего пользователя"
)
async def get_current_user(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Получение информации о текущем пользователе"""
    return await proxy_request_to_auth("me", request)


@router.post(
    "/verify-token",
    include_in_schema=True,
    summary="Проверка токена доступа (POST)",
    description="Проверка валидности токена доступа (access_token) через POST запрос",
    response_description="Информация о токене и пользователе при успешной верификации"
)
@router.get(
    "/verify-token",
    include_in_schema=True,
    summary="Проверка токена доступа (GET)",
    description="Проверка валидности токена доступа (access_token) через GET запрос",
    response_description="Информация о токене и пользователе при успешной верификации"
)
async def verify_token(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """
    Проверка токена.
    Важно: этот эндпоинт должен сохранять все заголовки запроса, включая Authorization,
    при проксировании к сервису авторизации.
    Поддерживает как GET, так и POST запросы.
    """
    logger.info(f"Proxying verify-token request with Authorization header, method: {request.method}")
    return await proxy_request_to_auth("verify-token", request)


######################################################################
# ПРОКСИРОВАНИЕ ОСТАЛЬНЫХ ЗАПРОСОВ
######################################################################

@router.api_route(
    "/{path:path}",
    methods=["GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"],
    include_in_schema=False  # Скрываем из документации чтобы не создавать путаницу
)
async def proxy_auth_requests(
        path: str,
        request: Request,
        _: bool = Depends(check_auth_service_health)
) -> Response:
    """
    Проксирует любые другие запросы к сервису авторизации.
    Это позволяет API Gateway перенаправлять любые запросы без явного определения всех эндпоинтов.

    Parameters:
    - **path**: Путь для проксирования на сервис авторизации
    """
    logger.info(f"Proxying generic request: {path}")
    return await proxy_request_to_auth(path, request)
