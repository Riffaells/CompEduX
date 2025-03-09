from sqlalchemy import Boolean, Column, Integer, String, DateTime, ForeignKey, Enum, Text
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
import enum
from database import Base

class ChallengeType(enum.Enum):
    QUIZ = "quiz"
    CODING = "coding"
    LOGIC = "logic"

class Competition(Base):
    __tablename__ = "competitions"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    description = Column(Text, nullable=True)
    rules = Column(Text)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    deadline = Column(DateTime(timezone=True), nullable=True)
    is_active = Column(Boolean, default=True)
    owner_id = Column(Integer, index=True)

    # Связи
    challenges = relationship("Challenge", back_populates="competition")
    participants = relationship("CompetitionParticipant", back_populates="competition")

class Challenge(Base):
    __tablename__ = "challenges"

    id = Column(Integer, primary_key=True, index=True)
    competition_id = Column(Integer, ForeignKey("competitions.id"))
    type = Column(Enum(ChallengeType))
    title = Column(String, index=True)
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
