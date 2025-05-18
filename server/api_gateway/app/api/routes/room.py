"""
API routes for room service
"""
import json
from typing import Any, Dict, List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query, Response, status, Request
from fastapi.responses import JSONResponse, StreamingResponse

from app.core.config import SERVICE_ROUTES
from app.core.deps import get_current_active_user
from app.core.proxy import (async_http_request, handle_request,
                            route_request_to_service)

router = APIRouter()

# Получаем настройки для room service
room_service = SERVICE_ROUTES.get("room")
if not room_service:
    raise ValueError("Room service configuration not found in SERVICE_ROUTES")


@router.get("/{path:path}")
async def proxy_room_get(
    path: str,
    response: Response,
    request: Request,
    current_user: Optional[Dict[str, Any]] = Depends(get_current_active_user)
):
    """
    Proxy GET requests to the room service
    """
    # Получаем заголовок авторизации из оригинального запроса
    auth_header = request.headers.get("Authorization")
    
    return await route_request_to_service(
        "room", f"/{path}", "GET", 
        current_user=current_user, 
        response=response,
        auth_header=auth_header
    )


@router.post("/{path:path}")
async def proxy_room_post(
    path: str,
    response: Response,
    request: Request,
    data: Dict[str, Any],
    current_user: Optional[Dict[str, Any]] = Depends(get_current_active_user)
):
    """
    Proxy POST requests to the room service
    """
    # Получаем заголовок авторизации из оригинального запроса
    auth_header = request.headers.get("Authorization")
    
    return await route_request_to_service(
        "room", f"/{path}", "POST", 
        json_data=data, 
        current_user=current_user, 
        response=response,
        auth_header=auth_header
    )


@router.put("/{path:path}")
async def proxy_room_put(
    path: str,
    response: Response,
    request: Request,
    data: Dict[str, Any],
    current_user: Optional[Dict[str, Any]] = Depends(get_current_active_user)
):
    """
    Proxy PUT requests to the room service
    """
    # Получаем заголовок авторизации из оригинального запроса
    auth_header = request.headers.get("Authorization")
    
    return await route_request_to_service(
        "room", f"/{path}", "PUT", 
        json_data=data,
        current_user=current_user, 
        response=response,
        auth_header=auth_header
    )


@router.delete("/{path:path}")
async def proxy_room_delete(
    path: str,
    response: Response,
    request: Request,
    current_user: Optional[Dict[str, Any]] = Depends(get_current_active_user)
):
    """
    Proxy DELETE requests to the room service
    """
    # Получаем заголовок авторизации из оригинального запроса
    auth_header = request.headers.get("Authorization")
    
    return await route_request_to_service(
        "room", f"/{path}", "DELETE", 
        current_user=current_user, 
        response=response,
        auth_header=auth_header
    )


@router.patch("/{path:path}")
async def proxy_room_patch(
    path: str,
    response: Response,
    request: Request,
    data: Dict[str, Any],
    current_user: Optional[Dict[str, Any]] = Depends(get_current_active_user)
):
    """
    Proxy PATCH requests to the room service
    """
    # Получаем заголовок авторизации из оригинального запроса
    auth_header = request.headers.get("Authorization")
    
    return await route_request_to_service(
        "room", f"/{path}", "PATCH", 
        json_data=data,
        current_user=current_user, 
        response=response,
        auth_header=auth_header
    ) 