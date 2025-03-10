from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.core.config import settings
from app.models.user import Base

# Создаем движок SQLAlchemy
engine = create_engine(
    settings.SQLALCHEMY_DATABASE_URI,
    pool_pre_ping=True,  # Проверка соединения перед использованием
)

# Создаем фабрику сессий
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


# Функция-зависимость для получения сессии БД
def get_db():
    """
    Функция-зависимость для получения сессии базы данных.

    Yields:
        Session: Сессия SQLAlchemy для работы с базой данных.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
