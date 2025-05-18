"""
API endpoints for working with rooms
"""
from typing import Dict, List, Optional, Any
from uuid import UUID
import time
from datetime import datetime, timezone, timedelta

from fastapi import APIRouter, Depends, HTTPException, Query, Path, Body, status, Request
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_db, get_current_user, get_current_active_user
from app.crud import room_crud, room_participant_crud, room_progress_crud
from app.models.room import Room, RoomParticipant, RoomStatus, RoomParticipantRole
from app.schemas.room import (
    RoomCreate, RoomUpdate, RoomResponse, RoomDetailResponse,
    RoomParticipantCreate, RoomParticipantUpdate, RoomParticipantResponse,
    RoomProgressCreate, RoomProgressUpdate, RoomProgressResponse,
    RoomCodeJoin, RoomJoinResponse, ParticipantList, RoomList
)

from common.logger import get_logger

# Настраиваем логгер
logger = get_logger("room_service.api.endpoints.rooms")

router = APIRouter()

# Словарь для отслеживания неудачных попыток входа по коду
# Структура: {ip_address: {"attempts": count, "last_attempt": timestamp}}
join_attempts = {}
MAX_ATTEMPTS = 5  # Максимальное количество неудачных попыток
BLOCK_DURATION = 300  # Время блокировки в секундах (5 минут)


@router.post("/", status_code=status.HTTP_201_CREATED, response_model=RoomResponse)
async def create_room(
    *,
    db: AsyncSession = Depends(get_db),
    room_in: RoomCreate,
    current_user: Dict[str, Any] = Depends(get_current_active_user)
):
    """
    Create a new room.
    
    The current user will automatically be added as a participant with OWNER role.
    Owner ID is automatically set to the current authenticated user.
    Sensitive data like created_at and room code are generated on the server.
    """
    try:
        # Create a new dict with all the room_in data
        room_data = room_in.model_dump()
        
        # Set the owner_id to the current user's ID
        room_data["owner_id"] = current_user["id"]
        
        # Always set created_at and updated_at to current time
        from datetime import datetime, timezone
        current_time = datetime.now(timezone.utc)
        room_data["created_at"] = current_time
        room_data["updated_at"] = current_time
        
        # Note: The room code is automatically generated in the Room model constructor
        
        # Create the room
        room = await room_crud.create(db=db, obj_in=room_data)
        
        logger.info(f"Room created: {room.id} by user {current_user['id']} with code {room.code}")
        return room
        
    except ValueError as e:
        # Перехватываем ошибки валидации
        logger.error(f"Validation error when creating room: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=f"Invalid room data: {str(e)}"
        )
    except Exception as e:
        # Общие ошибки
        logger.error(f"Error creating room: {str(e)}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to create room: {str(e)}"
        )


@router.get("/", response_model=RoomList)
async def list_rooms(
    *,
    db: AsyncSession = Depends(get_db),
    skip: int = Query(0, ge=0, description="Skip N records"),
    limit: int = Query(100, ge=1, le=100, description="Limit to N records"),
    owner_id: Optional[UUID] = Query(None, description="Filter by owner"),
    course_id: Optional[UUID] = Query(None, description="Filter by course"),
    status: Optional[RoomStatus] = Query(None, description="Filter by status"),
    search: Optional[str] = Query(None, description="Search term"),
    language: str = Query("en", description="Language for search"),
    sort_by: str = Query("created_at", description="Field to sort by"),
    sort_order: str = Query("desc", description="Sort order (asc/desc)"),
    current_user: Dict[str, Any] = Depends(get_current_user)
):
    """
    Get a list of rooms with pagination and filtering options.
    """
    # Построение фильтров
    filters = {
        "owner_id": owner_id,
        "course_id": course_id,
        "status": status,
        "search": search,
        "language": language
    }
    
    # Получение комнат с учетом фильтров
    rooms, total = await room_crud.get_multi(
        db=db, 
        skip=skip, 
        limit=limit,
        filters=filters,
        sort_by=sort_by,
        sort_order=sort_order
    )
    
    return {
        "items": rooms,
        "total": total,
        "page": skip // limit if limit > 0 else 0,
        "size": len(rooms) if rooms else 0,
        "pages": (total + limit - 1) // limit if limit > 0 else 0
    }


