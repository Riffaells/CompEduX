package model.course

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Расширенная модель урока с полным содержимым
 */
@Serializable
data class LessonDetailsDomain(
    val id: String,
    @SerialName("module_id")
    val moduleId: String,
    @SerialName("course_id")
    val courseId: String,
    val title: LocalizedContent,
    val description: LocalizedContent? = null,
    val content: LocalizedContent,
    val order: Int,
    @SerialName("content_type")
    val contentType: LessonContentTypeDomain,
    val duration: Int? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    @SerialName("is_published")
    val isPublished: Boolean = false,
    @SerialName("tree_node_id")
    val treeNodeId: String? = null,
    val resources: List<LessonResourceDomain> = emptyList(),
    val articles: List<LessonArticleDomain> = emptyList(),
    val quizzes: List<QuizDomain> = emptyList(),
    @SerialName("lesson_metadata")
    val metadata: LessonMetadataDomain? = null
) {
    /**
     * Parse created_at timestamp string to Long
     */
    fun getCreatedAtMillis(): Long {
        return try {
            if (createdAt.isNotBlank()) {
                Instant.parse(createdAt).toEpochMilliseconds()
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Parse updated_at timestamp string to Long
     */
    fun getUpdatedAtMillis(): Long {
        return try {
            if (updatedAt.isNotBlank()) {
                Instant.parse(updatedAt).toEpochMilliseconds()
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Модель для статьи, связанной с уроком
 */
@Serializable
data class LessonArticleDomain(
    val id: String,
    val title: LocalizedContent,
    val description: LocalizedContent? = null,
    val content: LocalizedContent,
    @SerialName("course_id")
    val courseId: String,
    val slug: String,
    val language: String,
    val order: Int,
    @SerialName("is_published")
    val isPublished: Boolean = false
)

/**
 * Модель для метаданных урока
 */
@Serializable
data class LessonMetadataDomain(
    val difficulty: LessonDifficultyDomain = LessonDifficultyDomain.BEGINNER,
    val prerequisites: List<String> = emptyList(),
    val objectives: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val estimatedTime: Int? = null,
    val interactiveElements: Boolean = false,
    val quizCount: Int = 0,
    val exerciseCount: Int = 0
)

/**
 * Уровень сложности урока
 */
@Serializable
enum class LessonDifficultyDomain {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}

/**
 * Статус прохождения урока пользователем
 */
@Serializable
enum class LessonProgressStatusDomain {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}

/**
 * Модель для списка уроков с пагинацией
 */
@Serializable
data class LessonListDomain(
    val items: List<CourseLessonDomain> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val size: Int = 20,
    val pages: Int = 0
) {
    /**
     * Проверка, пуст ли список
     */
    val isEmpty: Boolean
        get() = items.isEmpty()

    /**
     * Проверка, является ли эта страница первой
     */
    val isFirst: Boolean
        get() = page == 0

    /**
     * Проверка, является ли эта страница последней
     */
    val isLast: Boolean
        get() = page >= pages - 1 || isEmpty

    companion object {
        /**
         * Create an empty lesson list
         */
        fun empty(): LessonListDomain {
            return LessonListDomain(
                items = emptyList(),
                total = 0,
                page = 0,
                size = 0,
                pages = 0
            )
        }

        /**
         * Create a list from lessons without pagination info
         */
        fun fromList(lessons: List<CourseLessonDomain>): LessonListDomain {
            val nonEmptyList = lessons.isNotEmpty()
            return LessonListDomain(
                items = lessons,
                total = lessons.size,
                page = 0,
                size = lessons.size,
                pages = if (nonEmptyList) 1 else 0
            )
        }
    }
} 