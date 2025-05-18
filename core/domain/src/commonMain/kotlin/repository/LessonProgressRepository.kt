package repository

import model.DomainResult
import model.course.CourseProgressDomain
import model.course.LessonProgressDomain
import model.course.LessonProgressStatusDomain

/**
 * Репозиторий для работы с прогрессом пользователя по урокам
 */
interface LessonProgressRepository {
    /**
     * Получить прогресс пользователя по уроку
     */
    suspend fun getLessonProgress(userId: String, lessonId: String): DomainResult<LessonProgressDomain?>
    
    /**
     * Получить прогресс пользователя по всем урокам курса
     */
    suspend fun getLessonProgressForCourse(userId: String, courseId: String): DomainResult<List<LessonProgressDomain>>
    
    /**
     * Получить общий прогресс пользователя по курсу
     */
    suspend fun getCourseProgress(userId: String, courseId: String): DomainResult<CourseProgressDomain?>
    
    /**
     * Обновить статус прогресса пользователя по уроку
     */
    suspend fun updateLessonProgressStatus(
        userId: String, 
        lessonId: String, 
        status: LessonProgressStatusDomain
    ): DomainResult<LessonProgressDomain>
    
    /**
     * Обновить процент завершения урока
     */
    suspend fun updateLessonCompletionPercentage(
        userId: String, 
        lessonId: String, 
        percentage: Int
    ): DomainResult<LessonProgressDomain>
    
    /**
     * Обновить позицию в уроке (например, временная метка видео)
     */
    suspend fun updateLessonPosition(
        userId: String, 
        lessonId: String, 
        position: String
    ): DomainResult<LessonProgressDomain>
    
    /**
     * Отметить урок как начатый
     */
    suspend fun markLessonStarted(
        userId: String, 
        lessonId: String,
        courseId: String,
        moduleId: String
    ): DomainResult<LessonProgressDomain>
    
    /**
     * Отметить урок как завершенный
     */
    suspend fun markLessonCompleted(
        userId: String, 
        lessonId: String
    ): DomainResult<LessonProgressDomain>
    
    /**
     * Добавить время, проведенное в уроке
     */
    suspend fun addLessonTimeSpent(
        userId: String, 
        lessonId: String, 
        seconds: Int
    ): DomainResult<LessonProgressDomain>
} 