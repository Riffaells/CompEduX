from typing import Dict, List, Optional, Any
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, Query, Path, Body, status
from sqlalchemy.ext.asyncio import AsyncSession

from common.logger import get_logger
from ...db.session import get_db
from ...models.user import UserModel
from ...models.enums import FriendshipStatus
from ...schemas.friendship import (
    FriendshipCreate, FriendshipUpdate, FriendshipResponse,
    FriendWithStatusSchema, FriendListResponse
)
from ...services.auth import get_current_user, get_user_by_id
from ...services.friendship import (
    get_friendship, get_friendship_between_users, get_friendship_status,
    get_friends, create_friendship_request, update_friendship_status, delete_friendship
)

# Создаем логгер
logger = get_logger(__name__)

router = APIRouter()


@router.get("/", response_model=FriendListResponse)
async def list_friends(
    status: Optional[FriendshipStatus] = Query(None, description="Filter by friendship status"),
    skip: int = Query(0, ge=0, description="Skip N records"),
    limit: int = Query(100, ge=1, le=100, description="Limit to N records"),
    current_user: UserModel = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Get a list of current user's friends.
    
    Allows filtering by friendship status:
    - PENDING: Friend requests waiting for acceptance
    - ACCEPTED: Accepted friendships
    - REJECTED: Rejected friend requests
    - BLOCKED: Blocked users
    """
    logger.info(f"User {current_user.id} requested friends list with status={status}, skip={skip}, limit={limit}")
    
    try:
        friends, total = await get_friends(
            db=db,
            user_id=current_user.id,
            status=status,
            skip=skip,
            limit=limit
        )
        
        logger.info(f"Successfully retrieved {len(friends)} friends for user {current_user.id}")
        
        response = {
            "items": friends,
            "total": total,
            "page": skip // limit if limit > 0 else 0,
            "size": len(friends) if friends else 0,
            "pages": (total + limit - 1) // limit if limit > 0 else 0
        }
        
        return response
        
    except Exception as e:
        logger.error(f"Error getting friends list for user {current_user.id}: {str(e)}")
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
            detail=f"Error retrieving friends: {str(e)}"
        )


@router.post("/", response_model=FriendshipResponse)
async def send_friend_request(
    friendship_in: FriendshipCreate,
    current_user: UserModel = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Send a friend request to another user.
    """
    logger.info(f"User {current_user.id} sending friend request to {friendship_in.friend_id}")
    
    try:
        # Проверяем, что пользователь не отправляет запрос самому себе
        if friendship_in.friend_id == current_user.id:
            logger.warning(f"User {current_user.id} attempted to send friend request to themselves")
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Cannot send friend request to yourself"
            )
        
        # Проверяем, что пользователь существует
        friend = await get_user_by_id(db, friendship_in.friend_id)
        if not friend:
            logger.warning(f"User {current_user.id} attempted to send friend request to non-existent user {friendship_in.friend_id}")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User not found"
            )
        
        # Создаем запрос на дружбу
        friendship = await create_friendship_request(
            db=db,
            user_id=current_user.id,
            friend_id=friendship_in.friend_id
        )
        
        logger.info(f"Friend request created: {friendship.id} from {current_user.id} to {friendship_in.friend_id}")
        return friendship
        
    except HTTPException:
        # Re-raise HTTP exceptions
        raise
    
    except Exception as e:
        logger.error(f"Error creating friend request from {current_user.id} to {friendship_in.friend_id}: {str(e)}")
        logger.error(f"Error type: {type(e).__name__}")
        
        # Handle greenlet_spawn errors specifically
        if "greenlet_spawn has not been called" in str(e):
            logger.error("Detected greenlet_spawn error in async context")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Database access error in async context"
            )
        
        # Generic error
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error creating friend request: {str(e)}"
        )