@router.get("/my", response_model=RoomList)
async def list_my_rooms(
    *,
    db: AsyncSession = Depends(get_db),
    skip: int = Query(0, ge=0, description="Skip N records"),
    limit: int = Query(100, ge=1, le=100, description="Limit to N records"),
    status: Optional[RoomStatus] = Query(None, description="Filter by status"),
    current_user: Dict[str, Any] = Depends(get_current_active_user)
):
    """
    Get a list of rooms that the current user is participating in.
    """
    # Получение комнат, в которых участвует пользователь
    rooms, total = await room_participant_crud.get_user_rooms(
        db=db,
        user_id=current_user["id"],
        skip=skip,
        limit=limit,
        status=status
    )
    
    return {
        "items": rooms,
        "total": total,
        "page": 0,
        "size": len(rooms) if rooms else 0,
        "pages": (total + limit - 1) // limit if limit > 0 else 0
    }


@router.get("/{room_id}", response_model=RoomDetailResponse)
async def get_room(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    current_user: Dict[str, Any] = Depends(get_current_user)
):
    """
    Get a specific room by ID.
    """
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # TODO: Добавить проверку доступа к комнате через participant
    
    return room


@router.put("/{room_id}", response_model=RoomResponse)
async def update_room(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    room_in: RoomUpdate,
    current_user: Dict[str, Any] = Depends(get_current_active_user)
):
    """
    Update a room by ID.
    
    Only the room owner or a teacher can update room details.
    """
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # Проверка, является ли пользователь владельцем или учителем
    participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=current_user["id"])
    if not participant or participant.role not in [RoomParticipantRole.OWNER, RoomParticipantRole.TEACHER]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only room owner or teacher can update room details"
        )
    
    # Обновляем комнату
    updated_room = await room_crud.update(db=db, db_obj=room, obj_in=room_in)
    return updated_room


@router.delete("/{room_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_room(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    current_user: Dict[str, Any] = Depends(get_current_active_user)
):
    """
    Delete a room by ID.
    
    Only the room owner can delete a room.
    """
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # Проверка, является ли пользователь владельцем
    if room.owner_id != current_user["id"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only room owner can delete a room"
        )
    
    # Удаляем комнату
    await room_crud.delete(db=db, room_id=room_id)


