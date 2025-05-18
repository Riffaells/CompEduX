package repository

import model.DomainResult
import model.course.QuizDomain
import model.course.QuizResultDomain

/**
 * Репозиторий для работы с квизами и тестами
 */
interface QuizRepository {
    /**
     * Получить квиз по ID
     */
    suspend fun getQuiz(quizId: String): DomainResult<QuizDomain>
    
    /**
     * Получить квизы для урока
     */
    suspend fun getQuizzesForLesson(lessonId: String): DomainResult<List<QuizDomain>>
    
    /**
     * Получить результаты прохождения квиза пользователем
     */
    suspend fun getQuizResults(userId: String, quizId: String): DomainResult<List<QuizResultDomain>>
    
    /**
     * Сохранить результат прохождения квиза
     */
    suspend fun saveQuizResult(quizResult: QuizResultDomain): DomainResult<QuizResultDomain>
} 