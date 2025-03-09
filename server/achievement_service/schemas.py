from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime

class AchievementBase(BaseModel):
    title: str
    description: str
    points: int = 0
    icon_url: Optional[str] = None

class AchievementCreate(AchievementBase):
    pass

class Achievement(AchievementBase):
    id: int
    created_at: datetime

    class Config:
        orm_mode = True

class AchievementDetail(Achievement):
    users_count: int = 0

    class Config:
        orm_mode = True

class UserAchievementBase(BaseModel):
    user_id: int
    achievement_id: int

class UserAchievementCreate(UserAchievementBase):
    pass

class UserAchievement(UserAchievementBase):
    id: int
    received_at: datetime
    achievement: Achievement

    class Config:
        orm_mode = True

class AchievementRuleBase(BaseModel):
    achievement_id: int
    rule_type: str
    threshold: int

class AchievementRuleCreate(AchievementRuleBase):
    pass

class AchievementRule(AchievementRuleBase):
    id: int
    created_at: datetime

    class Config:
        orm_mode = True

class UserAchievementProgress(BaseModel):
    user_id: int
    achievement_id: int
    current_progress: int
    threshold: int
    completed: bool

    class Config:
        orm_mode = True
