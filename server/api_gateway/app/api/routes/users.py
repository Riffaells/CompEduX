"""
Маршруты для управления пользователями
"""
from fastapi import APIRouter, Depends, Request, Response

from app.core.config import settings
from app.core.proxy import proxy_request, check_service_health
from common.logger import get_logger

# Настройка логирования
logger = get_logger("users_proxy")

router = APIRouter()

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

@router.get(
    "/",
    include_in_schema=True,
    summary="Список пользователей",
    description="Получение списка пользователей с возможностью поиска и фильтрации",
    response_description="Список пользователей"
)
async def list_users(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Получение списка пользователей"""
    return await proxy_request_to_auth("users", request)

@router.get(
    "/id/{user_id}",
    include_in_schema=True,
    summary="Информация о пользователе по ID",
    description="Получение подробной информации о пользователе по его ID",
    response_description="Данные пользователя"
)
async def get_user_by_id(user_id: str, request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Получение информации о пользователе по ID"""
    return await proxy_request_to_auth(f"users/id/{user_id}", request)

@router.put(
    "/id/{user_id}",
    include_in_schema=True,
    summary="Обновление данных пользователя",
    description="Обновление информации о пользователе по его ID",
    response_description="Обновленные данные пользователя"
)
async def update_user(user_id: str, request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Обновление информации о пользователе"""
    return await proxy_request_to_auth(f"users/id/{user_id}", request)

@router.delete(
    "/id/{user_id}",
    include_in_schema=True,
    summary="Удаление пользователя",
    description="Удаление пользователя по его ID (только для администраторов)",
    response_description="Подтверждение успешного удаления"
)
async def delete_user(user_id: str, request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Удаление пользователя (только для администраторов)"""
    return await proxy_request_to_auth(f"users/id/{user_id}", request)

@router.get(
    "/username/{username}",
    include_in_schema=True,
    summary="Профиль пользователя по имени",
    description="Получение профиля пользователя по его имени пользователя",
    response_description="Публичный профиль пользователя"
)
async def get_user_profile(username: str, request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """Получение профиля пользователя по имени пользователя"""
    return await proxy_request_to_auth(f"users/username/{username}", request) 