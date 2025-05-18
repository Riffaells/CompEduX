"""
Friendship service for managing user friendships
"""
from typing import List, Optional, Tuple, Dict, Any
from uuid import UUID

from sqlalchemy import select, or_, and_, func
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import aliased

from ..models.user import UserModel, UserProfileModel
from ..models.associations import FriendshipModel
from ..models.enums import FriendshipStatus


async def get_friendship(db: AsyncSession, friendship_id: UUID) -> Optional[FriendshipModel]:
    """
    Get a friendship by ID
    
    Args:
        db: Database session
        friendship_id: Friendship ID
        
    Returns:
        FriendshipModel or None if not found
    """
    result = await db.execute(select(FriendshipModel).where(FriendshipModel.id == friendship_id))
    return result.scalars().first()


async def get_friendship_between_users(
    db: AsyncSession, 
    user_id: UUID, 
    friend_id: UUID
) -> Optional[FriendshipModel]:
    """
    Get a friendship between two users
    
    Args:
        db: Database session
        user_id: User ID
        friend_id: Friend ID
        
    Returns:
        FriendshipModel or None if not found
    """
    # Проверяем прямую дружбу (user -> friend)
    result = await db.execute(
        select(FriendshipModel).where(
            and_(
                FriendshipModel.user_id == user_id,
                FriendshipModel.friend_id == friend_id
            )
        )
    )
    direct = result.scalars().first()
    if direct:
        return direct
    
    # Проверяем обратную дружбу (friend -> user)
    result = await db.execute(
        select(FriendshipModel).where(
            and_(
                FriendshipModel.user_id == friend_id,
                FriendshipModel.friend_id == user_id
            )
        )
    )
    reverse = result.scalars().first()
    return reverse


async def get_friendship_status(
    db: AsyncSession, 
    user_id: UUID, 
    friend_id: UUID
) -> Optional[FriendshipStatus]:
    """
    Get friendship status between two users
    
    Args:
        db: Database session
        user_id: User ID
        friend_id: Friend ID
        
    Returns:
        FriendshipStatus or None if no relationship exists
    """
    friendship = await get_friendship_between_users(db, user_id, friend_id)
    return friendship.status if friendship else None


