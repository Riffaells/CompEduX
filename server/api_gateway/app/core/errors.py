from typing import Optional, Dict, Any, Union, List

from fastapi import HTTPException, status
from fastapi.responses import JSONResponse
from starlette.requests import Request

from common.logger import get_logger

logger = get_logger("api_gateway.errors")


class APIError(Exception):
    """
    Базовый класс для всех API ошибок.
    Определяет стандартный формат ошибок для API Gateway.
    """

    def __init__(
            self,
            message: str,
            error_code: int = status.HTTP_500_INTERNAL_SERVER_ERROR,
            details: Optional[str] = None,
            headers: Optional[Dict[str, str]] = None
    ):
        self.message = message
        self.error_code = error_code
        self.details = details
        self.headers = headers
        super().__init__(self.message)


class ServiceUnavailableError(APIError):
    """Ошибка недоступности сервиса"""

    def __init__(
            self,
            service_name: str,
            details: Optional[str] = None,
            headers: Optional[Dict[str, str]] = None
    ):
        message = f"Сервис {service_name} временно недоступен"
        super().__init__(
            message=message,
            error_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            details=details,
            headers=headers
        )


class AuthenticationError(APIError):
    """Ошибка аутентификации"""

    def __init__(
            self,
            message: str = "Ошибка аутентификации",
            details: Optional[str] = None,
            headers: Optional[Dict[str, str]] = None
    ):
        super().__init__(
            message=message,
            error_code=status.HTTP_401_UNAUTHORIZED,
            details=details,
            headers=headers or {"WWW-Authenticate": "Bearer"}
        )


class NotFoundError(APIError):
    """Ресурс не найден"""

    def __init__(
            self,
            resource_type: str,
            resource_id: Optional[str] = None,
            details: Optional[str] = None
    ):
        message = f"{resource_type} не найден"
        if resource_id:
            message = f"{resource_type} с ID {resource_id} не найден"
        super().__init__(
            message=message,
            error_code=status.HTTP_404_NOT_FOUND,
            details=details
        )


class ValidationError(APIError):
    """Ошибка валидации"""

    def __init__(
            self,
            message: str = "Ошибка валидации данных",
            details: Optional[Union[str, List[Dict[str, Any]]]] = None
    ):
        super().__init__(
            message=message,
            error_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            details=details
        )


# Обработчик для перехвата и форматирования ошибок API
async def api_error_handler(request: Request, exc: APIError) -> JSONResponse:
    """
    Обработчик ошибок API для преобразования их в стандартный формат.
    Регистрируется как exception_handler в FastAPI.
    """
    logger.error(f"API Error: {exc.message} (code: {exc.error_code})")
    if exc.details:
        logger.debug(f"Error details: {exc.details}")

    response = {
        "message": exc.message,
        "error": exc.error_code,
    }

    if exc.details:
        response["details"] = exc.details

    return JSONResponse(
        status_code=exc.error_code,
        content=response,
        headers=exc.headers
    )


# Обработчик для перехвата HTTPException и преобразования их в APIError
async def http_exception_handler(request: Request, exc: HTTPException) -> JSONResponse:
    """
    Обработчик стандартных HTTPException для преобразования их в APIError.
    Регистрируется как exception_handler в FastAPI.
    """
    api_error = APIError(
        message=str(exc.detail),
        error_code=exc.status_code,
        headers=exc.headers
    )
    return await api_error_handler(request, api_error)


# Обработчик для всех необработанных исключений
async def general_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    """
    Обработчик всех необработанных исключений.
    Преобразует их в APIError с кодом 500.
    Регистрируется как exception_handler в FastAPI.
    """
    logger.exception(f"Unhandled exception: {str(exc)}")
    api_error = APIError(
        message="Внутренняя ошибка сервера",
        error_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        details=str(exc) if str(exc) else None
    )
    return await api_error_handler(request, api_error)


# Функция для регистрации обработчиков ошибок в FastAPI
def register_exception_handlers(app):
    """
    Регистрирует все обработчики исключений в приложении FastAPI.
    """
    app.add_exception_handler(APIError, api_error_handler)
    app.add_exception_handler(HTTPException, http_exception_handler)
    app.add_exception_handler(Exception, general_exception_handler)