@router.get("/check/{user_id}", response_model=Dict[str, Any])
async def check_friendship_status(
    user_id: UUID,
    current_user: UserModel = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Check friendship status between current user and another user.
    """
    logger.info(f"User {current_user.id} checking friendship status with {user_id}")
    
    try:
        # Проверяем, что пользователь существует
        user = await get_user_by_id(db, user_id)
        if not user:
            logger.warning(f"User {user_id} not found when checking friendship status")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User not found"
            )
        
        # Проверяем статус дружбы
        friendship = await get_friendship_between_users(
            db=db,
            user_id=current_user.id,
            friend_id=user_id
        )
        
        if not friendship:
            logger.info(f"No friendship found between {current_user.id} and {user_id}")
            return {
                "status": None,
                "friendship_id": None,
                "direction": None,
                "is_friend": False
            }
        
        # Определяем направление дружбы
        direction = "outgoing" if friendship.user_id == current_user.id else "incoming"
        
        logger.info(f"Friendship status between {current_user.id} and {user_id}: {friendship.status}, direction: {direction}")
        return {
            "status": friendship.status,
            "friendship_id": friendship.id,
            "direction": direction,
            "is_friend": friendship.status == FriendshipStatus.ACCEPTED
        }
        
    except HTTPException:
        # Re-raise HTTP exceptions
        raise
    
    except Exception as e:
        logger.error(f"Error checking friendship status between {current_user.id} and {user_id}: {str(e)}")
        logger.error(f"Error type: {type(e).__name__}")
        
        # Handle greenlet_spawn errors specifically
        if "greenlet_spawn has not been called" in str(e):
            logger.error("Detected greenlet_spawn error in async context")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Database access error in async context"
            )
        
        # Generic error
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error checking friendship status: {str(e)}"
        )


@router.put("/{friendship_id}", response_model=FriendshipResponse)
async def update_friendship(
    friendship_id: UUID,
    friendship_in: FriendshipUpdate,
    current_user: UserModel = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Update friendship status (accept, reject, or block).
    """
    logger.info(f"User {current_user.id} updating friendship {friendship_id} to status {friendship_in.status}")
    
    try:
        # Получаем запрос на дружбу
        friendship = await get_friendship(db=db, friendship_id=friendship_id)
        if not friendship:
            logger.warning(f"Friendship {friendship_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Friendship not found"
            )
        
        # Проверяем, что пользователь имеет право обновлять этот запрос
        # (должен быть либо отправителем, либо получателем)
        if friendship.user_id != current_user.id and friendship.friend_id != current_user.id:
            logger.warning(f"User {current_user.id} not authorized to update friendship {friendship_id}")
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to update this friendship"
            )
        
        # Обновляем статус дружбы
        updated_friendship = await update_friendship_status(
            db=db,
            friendship_id=friendship_id,
            status=friendship_in.status
        )
        
        logger.info(f"Friendship {friendship_id} updated to status {friendship_in.status}")
        return updated_friendship
        
    except HTTPException:
        # Re-raise HTTP exceptions
        raise
    
    except Exception as e:
        logger.error(f"Error updating friendship {friendship_id}: {str(e)}")
        logger.error(f"Error type: {type(e).__name__}")
        
        # Handle greenlet_spawn errors specifically
        if "greenlet_spawn has not been called" in str(e):
            logger.error("Detected greenlet_spawn error in async context")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Database access error in async context"
            )
        
        # Generic error
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error updating friendship: {str(e)}"
        )


@router.delete("/{friendship_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_friendship(
    friendship_id: UUID,
    current_user: UserModel = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Delete a friendship or friend request.
    """
    logger.info(f"User {current_user.id} deleting friendship {friendship_id}")
    
    try:
        # Получаем запрос на дружбу
        friendship = await get_friendship(db=db, friendship_id=friendship_id)
        if not friendship:
            logger.warning(f"Friendship {friendship_id} not found")
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Friendship not found"
            )
        
        # Проверяем, что пользователь имеет право удалять этот запрос
        if friendship.user_id != current_user.id and friendship.friend_id != current_user.id:
            logger.warning(f"User {current_user.id} not authorized to delete friendship {friendship_id}")
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Not authorized to delete this friendship"
            )
        
        # Удаляем дружбу
        success = await delete_friendship(db=db, friendship_id=friendship_id)
        if not success:
            logger.error(f"Failed to delete friendship {friendship_id}")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to delete friendship"
            )
        
        logger.info(f"Friendship {friendship_id} deleted successfully")
        return None
        
    except HTTPException:
        # Re-raise HTTP exceptions
        raise
    
    except Exception as e:
        logger.error(f"Error deleting friendship {friendship_id}: {str(e)}")
        logger.error(f"Error type: {type(e).__name__}")
        
        # Handle greenlet_spawn errors specifically
        if "greenlet_spawn has not been called" in str(e):
            logger.error("Detected greenlet_spawn error in async context")
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Database access error in async context"
            )
        
        # Generic error
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error deleting friendship: {str(e)}"
        ) 