@router.post("/join", response_model=RoomJoinResponse)
async def join_room_by_code(
    *,
    db: AsyncSession = Depends(get_db),
    join_data: RoomCodeJoin,
    current_user: Dict[str, Any] = Depends(get_current_active_user),
    request: Request
):
    """
    Join a room using a room code.
    User ID is automatically taken from the current authenticated user.
    
    Rate limiting is applied to prevent brute force attacks.
    """
    # Получаем IP-адрес клиента
    client_ip = request.client.host if request.client else "unknown"
    
    # Проверяем, не заблокирован ли IP
    if client_ip in join_attempts:
        attempts_data = join_attempts[client_ip]
        if attempts_data["attempts"] >= MAX_ATTEMPTS:
            # Проверяем, прошло ли достаточно времени с момента блокировки
            time_passed = time.time() - attempts_data["last_attempt"]
            if time_passed < BLOCK_DURATION:
                remaining = int(BLOCK_DURATION - time_passed)
                logger.warning(f"Rate limit exceeded for IP {client_ip} when joining room")
                raise HTTPException(
                    status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                    detail=f"Too many failed attempts. Please try again in {remaining} seconds."
                )
            else:
                # Сбрасываем счетчик попыток, если время блокировки истекло
                join_attempts[client_ip] = {"attempts": 0, "last_attempt": time.time()}
    
    # Находим комнату по коду
    room = await room_crud.get_by_code(db=db, code=join_data.code)
    if not room:
        # Увеличиваем счетчик неудачных попыток
        if client_ip in join_attempts:
            join_attempts[client_ip]["attempts"] += 1
            join_attempts[client_ip]["last_attempt"] = time.time()
        else:
            join_attempts[client_ip] = {"attempts": 1, "last_attempt": time.time()}
            
        logger.info(f"Failed room join attempt with code {join_data.code} from IP {client_ip}")
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found with the provided code"
        )
    
    # Сбрасываем счетчик неудачных попыток при успешном входе
    if client_ip in join_attempts:
        join_attempts[client_ip] = {"attempts": 0, "last_attempt": time.time()}
    
    # Проверяем статус комнаты
    if room.status != RoomStatus.ACTIVE and room.status != RoomStatus.PENDING:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=f"Cannot join room with status {room.status}"
        )
    
    # Проверяем максимальное количество участников, если оно установлено
    if room.max_participants > 0:
        participants, total = await room_participant_crud.get_room_participants(
            db=db, 
            room_id=room.id,
            skip=0,
            limit=1000  # Большое значение, чтобы получить всех участников
        )
        if total >= room.max_participants:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Room has reached maximum number of participants"
            )
    
    # Проверяем, не состоит ли пользователь уже в комнате
    existing_participant = await room_participant_crud.get(db=db, room_id=room.id, user_id=current_user["id"])
    if existing_participant:
        return {
            "room_id": room.id,
            "joined": False,
            "message": "You are already a participant in this room"
        }
    
    # Добавляем пользователя как участника
    participant_data = RoomParticipantCreate(
        user_id=current_user["id"],
        role=RoomParticipantRole.STUDENT,
        participant_metadata={}
    )
    
    participant = await room_participant_crud.create(db=db, room_id=room.id, obj_in=participant_data)
    if not participant:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to join the room"
        )
    
    # Если комната была в состоянии PENDING, меняем ее на ACTIVE после присоединения первого участника
    if room.status == RoomStatus.PENDING:
        await room_crud.update(
            db=db, 
            db_obj=room, 
            obj_in=RoomUpdate(status=RoomStatus.ACTIVE)
        )
    
    logger.info(f"User {current_user['id']} successfully joined room {room.id} with code {join_data.code}")
    return {
        "room_id": room.id,
        "joined": True,
        "message": "Successfully joined the room"
    }


@router.get("/{room_id}/participants", response_model=ParticipantList)
async def get_room_participants(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    skip: int = Query(0, ge=0, description="Skip N records"),
    limit: int = Query(100, ge=1, le=100, description="Limit to N records"),
    current_user: Dict[str, Any] = Depends(get_current_user)
):
    """
    Get a list of participants in a room.
    """
    # Проверяем существование комнаты
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # TODO: Добавить проверку доступа к комнате
    
    # Получаем участников комнаты
    participants, total = await room_participant_crud.get_room_participants(
        db=db,
        room_id=room_id,
        skip=skip,
        limit=limit
    )
    
    return {
        "participants": participants,
        "total": total
    }


