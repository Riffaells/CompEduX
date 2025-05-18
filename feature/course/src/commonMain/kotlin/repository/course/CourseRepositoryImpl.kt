package repository.course

import api.course.NetworkCourseApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logging.Logger
import model.DomainError
import model.DomainResult
import model.course.CourseDomain
import model.course.CourseListDomain
import model.course.CourseQueryParams
import repository.auth.TokenRepository

/**
 * Реализация репозитория курсов
 * Обеспечивает взаимодействие между доменным слоем и сетевым API
 */
class CourseRepositoryImpl(
    private val networkCourseApi: NetworkCourseApi,
    private val tokenRepository: TokenRepository,
    private val logger: Logger
) : CourseRepository {

    override suspend fun getCourse(courseId: String): DomainResult<CourseDomain> = withContext(Dispatchers.Default) {
        logger.d("CourseRepositoryImpl: getCourse($courseId)")
        
        // Выполняем запрос напрямую, без проверки токена
        val result = networkCourseApi.getCourse(courseId)
        
        // Обрабатываем результат
        when (result) {
            is DomainResult.Success -> {
                logger.i("Course retrieved successfully: $courseId")
            }
            is DomainResult.Error -> {
                logger.e("Failed to get course: ${result.error.message}")
            }
            is DomainResult.Loading -> {
                // Не выполняем действий в состоянии загрузки
            }
        }
        
        result
    }

    override suspend fun getCourses(params: CourseQueryParams): DomainResult<CourseListDomain> =
        withContext(Dispatchers.Default) {
            logger.d("CourseRepositoryImpl: getCourses(${params.toMap().keys})")
            
            // Преобразуем параметры запроса в Map для API
            val queryParams = params.toMap()
            
            // Выполняем запрос напрямую, без проверки токена
            val result = networkCourseApi.getCourses(queryParams)
            
            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Courses retrieved successfully, count: ${result.data.items.size}")
                }
                is DomainResult.Error -> {
                    logger.e("Failed to get courses: ${result.error.message}")
                }
                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }
            
            result
        }

    override suspend fun getCoursesByAuthor(
        authorId: String,
        page: Int,
        pageSize: Int
    ): DomainResult<CourseListDomain> {
        // Создаем параметры запроса
        val params = CourseQueryParams(
            authorId = authorId,
            page = page,
            size = pageSize
        )
        
        // Используем существующий метод getCourses
        return getCourses(params)
    }

    override suspend fun getPopularCourses(limit: Int): DomainResult<List<CourseDomain>> {
        // TODO: Implement when API supports this
        return DomainResult.Error(DomainError.unknownError("Not implemented"))
    }

    override suspend fun createCourse(course: CourseDomain): DomainResult<CourseDomain> =
        withContext(Dispatchers.Default) {
            logger.d("CourseRepositoryImpl: createCourse(${course.title.content})")
            
            // Получаем токен из репозитория
            val token = tokenRepository.getAccessToken()
            
            if (token == null) {
                logger.w("Cannot create course: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }
            
            val result = networkCourseApi.createCourse(course, token)
            
            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Course created successfully: ${result.data.id}")
                }
                is DomainResult.Error -> {
                    logger.e("Failed to create course: ${result.error.message}")
                }
                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }
            
            result
        }

    override suspend fun updateCourse(courseId: String, course: CourseDomain): DomainResult<CourseDomain> =
        withContext(Dispatchers.Default) {
            logger.d("CourseRepositoryImpl: updateCourse($courseId)")
            
            // Получаем токен из репозитория
            val token = tokenRepository.getAccessToken()
            
            if (token == null) {
                logger.w("Cannot update course: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }
            
            val result = networkCourseApi.updateCourse(courseId, course, token)
            
            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Course updated successfully: $courseId")
                }
                is DomainResult.Error -> {
                    logger.e("Failed to update course: ${result.error.message}")
                }
                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }
            
            result
        }

    override suspend fun deleteCourse(courseId: String): DomainResult<Unit> = withContext(Dispatchers.Default) {
        logger.d("CourseRepositoryImpl: deleteCourse($courseId)")
        
        // Получаем токен из репозитория
        val token = tokenRepository.getAccessToken()
        
        if (token == null) {
            logger.w("Cannot delete course: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
        }
        
        val result = networkCourseApi.deleteCourse(courseId, token)
        
        // Обрабатываем результат
        when (result) {
            is DomainResult.Success -> {
                logger.i("Course deleted successfully: $courseId")
            }
            is DomainResult.Error -> {
                logger.e("Failed to delete course: ${result.error.message}")
            }
            is DomainResult.Loading -> {
                // Не выполняем действий в состоянии загрузки
            }
        }
        
        result
    }
}
