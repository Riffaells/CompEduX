package usecase.lesson

import model.DomainResult
import model.course.CourseLessonDomain
import model.course.LessonDetailsDomain
import model.course.LessonListDomain
import model.course.LessonQueryParams
import repository.LessonRepository

/**
 * Класс для работы с уроками курса
 */
class LessonUseCases(
    private val lessonRepository: LessonRepository
) {
    /**
     * Получить урок по ID
     */
    suspend fun getLesson(lessonId: String): DomainResult<CourseLessonDomain> {
        return lessonRepository.getLesson(lessonId)
    }

    /**
     * Получить детальную информацию об уроке по ID
     */
    suspend fun getLessonDetails(lessonId: String): DomainResult<LessonDetailsDomain> {
        return lessonRepository.getLessonDetails(lessonId)
    }

    /**
     * Получить список уроков для курса
     */
    suspend fun getLessonsForCourse(courseId: String): DomainResult<List<CourseLessonDomain>> {
        return lessonRepository.getLessonsForCourse(courseId)
    }

    /**
     * Получить список уроков для модуля
     */
    suspend fun getLessonsForModule(moduleId: String): DomainResult<List<CourseLessonDomain>> {
        return lessonRepository.getLessonsForModule(moduleId)
    }
    
    /**
     * Получить список уроков с пагинацией и фильтрацией
     */
    suspend fun getLessons(params: LessonQueryParams): DomainResult<LessonListDomain> {
        return lessonRepository.getLessons(params)
    }
    
    /**
     * Получить следующий урок в модуле
     */
    suspend fun getNextLesson(lessonId: String, moduleId: String): DomainResult<CourseLessonDomain?> {
        return lessonRepository.getNextLesson(lessonId, moduleId)
    }
    
    /**
     * Получить предыдущий урок в модуле
     */
    suspend fun getPreviousLesson(lessonId: String, moduleId: String): DomainResult<CourseLessonDomain?> {
        return lessonRepository.getPreviousLesson(lessonId, moduleId)
    }
    
    /**
     * Получить уроки, связанные с узлом дерева технологий
     */
    suspend fun getLessonsForTreeNode(courseId: String, treeNodeId: String): DomainResult<List<CourseLessonDomain>> {
        return lessonRepository.getLessonsForTreeNode(courseId, treeNodeId)
    }
} 