import enum


class UserRole(str, enum.Enum):
    """
    User roles in the system.

    Attributes:
        ADMIN: System administrator with full privileges.
        USER: Regular user with basic privileges.
        MODERATOR: Moderator with extended privileges for content moderation.
    """
    ADMIN = "ADMIN"
    USER = "USER"
    MODERATOR = "MODERATOR"


class OAuthProvider(str, enum.Enum):
    """
    Supported OAuth authentication providers.

    Attributes:
        GOOGLE: Authentication through Google.
        GITHUB: Authentication through GitHub.
        EMAIL: Standard authentication via email and password.
        FACEBOOK: Authentication through Facebook.
        TWITTER: Authentication through Twitter.
        APPLE: Authentication through Apple.
        VK: Authentication through VK.
        YANDEX: Authentication through Yandex.
    """
    GOOGLE = "GOOGLE"
    GITHUB = "GITHUB"
    EMAIL = "EMAIL"
    FACEBOOK = "FACEBOOK"
    TWITTER = "TWITTER"
    APPLE = "APPLE"
    VK = "VK"
    YANDEX = "YANDEX"


class PrivacyLevel(str, enum.Enum):
    """
    Privacy levels for various user data.

    Attributes:
        PUBLIC: Data is visible to all users.
        REGISTERED: Data is visible to registered users.
        FRIENDS: Data is visible only to user's friends.
        PRIVATE: Data is visible only to the user themselves.
    """
    PUBLIC = "PUBLIC"  # Visible to everyone
    REGISTERED = "REGISTERED"  # Visible to registered users
    FRIENDS = "FRIENDS"  # Visible only to friends
    PRIVATE = "PRIVATE"  # Visible only to the user


class BeveragePreference(str, enum.Enum):
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
        ENERGY_DRINK: Energy drink preference.
        NONE: No preference specified.
    """
    COFFEE = "COFFEE"
    TEA = "TEA"
    WATER = "WATER"
    JUICE = "JUICE"
    SODA = "SODA"
    ENERGY_DRINK = "ENERGY_DRINK"
    NONE = "NONE"


class FriendshipStatus(str, enum.Enum):
    """Статусы дружбы между пользователями"""
    PENDING = "PENDING"  # Запрос на дружбу отправлен, ожидает подтверждения
    ACCEPTED = "ACCEPTED"  # Дружба подтверждена
    REJECTED = "REJECTED"  # Запрос на дружбу отклонен
    BLOCKED = "BLOCKED"  # Пользователь заблокирован
