package usecase.lesson

import model.DomainResult
import model.course.QuizDomain
import model.course.QuizResultDomain
import repository.QuizRepository

/**
 * Класс для работы с квизами и тестами
 */
class QuizUseCases(
    private val quizRepository: QuizRepository
) {
    /**
     * Получить квиз по ID
     */
    suspend fun getQuiz(quizId: String): DomainResult<QuizDomain> {
        return quizRepository.getQuiz(quizId)
    }
    
    /**
     * Получить квизы для урока
     */
    suspend fun getQuizzesForLesson(lessonId: String): DomainResult<List<QuizDomain>> {
        return quizRepository.getQuizzesForLesson(lessonId)
    }
    
    /**
     * Получить результаты прохождения квиза пользователем
     */
    suspend fun getQuizResults(userId: String, quizId: String): DomainResult<List<QuizResultDomain>> {
        return quizRepository.getQuizResults(userId, quizId)
    }
    
    /**
     * Сохранить результат прохождения квиза
     */
    suspend fun saveQuizResult(quizResult: QuizResultDomain): DomainResult<QuizResultDomain> {
        return quizRepository.saveQuizResult(quizResult)
    }
} 