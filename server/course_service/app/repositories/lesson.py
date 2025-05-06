from typing import List, Optional, Tuple, Set
from uuid import UUID

from app.models.lesson import Lesson, lesson_article
from app.models.article import Article
from app.schemas.lesson import LessonCreate, LessonUpdate
from sqlalchemy import select, func, update, delete, insert, and_, or_
from sqlalchemy.ext.asyncio import AsyncSession


class LessonRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def create_lesson(self, course_id: UUID, lesson_data: LessonCreate) -> Lesson:
        """Create a new lesson for a course."""
        lesson = Lesson(
            course_id=course_id,
            slug=lesson_data.slug,
            language=lesson_data.language,
            title=lesson_data.title,
            content=lesson_data.content,
            description=lesson_data.description,
            order=lesson_data.order,
            duration=lesson_data.duration,
            is_published=lesson_data.is_published,
            lesson_metadata=lesson_data.lesson_metadata,
            tree_node_id=lesson_data.tree_node_id
        )
        self.session.add(lesson)
        await self.session.commit()
        await self.session.refresh(lesson)
        return lesson

    async def get_lesson(self, lesson_id: UUID) -> Optional[Lesson]:
        """Get a lesson by ID."""
        result = await self.session.execute(
            select(Lesson).where(Lesson.id == lesson_id)
        )
        return result.scalars().first()

    async def get_lesson_by_slug(self, course_id: UUID, slug: str, language: str) -> Optional[Lesson]:
        """Get a lesson by course ID, slug, and language."""
        result = await self.session.execute(
            select(Lesson).where(
                Lesson.course_id == course_id,
                Lesson.slug == slug,
                Lesson.language == language
            )
        )
        return result.scalars().first()

    async def get_lessons_by_tree_node(self, course_id: UUID, tree_node_id: str) -> List[Lesson]:
        """Get all lessons associated with a tree node."""
        result = await self.session.execute(
            select(Lesson).where(
                Lesson.course_id == course_id,
                Lesson.tree_node_id == tree_node_id
            ).order_by(Lesson.order)
        )
        return result.scalars().all()

    async def get_lessons(self,
                         course_id: UUID,
                         skip: int = 0,
                         limit: int = 100,
                         language: Optional[str] = None,
                         is_published: Optional[bool] = None,
                         tree_node_id: Optional[str] = None) -> Tuple[List[Lesson], int]:
        """Get lessons for a course with pagination and filtering."""
        query = select(Lesson).where(Lesson.course_id == course_id)

        if language:
            query = query.where(Lesson.language == language)

        if is_published is not None:
            query = query.where(Lesson.is_published == is_published)

        if tree_node_id:
            query = query.where(Lesson.tree_node_id == tree_node_id)

        # Count total lessons matching filters
        count_query = select(func.count()).select_from(query.subquery())
        total = await self.session.execute(count_query)
        total_count = total.scalar() or 0

        # Apply pagination
        query = query.order_by(Lesson.order).offset(skip).limit(limit)

        result = await self.session.execute(query)
        lessons = result.scalars().all()

        return lessons, total_count

    async def update_lesson(self, lesson_id: UUID, lesson_data: LessonUpdate) -> Optional[Lesson]:
        """Update an existing lesson."""
        update_data = lesson_data.dict(exclude_unset=True)

        if not update_data:
            # If no data to update, just return the current lesson
            return await self.get_lesson(lesson_id)

        await self.session.execute(
            update(Lesson)
            .where(Lesson.id == lesson_id)
            .values(**update_data)
        )
        await self.session.commit()

        return await self.get_lesson(lesson_id)

    async def delete_lesson(self, lesson_id: UUID) -> bool:
        """Delete a lesson by ID."""
        result = await self.session.execute(
            delete(Lesson).where(Lesson.id == lesson_id)
        )
        await self.session.commit()
        return result.rowcount > 0

    async def add_article_to_lesson(self, lesson_id: UUID, article_id: UUID) -> bool:
        """Associate an article with a lesson."""
        # Check if the association already exists
        result = await self.session.execute(
            select(lesson_article).where(
                lesson_article.c.lesson_id == lesson_id,
                lesson_article.c.article_id == article_id
            )
        )

        if result.first():
            # Association already exists
            return True

        # Add the association
        await self.session.execute(
            insert(lesson_article).values(
                lesson_id=lesson_id,
                article_id=article_id
            )
        )
        await self.session.commit()
        return True

    async def remove_article_from_lesson(self, lesson_id: UUID, article_id: UUID) -> bool:
        """Remove an article association from a lesson."""
        result = await self.session.execute(
            delete(lesson_article).where(
                lesson_article.c.lesson_id == lesson_id,
                lesson_article.c.article_id == article_id
            )
        )
        await self.session.commit()
        return result.rowcount > 0

    async def get_lesson_articles(self, lesson_id: UUID) -> List[Article]:
        """Get all articles associated with a lesson."""
        result = await self.session.execute(
            select(Article)
            .join(lesson_article, Article.id == lesson_article.c.article_id)
            .where(lesson_article.c.lesson_id == lesson_id)
            .order_by(Article.order)
        )
        return result.scalars().all()

    async def get_lessons_by_article(self, article_id: UUID) -> List[Lesson]:
        """Get all lessons that use a specific article."""
        result = await self.session.execute(
            select(Lesson)
            .join(lesson_article, Lesson.id == lesson_article.c.lesson_id)
            .where(lesson_article.c.article_id == article_id)
            .order_by(Lesson.order)
        )
        return result.scalars().all()
