"""
API routes for user management.

This module contains endpoints for user management, including listing, retrieving,
updating, and deleting users.
"""
import uuid
from typing import Dict, Any, Optional, List
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, Query, Path, status
from sqlalchemy import select, or_, func
from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger
from ..utils import prepare_user_response
from ...core.config import settings
from ...db.session import get_db
from ...models.user import UserModel, UserRole, UserProfileModel
from ...schemas import UserResponseSchema, UserUpdateSchema, UserPublicProfileSchema, UserList
from ...services.auth import get_current_user, get_user_by_id, get_user_by_username, get_password_hash

# Create base logger
logger = get_logger(__name__)

router = APIRouter()


def get_current_admin_user(current_user: UserModel = Depends(get_current_user)) -> UserModel:
    """Check if the current user is an administrator"""
    if current_user.role != UserRole.ADMIN:
        logger.warning(f"Access denied: User {current_user.id} attempted admin action")
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    return current_user


@router.get("/", response_model=UserList)
async def read_users(
        search: Optional[str] = Query(None, min_length=2, description="Поисковый запрос (имя пользователя, email)"),
        skip: int = Query(0, ge=0, description="Skip N records"),
        limit: int = Query(20, ge=1, le=100, description="Limit to N records"),
        role: Optional[UserRole] = Query(None, description="Filter by user role"),
        is_verified: Optional[bool] = Query(None, description="Filter by verification status"),
        sort_by: str = Query("created_at", description="Field to sort by (created_at, username, email)"),
        sort_order: str = Query("desc", description="Sort order (asc/desc)"),
        current_user: UserModel = Depends(get_current_user),
        db: AsyncSession = Depends(get_db)
):
    """
    Получение списка пользователей с расширенными параметрами фильтрации и сортировки.
    
    Возвращает список пользователей, соответствующих критериям поиска.
    Администраторы видят полную информацию, обычные пользователи - только публичные данные.
    """
    logger.info(f"User {current_user.id} ({current_user.username}) requested user list")
    logger.debug(f"Query parameters: search={search}, skip={skip}, limit={limit}, role={role}, "
               f"is_verified={is_verified}, sort_by={sort_by}, sort_order={sort_order}")
    
    try:
        # Базовый запрос для выборки пользователей
        logger.debug("Building base query")
        
        # Создаем запрос, который выбирает только необходимые поля напрямую
        # Это избегает проблем с ленивой загрузкой
        if current_user.role == UserRole.ADMIN:
            # Для админа выбираем больше полей
            base_query = select(
                UserModel.id,
                UserModel.username,
                UserModel.email,
                UserModel.is_active,
                UserModel.is_verified,
                UserModel.role,
                UserModel.created_at,
                UserModel.updated_at,
                UserModel.last_login_at,
                UserModel.auth_provider,
                UserModel.rating
            )
        else:
            # Для обычных пользователей только базовые поля
            base_query = select(
                UserModel.id,
                UserModel.username,
                UserModel.is_verified,
                UserModel.created_at
            )
        
        # Для обычных пользователей исключаем из результатов текущего пользователя
        if current_user.role != UserRole.ADMIN:
            logger.debug(f"Excluding current user {current_user.id} from results (non-admin)")
            base_query = base_query.where(UserModel.id != current_user.id)
        
        # Применяем фильтры
        # Поиск по имени пользователя или email
        if search:
            search_query = f"%{search}%"
            logger.debug(f"Applying search filter with query: {search_query}")
            base_query = base_query.where(
                or_(
                    UserModel.username.ilike(search_query),
                    UserModel.email.ilike(search_query)
                )
            )
            logger.debug(f"Applied search filter: {search}")
        
        # Фильтр по роли
        if role:
            logger.debug(f"Applying role filter: {role}")
            base_query = base_query.where(UserModel.role == role)
            logger.debug(f"Applied role filter: {role}")
        
        # Фильтр по статусу верификации
        if is_verified is not None:
            logger.debug(f"Applying verification filter: {is_verified}")
            base_query = base_query.where(UserModel.is_verified == is_verified)
            logger.debug(f"Applied verification filter: {is_verified}")
        
        # Применяем сортировку
        logger.debug(f"Applying sorting by {sort_by} in {sort_order} order")
        if sort_by == "username":
            if sort_order.lower() == "asc":
                base_query = base_query.order_by(UserModel.username.asc())
            else:
                base_query = base_query.order_by(UserModel.username.desc())
        elif sort_by == "email":
            if sort_order.lower() == "asc":
                base_query = base_query.order_by(UserModel.email.asc())
            else:
                base_query = base_query.order_by(UserModel.email.desc())
        else:  # По умолчанию сортировка по created_at
            if sort_order.lower() == "asc":
                base_query = base_query.order_by(UserModel.created_at.asc())
            else:
                base_query = base_query.order_by(UserModel.created_at.desc())
        
        logger.debug(f"Applied sorting: {sort_by} {sort_order}")
        
        # Выполняем запрос с пагинацией
        logger.debug(f"Executing query with offset={skip}, limit={limit}")
        result = await db.execute(base_query.offset(skip).limit(limit))
        
        # Получаем результаты как словари (а не как объекты модели)
        # Это избегает проблем с ленивой загрузкой
        user_rows = result.all()
        logger.debug(f"Found {len(user_rows)} users")
        
        # Получаем общее количество результатов для пагинации
        logger.debug("Calculating total count for pagination")
        count_query = select(func.count()).select_from(UserModel).where(base_query.whereclause)
        total_count = await db.execute(count_query)
        total = total_count.scalar() or 0
        
        logger.debug(f"Total users count: {total}")
        
        # Преобразуем результаты в словари для ответа
        user_profiles = []
        for row in user_rows:
            try:
                if current_user.role == UserRole.ADMIN:
                    # Для админа создаем расширенный профиль
                    profile = {
                        "id": row.id,
                        "username": row.username,
                        "email": row.email,
                        "is_active": row.is_active,
                        "is_verified": row.is_verified,
                        "role": row.role,
                        "created_at": row.created_at,
                        "updated_at": row.updated_at,
                        "last_login_at": row.last_login_at,
                        "auth_provider": row.auth_provider,
                        "rating": row.rating
                    }
                else:
                    # Для обычного пользователя - только базовые поля
                    profile = {
                        "id": row.id,
                        "username": row.username,
                        "is_verified": row.is_verified,
                        "created_at": row.created_at
                    }
                
                user_profiles.append(profile)
            except Exception as e:
                logger.error(f"Error processing user row: {str(e)}")
                logger.error(f"Error type: {type(e).__name__}")
                # Skip this user but continue processing others
        
        logger.debug(f"Prepared {len(user_profiles)} user profiles")
        
        # Формируем ответ с пагинацией
        logger.debug("Building response with pagination")
        response = {
            "items": user_profiles,
            "total": total,
            "page": skip // limit if limit > 0 else 0,
            "size": len(user_profiles),
            "pages": (total + limit - 1) // limit if limit > 0 else 0
        }
        
        # Добавляем отладочную информацию, если включен режим отладки
        if settings.DEBUG:
            logger.debug("Adding debug info to response")
            response["debug_info"] = {
                "current_user_id": str(current_user.id),
                "current_user_role": str(current_user.role),
                "query_params": {
                    "search": search,
                    "skip": skip,
                    "limit": limit,
                    "role": str(role) if role else None,
                    "is_verified": is_verified,
                    "sort_by": sort_by,
                    "sort_order": sort_order
                }
            }
        
        logger.info(f"Successfully returned user list with {len(user_profiles)} users")
        return response
        
    except Exception as e:
        logger.error(f"Error getting user list: {str(e)}")
        logger.error(f"Error type: {type(e).__name__}")
        
        # Handle greenlet_spawn errors specifically
        if "greenlet_spawn has not been called" in str(e):
            logger.error("Detected greenlet_spawn error in async context")
            logger.error("This is likely caused by trying to access lazy-loaded attributes")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Database access error in async context. This is usually caused by trying to access lazy-loaded attributes."
            )
        
        # Re-raise HTTP exceptions
        if isinstance(e, HTTPException):
            raise e
        
        # Generic error
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error retrieving users: {str(e)}"
        )


