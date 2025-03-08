import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.db.base import Base, engine
from app.models.models import User

def reset_database():
    # Удаляем все таблицы
    Base.metadata.drop_all(bind=engine)
    # Создаем таблицы заново
    Base.metadata.create_all(bind=engine)
    print("Database has been reset successfully!")

if __name__ == "__main__":
    reset_database()
