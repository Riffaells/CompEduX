package usecase.lesson

import model.DomainResult
import model.course.CourseProgressDomain
import model.course.LessonProgressDomain
import model.course.LessonProgressStatusDomain
import repository.LessonProgressRepository

/**
 * Класс для работы с прогрессом пользователя по урокам
 */
class LessonProgressUseCases(
    private val lessonProgressRepository: LessonProgressRepository
) {
    /**
     * Получить прогресс пользователя по уроку
     */
    suspend fun getLessonProgress(userId: String, lessonId: String): DomainResult<LessonProgressDomain?> {
        return lessonProgressRepository.getLessonProgress(userId, lessonId)
    }
    
    /**
     * Получить прогресс пользователя по всем урокам курса
     */
    suspend fun getLessonProgressForCourse(userId: String, courseId: String): DomainResult<List<LessonProgressDomain>> {
        return lessonProgressRepository.getLessonProgressForCourse(userId, courseId)
    }
    
    /**
     * Получить общий прогресс пользователя по курсу
     */
    suspend fun getCourseProgress(userId: String, courseId: String): DomainResult<CourseProgressDomain?> {
        return lessonProgressRepository.getCourseProgress(userId, courseId)
    }
    
    /**
     * Начать урок (отметить как начатый)
     */
    suspend fun startLesson(userId: String, lessonId: String, courseId: String, moduleId: String): DomainResult<LessonProgressDomain> {
        return lessonProgressRepository.markLessonStarted(userId, lessonId, courseId, moduleId)
    }
    
    /**
     * Завершить урок (отметить как завершенный)
     */
    suspend fun completeLesson(userId: String, lessonId: String): DomainResult<LessonProgressDomain> {
        return lessonProgressRepository.markLessonCompleted(userId, lessonId)
    }
    
    /**
     * Обновить позицию в уроке (например, временная метка видео)
     */
    suspend fun updateLessonPosition(userId: String, lessonId: String, position: String): DomainResult<LessonProgressDomain> {
        return lessonProgressRepository.updateLessonPosition(userId, lessonId, position)
    }
    
    /**
     * Обновить процент завершения урока
     */
    suspend fun updateLessonCompletionPercentage(userId: String, lessonId: String, percentage: Int): DomainResult<LessonProgressDomain> {
        return lessonProgressRepository.updateLessonCompletionPercentage(userId, lessonId, percentage)
    }
    
    /**
     * Добавить время, проведенное в уроке
     */
    suspend fun addLessonTimeSpent(userId: String, lessonId: String, seconds: Int): DomainResult<LessonProgressDomain> {
        return lessonProgressRepository.addLessonTimeSpent(userId, lessonId, seconds)
    }
} 