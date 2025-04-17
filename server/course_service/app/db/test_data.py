# -*- coding: utf-8 -*-
"""
Test data initialization module for the course service.
This module provides functions to create and manage test courses data for development and testing.
"""
import logging
import secrets
import string
import uuid
from typing import Dict, List, Optional, Tuple, Union

from sqlalchemy.orm import Session

from app.crud.course import course_crud
from app.crud.localization import localization_crud
from app.crud.technology_tree import technology_tree_crud
from app.db.session import SessionLocal
from app.models.base import Base
from app.models.course import Course
from app.models.localization import Localization
from app.models.technology_tree import TechnologyTree
from app.models.tag import Tag
from app.schemas.course import CourseCreate
from app.schemas.localization import LocalizationCreate
from app.schemas.technology_tree import TechnologyTreeCreate
from app.crud.tag import get_tag_by_name, create_tag_with_translations

logger = logging.getLogger(__name__)

# Тестовые теги с поддержкой мультиязычности
TEST_TAGS = [
    {"name_en": "programming", "name_ru": "программирование"},
    {"name_en": "python", "name_ru": "питон"},
    {"name_en": "beginner", "name_ru": "начинающий"},
    {"name_en": "web", "name_ru": "веб"},
    {"name_en": "javascript", "name_ru": "джаваскрипт"},
    {"name_en": "react", "name_ru": "реакт"},
    {"name_en": "frontend", "name_ru": "фронтенд"},
    {"name_en": "data science", "name_ru": "наука о данных"},
    {"name_en": "statistics", "name_ru": "статистика"},
    {"name_en": "machine learning", "name_ru": "машинное обучение"},
    {"name_en": "mobile", "name_ru": "мобильная разработка"},
    {"name_en": "flutter", "name_ru": "флаттер"},
    {"name_en": "dart", "name_ru": "дарт"},
    {"name_en": "cross-platform", "name_ru": "кросс-платформенный"},
    {"name_en": "devops", "name_ru": "девопс"},
    {"name_en": "CI/CD", "name_ru": "CI/CD"},
    {"name_en": "docker", "name_ru": "докер"},
    {"name_en": "kubernetes", "name_ru": "кубернетес"},
    {"name_en": "cloud", "name_ru": "облако"},
    {"name_en": "aws", "name_ru": "aws"},
    {"name_en": "serverless", "name_ru": "бессерверный"},
    {"name_en": "infrastructure", "name_ru": "инфраструктура"},
    {"name_en": "security", "name_ru": "безопасность"},
    {"name_en": "network", "name_ru": "сеть"},
    {"name_en": "encryption", "name_ru": "шифрование"},
    {"name_en": "ethical hacking", "name_ru": "этический хакинг"},
]

# Тестовые локализации
TEST_LOCALIZATIONS = {
    "global": {
        "en": {
            "app.title": "CompEduX Learning Platform",
            "app.description": "A comprehensive platform for learning programming and IT skills",
            "app.welcome": "Welcome to CompEduX!",
            "nav.home": "Home",
            "nav.courses": "Courses",
            "nav.profile": "Profile",
            "nav.settings": "Settings",
            "footer.copyright": "© 2025 CompEduX. All rights reserved.",
            "footer.terms": "Terms of Service",
            "footer.privacy": "Privacy Policy"
        },
        "ru": {
            "app.title": "Учебная платформа CompEduX",
            "app.description": "Комплексная платформа для изучения программирования и IT-навыков",
            "app.welcome": "Добро пожаловать в CompEduX!",
            "nav.home": "Главная",
            "nav.courses": "Курсы",
            "nav.profile": "Профиль",
            "nav.settings": "Настройки",
            "footer.copyright": "© 2025 CompEduX. Все права защищены.",
            "footer.terms": "Условия использования",
            "footer.privacy": "Политика конфиденциальности"
        }
    },
    "python_tech_tree": {
        "en": {
            "course.intro": "Introduction to Python",
            "course.intro.desc": "Getting started with Python basics",
            "course.basics": "Python Fundamentals",
            "course.basics.desc": "Learn core Python concepts and syntax",
            "course.advanced": "Advanced Python",
            "course.advanced.desc": "Master advanced Python features and patterns"
        },
        "ru": {
            "course.intro": "Введение в Python",
            "course.intro.desc": "Начало работы с основами Python",
            "course.basics": "Основы Python",
            "course.basics.desc": "Изучите основные концепции и синтаксис Python",
            "course.advanced": "Продвинутый Python",
            "course.advanced.desc": "Освойте продвинутые функции и шаблоны Python"
        }
    }
}

