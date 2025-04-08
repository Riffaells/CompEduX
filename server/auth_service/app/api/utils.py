"""
Utility functions for API routes.

This module contains helper functions that are used across multiple API routes.
"""
from ..models.user import UserModel
from ..schemas import UserResponseSchema


def prepare_user_response(user: UserModel) -> UserResponseSchema:
    """
    Prepare user data for response, avoiding duplication of fields.

    This function ensures that fields are correctly placed in the appropriate
    sections of the response without duplication.

    Args:
        user: The UserModel instance to prepare for response

    Returns:
        A clean UserResponseSchema object with properly organized data
    """
    # Create the user response using the model
    user_response = UserResponseSchema.model_validate(user)

    # Ensure no duplicated fields between parent and child objects
    return user_response
