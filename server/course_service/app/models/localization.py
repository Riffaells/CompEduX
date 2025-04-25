"""
Localization model for multilingual text support
"""
import uuid
from datetime import datetime

from app.models.base import Base
from sqlalchemy import Column, DateTime, String, JSON
from sqlalchemy.dialects.postgresql import UUID


class Localization(Base):
    """
    Model for storing localized strings for different entities
    """
    __tablename__ = "localizations"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)

    # Идентификатор, связанный с типом объекта (курс, roadmap и т.д.)
    # Например: "course_1", "roadmap_2", "global"
    namespace = Column(String(100), nullable=False, index=True)

    # Сохраняем все переводы для данного пространства имен в формате JSON:
    # {
    #   "en": {
    #     "key1": "Text in English",
    #     "key2": "Another text in English"
    #   },
    #   "ru": {
    #     "key1": "Текст на русском",
    #     "key2": "Другой текст на русском"
    #   }
    # }
    translations = Column(JSON, nullable=False)

    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)

    def __repr__(self):
        return f"<Localization id={self.id}, namespace={self.namespace}>"

    def get_text(self, key: str, lang: str, default: str = "") -> str:
        """
        Get localized text for the given key and language

        Args:
            key: Translation key
            lang: Language code
            default: Default text if translation not found

        Returns:
            Localized text or default if not found
        """
        if not self.translations:
            return default

        lang_dict = self.translations.get(lang, {})
        return lang_dict.get(key, default)
