from typing import List
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status, Path
from sqlalchemy.orm import Session

from ...db.session import get_db
from ...models.user import UserModel, UserRole
from ...schemas import UserResponseSchema, UserUpdateSchema, UserPublicProfileSchema, UserCreateSchema
from ...services.auth import get_current_user, get_user_by_id, get_user_by_username, create_user
from ..utils import prepare_user_response

router = APIRouter()


def get_current_admin_user(current_user: UserModel = Depends(get_current_user)) -> UserModel:
    """Check if the current user is an administrator"""
    if current_user.role != UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )
    return current_user


@router.get("/", response_model=List[UserResponseSchema])
async def read_users(
    skip: int = 0,
    limit: int = 100,
    current_user: UserModel = Depends(get_current_admin_user),
    db: Session = Depends(get_db)
):
    """Get a list of users (admin only)"""
    users = db.query(UserModel).offset(skip).limit(limit).all()
    return [prepare_user_response(user) for user in users]


@router.get("/id/{user_id}", response_model=UserResponseSchema)
async def read_user(
    user_id: UUID,
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Get user information by ID"""
    # Regular users can only get information about themselves
    if current_user.id != user_id and current_user.role != UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )

    user = get_user_by_id(db, user_id)

    if user is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    return prepare_user_response(user)


@router.patch("/id/{user_id}", response_model=UserResponseSchema)
async def update_user(
    user_id: UUID,
    user_data: UserUpdateSchema,
    current_user: UserModel = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """Update user information"""
    # Regular users can only update their own information
    # and cannot change their role
    if current_user.id != user_id and current_user.role != UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )

    if current_user.role != UserRole.ADMIN and user_data.role is not None:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions to change role"
        )

    user = get_user_by_id(db, user_id)

    if user is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    # Update user fields
    user_data_dict = user_data.model_dump(exclude_unset=True)

    for key, value in user_data_dict.items():
        if key == "password" and value:
            from ...services.auth import get_password_hash
            setattr(user, "hashed_password", get_password_hash(value))
        elif hasattr(user, key):
            setattr(user, key, value)

    db.commit()
    db.refresh(user)

    return prepare_user_response(user)


@router.delete("/id/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_user(
    user_id: UUID,
    current_user: UserModel = Depends(get_current_admin_user),
    db: Session = Depends(get_db)
):
    """Delete a user (admin only)"""
    user = get_user_by_id(db, user_id)

    if user is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    # Prevent admin from deleting themselves
    if user.id == current_user.id:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Cannot delete yourself"
        )

    db.delete(user)
    db.commit()

    return None


@router.get("/username/{username}", response_model=UserPublicProfileSchema)
async def get_user_profile(
    username: str = Path(..., min_length=3, max_length=30),
    db: Session = Depends(get_db),
    current_user: UserModel = Depends(get_current_user)
):
    """
    Get a user's public profile by username.

    Returns the public profile of the user with the specified username.
    Only authenticated users can view profiles.
    """
    user = get_user_by_username(db, username)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    # TODO: Implement privacy settings check
    # For now, return the public profile for all authenticated users

    return user
