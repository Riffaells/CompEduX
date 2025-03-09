from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
import httpx
import os

from database import get_db, engine
import models
import schemas

# Create tables
models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="Achievement Service")

# Environment variables
AUTH_SERVICE_URL = os.getenv("AUTH_SERVICE_URL", "http://auth_service:8000")

# Helper function to verify token with auth service
async def verify_token(token: str):
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(
                f"{AUTH_SERVICE_URL}/users/me",
                headers={"Authorization": f"Bearer {token}"}
            )
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            print(f"Error verifying token: {e}")
            return None

# Achievements endpoints
@app.get("/achievements/", response_model=List[schemas.Achievement])
def get_achievements(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    achievements = db.query(models.Achievement).offset(skip).limit(limit).all()
    return achievements

@app.get("/achievements/{achievement_id}", response_model=schemas.AchievementDetail)
def get_achievement(achievement_id: int, db: Session = Depends(get_db)):
    achievement = db.query(models.Achievement).filter(models.Achievement.id == achievement_id).first()
    if achievement is None:
        raise HTTPException(status_code=404, detail="Achievement not found")

    # Count users who have this achievement
    users_count = db.query(models.UserAchievement).filter(
        models.UserAchievement.achievement_id == achievement_id
    ).count()

    achievement_detail = schemas.AchievementDetail.from_orm(achievement)
    achievement_detail.users_count = users_count

    return achievement_detail

@app.post("/achievements/", response_model=schemas.Achievement, status_code=status.HTTP_201_CREATED)
async def create_achievement(
    achievement: schemas.AchievementCreate,
    db: Session = Depends(get_db),
    authorization: Optional[str] = None
):
    if authorization is None:
        raise HTTPException(status_code=401, detail="Not authenticated")

    token = authorization.split("Bearer ")[1]
    user = await verify_token(token)

    if user is None or user.get("role") != "admin":
        raise HTTPException(status_code=403, detail="Not authorized to create achievements")

    db_achievement = models.Achievement(**achievement.dict())
    db.add(db_achievement)
    db.commit()
    db.refresh(db_achievement)
    return db_achievement

# User achievements endpoints
@app.get("/users/{user_id}/achievements", response_model=List[schemas.UserAchievement])
def get_user_achievements(user_id: int, db: Session = Depends(get_db)):
    user_achievements = db.query(models.UserAchievement).filter(
        models.UserAchievement.user_id == user_id
    ).all()
    return user_achievements

@app.post("/users/{user_id}/achievements", response_model=schemas.UserAchievement)
async def award_achievement(
    user_id: int,
    achievement_data: schemas.UserAchievementCreate,
    db: Session = Depends(get_db),
    authorization: Optional[str] = None
):
    if authorization is None:
        raise HTTPException(status_code=401, detail="Not authenticated")

    token = authorization.split("Bearer ")[1]
    user = await verify_token(token)

    if user is None or (user.get("role") != "admin" and user.get("id") != user_id):
        raise HTTPException(status_code=403, detail="Not authorized to award achievements")

    # Check if achievement exists
    achievement = db.query(models.Achievement).filter(
        models.Achievement.id == achievement_data.achievement_id
    ).first()

    if achievement is None:
        raise HTTPException(status_code=404, detail="Achievement not found")

    # Check if user already has this achievement
    existing = db.query(models.UserAchievement).filter(
        models.UserAchievement.user_id == user_id,
        models.UserAchievement.achievement_id == achievement_data.achievement_id
    ).first()

    if existing:
        raise HTTPException(status_code=400, detail="User already has this achievement")

    # Award achievement
    user_achievement = models.UserAchievement(
        user_id=user_id,
        achievement_id=achievement_data.achievement_id
    )

    db.add(user_achievement)
    db.commit()
    db.refresh(user_achievement)

    return user_achievement

# Achievement rules endpoints
@app.post("/achievement-rules/", response_model=schemas.AchievementRule)
async def create_achievement_rule(
    rule: schemas.AchievementRuleCreate,
    db: Session = Depends(get_db),
    authorization: Optional[str] = None
):
    if authorization is None:
        raise HTTPException(status_code=401, detail="Not authenticated")

    token = authorization.split("Bearer ")[1]
    user = await verify_token(token)

    if user is None or user.get("role") != "admin":
        raise HTTPException(status_code=403, detail="Not authorized to create achievement rules")

    # Check if achievement exists
    achievement = db.query(models.Achievement).filter(
        models.Achievement.id == rule.achievement_id
    ).first()

    if achievement is None:
        raise HTTPException(status_code=404, detail="Achievement not found")

    db_rule = models.AchievementRule(**rule.dict())
    db.add(db_rule)
    db.commit()
    db.refresh(db_rule)

    return db_rule

@app.get("/users/{user_id}/progress", response_model=List[schemas.UserAchievementProgress])
async def get_user_achievement_progress(
    user_id: int,
    db: Session = Depends(get_db),
    authorization: Optional[str] = None
):
    if authorization is None:
        raise HTTPException(status_code=401, detail="Not authenticated")

    token = authorization.split("Bearer ")[1]
    user = await verify_token(token)

    if user is None or (user.get("id") != user_id and user.get("role") != "admin"):
        raise HTTPException(status_code=403, detail="Not authorized to view this user's progress")

    # Get all achievement rules
    rules = db.query(models.AchievementRule).all()

    # Get user's achievements
    user_achievements = db.query(models.UserAchievement).filter(
        models.UserAchievement.user_id == user_id
    ).all()

    completed_achievement_ids = [ua.achievement_id for ua in user_achievements]

    # TODO: Implement actual progress tracking logic
    # This would involve querying other services to get user stats
    # For now, we'll return dummy progress

    progress_list = []
    for rule in rules:
        # Skip rules for achievements the user already has
        if rule.achievement_id in completed_achievement_ids:
            continue

        # Dummy progress calculation - in a real app, you'd query other services
        current_progress = 0  # This would be calculated based on user activity

        progress = schemas.UserAchievementProgress(
            user_id=user_id,
            achievement_id=rule.achievement_id,
            current_progress=current_progress,
            threshold=rule.threshold,
            completed=False
        )

        progress_list.append(progress)

    return progress_list

# Health check endpoint
@app.get("/health")
def health_check():
    return {"status": "ok"}
