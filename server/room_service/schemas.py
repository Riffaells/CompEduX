from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime
from enum import Enum

class RoomType(str, Enum):
    PUBLIC = "public"
    PRIVATE = "private"
    DERIVED = "derived"

class ChallengeType(str, Enum):
    QUIZ = "quiz"
    CODING = "coding"
    LOGIC = "logic"

class RoomBase(BaseModel):
    name: str
    description: str
    type: RoomType = RoomType.PUBLIC
    parent_id: Optional[int] = None
    competition_id: Optional[int] = None

class RoomCreate(RoomBase):
    pass

class Room(RoomBase):
    id: int
    owner_id: int
    created_at: datetime
    updated_at: Optional[datetime] = None

    class Config:
        orm_mode = True

class RoomDetail(Room):
    members: List["RoomMember"] = []
    competition: Optional["Competition"] = None

    class Config:
        orm_mode = True

class RoomMemberBase(BaseModel):
    room_id: int
    user_id: int
    role: str = "member"

class RoomMemberCreate(RoomMemberBase):
    pass

class RoomMember(RoomMemberBase):
    id: int
    joined_at: datetime

    class Config:
        orm_mode = True

class InvitationBase(BaseModel):
    room_id: int
    user_id: int

class InvitationCreate(InvitationBase):
    expires_at: Optional[datetime] = None

class Invitation(InvitationBase):
    id: int
    inviter_id: int
    created_at: datetime
    expires_at: Optional[datetime] = None
    is_accepted: bool

    class Config:
        orm_mode = True

class ChallengeBase(BaseModel):
    competition_id: int
    type: ChallengeType
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
    submissions: List["ChallengeSubmission"] = []

    class Config:
        orm_mode = True

class CompetitionBase(BaseModel):
    title: str
    description: Optional[str] = None
    rules: str
    deadline: Optional[datetime] = None

class CompetitionCreate(CompetitionBase):
    pass

class Competition(CompetitionBase):
    id: int
    created_at: datetime

    class Config:
        orm_mode = True

class CompetitionDetail(Competition):
    challenges: List[Challenge] = []
    participants: List["CompetitionParticipant"] = []

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
    participant_id: int
    answer: str

class ChallengeSubmissionCreate(ChallengeSubmissionBase):
    pass

class ChallengeSubmission(ChallengeSubmissionBase):
    id: int
    is_correct: Optional[bool] = None
    points_earned: int = 0
    submitted_at: datetime

    class Config:
        orm_mode = True

# Update forward references
RoomDetail.update_forward_refs()
ChallengeDetail.update_forward_refs()
CompetitionDetail.update_forward_refs()
