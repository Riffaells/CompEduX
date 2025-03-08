from sqlalchemy import Boolean, Column, Integer, String, DateTime, ForeignKey, Enum, Text
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
import enum
from database import Base

class RoomType(enum.Enum):
    PUBLIC = "public"
    PRIVATE = "private"
    INHERITED = "inherited"

class Room(Base):
    __tablename__ = "rooms"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, index=True)
    description = Column(Text)
    type = Column(Enum(RoomType), default=RoomType.PUBLIC)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), onupdate=func.now())

    # Для наследуемых комнат
    parent_id = Column(Integer, ForeignKey("rooms.id"), nullable=True)

    # Владелец комнаты (ID пользователя)
    owner_id = Column(Integer, index=True)

    # Связь с другими комнатами (для наследуемых)
    children = relationship("Room", backref="parent", remote_side=[id])

class RoomMember(Base):
    __tablename__ = "room_members"

    id = Column(Integer, primary_key=True, index=True)
    room_id = Column(Integer, ForeignKey("rooms.id"))
    user_id = Column(Integer, index=True)
    joined_at = Column(DateTime(timezone=True), server_default=func.now())

    # Роль в комнате (админ, модератор, участник)
    role = Column(String, default="member")

class Invitation(Base):
    __tablename__ = "invitations"

    id = Column(Integer, primary_key=True, index=True)
    room_id = Column(Integer, ForeignKey("rooms.id"))
    user_id = Column(Integer, index=True)
    inviter_id = Column(Integer, index=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    expires_at = Column(DateTime(timezone=True))
    is_accepted = Column(Boolean, default=False)
