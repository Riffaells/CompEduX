# -*- coding: utf-8 -*-
"""
Statistics service module for auth_service
"""
import re
import logging
from datetime import datetime, UTC
from typing import Optional, Dict, Any, List, Tuple
from uuid import UUID
from fastapi import Request
from sqlalchemy import func, desc, distinct, text
from sqlalchemy.orm import Session

from ..models.stats import ClientStatModel

logger = logging.getLogger(__name__)

def parse_user_agent(user_agent: str) -> Dict[str, str]:
    """
    Парсит строку User-Agent и извлекает из нее информацию о системе пользователя

    Пример: "1.0.0/9 (Windows11, 10.0; Java 17.0.14)"
    """
    result = {
        "os_name": None,
        "os_version": None,
        "java_version": None
    }

    if not user_agent:
        return result

    # Извлекаем ОС и версию
    os_match = re.search(r'\(([^,;]+)[,;]?\s*([^;)]+)?', user_agent)
    if os_match:
        os_info = os_match.group(1).strip()
        # Разделяем имя ОС и версию, если они вместе (например, Windows11)
        os_name_version = re.match(r'(\D+)(\d.*)?', os_info)
        if os_name_version:
            result["os_name"] = os_name_version.group(1).strip()
            # Если версия сразу после имени, берем ее
            if os_name_version.group(2):
                result["os_version"] = os_name_version.group(2).strip()
            # Иначе проверяем второй параметр из основного совпадения
            elif os_match.group(2):
                result["os_version"] = os_match.group(2).strip()

    # Извлекаем версию Java
    java_match = re.search(r'Java\s+(\S+)', user_agent)
    if java_match:
        result["java_version"] = java_match.group(1).strip()

    return result

async def collect_client_stats(request: Request, user_id: Optional[UUID] = None, db: Session = None) -> None:
    """
    Собирает статистику о клиентском приложении из заголовков запроса
    и сохраняет в базу данных

    Args:
        request: Объект запроса FastAPI
        user_id: ID пользователя, если известен
        db: Сессия базы данных
    """
    if not db:
        return

    try:
        # Получаем заголовки
        headers = request.headers

        # Извлекаем информацию о клиенте
        app_name = headers.get("X-App-Name")
        app_version = headers.get("X-App-Version")
        client_platform = headers.get("X-Client-Platform")
        client_build_str = headers.get("X-Client-Build")
        client_version = headers.get("X-Client-Version")
        user_agent = headers.get("User-Agent")

        # Проверяем наличие минимально необходимых данных
        if not client_platform and not app_version:
            logger.debug("Insufficient client data in headers, skipping stats collection")
            return

        # Преобразуем build в число
        try:
            client_build = int(client_build_str) if client_build_str else None
        except ValueError:
            client_build = None
            logger.debug(f"Invalid client build value: {client_build_str}")

        # Парсим User-Agent
        ua_info = parse_user_agent(user_agent)

        # Информация о запросе
        request_path = str(request.url.path)
        request_method = request.method
        client_ip = request.client.host if request.client else None

        # Проверяем, существует ли уже запись для этой комбинации
        existing_stat = None
        if user_id:
            # Если пользователь авторизован, ищем по ID пользователя и платформе
            existing_stat = db.query(ClientStatModel).filter(
                ClientStatModel.user_id == user_id,
                ClientStatModel.client_platform == client_platform,
                ClientStatModel.app_name == app_name,
                ClientStatModel.client_version == client_version
            ).first()

        if existing_stat:
            # Обновляем существующую запись
            existing_stat.update_last_seen()
            # Обновляем информацию, если изменилась
            if app_version and existing_stat.app_version != app_version:
                existing_stat.app_version = app_version
            if client_build and existing_stat.client_build != client_build:
                existing_stat.client_build = client_build
            db.commit()
            logger.debug(f"Updated client stats for user {user_id}, platform {client_platform}")
        else:
            # Создаем новую запись
            new_stat = ClientStatModel(
                user_id=user_id,
                app_name=app_name,
                app_version=app_version,
                client_platform=client_platform,
                client_build=client_build,
                client_version=client_version,
                user_agent=user_agent,
                os_name=ua_info["os_name"],
                os_version=ua_info["os_version"],
                java_version=ua_info["java_version"],
                request_path=request_path,
                request_method=request_method,
                request_ip=client_ip
            )
            db.add(new_stat)
            db.commit()
            logger.debug(f"Created new client stats record for platform {client_platform}" +
                      (f", user {user_id}" if user_id else ""))

    except Exception as e:
        logger.exception(f"Error collecting client stats: {str(e)}")
        if db:
            db.rollback()

def get_platform_stats(db: Session) -> List[Dict[str, Any]]:
    """
    Получает статистику по платформам

    Returns:
        Список словарей с статистикой по платформам
    """
    try:
        # Группируем по платформе и считаем количество пользователей
        stats = db.query(
            ClientStatModel.client_platform,
            func.count(distinct(ClientStatModel.user_id)).label("user_count"),
            func.count(ClientStatModel.id).label("request_count")
        ).group_by(
            ClientStatModel.client_platform
        ).order_by(
            desc("user_count")
        ).all()

        return [
            {
                "platform": stat.client_platform or "Unknown",
                "user_count": stat.user_count,
                "request_count": stat.request_count
            }
            for stat in stats
        ]
    except Exception as e:
        logger.exception(f"Error getting platform stats: {str(e)}")
        return []

def get_os_stats(db: Session) -> List[Dict[str, Any]]:
    """
    Получает статистику по операционным системам

    Returns:
        Список словарей с статистикой по ОС
    """
    try:
        # Группируем по ОС и версии, считаем количество пользователей
        stats = db.query(
            ClientStatModel.os_name,
            ClientStatModel.os_version,
            func.count(distinct(ClientStatModel.user_id)).label("user_count"),
            func.count(ClientStatModel.id).label("request_count")
        ).group_by(
            ClientStatModel.os_name,
            ClientStatModel.os_version
        ).order_by(
            desc("user_count")
        ).all()

        return [
            {
                "os": f"{stat.os_name or 'Unknown'} {stat.os_version or ''}".strip(),
                "user_count": stat.user_count,
                "request_count": stat.request_count
            }
            for stat in stats
        ]
    except Exception as e:
        logger.exception(f"Error getting OS stats: {str(e)}")
        return []

def get_app_version_stats(db: Session) -> List[Dict[str, Any]]:
    """
    Получает статистику по версиям приложения

    Returns:
        Список словарей с статистикой по версиям
    """
    try:
        # Группируем по версии приложения, считаем количество пользователей
        stats = db.query(
            ClientStatModel.app_version,
            ClientStatModel.client_version,
            func.count(distinct(ClientStatModel.user_id)).label("user_count"),
            func.count(ClientStatModel.id).label("request_count")
        ).group_by(
            ClientStatModel.app_version,
            ClientStatModel.client_version
        ).order_by(
            desc("user_count")
        ).all()

        return [
            {
                "app_version": stat.app_version or "Unknown",
                "client_version": stat.client_version or "Unknown",
                "user_count": stat.user_count,
                "request_count": stat.request_count
            }
            for stat in stats
        ]
    except Exception as e:
        logger.exception(f"Error getting app version stats: {str(e)}")
        return []
