package model.course

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Модель прогресса пользователя по уроку
 */
@Serializable
data class LessonProgressDomain(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("lesson_id")
    val lessonId: String,
    @SerialName("course_id")
    val courseId: String,
    @SerialName("module_id")
    val moduleId: String,
    val status: LessonProgressStatusDomain = LessonProgressStatusDomain.NOT_STARTED,
    @SerialName("completion_percentage")
    val completionPercentage: Int = 0,
    @SerialName("time_spent")
    val timeSpent: Int = 0, // в секундах
    @SerialName("last_position")
    val lastPosition: String? = null, // позиция в уроке (например, временная метка видео)
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("started_at")
    val startedAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String = "",
    val notes: String? = null,
    @SerialName("quiz_results")
    val quizResults: List<QuizResultDomain> = emptyList()
) {
    /**
     * Проверка, завершен ли урок
     */
    val isCompleted: Boolean
        get() = status == LessonProgressStatusDomain.COMPLETED
    
    /**
     * Проверка, начат ли урок
     */
    val isStarted: Boolean
        get() = status != LessonProgressStatusDomain.NOT_STARTED
    
    /**
     * Получить время завершения урока в миллисекундах
     */
    fun getCompletedAtMillis(): Long {
        return try {
            if (!completedAt.isNullOrBlank()) {
                Instant.parse(completedAt).toEpochMilliseconds()
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Модель результата прохождения теста в уроке
 */
@Serializable
data class QuizResultDomain(
    val id: String,
    @SerialName("quiz_id")
    val quizId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("lesson_id")
    val lessonId: String,
    val score: Int,
    @SerialName("max_score")
    val maxScore: Int,
    @SerialName("completion_time")
    val completionTime: Int, // в секундах
    @SerialName("completed_at")
    val completedAt: String,
    val attempts: Int = 1,
    @SerialName("question_results")
    val questionResults: List<QuizQuestionResultDomain> = emptyList()
) {
    /**
     * Процент правильных ответов
     */
    val percentage: Int
        get() = if (maxScore > 0) (score * 100) / maxScore else 0
    
    /**
     * Проверка, пройден ли тест успешно (обычно > 70%)
     */
    val isPassed: Boolean
        get() = percentage >= 70
}

/**
 * Модель результата ответа на вопрос теста
 */
@Serializable
data class QuizQuestionResultDomain(
    @SerialName("question_id")
    val questionId: String,
    val correct: Boolean,
    @SerialName("selected_options")
    val selectedOptions: List<String> = emptyList(),
    @SerialName("points_earned")
    val pointsEarned: Int = 0,
    @SerialName("points_possible")
    val pointsPossible: Int = 0
)

/**
 * Модель общего прогресса по курсу
 */
@Serializable
data class CourseProgressDomain(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("course_id")
    val courseId: String,
    @SerialName("completed_lessons")
    val completedLessons: Int = 0,
    @SerialName("total_lessons")
    val totalLessons: Int = 0,
    @SerialName("completion_percentage")
    val completionPercentage: Int = 0,
    @SerialName("time_spent")
    val timeSpent: Int = 0, // в секундах
    @SerialName("started_at")
    val startedAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("last_accessed_at")
    val lastAccessedAt: String = "",
    @SerialName("last_lesson_id")
    val lastLessonId: String? = null
) {
    /**
     * Проверка, завершен ли курс
     */
    val isCompleted: Boolean
        get() = completedLessons == totalLessons && totalLessons > 0
} 