@router.post("/{room_id}/participants", response_model=RoomParticipantResponse)
async def add_room_participant(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    participant_in: RoomParticipantCreate,
    current_user: Dict[str, Any] = Depends(get_current_active_user)
):
    """
    Add a participant to a room.
    
    Only room owner or teacher can add participants.
    """
    # Проверяем существование комнаты
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # Проверяем права пользователя
    participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=current_user["id"])
    if not participant or participant.role not in [RoomParticipantRole.OWNER, RoomParticipantRole.TEACHER]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only room owner or teacher can add participants"
        )
    
    # Проверяем максимальное количество участников, если оно установлено
    if room.max_participants > 0:
        participants, total = await room_participant_crud.get_room_participants(
            db=db, 
            room_id=room.id,
            skip=0,
            limit=1000
        )
        if total >= room.max_participants:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Room has reached maximum number of participants"
            )
    
    # Проверяем, не существует ли уже участник
    existing_participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=participant_in.user_id)
    if existing_participant:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="User is already a participant in this room"
        )
    
    # Добавляем участника
    new_participant = await room_participant_crud.create(db=db, room_id=room_id, obj_in=participant_in)
    if not new_participant:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to add participant"
        )
    
    return new_participant


@router.put("/{room_id}/participants/{user_id}", response_model=RoomParticipantResponse)
async def update_room_participant(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    user_id: UUID = Path(..., description="User ID"),
    participant_in: RoomParticipantUpdate,
    current_user: Dict[str, Any] = Depends(get_current_active_user)
):
    """
    Update a participant's role or metadata.
    
    Only room owner can change participant roles.
    """
    # Проверяем существование комнаты
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # Проверяем наличие участника
    participant_to_update = await room_participant_crud.get(db=db, room_id=room_id, user_id=user_id)
    if not participant_to_update:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Participant not found"
        )
    
    # Проверяем права текущего пользователя
    current_participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=current_user["id"])
    if not current_participant or current_participant.role != RoomParticipantRole.OWNER:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only room owner can update participant roles"
        )
    
    # Запрещаем менять роль владельца
    if participant_to_update.role == RoomParticipantRole.OWNER and "role" in participant_in.dict(exclude_unset=True):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Cannot change the role of the room owner"
        )
    
    # Обновляем участника
    updated_participant = await room_participant_crud.update(
        db=db, 
        db_obj=participant_to_update, 
        obj_in=participant_in
    )
    
    return updated_participant


@router.delete("/{room_id}/participants/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
async def remove_room_participant(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    user_id: UUID = Path(..., description="User ID"),
    current_user: Dict[str, Any] = Depends(get_current_active_user)
):
    """
    Remove a participant from a room.
    
    Participants can leave by removing themselves.
    Owner and teachers can remove participants.
    Owner cannot be removed.
    """
    # Проверяем существование комнаты
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # Проверяем наличие участника
    participant_to_remove = await room_participant_crud.get(db=db, room_id=room_id, user_id=user_id)
    if not participant_to_remove:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Participant not found"
        )
    
    # Запрещаем удалять владельца
    if participant_to_remove.role == RoomParticipantRole.OWNER:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Cannot remove the room owner"
        )
    
    # Проверяем права
    # Если пользователь удаляет сам себя - это всегда разрешено
    if user_id != current_user["id"]:
        # Если не себя - проверяем, имеет ли текущий пользователь права на удаление других
        current_participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=current_user["id"])
        if not current_participant or current_participant.role not in [RoomParticipantRole.OWNER, RoomParticipantRole.TEACHER]:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Only room owner or teacher can remove participants"
            )
    
    # Удаляем участника
    await room_participant_crud.delete(db=db, room_id=room_id, user_id=user_id)


@router.post("/{room_id}/progress", response_model=RoomProgressResponse)
async def create_progress_record(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    progress_in: RoomProgressCreate,
    current_user: Dict[str, Any] = Depends(get_current_active_user)
):
    """
    Create a new progress record for a node in a room.
    
    Users can only create progress records for themselves.
    Teachers and owners can create progress records for any participant.
    """
    # Проверяем существование комнаты
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # Если пользователь создает запись не для себя, проверяем права
    if progress_in.user_id != current_user["id"]:
        current_participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=current_user["id"])
        if not current_participant or current_participant.role not in [RoomParticipantRole.OWNER, RoomParticipantRole.TEACHER]:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="You can only create progress records for yourself"
            )
    
    # Проверяем, что пользователь является участником комнаты
    target_participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=progress_in.user_id)
    if not target_participant:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User is not a participant in this room"
        )
    
    # Проверяем, существует ли уже запись о прогрессе для этого узла
    existing_progress = await room_progress_crud.get_by_node(
        db=db, 
        room_id=room_id, 
        user_id=progress_in.user_id, 
        node_id=progress_in.node_id
    )
    
    if existing_progress:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Progress record already exists for this node"
        )
    
    # Создаем запись о прогрессе
    progress = await room_progress_crud.create(db=db, room_id=room_id, obj_in=progress_in)
    return progress


