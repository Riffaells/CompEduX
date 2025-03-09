from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from enum import Enum

class ChallengeType(str, Enum):
    QUIZ = "quiz"
    CODING = "coding"
    LOGIC = "logic"

class ChallengeBase(BaseModel):
    competition_id: int
    type: ChallengeType
    title: str
    content: str
    correct_answer: Optional[str] = None
    points: int = 0

class ChallengeCreate(ChallengeBase):
    pass

class Challenge(ChallengeBase):
    id: int
    created_at: datetime

    class Config:
        orm_mode = True

class ChallengeDetail(Challenge):
    submissions_count: int = 0

    class Config:
        orm_mode = True

class CompetitionBase(BaseModel):
    title: str
    description: Optional[str] = None
    rules: str
    deadline: Optional[datetime] = None
    is_active: bool = True

class CompetitionCreate(CompetitionBase):
    pass

class Competition(CompetitionBase):
    id: int
    owner_id: int
    created_at: datetime

    class Config:
        orm_mode = True

class CompetitionDetail(Competition):
    challenges: List[Challenge] = []
    participants_count: int = 0

    class Config:
        orm_mode = True

class CompetitionParticipantBase(BaseModel):
    competition_id: int
    user_id: int

class CompetitionParticipantCreate(CompetitionParticipantBase):
    pass

class CompetitionParticipant(CompetitionParticipantBase):
    id: int
    joined_at: datetime
    total_points: int = 0

    class Config:
        orm_mode = True

class ChallengeSubmissionBase(BaseModel):
    challenge_id: int
    answer: str

class ChallengeSubmissionCreate(ChallengeSubmissionBase):
    pass

class ChallengeSubmission(ChallengeSubmissionBase):
    id: int
    participant_id: int
    is_correct: Optional[bool] = None
    points_earned: int = 0
    submitted_at: datetime

    class Config:
        orm_mode = True

class LeaderboardEntry(BaseModel):
    user_id: int
    username: str
    total_points: int
    position: int

    class Config:
        orm_mode = True