# Тестовые курсы
TEST_COURSES = [
    {
        "title": "Introduction to Python Programming",
        "description": "A comprehensive introduction to Python programming language.",
        "instructor_id": "00000000-0000-0000-0000-000000000001",
        "tags": ["programming", "python", "beginner"]
    },
    {
        "title": "Web Development with JavaScript",
        "description": "Master modern JavaScript frameworks and libraries to build interactive web applications.",
        "instructor_id": "00000000-0000-0000-0000-000000000002",
        "tags": ["web", "javascript", "react", "frontend"]
    },
    {
        "title": "Data Science Fundamentals",
        "description": "Learn the essential skills needed for data analysis and visualization.",
        "instructor_id": "00000000-0000-0000-0000-000000000001",
        "tags": ["data science", "statistics", "python", "machine learning"]
    },
    {
        "title": "Mobile App Development with Flutter",
        "description": "Build cross-platform mobile applications with Google's Flutter framework.",
        "instructor_id": "00000000-0000-0000-0000-000000000003",
        "tags": ["mobile", "flutter", "dart", "cross-platform"]
    },
    {
        "title": "DevOps and CI/CD Pipelines",
        "description": "Master the principles and practices of DevOps, including continuous integration and continuous deployment.",
        "instructor_id": "00000000-0000-0000-0000-000000000002",
        "tags": ["devops", "CI/CD", "docker", "kubernetes"]
    },
    {
        "title": "Cloud Computing with AWS",
        "description": "Explore Amazon Web Services (AWS) cloud platform and learn how to design, deploy, and manage applications in the cloud.",
        "instructor_id": "00000000-0000-0000-0000-000000000003",
        "tags": ["cloud", "aws", "serverless", "infrastructure"]
    },
    {
        "title": "Cybersecurity Essentials",
        "description": "Understand the fundamentals of cybersecurity and learn to protect systems and networks from various threats.",
        "instructor_id": "00000000-0000-0000-0000-000000000001",
        "tags": ["security", "network", "encryption", "ethical hacking"]
    },
]

