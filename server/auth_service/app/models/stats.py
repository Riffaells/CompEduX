import uuid
from datetime import datetime

from sqlalchemy import Column, String, DateTime, Integer, BigInteger, ForeignKey
from sqlalchemy.dialects.postgresql import UUID

from .base import Base


class ClientStatModel(Base):
    """
    Модель для хранения статистики использования клиентского приложения
    """
    __tablename__ = "client_stats"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(UUID(as_uuid=True), ForeignKey("users.id"), nullable=True)

    # Информация о клиенте
    app_name = Column(String, nullable=True)
    app_version = Column(String, nullable=True)
    client_platform = Column(String, nullable=True)
    client_build = Column(BigInteger, nullable=True)
    client_version = Column(String, nullable=True)
    user_agent = Column(String, nullable=True)

    # Информация о системе пользователя
    os_name = Column(String, nullable=True)
    os_version = Column(String, nullable=True)
    java_version = Column(String, nullable=True)

    # Информация о запросе
    request_path = Column(String, nullable=True)
    request_method = Column(String, nullable=True)
    request_ip = Column(String, nullable=True)

    # Временные метки
    created_at = Column(DateTime(timezone=True), default=lambda: datetime.now())
    last_seen_at = Column(DateTime(timezone=True), default=lambda: datetime.now(),
                          onupdate=lambda: datetime.now())

    # Счетчик активности
    request_count = Column(Integer, default=1)

    def update_last_seen(self):
        """Обновляет время последнего запроса и увеличивает счетчик запросов"""
        self.last_seen_at = datetime.now()
        self.request_count += 1
