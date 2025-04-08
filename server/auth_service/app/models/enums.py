from enum import Enum

class UserRole(str, Enum):
    """
    User roles in the system.

    Attributes:
        ADMIN: System administrator with full privileges.
        USER: Regular user with basic privileges.
        MODERATOR: Moderator with extended privileges for content moderation.
    """
    ADMIN = "admin"
    USER = "user"
    MODERATOR = "moderator"


class OAuthProvider(str, Enum):
    """
    Supported OAuth authentication providers.

    Attributes:
        GOOGLE: Authentication through Google.
        GITHUB: Authentication through GitHub.
        EMAIL: Standard authentication via email and password.
    """
    GOOGLE = "google"
    GITHUB = "github"
    EMAIL = "email"


class PrivacyLevel(str, Enum):
    """
    Privacy levels for various user data.

    Attributes:
        PUBLIC: Data is visible to all users.
        FRIENDS: Data is visible only to user's friends.
        PRIVATE: Data is visible only to the user themselves.
    """
    PUBLIC = "public"      # Visible to everyone
    FRIENDS = "friends"    # Visible only to friends
    PRIVATE = "private"    # Visible only to the user


class BeveragePreference(str, Enum):
    """
    User's preferred beverage for breaks.

    This is used to enhance user experience by showing personalized
    messages during break times.

    Attributes:
        COFFEE: Coffee preference.
        TEA: Tea preference.
        WATER: Water preference.
        JUICE: Juice preference.
        SODA: Soda preference.
        NONE: No preference specified.
    """
    COFFEE = "coffee"
    TEA = "tea"
    WATER = "water"
    JUICE = "juice"
    SODA = "soda"
    NONE = "none"
