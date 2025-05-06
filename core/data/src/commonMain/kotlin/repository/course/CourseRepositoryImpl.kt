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

        // Проверяем наличие токена доступа
        val accessToken = tokenRepository.getAccessToken()
        if (accessToken == null) {
            logger.w("Cannot get course: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
        }

        // Выполняем запрос
        val result = networkCourseApi.getCourse(courseId)

        // Обрабатываем результат
        when (result) {
            is DomainResult.Success -> {
                logger.i("Course retrieved successfully: $courseId")
            }

            is DomainResult.Error -> {
                logger.e("Failed to get course: ${result.error.message}")

                // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                    return@withContext getCourse(courseId)
                }
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

            // Проверяем наличие токена доступа
            val accessToken = tokenRepository.getAccessToken()
            if (accessToken == null) {
                logger.w("Cannot get courses: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
            }

            // Преобразуем параметры запроса в Map для API
            val queryParams = params.toMap()

            // Выполняем запрос
            val result = networkCourseApi.getCourses(queryParams)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Courses retrieved successfully, count: ${result.data.items.size}")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to get courses: ${result.error.message}")

                    // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                    if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                        return@withContext getCourses(params)
                    }
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    override suspend fun createCourse(course: CourseDomain): DomainResult<CourseDomain> =
        withContext(Dispatchers.Default) {
            logger.d("CourseRepositoryImpl: createCourse(${course.title.content})")

            // Проверяем наличие токена доступа
            val accessToken = tokenRepository.getAccessToken()
            if (accessToken == null) {
                logger.w("Cannot create course: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
            }

            // Выполняем запрос
            val result = networkCourseApi.createCourse(course)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Course created successfully: ${result.data.id}")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to create course: ${result.error.message}")

                    // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                    if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                        return@withContext createCourse(course)
                    }
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

            // Проверяем наличие токена доступа
            val accessToken = tokenRepository.getAccessToken()
            if (accessToken == null) {
                logger.w("Cannot update course: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
            }

            // Выполняем запрос
            val result = networkCourseApi.updateCourse(courseId, course)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Course updated successfully: $courseId")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to update course: ${result.error.message}")

                    // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                    if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                        return@withContext updateCourse(courseId, course)
                    }
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    override suspend fun deleteCourse(courseId: String): DomainResult<Unit> = withContext(Dispatchers.Default) {
        logger.d("CourseRepositoryImpl: deleteCourse($courseId)")

        // Проверяем наличие токена доступа
        val accessToken = tokenRepository.getAccessToken()
        if (accessToken == null) {
            logger.w("Cannot delete course: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
        }

        // Выполняем запрос
        val result = networkCourseApi.deleteCourse(courseId)

        // Обрабатываем результат
        when (result) {
            is DomainResult.Success -> {
                logger.i("Course deleted successfully: $courseId")
            }

            is DomainResult.Error -> {
                logger.e("Failed to delete course: ${result.error.message}")

                // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                    return@withContext deleteCourse(courseId)
                }
            }

            is DomainResult.Loading -> {
                // Не выполняем действий в состоянии загрузки
            }
        }

        result
    }

    /**
     * Обновляет токен доступа, если это необходимо и возможно
     * @return true, если токен был успешно обновлен
     */
    private suspend fun refreshTokenIfNeeded(): Boolean {
        logger.d("Attempting to refresh token")

        // Проверяем наличие refresh token
        val refreshToken = tokenRepository.getRefreshToken() ?: run {
            logger.w("Cannot refresh token: No refresh token")
            return false
        }

        // Здесь должен быть вызов метода обновления токена из AuthRepository
        // Для простоты и независимости от реализации AuthRepository,
        // пока просто возвращаем false
        logger.w("Token refresh not implemented in CourseRepository")
        return false
    }
}
