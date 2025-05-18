"""
Маршруты для функциональности дружбы
"""
import json

from fastapi import APIRouter, Request, Response, Depends

from app.core.config import settings
from app.core.proxy import check_service_health, proxy_request, get_http_client
from common.logger import get_logger

# Настройка логирования
logger = get_logger("friends_proxy")

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
    summary="Список друзей",
    description="Получение списка друзей текущего пользователя с возможностью фильтрации по статусу",
    response_description="Список друзей с информацией о статусе дружбы"
)
async def list_friends(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """
    Получение списка друзей.
    
    Параметры запроса:
    - status: Фильтр по статусу дружбы (PENDING, ACCEPTED, REJECTED, BLOCKED)
    - skip: Количество пропускаемых записей
    - limit: Максимальное количество записей
    """
    return await proxy_request_to_auth("friends", request)


@router.post(
    "/",
    include_in_schema=True,
    summary="Отправить запрос на дружбу",
    description="Отправка запроса на дружбу другому пользователю",
    response_description="Информация о созданном запросе на дружбу",
    status_code=201
)
async def send_friend_request(request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """
    Отправка запроса на дружбу.
    
    Тело запроса:
    ```json
    {
        "friend_id": "uuid-пользователя"
    }
    ```
    """
    return await proxy_request_to_auth("friends", request)


@router.get(
    "/check/{user_id}",
    include_in_schema=True,
    summary="Проверка статуса дружбы",
    description="Проверка статуса дружбы между текущим пользователем и указанным пользователем",
    response_description="Информация о статусе дружбы"
)
async def check_friendship_status(user_id: str, request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """
    Проверка статуса дружбы с конкретным пользователем.
    
    Возвращает информацию о статусе дружбы, включая:
    - status: Статус дружбы (PENDING, ACCEPTED, REJECTED, BLOCKED) или null
    - friendship_id: ID дружбы или null
    - direction: Направление запроса (incoming/outgoing) или null
    - is_friend: true, если пользователи являются друзьями (статус ACCEPTED)
    """
    return await proxy_request_to_auth(f"friends/check/{user_id}", request)


@router.put(
    "/{friendship_id}",
    include_in_schema=True,
    summary="Обновить статус дружбы",
    description="Обновление статуса дружбы (принятие, отклонение, блокировка)",
    response_description="Обновленная информация о дружбе"
)
async def update_friendship(friendship_id: str, request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """
    Обновление статуса дружбы.
    
    Тело запроса:
    ```json
    {
        "status": "ACCEPTED" // или "REJECTED", "BLOCKED"
    }
    ```
    """
    return await proxy_request_to_auth(f"friends/{friendship_id}", request)


@router.delete(
    "/{friendship_id}",
    include_in_schema=True,
    summary="Удалить дружбу",
    description="Удаление дружбы или запроса на дружбу",
    response_description="Подтверждение успешного удаления",
    status_code=204
)
async def remove_friendship(friendship_id: str, request: Request, _: bool = Depends(check_auth_service_health)) -> Response:
    """
    Удаление дружбы.
    
    Полностью удаляет запись о дружбе между пользователями.
    """
    return await proxy_request_to_auth(f"friends/{friendship_id}", request) 