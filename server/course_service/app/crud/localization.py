"""
CRUD operations for Localization model
"""
import uuid
from typing import Dict, List, Optional, Any, Union, Set

from app.models.localization import Localization
from app.schemas.localization import LocalizationCreate, LocalizationUpdate
from fastapi.encoders import jsonable_encoder
from sqlalchemy.orm import Session

from common.logger import get_logger

# Создаем логгер для этого модуля
logger = get_logger("course_service.crud.localization")


class CRUDLocalization:
    """CRUD operations for Localization model"""

    def get(self, db: Session, id: uuid.UUID) -> Optional[Localization]:
        """
        Get a localization by ID

        Args:
            db: Database session
            id: UUID of the localization

        Returns:
            Localization if found, None otherwise
        """
        return db.query(Localization).filter(Localization.id == id).first()

    def get_by_namespace(self, db: Session, namespace: str) -> Optional[Localization]:
        """
        Get a localization by namespace

        Args:
            db: Database session
            namespace: Namespace of the localization

        Returns:
            Localization if found, None otherwise
        """
        return db.query(Localization).filter(Localization.namespace == namespace).first()

    def get_multi(
            self, db: Session, *, skip: int = 0, limit: int = 100
    ) -> List[Localization]:
        """
        Get multiple localizations with pagination

        Args:
            db: Database session
            skip: Number of records to skip (offset)
            limit: Maximum number of records to return

        Returns:
            List of localizations
        """
        return db.query(Localization).offset(skip).limit(limit).all()

    def create(self, db: Session, *, obj_in: LocalizationCreate) -> Localization:
        """
        Create a new localization

        Args:
            db: Database session
            obj_in: Localization data

        Returns:
            Created Localization object
        """
        # Check if localization with this namespace already exists
        existing = self.get_by_namespace(db, obj_in.namespace)
        if existing:
            # Update existing localization instead of creating a new one
            return self.update(db, db_obj=existing, obj_in=obj_in)

        obj_data = jsonable_encoder(obj_in)
        db_obj = Localization(**obj_data)

        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def update(
            self, db: Session, *, db_obj: Localization, obj_in: Union[LocalizationUpdate, Dict[str, Any]]
    ) -> Localization:
        """
        Update a localization

        Args:
            db: Database session
            db_obj: Localization object to update
            obj_in: Updated localization data

        Returns:
            Updated Localization object
        """
        if isinstance(obj_in, dict):
            update_data = obj_in
        else:
            update_data = obj_in.dict(exclude_unset=True)

        # Handle translations specially to merge instead of replace
        if "translations" in update_data and db_obj.translations:
            if isinstance(update_data["translations"], dict) and isinstance(db_obj.translations, dict):
                # Merge language by language
                for lang, texts in update_data["translations"].items():
                    if lang not in db_obj.translations:
                        db_obj.translations[lang] = {}

                    # Merge text keys
                    if isinstance(texts, dict):
                        db_obj.translations[lang].update(texts)

                # Remove from update_data to prevent overwriting in the loop below
                del update_data["translations"]

        # Update other fields
        for field in update_data:
            setattr(db_obj, field, update_data[field])

        db.add(db_obj)
        db.commit()
        db.refresh(db_obj)
        return db_obj

    def remove(self, db: Session, *, id: uuid.UUID) -> Localization:
        """
        Remove a localization

        Args:
            db: Database session
            id: UUID of the localization to remove

        Returns:
            Removed Localization object
        """
        obj = db.query(Localization).get(id)
        db.delete(obj)
        db.commit()
        return obj

    def get_available_languages(self, db: Session, namespace: str = None) -> Set[str]:
        """
        Get set of available languages in the localization tables

        Args:
            db: Database session
            namespace: Optional namespace to filter by

        Returns:
            Set of language codes
        """
        languages = set()
        query = db.query(Localization)

        if namespace:
            query = query.filter(Localization.namespace == namespace)

        localizations = query.all()

        for loc in localizations:
            if loc.translations:
                languages.update(loc.translations.keys())

        return languages

    def get_translation(
            self, db: Session, namespace: str, key: str, lang: str, default: str = ""
    ) -> str:
        """
        Get a specific translation

        Args:
            db: Database session
            namespace: Namespace of the localization
            key: Translation key
            lang: Language code
            default: Default text if translation not found

        Returns:
            Localized text or default if not found
        """
        localization = self.get_by_namespace(db, namespace)
        if not localization:
            return default

        return localization.get_text(key, lang, default)


localization_crud = CRUDLocalization()
