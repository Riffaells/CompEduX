from sqlalchemy import Boolean, Column, Integer, String, DateTime, ForeignKey, Enum, Text, Table
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
import enum
from database import Base

class RoomType(enum.Enum):
    PUBLIC = "public"
    PRIVATE = "private"
    DERIVED = "derived"  # Changed from INHERITED to match user's spec

class ChallengeType(enum.Enum):
    QUIZ = "quiz"
    CODING = "coding"
    LOGIC = "logic"

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

    # ID соревнования (если есть)
    competition_id = Column(Integer, ForeignKey("competitions.id"), nullable=True)
    competition = relationship("Competition", back_populates="room")

    # Связь с другими комнатами (для наследуемых)
    children = relationship("Room", backref="parent", remote_side=[id])

    # Связь с участниками
    members = relationship("RoomMember", back_populates="room")
    invitations = relationship("Invitation", back_populates="room")

class RoomMember(Base):
    __tablename__ = "room_members"

    id = Column(Integer, primary_key=True, index=True)
    room_id = Column(Integer, ForeignKey("rooms.id"))
    user_id = Column(Integer, index=True)
    joined_at = Column(DateTime(timezone=True), server_default=func.now())

    # Роль в комнате (админ, модератор, участник)
    role = Column(String, default="member")

    room = relationship("Room", back_populates="members")

class Invitation(Base):
    __tablename__ = "invitations"

    id = Column(Integer, primary_key=True, index=True)
    room_id = Column(Integer, ForeignKey("rooms.id"))
    user_id = Column(Integer, index=True)
    inviter_id = Column(Integer, index=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    expires_at = Column(DateTime(timezone=True))
    is_accepted = Column(Boolean, default=False)

    room = relationship("Room", back_populates="invitations")

class Competition(Base):
    __tablename__ = "competitions"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    description = Column(Text, nullable=True)
    rules = Column(Text)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    deadline = Column(DateTime(timezone=True), nullable=True)

    # Связи
    room = relationship("Room", back_populates="competition", uselist=False)
    challenges = relationship("Challenge", back_populates="competition")
    participants = relationship("CompetitionParticipant", back_populates="competition")

class Challenge(Base):
    __tablename__ = "challenges"

    id = Column(Integer, primary_key=True, index=True)
    competition_id = Column(Integer, ForeignKey("competitions.id"))
    type = Column(Enum(ChallengeType))
    content = Column(Text)
    correct_answer = Column(String, nullable=True)
    points = Column(Integer, default=0)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    competition = relationship("Competition", back_populates="challenges")
    submissions = relationship("ChallengeSubmission", back_populates="challenge")

class CompetitionParticipant(Base):
    __tablename__ = "competition_participants"

    id = Column(Integer, primary_key=True, index=True)
    competition_id = Column(Integer, ForeignKey("competitions.id"))
    user_id = Column(Integer, index=True)
    joined_at = Column(DateTime(timezone=True), server_default=func.now())
    total_points = Column(Integer, default=0)

    competition = relationship("Competition", back_populates="participants")
    submissions = relationship("ChallengeSubmission", back_populates="participant")

class ChallengeSubmission(Base):
    __tablename__ = "challenge_submissions"

    id = Column(Integer, primary_key=True, index=True)
    challenge_id = Column(Integer, ForeignKey("challenges.id"))
    participant_id = Column(Integer, ForeignKey("competition_participants.id"))
    answer = Column(Text)
    is_correct = Column(Boolean, nullable=True)
    points_earned = Column(Integer, default=0)
    submitted_at = Column(DateTime(timezone=True), server_default=func.now())

    challenge = relationship("Challenge", back_populates="submissions")
    participant = relationship("CompetitionParticipant", back_populates="submissions")