@router.get("/id/{user_id}", response_model=UserResponseSchema)
async def read_user(
        user_id: UUID,
        current_user: UserModel = Depends(get_current_user),
        db: AsyncSession = Depends(get_db)
):
    """Get user information by ID"""
    logger.info(f"User {current_user.id} requested info for user {user_id}")
    
    try:
        # Regular users can only get information about themselves
        if current_user.id != user_id and current_user.role != UserRole.ADMIN:
            logger.warning(f"Access denied: User {current_user.id} attempted to view user {user_id}")
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not enough permissions"
            )

        user = await get_user_by_id(db, user_id)

        if user is None:
            logger.warning(f"User not found: {user_id}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User not found"
            )

        logger.info(f"Successfully retrieved user {user_id}")
        return await prepare_user_response(user)
        
    except Exception as e:
        logger.error(f"Error retrieving user {user_id}: {str(e)}")
        
        # Handle greenlet_spawn errors specifically
        if "greenlet_spawn has not been called" in str(e):
            logger.error("Detected greenlet_spawn error in async context")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Database access error in async context. This is usually caused by trying to access lazy-loaded attributes."
            )
        
        # Re-raise HTTP exceptions
        if isinstance(e, HTTPException):
            raise e
        
        # Generic error
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error retrieving user: {str(e)}"
        )