# Пример технологического дерева для курса по Python
PYTHON_TECH_TREE = {
    "metadata": {
        "version": "1.0",
        "defaultLanguage": "en",
        "author": "CompEduX Team"
    },
    "displaySettings": {
        "theme": "dark",
        "defaultScale": 1.0,
        "gridSize": 50,
        "background": "grid"
    },
    "nodes": [
        {
            "id": "node1",
            "titleKey": "course.intro",
            "descriptionKey": "course.intro.desc",
            "position": {"x": 100, "y": 150},
            "style": "circular",
            "styleClass": "beginner",
            "state": "available",
            "difficulty": 1,
            "estimatedTime": 20,
            "children": ["node2", "node3"],
            "contentId": "content123",
            "requirements": []
        },
        {
            "id": "node2",
            "titleKey": "course.basics",
            "descriptionKey": "course.basics.desc",
            "position": {"x": 200, "y": 100},
            "style": "hexagon",
            "styleClass": "intermediate",
            "state": "locked",
            "difficulty": 2,
            "estimatedTime": 45,
            "children": ["node4"],
            "contentId": "content456",
            "requirements": ["node1"]
        },
        {
            "id": "node3",
            "titleKey": "course.advanced",
            "descriptionKey": "course.advanced.desc",
            "position": {"x": 200, "y": 200},
            "style": "square",
            "styleClass": "advanced",
            "state": "locked",
            "difficulty": 3,
            "estimatedTime": 60,
            "children": [],
            "contentId": "content789",
            "requirements": ["node2"]
        }
    ],
    "connections": [
        {
            "id": "conn1",
            "from": "node1",
            "to": "node2",
            "style": "solid_arrow",
            "styleClass": "required",
            "label": "Move to basics"
        },
        {
            "id": "conn2",
            "from": "node2",
            "to": "node3",
            "style": "dashed_line",
            "styleClass": "optional",
            "label": "Advanced topics"
        }
    ],
    "styles": {
        "nodeStyles": {
            "beginner": {
                "color": "#4CAF50",
                "borderColor": "#2E7D32",
                "shape": "circular",
                "icon": "icons/beginner.svg"
            },
            "intermediate": {
                "color": "#2196F3",
                "borderColor": "#0D47A1",
                "shape": "hexagon",
                "icon": "icons/intermediate.svg"
            },
            "advanced": {
                "color": "#FF5722",
                "borderColor": "#BF360C",
                "shape": "square",
                "icon": "icons/advanced.svg"
            }
        },
        "connectionStyles": {
            "required": {
                "color": "#FFFFFF",
                "thickness": 2,
                "style": "solid_arrow"
            },
            "optional": {
                "color": "#AAAAAA",
                "thickness": 1,
                "style": "dashed_line"
            }
        }
    }
}


def create_tags(db: Session) -> Dict[str, int]:
    """
    Create test tags in the database

    Args:
        db: Database session

    Returns:
        Dictionary mapping tag names to tag IDs
    """
    tag_map = {}

    for tag_data in TEST_TAGS:
        name_en = tag_data.get("name_en", "")
        name_ru = tag_data.get("name_ru", "")

        # Проверяем, существует ли тег с таким английским названием
        existing_tag = get_tag_by_name(db, name_en, "en")

        if existing_tag:
            # Тег уже существует
            tag_map[name_en] = existing_tag.id
            logger.info(f"Тег '{name_en}' уже существует (ID: {existing_tag.id})")
        else:
            # Создаем новый тег с переводами
            translations = {
                "en": name_en
            }

            # Добавляем русский перевод, если он есть
            if name_ru:
                translations["ru"] = name_ru

            # Создаем тег с переводами
            try:
                new_tag = create_tag_with_translations(db, translations)
                tag_map[name_en] = new_tag.id
                logger.info(f"Создан тег '{name_en}' (ID: {new_tag.id})")
            except Exception as e:
                logger.error(f"Ошибка при создании тега '{name_en}': {e}")

    return tag_map


def create_localizations(db: Session) -> Dict[str, uuid.UUID]:
    """
    Create test localizations in the database

    Args:
        db: Database session

    Returns:
        Dictionary mapping namespace to localization IDs
    """
    localization_map = {}

    for namespace, translations in TEST_LOCALIZATIONS.items():
        # Check if localization with this namespace already exists
        existing = localization_crud.get_by_namespace(db, namespace)

        if existing:
            localization_map[namespace] = existing.id
            logger.info(f"Localization for namespace '{namespace}' already exists")
            continue

        # Create new localization
        loc_create = LocalizationCreate(
            namespace=namespace,
            translations=translations
        )

        localization = localization_crud.create(db=db, obj_in=loc_create)
        localization_map[namespace] = localization.id
        logger.info(f"Created localization for namespace: {namespace}")

    return localization_map


