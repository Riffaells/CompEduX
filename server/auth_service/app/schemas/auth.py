from pydantic import BaseModel, EmailStr


class UserLoginSchema(BaseModel):
    """
    Schema for user authentication.

    Used for logging into the system.
    """
    email: EmailStr
    password: str


class TokenSchema(BaseModel):
    """
    Schema for authentication tokens.

    Contains access token and refresh token.
    """
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


class TokenRefreshSchema(BaseModel):
    """
    Schema for refreshing access token.

    Used to obtain a new access token using a refresh token.
    """
    refresh_token: str
