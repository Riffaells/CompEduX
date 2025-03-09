from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
import httpx
import os
import sys
import asyncio

# Добавляем путь к общим модулям
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

from database import get_db, engine
import models
import schemas
from common.auth import get_current_user, get_current_user_id, check_admin_role, check_moderator_role

# Create tables
models.Base.metadata.create_all(bind=engine)

app = FastAPI(title="Competition Service")

# Environment variables
AUTH_SERVICE_URL = os.getenv("AUTH_SERVICE_URL", "http://auth_service:8000")

# Helper function to get user info
async def get_user_info(user_id: int):
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(f"{AUTH_SERVICE_URL}/users/{user_id}")
            if response.status_code == 200:
                return response.json()
            return None
        except Exception as e:
            print(f"Error getting user info: {e}")
            return None

# Competitions endpoints
@app.get("/competitions/", response_model=List[schemas.Competition])
async def get_competitions(
    skip: int = 0,
    limit: int = 100,
    active_only: bool = False,
    db: Session = Depends(get_db)
):
    query = db.query(models.Competition)

    if active_only:
        query = query.filter(models.Competition.is_active == True)

    competitions = query.offset(skip).limit(limit).all()
    return competitions

@app.get("/competitions/{competition_id}", response_model=schemas.CompetitionDetail)
async def get_competition(competition_id: int, db: Session = Depends(get_db)):
    competition = db.query(models.Competition).filter(models.Competition.id == competition_id).first()

    if competition is None:
        raise HTTPException(status_code=404, detail="Competition not found")

    # Count participants
    participants_count = db.query(models.CompetitionParticipant).filter(
        models.CompetitionParticipant.competition_id == competition_id
    ).count()

    # Get challenges
    challenges = db.query(models.Challenge).filter(
        models.Challenge.competition_id == competition_id
    ).all()

    competition_detail = schemas.CompetitionDetail.from_orm(competition)
    competition_detail.participants_count = participants_count
    competition_detail.challenges = challenges

    return competition_detail

