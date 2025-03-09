from sqlalchemy import Boolean, Column, Integer, String, DateTime, ForeignKey, Text
from sqlalchemy.sql import func
from sqlalchemy.orm import relationship
from database import Base

class Achievement(Base):
    __tablename__ = "achievements"

    id = Column(Integer, primary_key=True, index=True)
    title = Column(String, index=True)
    description = Column(Text)
    points = Column(Integer, default=0)
    icon_url = Column(String, nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    # Связь с пользователями, получившими достижение
    users = relationship("UserAchievement", back_populates="achievement")

class UserAchievement(Base):
    __tablename__ = "user_achievements"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, index=True)
    achievement_id = Column(Integer, ForeignKey("achievements.id"))
    received_at = Column(DateTime(timezone=True), server_default=func.now())

    # Связь с достижением
    achievement = relationship("Achievement", back_populates="users")

class AchievementRule(Base):
    __tablename__ = "achievement_rules"

    id = Column(Integer, primary_key=True, index=True)
    achievement_id = Column(Integer, ForeignKey("achievements.id"))
    rule_type = Column(String)  # Тип правила (например, "competitions_won", "challenges_completed")
    threshold = Column(Integer)  # Порог для получения (например, 5 выигранных соревнований)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    # Связь с достижением
    achievement = relationship("Achievement")