async def get_friends(
    db: AsyncSession,
    user_id: UUID,
    status: Optional[FriendshipStatus] = None,
    skip: int = 0,
    limit: int = 100
) -> Tuple[List[Dict[str, Any]], int]:
    """
    Get friends of a user
    
    Args:
        db: Database session
        user_id: User ID
        status: Optional filter by friendship status
        skip: Number of records to skip
        limit: Maximum number of records to return
        
    Returns:
        Tuple of (list of friend users with friendship info, total count)
    """
    # Создаем алиасы для таблиц
    UserAlias = aliased(UserModel)
    ProfileAlias = aliased(UserProfileModel)
    FriendshipAlias = aliased(FriendshipModel)
    
    # Запрос для исходящих дружеских связей (user -> friend) с прямой выборкой полей
    outgoing_query = (
        select(
            UserAlias.id,
            UserAlias.username,
            UserAlias.email,
            UserAlias.is_verified,
            ProfileAlias.first_name,
            ProfileAlias.last_name,
            ProfileAlias.avatar_url,
            FriendshipAlias.id.label("friendship_id"),
            FriendshipAlias.status,
            FriendshipAlias.requested_at,
            FriendshipAlias.user_id,
            FriendshipAlias.friend_id
        )
        .join(FriendshipAlias, UserAlias.id == FriendshipAlias.friend_id)
        .outerjoin(ProfileAlias, UserAlias.id == ProfileAlias.user_id)
        .where(FriendshipAlias.user_id == user_id)
    )
    
    # Запрос для входящих дружеских связей (friend -> user) с прямой выборкой полей
    incoming_query = (
        select(
            UserAlias.id,
            UserAlias.username,
            UserAlias.email,
            UserAlias.is_verified,
            ProfileAlias.first_name,
            ProfileAlias.last_name,
            ProfileAlias.avatar_url,
            FriendshipAlias.id.label("friendship_id"),
            FriendshipAlias.status,
            FriendshipAlias.requested_at,
            FriendshipAlias.user_id,
            FriendshipAlias.friend_id
        )
        .join(FriendshipAlias, UserAlias.id == FriendshipAlias.user_id)
        .outerjoin(ProfileAlias, UserAlias.id == ProfileAlias.user_id)
        .where(FriendshipAlias.friend_id == user_id)
    )
    
    # Применяем фильтр по статусу, если указан
    if status:
        outgoing_query = outgoing_query.where(FriendshipAlias.status == status)
        incoming_query = incoming_query.where(FriendshipAlias.status == status)
    
    # Выполняем запросы для подсчета
    outgoing_count_result = await db.execute(select(func.count()).select_from(outgoing_query.subquery()))
    incoming_count_result = await db.execute(select(func.count()).select_from(incoming_query.subquery()))
    
    outgoing_count = outgoing_count_result.scalar() or 0
    incoming_count = incoming_count_result.scalar() or 0
    
    total = outgoing_count + incoming_count
    
    # Применяем пагинацию к обоим запросам
    outgoing_query = outgoing_query.offset(skip).limit(limit)
    
    # Получаем входящие связи только если нужно больше результатов
    remaining = limit - min(outgoing_count, limit)
    incoming_skip = max(0, skip - outgoing_count)
    incoming_query = incoming_query.offset(incoming_skip).limit(remaining)
    
    # Выполняем запросы
    outgoing_result = await db.execute(outgoing_query)
    outgoing_rows = outgoing_result.all()
    
    incoming_result = await db.execute(incoming_query)
    incoming_rows = incoming_result.all()
    
    # Объединяем результаты
    friends = []
    
    # Обрабатываем исходящие дружеские связи
    for row in outgoing_rows:
        friend_data = {
            "id": row.id,
            "username": row.username,
            "email": row.email,  # Можно добавить проверку приватности здесь
            "is_verified": row.is_verified,
            "friendship_id": row.friendship_id,
            "status": row.status,
            "requested_at": row.requested_at,
            "direction": "outgoing"
        }
        
        # Добавляем данные профиля, если они есть
        if row.first_name or row.last_name or row.avatar_url:
            friend_data.update({
                "first_name": row.first_name,
                "last_name": row.last_name,
                "avatar_url": row.avatar_url
            })
        
        friends.append(friend_data)
    
    # Обрабатываем входящие дружеские связи
    for row in incoming_rows:
        friend_data = {
            "id": row.id,
            "username": row.username,
            "email": row.email,  # Можно добавить проверку приватности здесь
            "is_verified": row.is_verified,
            "friendship_id": row.friendship_id,
            "status": row.status,
            "requested_at": row.requested_at,
            "direction": "incoming"
        }
        
        # Добавляем данные профиля, если они есть
        if row.first_name or row.last_name or row.avatar_url:
            friend_data.update({
                "first_name": row.first_name,
                "last_name": row.last_name,
                "avatar_url": row.avatar_url
            })
        
        friends.append(friend_data)
    
    return friends, total


async def create_friendship_request(
    db: AsyncSession,
    user_id: UUID,
    friend_id: UUID
) -> FriendshipModel:
    """
    Create a new friendship request
    
    Args:
        db: Database session
        user_id: User ID (requester)
        friend_id: Friend ID (recipient)
        
    Returns:
        Created friendship object
    """
    # Проверяем, существует ли уже дружба
    existing = await get_friendship_between_users(db, user_id, friend_id)
    if existing:
        return existing
    
    # Создаем новый запрос на дружбу
    friendship = FriendshipModel(
        user_id=user_id,
        friend_id=friend_id,
        status=FriendshipStatus.PENDING
    )
    db.add(friendship)
    await db.commit()
    await db.refresh(friendship)
    return friendship


async def update_friendship_status(
    db: AsyncSession,
    friendship_id: UUID,
    status: FriendshipStatus
) -> Optional[FriendshipModel]:
    """
    Update friendship status
    
    Args:
        db: Database session
        friendship_id: Friendship ID
        status: New status
        
    Returns:
        Updated friendship object or None if not found
    """
    friendship = await get_friendship(db, friendship_id)
    if not friendship:
        return None
    
    friendship.status = status
    db.add(friendship)
    await db.commit()
    await db.refresh(friendship)
    return friendship


async def delete_friendship(
    db: AsyncSession,
    friendship_id: UUID
) -> bool:
    """
    Delete a friendship
    
    Args:
        db: Database session
        friendship_id: Friendship ID
        
    Returns:
        True if friendship was deleted, False otherwise
    """
    friendship = await get_friendship(db, friendship_id)
    if not friendship:
        return False
    
    await db.delete(friendship)
    await db.commit()
    return True 