from typing import Optional
from uuid import UUID
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user_id, get_db
from app.repositories.article import ArticleRepository
from app.schemas.article import ArticleCreate, ArticleResponse, ArticleUpdate, ArticleListResponse

router = APIRouter()


@router.post("/{course_id}/articles", response_model=ArticleResponse, status_code=status.HTTP_201_CREATED)
async def create_article(
    course_id: UUID,
    article_data: ArticleCreate,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Create a new article for a course.
    """
    article_repo = ArticleRepository(db)

    # Check if article with the same slug and language already exists
    existing_article = await article_repo.get_article_by_slug(
        course_id=course_id,
        slug=article_data.slug,
        language=article_data.language
    )

    if existing_article:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Article with this slug and language already exists for this course"
        )

    article = await article_repo.create_article(course_id, article_data)
    return article


@router.get("/{course_id}/articles", response_model=ArticleListResponse)
async def list_articles(
    course_id: UUID,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=100),
    language: Optional[str] = None,
    is_published: Optional[bool] = None,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    List all articles for a course with optional filtering.
    """
    article_repo = ArticleRepository(db)
    articles, total = await article_repo.get_articles(
        course_id=course_id,
        skip=skip,
        limit=limit,
        language=language,
        is_published=is_published
    )

    return ArticleListResponse(items=articles, total=total)


@router.get("/{course_id}/articles/{article_id}", response_model=ArticleResponse)
async def get_article(
    course_id: UUID,
    article_id: UUID,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Get a specific article by ID.
    """
    article_repo = ArticleRepository(db)
    article = await article_repo.get_article(article_id)

    if not article or article.course_id != course_id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Article not found"
        )

    return article


@router.put("/{course_id}/articles/{article_id}", response_model=ArticleResponse)
async def update_article(
    course_id: UUID,
    article_id: UUID,
    article_data: ArticleUpdate,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Update an article by ID.
    """
    article_repo = ArticleRepository(db)

    # Verify article exists and belongs to the specified course
    article = await article_repo.get_article(article_id)
    if not article or article.course_id != course_id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Article not found"
        )

    # If slug or language is changing, check for conflicts
    if (article_data.slug and article_data.slug != article.slug) or \
       (article_data.language and article_data.language != article.language):
        slug = article_data.slug or article.slug
        language = article_data.language or article.language

        existing_article = await article_repo.get_article_by_slug(
            course_id=course_id,
            slug=slug,
            language=language
        )

        if existing_article and existing_article.id != article_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Another article with this slug and language already exists"
            )

    updated_article = await article_repo.update_article(article_id, article_data)
    return updated_article


@router.delete("/{course_id}/articles/{article_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_article(
    course_id: UUID,
    article_id: UUID,
    db: AsyncSession = Depends(get_db),
    current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Delete an article by ID.
    """
    article_repo = ArticleRepository(db)

    # Verify article exists and belongs to the specified course
    article = await article_repo.get_article(article_id)
    if not article or article.course_id != course_id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Article not found"
        )

    success = await article_repo.delete_article(article_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to delete article"
        )