@router.put("/id/{user_id}", response_model=UserResponseSchema)
async def update_user(
        user_id: UUID,
        user_update: UserUpdateSchema,
        current_user: UserModel = Depends(get_current_user),
        db: AsyncSession = Depends(get_db)
):
    """Update user information"""
    logger.info(f"User {current_user.id} requested to update user {user_id}")
    logger.debug(f"Update data: {user_update.model_dump(exclude={'password'})}")
    
    try:
        # Regular users can only update their own profile
        if current_user.id != user_id and current_user.role != UserRole.ADMIN:
            logger.warning(f"Access denied: User {current_user.id} attempted to update user {user_id}")
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not enough permissions"
            )

        user = await get_user_by_id(db, user_id)
        if user is None:
            logger.warning(f"User not found: {user_id}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User not found"
            )

        # Update core user fields
        updated_fields = []
        for field in ["username", "email"]:
            if hasattr(user_update, field) and getattr(user_update, field) is not None:
                old_value = getattr(user, field)
                new_value = getattr(user_update, field)
                if old_value != new_value:
                    setattr(user, field, new_value)
                    updated_fields.append(field)
                    logger.debug(f"Updated {field}: {old_value} -> {new_value}")

        # Update profile
        if user.profile:
            for field in ["first_name", "last_name", "avatar_url", "bio", "location"]:
                if hasattr(user_update, field) and getattr(user_update, field) is not None:
                    old_value = getattr(user.profile, field)
                    new_value = getattr(user_update, field)
                    if old_value != new_value:
                        setattr(user.profile, field, new_value)
                        updated_fields.append(f"profile.{field}")
                        logger.debug(f"Updated profile.{field}: {old_value} -> {new_value}")
        else:
            # Create profile if it doesn't exist
            logger.debug("Creating new profile for user")
            profile_data = {}
            for field in ["first_name", "last_name", "avatar_url", "bio", "location"]:
                if hasattr(user_update, field) and getattr(user_update, field) is not None:
                    profile_data[field] = getattr(user_update, field)
            
            if profile_data:
                user.profile = UserProfileModel(user_id=user.id, **profile_data)
                updated_fields.append("profile (created)")

        # Update preferences
        if user.preferences and hasattr(user_update, "beverage_preference") and user_update.beverage_preference is not None:
            old_value = user.preferences.beverage_preference
            new_value = user_update.beverage_preference
            if old_value != new_value:
                user.preferences.beverage_preference = new_value
                updated_fields.append("preferences.beverage_preference")
                logger.debug(f"Updated preferences.beverage_preference: {old_value} -> {new_value}")

        # Update password if provided
        if user_update.password:
            logger.debug("Updating password")
            user.hashed_password = get_password_hash(user_update.password)
            updated_fields.append("password")

        # Save changes to database
        if updated_fields:
            db.add(user)
            await db.commit()
            await db.refresh(user)
            logger.info(f"Successfully updated user {user_id}, fields: {', '.join(updated_fields)}")
        else:
            logger.info(f"No changes made to user {user_id}")

        return await prepare_user_response(user)
        
    except Exception as e:
        logger.error(f"Error updating user {user_id}: {str(e)}")
        
        # Handle greenlet_spawn errors specifically
        if "greenlet_spawn has not been called" in str(e):
            logger.error("Detected greenlet_spawn error in async context")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Database access error in async context. This is usually caused by trying to access lazy-loaded attributes."
            )
        
        # Handle transaction errors
        if "Transaction" in str(e):
            await db.rollback()
            logger.error("Rolled back transaction due to error")
        
        # Re-raise HTTP exceptions
        if isinstance(e, HTTPException):
            raise e
        
        # Generic error
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error updating user: {str(e)}"
        )


