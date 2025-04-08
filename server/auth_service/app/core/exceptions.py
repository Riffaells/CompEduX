from fastapi import Request, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from pydantic import BaseModel
from typing import Any, Dict, Optional, List, Union

class ErrorResponse(BaseModel):
    """Model for error response"""
    error: str
    message: str
    detailed: Optional[Union[str, Dict[str, Any], List[Dict[str, Any]]]] = None


class APIException(Exception):
    """Base exception for API errors"""
    def __init__(
        self,
        status_code: int,
        error: str,
        message: str,
        detailed: Optional[Union[str, Dict[str, Any], List[Dict[str, Any]]]] = None
    ):
        self.status_code = status_code
        self.error = error
        self.message = message
        self.detailed = detailed


class BadRequestException(APIException):
    """400 Bad Request Exception"""
    def __init__(self, message: str = "Bad Request", detailed: Optional[Any] = None):
        super().__init__(
            status_code=status.HTTP_400_BAD_REQUEST,
            error="BAD_REQUEST",
            message=message,
            detailed=detailed
        )


class UnauthorizedException(APIException):
    """401 Unauthorized Exception"""
    def __init__(self, message: str = "Authentication required", detailed: Optional[Any] = None):
        super().__init__(
            status_code=status.HTTP_401_UNAUTHORIZED,
            error="UNAUTHORIZED",
            message=message,
            detailed=detailed
        )


class ForbiddenException(APIException):
    """403 Forbidden Exception"""
    def __init__(self, message: str = "Access forbidden", detailed: Optional[Any] = None):
        super().__init__(
            status_code=status.HTTP_403_FORBIDDEN,
            error="FORBIDDEN",
            message=message,
            detailed=detailed
        )


class NotFoundException(APIException):
    """404 Not Found Exception"""
    def __init__(self, message: str = "Resource not found", detailed: Optional[Any] = None):
        super().__init__(
            status_code=status.HTTP_404_NOT_FOUND,
            error="NOT_FOUND",
            message=message,
            detailed=detailed
        )


class ConflictException(APIException):
    """409 Conflict Exception"""
    def __init__(self, message: str = "Resource conflict", detailed: Optional[Any] = None):
        super().__init__(
            status_code=status.HTTP_409_CONFLICT,
            error="CONFLICT",
            message=message,
            detailed=detailed
        )


class InternalServerErrorException(APIException):
    """500 Internal Server Error Exception"""
    def __init__(self, message: str = "Internal server error", detailed: Optional[Any] = None):
        super().__init__(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            error="INTERNAL_SERVER_ERROR",
            message=message,
            detailed=detailed
        )


async def api_exception_handler(request: Request, exc: APIException) -> JSONResponse:
    """Handler for API exceptions"""
    return JSONResponse(
        status_code=exc.status_code,
        content=ErrorResponse(
            error=exc.error,
            message=exc.message,
            detailed=exc.detailed
        ).dict(exclude_none=True)
    )


async def validation_exception_handler(request: Request, exc: RequestValidationError) -> JSONResponse:
    """Handler for validation errors"""
    errors = exc.errors()

    # Format the error details into a readable string
    error_details = []
    for error in errors:
        loc = " -> ".join([str(l) for l in error["loc"]])
        error_details.append(f"{loc}: {error['msg']}")

    detailed_message = "\n".join(error_details)

    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content=ErrorResponse(
            error="VALIDATION_ERROR",
            message="Request validation error",
            detailed=detailed_message
        ).dict(exclude_none=True)
    )