@router.get("/{room_id}/progress/{user_id}", response_model=List[RoomProgressResponse])
async def get_user_progress(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    user_id: UUID = Path(..., description="User ID"),
    current_user: Dict[str, Any] = Depends(get_current_user)
):
    """
    Get all progress records for a user in a room.
    
    Users can only view their own progress.
    Teachers and owners can view progress of any participant.
    """
    # Проверяем существование комнаты
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # Если пользователь запрашивает не свой прогресс, проверяем права
    if user_id != current_user["id"]:
        current_participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=current_user["id"])
        if not current_participant or current_participant.role not in [RoomParticipantRole.OWNER, RoomParticipantRole.TEACHER]:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="You can only view your own progress"
            )
    
    # Проверяем, что пользователь является участником комнаты
    target_participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=user_id)
    if not target_participant:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User is not a participant in this room"
        )
    
    # Получаем прогресс пользователя
    progress = await room_progress_crud.get_user_progress(db=db, room_id=room_id, user_id=user_id)
    return progress


@router.put("/{room_id}/progress/{progress_id}", response_model=RoomProgressResponse)
async def update_progress_record(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    progress_id: UUID = Path(..., description="Progress ID"),
    progress_in: RoomProgressUpdate,
    current_user: Dict[str, Any] = Depends(get_current_active_user)
):
    """
    Update a progress record.
    
    Users can only update their own progress.
    Teachers and owners can update progress of any participant.
    """
    # Проверяем существование комнаты
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # Находим запись о прогрессе
    progress = await room_progress_crud.get(db=db, progress_id=progress_id)
    if not progress or progress.room_id != room_id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Progress record not found in this room"
        )
    
    # Если пользователь обновляет не свой прогресс, проверяем права
    if progress.user_id != current_user["id"]:
        current_participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=current_user["id"])
        if not current_participant or current_participant.role not in [RoomParticipantRole.OWNER, RoomParticipantRole.TEACHER]:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="You can only update your own progress"
            )
    
    # Обновляем прогресс
    updated_progress = await room_progress_crud.update(db=db, db_obj=progress, obj_in=progress_in)
    return updated_progress


@router.delete("/{room_id}/progress/{progress_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_progress_record(
    *,
    db: AsyncSession = Depends(get_db),
    room_id: UUID = Path(..., description="Room ID"),
    progress_id: UUID = Path(..., description="Progress ID"),
    current_user: Dict[str, Any] = Depends(get_current_active_user)
):
    """
    Delete a progress record.
    
    Only teachers and owners can delete progress records.
    """
    # Проверяем существование комнаты
    room = await room_crud.get(db=db, room_id=room_id)
    if not room:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Room not found"
        )
    
    # Находим запись о прогрессе
    progress = await room_progress_crud.get(db=db, progress_id=progress_id)
    if not progress or progress.room_id != room_id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Progress record not found in this room"
        )
    
    # Проверяем права
    current_participant = await room_participant_crud.get(db=db, room_id=room_id, user_id=current_user["id"])
    if not current_participant or current_participant.role not in [RoomParticipantRole.OWNER, RoomParticipantRole.TEACHER]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Only room owner or teacher can delete progress records"
        )
    
    # Удаляем прогресс
    await room_progress_crud.delete(db=db, progress_id=progress_id)