@router.delete("/id/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_user(
        user_id: UUID,
        current_user: UserModel = Depends(get_current_admin_user),
        db: AsyncSession = Depends(get_db)
):
    """Delete a user (admin only)"""
    logger.info(f"Admin {current_user.id} requested to delete user {user_id}")
    
    try:
        user = await get_user_by_id(db, user_id)

        if user is None:
            logger.warning(f"User not found: {user_id}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User not found"
            )

        # Prevent admin from deleting themselves
        if user.id == current_user.id:
            logger.warning(f"Admin {current_user.id} attempted to delete themselves")
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Cannot delete yourself"
            )

        db.delete(user)
        await db.commit()
        logger.info(f"Successfully deleted user {user_id}")

        return None
        
    except Exception as e:
        logger.error(f"Error deleting user {user_id}: {str(e)}")
        
        # Handle transaction errors
        if "Transaction" in str(e):
            await db.rollback()
            logger.error("Rolled back transaction due to error")
        
        # Re-raise HTTP exceptions
        if isinstance(e, HTTPException):
            raise e
        
        # Generic error
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error deleting user: {str(e)}"
        )


@router.get("/username/{username}", response_model=UserPublicProfileSchema)
async def get_user_profile(
        username: str = Path(..., min_length=3, max_length=30),
        db: AsyncSession = Depends(get_db),
        current_user: UserModel = Depends(get_current_user)
):
    """
    Get a user's public profile by username.

    Returns the public profile of the user with the specified username.
    Only authenticated users can view profiles.
    """
    logger.info(f"User {current_user.id} requested profile for username: {username}")
    
    try:
        user = await get_user_by_username(db, username)
        if not user:
            logger.warning(f"Username not found: {username}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User not found"
            )

        # Return public profile
        result = {
            "id": user.id,
            "username": user.username,
            "is_verified": user.is_verified,
            "created_at": user.created_at
        }
        
        # Add profile data if available
        if user.profile:
            result.update({
                "first_name": user.profile.first_name,
                "last_name": user.profile.last_name,
                "avatar_url": user.profile.avatar_url,
                "bio": user.profile.bio,
                "location": user.profile.location
            })
        
        logger.info(f"Successfully retrieved profile for username: {username}")
        return result
        
    except Exception as e:
        logger.error(f"Error retrieving profile for username {username}: {str(e)}")
        
        # Re-raise HTTP exceptions
        if isinstance(e, HTTPException):
            raise e
        
        # Generic error
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error retrieving user profile: {str(e)}"
        )