@app.post("/competitions/", response_model=schemas.Competition, status_code=status.HTTP_201_CREATED)
async def create_competition(
    competition: schemas.CompetitionCreate,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    db_competition = models.Competition(
        **competition.dict(),
        owner_id=user_id
    )

    db.add(db_competition)
    db.commit()
    db.refresh(db_competition)

    return db_competition

@app.put("/competitions/{competition_id}", response_model=schemas.Competition)
async def update_competition(
    competition_id: int,
    competition: schemas.CompetitionCreate,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    db_competition = db.query(models.Competition).filter(models.Competition.id == competition_id).first()

    if db_competition is None:
        raise HTTPException(status_code=404, detail="Competition not found")

    # Check if user is owner or admin/moderator
    user_info = await get_current_user(token=None)
    if db_competition.owner_id != user_id and user_info.get("role") not in ["admin", "moderator"]:
        raise HTTPException(status_code=403, detail="Not authorized to update this competition")

    # Update fields
    for key, value in competition.dict().items():
        setattr(db_competition, key, value)

    db.commit()
    db.refresh(db_competition)

    return db_competition

@app.delete("/competitions/{competition_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_competition(
    competition_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    db_competition = db.query(models.Competition).filter(models.Competition.id == competition_id).first()

    if db_competition is None:
        raise HTTPException(status_code=404, detail="Competition not found")

    # Check if user is owner or admin
    user_info = await get_current_user(token=None)
    if db_competition.owner_id != user_id and user_info.get("role") != "admin":
        raise HTTPException(status_code=403, detail="Not authorized to delete this competition")

    # Delete competition
    db.delete(db_competition)
    db.commit()

    return None

# Challenges endpoints
@app.get("/challenges/", response_model=List[schemas.Challenge])
async def get_challenges(
    competition_id: Optional[int] = None,
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    query = db.query(models.Challenge)

    if competition_id:
        query = query.filter(models.Challenge.competition_id == competition_id)

    challenges = query.offset(skip).limit(limit).all()
    return challenges

@app.get("/challenges/{challenge_id}", response_model=schemas.ChallengeDetail)
async def get_challenge(challenge_id: int, db: Session = Depends(get_db)):
    challenge = db.query(models.Challenge).filter(models.Challenge.id == challenge_id).first()

    if challenge is None:
        raise HTTPException(status_code=404, detail="Challenge not found")

    # Count submissions
    submissions_count = db.query(models.ChallengeSubmission).filter(
        models.ChallengeSubmission.challenge_id == challenge_id
    ).count()

    challenge_detail = schemas.ChallengeDetail.from_orm(challenge)
    challenge_detail.submissions_count = submissions_count

    return challenge_detail

@app.post("/challenges/", response_model=schemas.Challenge, status_code=status.HTTP_201_CREATED)
async def create_challenge(
    challenge: schemas.ChallengeCreate,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    # Check if competition exists
    competition = db.query(models.Competition).filter(models.Competition.id == challenge.competition_id).first()

    if competition is None:
        raise HTTPException(status_code=404, detail="Competition not found")

    # Check if user is owner or admin/moderator
    user_info = await get_current_user(token=None)
    if competition.owner_id != user_id and user_info.get("role") not in ["admin", "moderator"]:
        raise HTTPException(status_code=403, detail="Not authorized to add challenges to this competition")

    db_challenge = models.Challenge(**challenge.dict())

    db.add(db_challenge)
    db.commit()
    db.refresh(db_challenge)

    return db_challenge

# Participants endpoints
@app.post("/competitions/{competition_id}/join", response_model=schemas.CompetitionParticipant)
async def join_competition(
    competition_id: int,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    # Check if competition exists
    competition = db.query(models.Competition).filter(models.Competition.id == competition_id).first()

    if competition is None:
        raise HTTPException(status_code=404, detail="Competition not found")

    # Check if user is already a participant
    existing_participant = db.query(models.CompetitionParticipant).filter(
        models.CompetitionParticipant.competition_id == competition_id,
        models.CompetitionParticipant.user_id == user_id
    ).first()

    if existing_participant:
        raise HTTPException(status_code=400, detail="Already joined this competition")

    # Create participant
    participant = models.CompetitionParticipant(
        competition_id=competition_id,
        user_id=user_id
    )

    db.add(participant)
    db.commit()
    db.refresh(participant)

    return participant

@app.get("/competitions/{competition_id}/participants", response_model=List[schemas.CompetitionParticipant])
async def get_competition_participants(
    competition_id: int,
    skip: int = 0,
    limit: int = 100,
    db: Session = Depends(get_db)
):
    # Check if competition exists
    competition = db.query(models.Competition).filter(models.Competition.id == competition_id).first()

    if competition is None:
        raise HTTPException(status_code=404, detail="Competition not found")

    participants = db.query(models.CompetitionParticipant).filter(
        models.CompetitionParticipant.competition_id == competition_id
    ).offset(skip).limit(limit).all()

    return participants

# Submissions endpoints
@app.post("/challenges/{challenge_id}/submit", response_model=schemas.ChallengeSubmission)
async def submit_challenge(
    challenge_id: int,
    submission: schemas.ChallengeSubmissionCreate,
    user_id: int = Depends(get_current_user_id),
    db: Session = Depends(get_db)
):
    # Check if challenge exists
    challenge = db.query(models.Challenge).filter(models.Challenge.id == challenge_id).first()

    if challenge is None:
        raise HTTPException(status_code=404, detail="Challenge not found")

    # Check if user is a participant in the competition
    participant = db.query(models.CompetitionParticipant).filter(
        models.CompetitionParticipant.competition_id == challenge.competition_id,
        models.CompetitionParticipant.user_id == user_id
    ).first()

    if participant is None:
        raise HTTPException(status_code=403, detail="You must join the competition first")

    # Check if user already submitted an answer for this challenge
    existing_submission = db.query(models.ChallengeSubmission).filter(
        models.ChallengeSubmission.challenge_id == challenge_id,
        models.ChallengeSubmission.participant_id == participant.id
    ).first()

    if existing_submission:
        raise HTTPException(status_code=400, detail="You have already submitted an answer for this challenge")

    # Create submission
    db_submission = models.ChallengeSubmission(
        challenge_id=challenge_id,
        participant_id=participant.id,
        answer=submission.answer
    )

    # Check if answer is correct
    if challenge.correct_answer:
        is_correct = challenge.correct_answer.strip().lower() == submission.answer.strip().lower()
        db_submission.is_correct = is_correct

        if is_correct:
            db_submission.points_earned = challenge.points

            # Update participant's total points
            participant.total_points += challenge.points
            db.add(participant)

    db.add(db_submission)
    db.commit()
    db.refresh(db_submission)

    return db_submission

@app.get("/competitions/{competition_id}/leaderboard", response_model=List[schemas.LeaderboardEntry])
async def get_competition_leaderboard(
    competition_id: int,
    db: Session = Depends(get_db)
):
    # Check if competition exists
    competition = db.query(models.Competition).filter(models.Competition.id == competition_id).first()

    if competition is None:
        raise HTTPException(status_code=404, detail="Competition not found")

    # Get participants ordered by total points
    participants = db.query(models.CompetitionParticipant).filter(
        models.CompetitionParticipant.competition_id == competition_id
    ).order_by(models.CompetitionParticipant.total_points.desc()).all()

    # Get user info for each participant
    leaderboard = []
    user_tasks = []

    for i, participant in enumerate(participants):
        user_tasks.append(get_user_info(participant.user_id))

    user_infos = await asyncio.gather(*user_tasks)

    for i, (participant, user_info) in enumerate(zip(participants, user_infos)):
        username = user_info.get("username", f"User {participant.user_id}") if user_info else f"User {participant.user_id}"

        leaderboard.append(schemas.LeaderboardEntry(
            user_id=participant.user_id,
            username=username,
            total_points=participant.total_points,
            position=i + 1
        ))

    return leaderboard

# Health check endpoint
@app.get("/health")
def health_check():
    return {"status": "ok"}
