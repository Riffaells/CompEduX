from fastapi import FastAPI, Depends, HTTPException, status, Header
from sqlalchemy.orm import Session
from typing import List, Optional
import models, schemas, database
from datetime import datetime, timedelta
import requests
import os

# Создаем таблицы в базе данных
models.Base.metadata.create_all(bind=database.engine)

app = FastAPI(title="Room Service")

# Функция для проверки токена через auth_service
async def get_current_user_id(authorization: Optional[str] = Header(None)):
    if not authorization:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Not authenticated",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # В реальном приложении здесь будет запрос к auth_service
    # для проверки токена и получения ID пользователя
    # Для примера просто возвращаем фиктивный ID
    return 1  # Фиктивный ID пользователя

# Функции для работы с комнатами
def get_room(db: Session, room_id: int):
    return db.query(models.Room).filter(models.Room.id == room_id).first()

def get_rooms(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Room).offset(skip).limit(limit).all()

def create_room(db: Session, room: schemas.RoomCreate, owner_id: int):
    db_room = models.Room(
        name=room.name,
        description=room.description,
        type=models.RoomType[room.type.upper()],
        parent_id=room.parent_id,
        owner_id=owner_id
    )
    db.add(db_room)
    db.commit()
    db.refresh(db_room)
    return db_room

def get_room_members(db: Session, room_id: int):
    return db.query(models.RoomMember).filter(models.RoomMember.room_id == room_id).all()

def add_room_member(db: Session, member: schemas.RoomMemberCreate):
    db_member = models.RoomMember(**member.dict())
    db.add(db_member)
    db.commit()
    db.refresh(db_member)
    return db_member

def create_invitation(db: Session, invitation: schemas.InvitationCreate, inviter_id: int):
    expires_at = invitation.expires_at or datetime.utcnow() + timedelta(days=7)
    db_invitation = models.Invitation(
        room_id=invitation.room_id,
        user_id=invitation.user_id,
        inviter_id=inviter_id,
        expires_at=expires_at
    )
    db.add(db_invitation)
    db.commit()
    db.refresh(db_invitation)
    return db_invitation

# Эндпоинты
@app.post("/rooms/", response_model=schemas.Room)
def create_new_room(
    room: schemas.RoomCreate,
    db: Session = Depends(database.get_db),
    current_user_id: int = Depends(get_current_user_id)
):
    # Проверка, если это наследуемая комната
    if room.parent_id:
        parent_room = get_room(db, room.parent_id)
        if not parent_room:
            raise HTTPException(status_code=404, detail="Parent room not found")

    return create_room(db=db, room=room, owner_id=current_user_id)

@app.get("/rooms/", response_model=List[schemas.Room])
def read_rooms(
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(database.get_db),
    current_user_id: int = Depends(get_current_user_id)
):
    rooms = get_rooms(db, skip=skip, limit=limit)
    return rooms

@app.get("/rooms/{room_id}", response_model=schemas.Room)
def read_room(
    room_id: int,
    db: Session = Depends(database.get_db),
    current_user_id: int = Depends(get_current_user_id)
):
    db_room = get_room(db, room_id=room_id)
    if db_room is None:
        raise HTTPException(status_code=404, detail="Room not found")
    return db_room

@app.post("/rooms/{room_id}/members/", response_model=schemas.RoomMember)
def add_member_to_room(
    room_id: int,
    user_id: int,
    db: Session = Depends(database.get_db),
    current_user_id: int = Depends(get_current_user_id)
):
    db_room = get_room(db, room_id=room_id)
    if db_room is None:
        raise HTTPException(status_code=404, detail="Room not found")

    # Проверка прав доступа (только владелец может добавлять участников)
    if db_room.owner_id != current_user_id:
        raise HTTPException(status_code=403, detail="Not enough permissions")

    # Проверка, если комната закрытая
    if db_room.type == models.RoomType.PRIVATE:
        # Здесь должна быть проверка приглашения
        pass

    member = schemas.RoomMemberCreate(room_id=room_id, user_id=user_id)
    return add_room_member(db=db, member=member)

@app.get("/rooms/{room_id}/members/", response_model=List[schemas.RoomMember])
def read_room_members(
    room_id: int,
    db: Session = Depends(database.get_db),
    current_user_id: int = Depends(get_current_user_id)
):
    db_room = get_room(db, room_id=room_id)
    if db_room is None:
        raise HTTPException(status_code=404, detail="Room not found")

    return get_room_members(db, room_id=room_id)

@app.post("/invitations/", response_model=schemas.Invitation)
def create_new_invitation(
    invitation: schemas.InvitationCreate,
    db: Session = Depends(database.get_db),
    current_user_id: int = Depends(get_current_user_id)
):
    db_room = get_room(db, room_id=invitation.room_id)
    if db_room is None:
        raise HTTPException(status_code=404, detail="Room not found")

    # Проверка прав доступа (только владелец может отправлять приглашения)
    if db_room.owner_id != current_user_id:
        raise HTTPException(status_code=403, detail="Not enough permissions")

    return create_invitation(db=db, invitation=invitation, inviter_id=current_user_id)

@app.get("/health")
def health_check():
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
