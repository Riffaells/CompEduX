from typing import List, Optional
from uuid import UUID
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.deps import get_db, get_current_user_id
from app.repositories.lesson import LessonRepository
from app.repositories.article import ArticleRepository
from app.repositories.course import CourseRepository
from app.crud.technology_tree import technology_tree_crud
from app.schemas.lesson import LessonResponse, LessonCreate, LessonUpdate, LessonListResponse, LessonWithContent

router = APIRouter()

@router.post("/", response_model=LessonResponse, status_code=status.HTTP_201_CREATED)
async def create_lesson(
    lesson_data: LessonCreate,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Create a new lesson.
    """
    lesson_repo = LessonRepository(db)
    course_repo = CourseRepository(db)

    # Verify course exists
    course = await course_repo.get_course(lesson_data.course_id)
    if not course:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {lesson_data.course_id} not found"
        )

    # Check if lesson with the same slug and language already exists
    existing_lesson = await lesson_repo.get_lesson_by_slug(
        course_id=lesson_data.course_id,
        slug=lesson_data.slug,
        language=lesson_data.language
    )

    if existing_lesson:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Lesson with this slug and language already exists for this course"
        )

    # Validate node_id if provided
    if lesson_data.tree_node_id:
        # Get the tree to check if node exists
        tree = await technology_tree_crud.get_by_course_id(db, lesson_data.course_id)
        if not tree or not tree.data or "nodes" not in tree.data or lesson_data.tree_node_id not in tree.data["nodes"]:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Node with ID {lesson_data.tree_node_id} not found in the course's technology tree"
            )

    lesson = await lesson_repo.create_lesson(lesson_data.course_id, lesson_data)
    return lesson

@router.get("/", response_model=LessonListResponse)
async def list_lessons(
    course_id: UUID,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=100),
    language: Optional[str] = None,
    is_published: Optional[bool] = None,
    tree_node_id: Optional[str] = None,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    List all lessons for a course with optional filtering.

    Parameters:
    - language: Filter by language code
    - is_published: Filter by publication status
    - tree_node_id: Filter by associated technology tree node
    """
    lesson_repo = LessonRepository(db)
    lessons, total = await lesson_repo.get_lessons(
        course_id=course_id,
        skip=skip,
        limit=limit,
        language=language,
        is_published=is_published,
        tree_node_id=tree_node_id
    )

    return LessonListResponse(items=lessons, total=total)

@router.get("/{lesson_id}", response_model=LessonResponse)
async def get_lesson(
    lesson_id: UUID,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Get a specific lesson by ID.
    """
    lesson_repo = LessonRepository(db)
    lesson = await lesson_repo.get_lesson(lesson_id)

    if not lesson:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lesson not found"
        )

    return lesson

@router.get("/{lesson_id}/content", response_model=LessonWithContent)
async def get_lesson_with_content(
    lesson_id: UUID,
    language: Optional[str] = None,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Get a lesson with its associated articles and materials.

    Parameters:
    - language: Preferred language for content. If not specified, uses the lesson's language.
    """
    lesson_repo = LessonRepository(db)
    article_repo = ArticleRepository(db)

    # Get the lesson
    lesson = await lesson_repo.get_lesson(lesson_id)
    if not lesson:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lesson not found"
        )

    # Set language to lesson language if not specified
    content_language = language or lesson.language

    # Get associated articles
    articles = []
    if lesson.article_ids:
        for article_id in lesson.article_ids:
            article = await article_repo.get_article(article_id)
            if article and article.language == content_language:
                articles.append(article)

    # Get node information if lesson is associated with a tree node
    node_info = None
    if lesson.tree_node_id:
        tree = await technology_tree_crud.get_by_course_id(db, lesson.course_id)
        if tree and tree.data and "nodes" in tree.data and lesson.tree_node_id in tree.data["nodes"]:
            node_info = tree.data["nodes"][lesson.tree_node_id]

    return LessonWithContent(
        **lesson.__dict__,
        articles=articles,
        node_info=node_info
    )

@router.put("/{lesson_id}", response_model=LessonResponse)
async def update_lesson(
    lesson_id: UUID,
    lesson_data: LessonUpdate,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Update a lesson by ID.
    """
    lesson_repo = LessonRepository(db)

    # Verify lesson exists
    lesson = await lesson_repo.get_lesson(lesson_id)
    if not lesson:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lesson not found"
        )

    # Validate tree_node_id if provided
    if lesson_data.tree_node_id is not None:
        if lesson_data.tree_node_id:  # If not None or empty string
            # Check if node exists in tree
            tree = await technology_tree_crud.get_by_course_id(db, lesson.course_id)
            if not tree or not tree.data or "nodes" not in tree.data or lesson_data.tree_node_id not in tree.data["nodes"]:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail=f"Node with ID {lesson_data.tree_node_id} not found in the course's technology tree"
                )

    updated_lesson = await lesson_repo.update_lesson(lesson_id, lesson_data)
    return updated_lesson

@router.delete("/{lesson_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_lesson(
    lesson_id: UUID,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Delete a lesson by ID.
    """
    lesson_repo = LessonRepository(db)

    # Verify lesson exists
    lesson = await lesson_repo.get_lesson(lesson_id)
    if not lesson:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lesson not found"
        )

    success = await lesson_repo.delete_lesson(lesson_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to delete lesson"
        )

@router.post("/{lesson_id}/articles/{article_id}", status_code=status.HTTP_200_OK)
async def add_article_to_lesson(
    lesson_id: UUID,
    article_id: UUID,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Associate an article with a lesson.
    """
    lesson_repo = LessonRepository(db)
    article_repo = ArticleRepository(db)

    # Verify lesson exists
    lesson = await lesson_repo.get_lesson(lesson_id)
    if not lesson:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lesson not found"
        )

    # Verify article exists and belongs to the same course as lesson
    article = await article_repo.get_article(article_id)
    if not article or article.course_id != lesson.course_id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Article not found or does not belong to the same course as the lesson"
        )

    # Add article to lesson
    success = await lesson_repo.add_article_to_lesson(lesson_id, article_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to add article to lesson"
        )

    return {"message": "Article added to lesson successfully"}

@router.delete("/{lesson_id}/articles/{article_id}", status_code=status.HTTP_200_OK)
async def remove_article_from_lesson(
    lesson_id: UUID,
    article_id: UUID,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Remove an article association from a lesson.
    """
    lesson_repo = LessonRepository(db)

    # Verify lesson exists
    lesson = await lesson_repo.get_lesson(lesson_id)
    if not lesson:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Lesson not found"
        )

    # Remove article from lesson
    success = await lesson_repo.remove_article_from_lesson(lesson_id, article_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Article not found in lesson"
        )

    return {"message": "Article removed from lesson successfully"}
