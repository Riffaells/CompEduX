package model.course

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Модель для квиза/теста в уроке
 */
@Serializable
data class QuizDomain(
    val id: String,
    @SerialName("lesson_id")
    val lessonId: String,
    val title: LocalizedContent,
    val description: LocalizedContent? = null,
    @SerialName("time_limit")
    val timeLimit: Int? = null, // в секундах
    @SerialName("passing_score")
    val passingScore: Int = 70, // процент для успешного прохождения
    @SerialName("max_attempts")
    val maxAttempts: Int? = null,
    @SerialName("show_answers")
    val showAnswers: Boolean = false,
    @SerialName("randomize_questions")
    val randomizeQuestions: Boolean = false,
    val questions: List<QuizQuestionDomain> = emptyList()
)

/**
 * Модель для вопроса в квизе/тесте
 */
@Serializable
data class QuizQuestionDomain(
    val id: String,
    @SerialName("quiz_id")
    val quizId: String,
    val text: LocalizedContent,
    val type: QuizQuestionTypeDomain,
    @SerialName("points_possible")
    val pointsPossible: Int = 1,
    val options: List<QuizOptionDomain> = emptyList(),
    @SerialName("correct_answer")
    val correctAnswer: String? = null, // для вопросов с одним ответом
    @SerialName("correct_answers")
    val correctAnswers: List<String> = emptyList(), // для вопросов с множественным выбором
    val explanation: LocalizedContent? = null,
    val order: Int = 0,
    @SerialName("is_required")
    val isRequired: Boolean = true
)

/**
 * Модель для варианта ответа в вопросе
 */
@Serializable
data class QuizOptionDomain(
    val id: String,
    @SerialName("question_id")
    val questionId: String,
    val text: LocalizedContent,
    @SerialName("is_correct")
    val isCorrect: Boolean = false,
    val order: Int = 0
)

/**
 * Типы вопросов в квизе/тесте
 */
@Serializable
enum class QuizQuestionTypeDomain {
    SINGLE_CHOICE,    // Один вариант ответа
    MULTIPLE_CHOICE,  // Несколько вариантов ответа
    TRUE_FALSE,       // Верно/Неверно
    SHORT_ANSWER,     // Короткий ответ текстом
    MATCHING,         // Сопоставление
    FILL_BLANK,       // Заполнить пропуск
    ORDERING,         // Расположить в правильном порядке
    ESSAY             // Развернутый ответ
} 