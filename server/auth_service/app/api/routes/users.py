from typing import List
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status, Path
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

from ..utils import prepare_user_response
from ...db.session import get_db
from ...models.user import UserModel, UserRole
from ...schemas import UserResponseSchema, UserUpdateSchema, UserPublicProfileSchema
from ...services.auth import get_current_user, get_user_by_id, get_user_by_username

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
        db: AsyncSession = Depends(get_db)
):
    """Get a list of users (admin only)"""
    result = await db.execute(select(UserModel).offset(skip).limit(limit))
    users = result.scalars().all()
    return [await prepare_user_response(user) for user in users]


@router.get("/id/{user_id}", response_model=UserResponseSchema)
async def read_user(
        user_id: UUID,
        current_user: UserModel = Depends(get_current_user),
        db: AsyncSession = Depends(get_db)
):
    """Get user information by ID"""
    # Regular users can only get information about themselves
    if current_user.id != user_id and current_user.role != UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )

    user = await get_user_by_id(db, user_id)

    if user is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    return await prepare_user_response(user)


@router.put("/id/{user_id}", response_model=UserResponseSchema)
async def update_user(
        user_id: UUID,
        user_update: UserUpdateSchema,
        current_user: UserModel = Depends(get_current_user),
        db: AsyncSession = Depends(get_db)
):
    """Update user information"""
    # Regular users can only update their own profile
    if current_user.id != user_id and current_user.role != UserRole.ADMIN:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Not enough permissions"
        )

    user = await get_user_by_id(db, user_id)
    if user is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    # Update core user fields
    for field in ["username", "email"]:
        if hasattr(user_update, field) and getattr(user_update, field) is not None:
            setattr(user, field, getattr(user_update, field))

    # Update profile
    if user.profile:
        for field in ["first_name", "last_name", "avatar_url", "bio", "location"]:
            if hasattr(user_update, field) and getattr(user_update, field) is not None:
                setattr(user.profile, field, getattr(user_update, field))

    # Update preferences
    if user.preferences and hasattr(user_update, "beverage_preference") and user_update.beverage_preference is not None:
        user.preferences.beverage_preference = user_update.beverage_preference

    # Update password if provided
    if user_update.password:
        from ...services.auth import get_password_hash
        user.hashed_password = get_password_hash(user_update.password)

    db.add(user)
    await db.commit()
    await db.refresh(user)

    return await prepare_user_response(user)


@router.delete("/id/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_user(
        user_id: UUID,
        current_user: UserModel = Depends(get_current_admin_user),
        db: AsyncSession = Depends(get_db)
):
    """Delete a user (admin only)"""
    user = await get_user_by_id(db, user_id)

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
    await db.commit()

    return None


@router.get("/username/{username}", response_model=UserPublicProfileSchema)
async def get_user_profile(
        username: str = Path(..., min_length=3, max_length=30),
        db: AsyncSession = Depends(get_db),
        current_user: UserModel = Depends(get_current_user)
):
    """
    Get a user's public profile by username.

    Returns the public profile of the user with the specified username.
    Only authenticated users can view profiles.
    """
    user = await get_user_by_username(db, username)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="User not found"
        )

    # TODO: Implement privacy settings check
    # For now, return the public profile for all authenticated users

    return user
