"""
CRUD operations for Tags
"""
import logging
from typing import Dict, List, Optional, Tuple, Union, Any
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session, joinedload

from app.models.tag import Tag, TagTranslation
from app.schemas.tag import TagCreate, TagUpdate, TagTranslationCreate

logger = logging.getLogger(__name__)


def get_tag(db: Session, tag_id: int) -> Optional[Tag]:
    """
    Get a tag by its ID with translations

    Args:
        db: Database session
        tag_id: Tag ID

    Returns:
        Tag object if found, None otherwise
    """
    return db.query(Tag).options(joinedload(Tag.translations)).filter(Tag.id == tag_id).first()


def get_tag_by_name(db: Session, name: str, language: str = "en") -> Optional[Tag]:
    """
    Get a tag by its name in a specific language

    Args:
        db: Database session
        name: Tag name
        language: Language code (default: "en")

    Returns:
        Tag object if found, None otherwise
    """
    tag_translation = db.query(TagTranslation).filter(
        TagTranslation.name == name,
        TagTranslation.language == language
    ).first()

    if tag_translation:
        return get_tag(db, tag_translation.tag_id)
    return None


def get_tags(db: Session, skip: int = 0, limit: int = 100) -> List[Tag]:
    """
    Get a list of tags with translations

    Args:
        db: Database session
        skip: Number of tags to skip
        limit: Maximum number of tags to return

    Returns:
        List of Tag objects
    """
    return db.query(Tag).options(joinedload(Tag.translations)).offset(skip).limit(limit).all()


def create_tag(db: Session, tag_in: TagCreate) -> Tag:
    """
    Create a new tag with translations

    Args:
        db: Database session
        tag_in: TagCreate schema

    Returns:
        Created Tag object

    Raises:
        SQLAlchemyError: On database error
    """
    try:
        # Create tag
        db_tag = Tag()
        db.add(db_tag)
        db.flush()  # This gives us the tag ID

        # Create translations
        for translation in tag_in.translations:
            db_translation = TagTranslation(
                tag_id=db_tag.id,
                language=translation.language,
                name=translation.name
            )
            db.add(db_translation)

        db.commit()
        db.refresh(db_tag)
        logger.info(f"Created tag with ID: {db_tag.id}")
        return db_tag

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Error creating tag: {str(e)}")
        raise


def create_tag_with_translations(db: Session, translations: Dict[str, str]) -> Tag:
    """
    Create a new tag with translations specified as a dictionary

    Args:
        db: Database session
        translations: Dictionary of language codes to tag names

    Returns:
        Created Tag object

    Raises:
        ValueError: If no translations are provided
        SQLAlchemyError: On database error
    """
    if not translations:
        raise ValueError("At least one translation must be provided")

    translations_list = [
        TagTranslationCreate(language=lang, name=name)
        for lang, name in translations.items()
    ]

    tag_create = TagCreate(translations=translations_list)
    return create_tag(db, tag_create)


def update_tag(db: Session, tag_id: int, tag_in: TagUpdate) -> Optional[Tag]:
    """
    Update a tag with new translations

    Args:
        db: Database session
        tag_id: ID of the tag to update
        tag_in: TagUpdate schema

    Returns:
        Updated Tag object, or None if tag not found

    Raises:
        SQLAlchemyError: On database error
    """
    try:
        db_tag = get_tag(db, tag_id)
        if db_tag is None:
            return None

        # Update translations if provided
        if tag_in.translations:
            # Remove existing translations
            for translation in db_tag.translations:
                db.delete(translation)
            db.flush()

            # Add new translations
            for translation in tag_in.translations:
                db_translation = TagTranslation(
                    tag_id=db_tag.id,
                    language=translation.language,
                    name=translation.name
                )
                db.add(db_translation)

        db.commit()
        db.refresh(db_tag)
        logger.info(f"Updated tag with ID: {tag_id}")
        return db_tag

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Error updating tag: {str(e)}")
        raise


def delete_tag(db: Session, tag_id: int) -> bool:
    """
    Delete a tag and its translations

    Args:
        db: Database session
        tag_id: ID of the tag to delete

    Returns:
        True if successful, False if tag not found

    Raises:
        SQLAlchemyError: On database error
    """
    try:
        db_tag = db.query(Tag).filter(Tag.id == tag_id).first()
        if db_tag is None:
            return False

        db.delete(db_tag)
        db.commit()
        logger.info(f"Deleted tag with ID: {tag_id}")
        return True

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Error deleting tag: {str(e)}")
        raise


def get_or_create_tag(db: Session, name: str, language: str = "en") -> Tuple[Tag, bool]:
    """
    Get a tag by name in specified language or create it if it doesn't exist

    Args:
        db: Database session
        name: Tag name
        language: Language code (default: "en")

    Returns:
        Tuple of (Tag object, True if created or False if existing)

    Raises:
        SQLAlchemyError: On database error
    """
    try:
        existing_tag = get_tag_by_name(db, name, language)
        if existing_tag:
            return existing_tag, False

        # Create a new tag with translation
        translations = {language: name}
        new_tag = create_tag_with_translations(db, translations)
        return new_tag, True

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Error in get_or_create_tag: {str(e)}")
        raise


def update_tag_translation(
    db: Session, tag_id: int, language: str, name: str
) -> Optional[Tag]:
    """
    Update or add a specific translation for a tag

    Args:
        db: Database session
        tag_id: ID of the tag to update
        language: Language code
        name: New tag name

    Returns:
        Updated Tag object, or None if tag not found

    Raises:
        SQLAlchemyError: On database error
    """
    try:
        db_tag = get_tag(db, tag_id)
        if db_tag is None:
            return None

        # Check if translation already exists
        existing = False
        for translation in db_tag.translations:
            if translation.language == language:
                translation.name = name
                existing = True
                break

        # Add new translation if it doesn't exist
        if not existing:
            db_translation = TagTranslation(
                tag_id=db_tag.id,
                language=language,
                name=name
            )
            db.add(db_translation)

        db.commit()
        db.refresh(db_tag)
        logger.info(f"Updated translation for tag {tag_id}, language: {language}")
        return db_tag

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Error updating tag translation: {str(e)}")
        raise


def remove_tag_translation(
    db: Session, tag_id: int, language: str
) -> Optional[Tag]:
    """
    Remove a specific translation from a tag

    Args:
        db: Database session
        tag_id: ID of the tag
        language: Language code of the translation to remove

    Returns:
        Updated Tag object, or None if tag not found

    Raises:
        ValueError: If trying to remove the last translation
        SQLAlchemyError: On database error
    """
    try:
        db_tag = get_tag(db, tag_id)
        if db_tag is None:
            return None

        # Check how many translations we have
        if len(db_tag.translations) <= 1:
            raise ValueError("Cannot remove the last translation of a tag")

        # Find and remove the translation
        for translation in db_tag.translations:
            if translation.language == language:
                db.delete(translation)
                break

        db.commit()
        db.refresh(db_tag)
        logger.info(f"Removed translation for tag {tag_id}, language: {language}")
        return db_tag

    except SQLAlchemyError as e:
        db.rollback()
        logger.error(f"Error removing tag translation: {str(e)}")
        raise


# Create a module-level singleton for convenience
tag_crud = {
    "get": get_tag,
    "get_by_name": get_tag_by_name,
    "get_all": get_tags,
    "create": create_tag,
    "create_with_translations": create_tag_with_translations,
    "update": update_tag,
    "delete": delete_tag,
    "get_or_create": get_or_create_tag,
    "update_translation": update_tag_translation,
    "remove_translation": remove_tag_translation
}
