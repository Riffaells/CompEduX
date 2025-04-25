# -*- coding: utf-8 -*-
"""
Test script for course, tag, technology tree, and localization models
"""
import os
import sys

from common.logger import get_logger

# Configuring paths properly
root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if root_dir not in sys.path:
    sys.path.insert(0, root_dir)

from app.db.session import SessionLocal
from app.db.test_data import create_test_courses, reset_database
from app.models.base import Base
from app.models.course import Course, generate_slug
from app.models.tag import Tag
from app.models.localization import Localization
from app.models.technology_tree import TechnologyTree



# Создаем логгер для тестов
logger = get_logger("course_service.tests.models")


def test_slug_generation():
    """Test that slug generation works correctly"""
    # Generate a few slugs and ensure they're unique
    slugs = set()
    for _ in range(100):
        slug = generate_slug()
        assert len(slug) == 8
        assert slug not in slugs
        slugs.add(slug)

    logger.info(f"Generated {len(slugs)} unique slugs")


def test_models():
    """Test that models can be created and queried"""
    db = SessionLocal()
    try:
        # Reset database
        reset_database(db)

        # Create test courses with technology trees
        created_courses = create_test_courses(db, force=True, create_tech_trees=True)

        # Check that courses were created
        courses = db.query(Course).all()
        logger.info(f"Found {len(courses)} courses in database")

        # Check course fields
        for course in courses:
            logger.info(f"Course: {course.title} (Slug: {course.slug})")

        # Check tags
        tags = db.query(Tag).all()
        logger.info(f"Found {len(tags)} tags in database")
        for tag in tags:
            # Получаем имена тегов на разных языках
            en_name = tag.name_en
            ru_name = tag.name_ru
            logger.info(f"Tag {tag.id}: {en_name} / {ru_name}")

        # Check course-tag relationships
        python_course = None
        for course in courses:
            if isinstance(course.title, dict) and course.title.get("en") == "Introduction to Python Programming":
                python_course = course
                break

        if python_course:
            logger.info(f"Python course has {len(python_course.tags)} tags:")
            for tag in python_course.tags:
                logger.info(f"  - {tag.name_en}")

        # Check technology trees
        tech_trees = db.query(TechnologyTree).all()
        logger.info(f"Found {len(tech_trees)} technology trees in database")

        # Check technology tree structure
        for tree in tech_trees:
            logger.info(f"Technology tree for course ID: {tree.course_id}")
            if tree.data:
                logger.info(f"Technology tree metadata: {tree.data.get('metadata', {})}")
                logger.info(f"Technology tree has {len(tree.data.get('nodes', []))} nodes")
                logger.info(f"Technology tree has {len(tree.data.get('connections', []))} connections")

        # Check localizations
        localizations = db.query(Localization).all()
        logger.info(f"Found {len(localizations)} localizations in database")

        for loc in localizations:
            logger.info(f"Localization namespace: {loc.namespace}")
            logger.info(f"Available languages: {', '.join(loc.translations.keys())}")

            # Show example of localized text
            if loc.namespace == "python_tech_tree" and "en" in loc.translations:
                logger.info("Example localizations:")
                for key in ["course.intro", "course.basics", "course.advanced"]:
                    en_text = loc.get_text(key, "en", "Not found")
                    ru_text = loc.get_text(key, "ru", "Not found")
                    logger.info(f"  {key}: EN='{en_text}', RU='{ru_text}'")

    finally:
        db.close()

if __name__ == "__main__":
    logger.info("Testing slug generation...")
    test_slug_generation()

    logger.info("\nTesting models...")
    test_models()

    logger.info("\nAll tests completed successfully!")