def create_test_courses(
    db: Optional[Session] = None,
    force: bool = False,
    course_ids: Optional[List[int]] = None,
    create_tech_trees: bool = False
) -> List[Dict[str, Union[str, uuid.UUID]]]:
    """
    Create test courses in the database for development and testing.

    Args:
        db: Database session. If None, a new session will be created.
        force: If True, recreates the courses even if they already exist.
        course_ids: List of indices to create specific courses from TEST_COURSES.
                   If None, all courses will be created.
        create_tech_trees: If True, also creates technology trees for courses.

    Returns:
        List of dictionaries with created course titles and IDs
    """
    if db is None:
        db = SessionLocal()

    created_courses = []

    try:
        # First create the tags
        tag_map = create_tags(db)

        # Create localizations
        localization_map = create_localizations(db)

        # Filter courses by indices if provided
        courses_to_create = [TEST_COURSES[i] for i in course_ids] if course_ids else TEST_COURSES

        for course_data in courses_to_create:
            # Convert to CourseCreate schema
            title = course_data["title"]

            # Check if course already exists by title
            existing_course = db.query(Course).filter(Course.title == title).first()

            if existing_course and not force:
                logger.info(f"Course '{title}' already exists, skipping...")
                continue
            elif existing_course and force:
                logger.info(f"Removing existing course '{title}' for recreation...")
                course_crud.remove(db, id=existing_course.id)

            # Create course
            logger.info(f"Creating course: {title}")
            course_create = CourseCreate(**course_data)
            course = course_crud.create(db=db, obj_in=course_create)

            created_info = {
                "title": title,
                "id": course.id,
                "slug": course.slug
            }
            created_courses.append(created_info)

            # Create technology tree if requested
            if create_tech_trees and title == "Introduction to Python Programming":
                tree_data = PYTHON_TECH_TREE.copy()

                # Check if technology tree already exists
                existing_tree = technology_tree_crud.get_by_course_id(db, course_id=course.id)
                if existing_tree:
                    if force:
                        logger.info(f"Removing existing technology tree for course '{title}'")
                        technology_tree_crud.remove(db, id=existing_tree.id)
                    else:
                        logger.info(f"Technology tree for course '{title}' already exists, skipping...")
                        continue

                # Create new technology tree
                logger.info(f"Creating technology tree for course: {title}")
                tree_create = TechnologyTreeCreate(
                    course_id=course.id,
                    data=tree_data
                )
                technology_tree_crud.create(db=db, obj_in=tree_create)

    except Exception as e:
        logger.error(f"Error creating test data: {e}")
        raise

    finally:
        if db is not None:
            db.close()

    return created_courses


def reset_database(db: Optional[Session] = None) -> Tuple[int, int, int]:
    """
    Remove all courses, tags, technology trees, and localizations from the database.

    Args:
        db: Database session. If None, a new session will be created.

    Returns:
        Tuple with (number of deleted courses, number of deleted tags, number of deleted technology trees)
    """
    if db is None:
        db = SessionLocal()

    try:
        # First remove technology trees (due to foreign key constraints)
        trees = db.query(TechnologyTree).all()
        tree_count = 0
        for tree in trees:
            db.delete(tree)
            tree_count += 1

        # Get all courses
        courses = db.query(Course).all()
        course_count = 0
        for course in courses:
            db.delete(course)
            course_count += 1

        # Get all tags
        tags = db.query(Tag).all()
        tag_count = 0
        for tag in tags:
            db.delete(tag)
            tag_count += 1

        # Get all localizations
        localizations = db.query(Localization).all()
        localization_count = 0
        for localization in localizations:
            db.delete(localization)
            localization_count += 1

        db.commit()

        logger.info(f"Deleted {course_count} courses, {tag_count} tags, {tree_count} technology trees, and {localization_count} localizations")
        return course_count, tag_count, tree_count

    finally:
        if db is not None:
            db.close()


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    logger.info("Creating test course data...")

    # Reset database first
    reset_database()

    # Create test courses with technology trees and localizations
    courses = create_test_courses(force=True, create_tech_trees=True)

    logger.info(f"Created {len(courses)} test courses")
    for course in courses:
        logger.info(f"  - {course['title']} (ID: {course['id']}, Slug: {course['slug']})")
