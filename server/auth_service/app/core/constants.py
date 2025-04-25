"""
Константы для сервиса аутентификации
"""
from datetime import timedelta

# JWT settings
JWT_ALGORITHM = "HS256"
ACCESS_TOKEN_DEFAULT_EXPIRE = timedelta(minutes=30)

# Test accounts
TEST_MAIN_EMAIL = "test@example.com"
TEST_MAIN_USERNAME = "test"
TEST_DEFAULT_PASSWORD = "test123"
TEST_PLUS_DOMAIN = "@test.com"  # test+XXX@test.com
TEST_PLUS_PREFIX = "test+"

# SQL injection patterns for detection
SQL_ERROR_PATTERNS = ["SQL:", "sqlalchemy", "psycopg2", "asyncpg"]

# Auth provider types
EMAIL_PROVIDER = "email"
GOOGLE_PROVIDER = "google"
GITHUB_PROVIDER = "github"

# User roles
ADMIN_ROLE = "admin"
USER_ROLE = "user"
MODERATOR_ROLE = "moderator"

# Default field values
DEFAULT_BEVERAGE = "coffee"
DEFAULT_THEME = "light"
DEFAULT_FONT_SIZE = "medium"

# Username generation
MIN_USERNAME_LENGTH = 3
MAX_USERNAME_LENGTH = 25
