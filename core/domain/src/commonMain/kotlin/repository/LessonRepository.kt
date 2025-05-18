package repository

import model.DomainResult
import model.course.CourseLessonDomain
import model.course.LessonDetailsDomain
import model.course.LessonListDomain
import model.course.LessonQueryParams

/**
 * Репозиторий для работы с уроками
 */
interface LessonRepository {
    /**
     * Получить урок по ID
     */
    suspend fun getLesson(lessonId: String): DomainResult<CourseLessonDomain>

    /**
     * Получить детальную информацию об уроке по ID
     */
    suspend fun getLessonDetails(lessonId: String): DomainResult<LessonDetailsDomain>

    /**
     * Получить список уроков для курса
     */
    suspend fun getLessonsForCourse(courseId: String): DomainResult<List<CourseLessonDomain>>

    /**
     * Получить список уроков для модуля
     */
    suspend fun getLessonsForModule(moduleId: String): DomainResult<List<CourseLessonDomain>>
    
    /**
     * Получить список уроков с пагинацией и фильтрацией
     */
    suspend fun getLessons(params: LessonQueryParams): DomainResult<LessonListDomain>
    
    /**
     * Получить следующий урок в модуле
     */
    suspend fun getNextLesson(lessonId: String, moduleId: String): DomainResult<CourseLessonDomain?>
    
    /**
     * Получить предыдущий урок в модуле
     */
    suspend fun getPreviousLesson(lessonId: String, moduleId: String): DomainResult<CourseLessonDomain?>
    
    /**
     * Получить уроки, связанные с узлом дерева технологий
     */
    suspend fun getLessonsForTreeNode(courseId: String, treeNodeId: String): DomainResult<List<CourseLessonDomain>>
} 