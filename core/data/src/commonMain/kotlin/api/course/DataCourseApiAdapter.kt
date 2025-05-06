package api.course

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logging.Logger
import model.DomainError
import model.DomainResult
import model.course.CourseDomain
import model.course.CourseListDomain
import repository.auth.TokenRepository

/**
 * Adapter connecting CourseApi with NetworkCourseApi
 * Allows abstracting the domain layer from the details of network request implementation
 */
class DataCourseApiAdapter(
    private val networkCourseApi: NetworkCourseApi,
    private val tokenRepository: TokenRepository,
    private val logger: Logger
) : CourseApi {

    override suspend fun getCourse(courseId: String): DomainResult<CourseDomain> = withContext(Dispatchers.Default) {
        logger.d("DataCourseApiAdapter: getCourse($courseId)")

        // Get saved token
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot get course: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
        }

        return@withContext networkCourseApi.getCourse(courseId)
    }

    override suspend fun getCourses(params: Map<String, Any?>): DomainResult<CourseListDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataCourseApiAdapter: getCourses(${params.keys})")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot get courses: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkCourseApi.getCourses(params)
        }

    override suspend fun createCourse(course: CourseDomain): DomainResult<CourseDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataCourseApiAdapter: createCourse(${course.id})")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot create course: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkCourseApi.createCourse(course)
        }

    override suspend fun updateCourse(courseId: String, course: CourseDomain): DomainResult<CourseDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataCourseApiAdapter: updateCourse($courseId)")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot update course: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkCourseApi.updateCourse(courseId, course)
        }

    override suspend fun deleteCourse(courseId: String): DomainResult<Unit> = withContext(Dispatchers.Default) {
        logger.d("DataCourseApiAdapter: deleteCourse($courseId)")

        // Get saved token
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot delete course: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
        }

        return@withContext networkCourseApi.deleteCourse(courseId)
    }
}
