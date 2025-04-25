from typing import List, Optional
from uuid import UUID
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, update, delete
from sqlalchemy.orm import selectinload

from app.models.article import Article
from app.schemas.article import ArticleCreate, ArticleUpdate


class ArticleRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def create_article(self, course_id: UUID, article_data: ArticleCreate) -> Article:
        article = Article(
            course_id=course_id,
            slug=article_data.slug,
            language=article_data.language,
            title=article_data.title,
            content=article_data.content,
            description=article_data.description,
            order=article_data.order,
            is_published=article_data.is_published
        )
        self.session.add(article)
        await self.session.commit()
        await self.session.refresh(article)
        return article

    async def get_article(self, article_id: UUID) -> Optional[Article]:
        result = await self.session.execute(
            select(Article).where(Article.id == article_id)
        )
        return result.scalars().first()

    async def get_article_by_slug(self, course_id: UUID, slug: str, language: str) -> Optional[Article]:
        result = await self.session.execute(
            select(Article).where(
                Article.course_id == course_id,
                Article.slug == slug,
                Article.language == language
            )
        )
        return result.scalars().first()

    async def get_articles(self,
                          course_id: UUID,
                          skip: int = 0,
                          limit: int = 100,
                          language: Optional[str] = None,
                          is_published: Optional[bool] = None) -> tuple[List[Article], int]:
        query = select(Article).where(Article.course_id == course_id)

        if language:
            query = query.where(Article.language == language)

        if is_published is not None:
            query = query.where(Article.is_published == is_published)

        # Count total articles matching filters
        count_query = select(func.count()).select_from(query.subquery())
        total = await self.session.execute(count_query)
        total_count = total.scalar() or 0

        # Apply pagination
        query = query.order_by(Article.order).offset(skip).limit(limit)

        result = await self.session.execute(query)
        articles = result.scalars().all()

        return articles, total_count

    async def update_article(self, article_id: UUID, article_data: ArticleUpdate) -> Optional[Article]:
        update_data = article_data.dict(exclude_unset=True)

        if not update_data:
            # If no data to update, just return the current article
            return await self.get_article(article_id)

        await self.session.execute(
            update(Article)
            .where(Article.id == article_id)
            .values(**update_data)
        )
        await self.session.commit()

        return await self.get_article(article_id)

    async def delete_article(self, article_id: UUID) -> bool:
        result = await self.session.execute(
            delete(Article).where(Article.id == article_id)
        )
        await self.session.commit()
        return result.rowcount > 0
