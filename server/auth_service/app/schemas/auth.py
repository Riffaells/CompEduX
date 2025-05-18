from pydantic import BaseModel, EmailStr


class UserLoginSchema(BaseModel):
    """
    Schema for user authentication.

    Used for logging into the system.
    """
    email: EmailStr
    password: str


class OAuthLoginSchema(BaseModel):
    """
    Schema for OAuth authentication.

    Used for logging into the system via OAuth providers.
    """
    provider: str
    token: str
    provider_user_id: str


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


class TokenPayloadSchema(BaseModel):
    """
    Schema for token payload.

    Contains information stored inside the JWT token.
    """
    sub: str  # user ID
    exp: int  # expiration timestamp


class TokenDataSchema(BaseModel):
    """
    Schema for token data.

    Contains user ID extracted from token.
    """
    user_id: str


class LoginSchema(BaseModel):
    """
    Schema for login request.

    Used for logging into the system.
    """
    username: str  # can be email or username
    password: str


class LoginResponseSchema(BaseModel):
    """
    Schema for login response.

    Contains authentication tokens and basic user information.
    """
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    user_id: str
    username: str


class RegistrationSchema(BaseModel):
    """
    Schema for user registration.

    Contains fields required for creating a new user.
    """
    email: EmailStr
    username: str
    password: str


class RegistrationResponseSchema(BaseModel):
    """
    Schema for registration response.

    Contains authentication tokens and basic user information.
    """
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    user_id: str
    username: str
