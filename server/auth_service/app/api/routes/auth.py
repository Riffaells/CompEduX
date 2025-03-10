from datetime import timedelta

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from ...core import settings
from ...db.session import get_db
from ...models.user import UserModel
from ...models.auth import RefreshTokenModel
from ...schemas import (
    TokenSchema,
    TokenRefreshSchema,
    UserCreateSchema,
    UserResponseSchema
)
from ...services.auth import (
    authenticate_user,
    create_access_token,
    create_refresh_token,
    create_user,
    get_current_user,
    refresh_access_token,
)

router = APIRouter()


@router.post("/register", response_model=UserResponseSchema, status_code=status.HTTP_201_CREATED)
async def register(user_data: UserCreateSchema, db: Session = Depends(get_db)):
    """
    Register a new user.

    Email and password are required. Username is optional and will be automatically
    generated based on the email if not provided.
    """
    return create_user(db, user_data)


@router.post("/login", response_model=TokenSchema)
async def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    """Authenticate user and get tokens"""
    user = authenticate_user(db, form_data.username, form_data.password)

    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )

    # Create access token
    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={"sub": str(user.id)},
        expires_delta=access_token_expires
    )

    # Create refresh token
    refresh_token = create_refresh_token(user.id, db)

    return {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer"
    }


@router.post("/refresh", response_model=TokenSchema)
async def refresh_token(token_data: TokenRefreshSchema, db: Session = Depends(get_db)):
    """Refresh access token using refresh token"""
    return refresh_access_token(token_data, db)


@router.post("/logout")
async def logout(
    token_data: TokenRefreshSchema,
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Logout (revoke refresh token)"""
    # Find refresh token in database
    db_token = db.query(RefreshTokenModel).filter(
        RefreshTokenModel.token == token_data.refresh_token,
        RefreshTokenModel.user_id == current_user.id,
        RefreshTokenModel.revoked == False
    ).first()

    if db_token:
        # Mark token as revoked
        db_token.revoked = True
        db.commit()

    return {"message": "Successfully logged out"}


@router.get("/me", response_model=UserResponseSchema)
async def read_users_me(current_user: UserModel = Depends(get_current_user)):
    """Get information about current user"""
    return current_user
