from typing import Optional, List
from uuid import UUID

from app.api.deps import get_current_user_id, get_db
from app.repositories.article import ArticleRepository
from app.repositories.course import CourseRepository
from app.schemas.article import (
    ArticleCreate, ArticleResponse, ArticleUpdate, 
    ArticleListResponse, ArticleLocalizedResponse,
    ArticleLanguagesResponse
)
from fastapi import APIRouter, Depends, HTTPException, Query, status, Path
from sqlalchemy.ext.asyncio import AsyncSession

router = APIRouter()


@router.post("/", response_model=ArticleResponse, status_code=status.HTTP_201_CREATED)
async def create_article(
        article_data: ArticleCreate,
        db: AsyncSession = Depends(get_db),
        current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Create a new article with multilingual content.
    
    Expects title and content in at least one language.
    """
    article_repo = ArticleRepository(db)
    course_repo = CourseRepository(db)

    # Check if course exists
    course = await course_repo.get_course(article_data.course_id)
    if not course:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Course with ID {article_data.course_id} not found"
        )

    # Check if article with the same slug already exists
    existing_article = await article_repo.get_article_by_slug(
        course_id=article_data.course_id,
        slug=article_data.slug
    )

    if existing_article:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Article with this slug already exists for this course"
        )

    # Validate that at least one language is provided for title and content
    if not article_data.title or len(article_data.title) == 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Title must be provided in at least one language"
        )
    
    if not article_data.content or len(article_data.content) == 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Content must be provided in at least one language"
        )

    article = await article_repo.create_article(article_data.course_id, article_data)
    return article


@router.get("/", response_model=ArticleListResponse)
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
    
    If language is provided, only articles with content in that language will be included.
    """
    article_repo = ArticleRepository(db)
    articles, total = await article_repo.get_articles(
        course_id=course_id,
        skip=skip,
        limit=limit,
        language=language,
        is_published=is_published
    )
    
    # Filter articles by language if specified
    if language:
        # Keep only articles that have the requested language
        articles = [article for article in articles if language in article.available_languages()]
        total = len(articles)

    return ArticleListResponse(items=articles, total=total)


@router.get("/{article_id}", response_model=ArticleResponse)
async def get_article(
        article_id: UUID,
        db: AsyncSession = Depends(get_db),
        current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Get a specific article by ID with all available languages.
    """
    article_repo = ArticleRepository(db)
    article = await article_repo.get_article(article_id)

    if not article:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Article not found"
        )

    return article


@router.get("/{article_id}/localized", response_model=ArticleLocalizedResponse)
async def get_localized_article(
        article_id: UUID,
        language: str = Query(..., description="Language code (e.g., 'en', 'ru')"),
        fallback: bool = Query(True, description="Whether to fall back to another language if requested language not found"),
        db: AsyncSession = Depends(get_db),
        current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Get a specific article by ID in a specific language.
    
    If fallback is True and the requested language is not available,
    content from another language will be returned.
    """
    article_repo = ArticleRepository(db)
    article = await article_repo.get_article(article_id)

    if not article:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Article not found"
        )
    
    # Check if the requested language is available
    available_languages = article.available_languages()
    if language not in available_languages and not fallback:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Article content not available in language '{language}'"
        )
    
    # Get localized content
    localized = article.get_localized_version(language, fallback)
    
    # Add language to response
    localized["language"] = language
    
    return ArticleLocalizedResponse(**localized)


@router.get("/{article_id}/languages", response_model=ArticleLanguagesResponse)
async def get_article_languages(
        article_id: UUID,
        db: AsyncSession = Depends(get_db),
        current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Get all available languages for a specific article.
    """
    article_repo = ArticleRepository(db)
    languages = await article_repo.get_article_languages(article_id)
    
    return ArticleLanguagesResponse(languages=languages)


@router.put("/{article_id}", response_model=ArticleResponse)
async def update_article(
        article_id: UUID,
        article_data: ArticleUpdate,
        db: AsyncSession = Depends(get_db),
        current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Update an article by ID.
    
    Can update multilingual content for specific languages without affecting other languages.
    """
    article_repo = ArticleRepository(db)

    # Verify article exists
    article = await article_repo.get_article(article_id)
    if not article:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Article not found"
        )

    # If slug is changing, check for conflicts
    if article_data.slug and article_data.slug != article.slug:
        existing_article = await article_repo.get_article_by_slug(
            course_id=article.course_id,
            slug=article_data.slug
        )

        if existing_article and existing_article.id != article_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Another article with this slug already exists"
            )
    
    # Special handling for multilingual fields to merge rather than replace
    if article_data.title:
        # Update only the languages provided, keep existing languages
        merged_title = dict(article.title) if article.title else {}
        merged_title.update(article_data.title)
        article_data.title = merged_title
    
    if article_data.description:
        # Update only the languages provided, keep existing languages
        merged_description = dict(article.description) if article.description else {}
        merged_description.update(article_data.description)
        article_data.description = merged_description
    
    if article_data.content:
        # Update only the languages provided, keep existing languages
        merged_content = dict(article.content) if article.content else {}
        merged_content.update(article_data.content)
        article_data.content = merged_content

    updated_article = await article_repo.update_article(article_id, article_data)
    return updated_article


@router.delete("/{article_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_article(
        article_id: UUID,
        db: AsyncSession = Depends(get_db),
        current_user_id: UUID = Depends(get_current_user_id)
):
    """
    Delete an article by ID.
    """
    article_repo = ArticleRepository(db)

    # Verify article exists
    article = await article_repo.get_article(article_id)
    if not article:
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
