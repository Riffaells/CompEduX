"""
Localization model for multilingual text support
"""
import uuid
from datetime import datetime
from typing import List, Dict, Any

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

    # Optional description for this localization namespace
    description = Column(String(500), nullable=True)

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

    def get_translation_statistics(self) -> List[Dict[str, Any]]:
        """
        Calculate translation statistics for each language

        Returns:
            List of dictionaries with statistics for each language
        """
        if not self.translations:
            return []

        result = []
        all_keys = set()

        # Collect all unique keys across all languages
        for lang_dict in self.translations.values():
            all_keys.update(lang_dict.keys())

        total_keys = len(all_keys)

        # Calculate statistics for each language
        for lang, translations in self.translations.items():
            translated_keys = len(translations)
            completion_percentage = (translated_keys / total_keys * 100) if total_keys > 0 else 0

            result.append({
                "language": lang,
                "total_keys": total_keys,
                "translated_keys": translated_keys,
                "completion_percentage": round(completion_percentage, 2)
            })

        return result
