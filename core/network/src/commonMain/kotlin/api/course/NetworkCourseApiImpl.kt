package api.course

import base.BaseNetworkApi
import client.safeSendWithErrorBody
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.io.IOException
import logging.Logger
import mapper.toDomain
import mapper.toNetwork
import model.DomainError
import model.DomainResult
import model.course.*
import kotlin.math.pow
import kotlin.random.Random

/**
 * Implementation of NetworkCourseApi that uses Ktor HttpClient
 * to perform API requests
 */
class NetworkCourseApiImpl(
    client: HttpClient,
    networkConfig: NetworkConfig,
    logger: Logger
) : BaseNetworkApi(client, networkConfig, logger), NetworkCourseApi {


    /**
     * Get a course by its identifier
     * @param courseId the course identifier
     * @param token authorization token
     * @return course data result
     */
    override suspend fun getCourse(courseId: String, token: String?): DomainResult<CourseDomain> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkCourse, NetworkCourseErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId")
                        method = HttpMethod.Get
                        // Add authorization header if token is provided
                        token?.let { header(HttpHeaders.Authorization, "Bearer $it") }

                        logger.d("Getting course with ID: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Get course failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Course retrieved successfully: $courseId")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Get course")
            }
        }
    }

    /**
     * Get a list of courses with filtering and pagination
     * @param params query parameters as a map
     * @param token authorization token
     * @return paginated list of courses result
     */
    override suspend fun getCourses(params: Map<String, Any?>, token: String?): DomainResult<CourseListDomain> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkCourseList, NetworkCourseErrorResponse>(
                    {
                        url("$apiUrl/courses")
                        method = HttpMethod.Get
                        // Add authorization header if token is provided
                        token?.let { header(HttpHeaders.Authorization, "Bearer $it") }

                        // Add query parameters
                        params.forEach { (key, value) ->
                            when (value) {
                                is List<*> -> parameter(key, value.joinToString(","))
                                else -> parameter(key, value)
                            }
                        }

                        logger.d("Getting courses list with params: $params")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Get courses failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Courses retrieved successfully, count: ${(it.data.items.size)}")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Get courses")
            }
        }
    }

    /**
     * Get a list of popular courses
     * @param limit maximum number of courses
     * @param token authorization token
     * @return list of courses result
     */
    override suspend fun getPopularCourses(limit: Int, token: String?): DomainResult<List<CourseDomain>> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkCourseList, NetworkCourseErrorResponse>(
                    {
                        url("$apiUrl/courses")
                        method = HttpMethod.Get
                        // Add authorization header if token is provided
                        token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                        parameter("limit", limit)
                        parameter("sort", "popularity")

                        logger.d("Getting popular courses, limit: $limit")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Get popular courses failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Popular courses retrieved successfully, count: ${(it.data.items.size)}")
                    }
                }.map { networkResponse ->
                    networkResponse.items.map { it.toDomain() }
                }
            } catch (e: Exception) {
                processApiException(e, "Get popular courses")
            }
        }
    }

    /**
     * Get a list of new courses
     * @param limit maximum number of courses
     * @param token authorization token
     * @return list of courses result
     */
    override suspend fun getNewCourses(limit: Int, token: String?): DomainResult<List<CourseDomain>> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkCourseList, NetworkCourseErrorResponse>(
                    {
                        url("$apiUrl/courses")
                        method = HttpMethod.Get
                        // Add authorization header if token is provided
                        token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                        parameter("limit", limit)
                        parameter("sort", "created_at")
                        parameter("order", "desc")

                        logger.d("Getting new courses, limit: $limit")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Get new courses failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("New courses retrieved successfully, count: ${(it.data.items.size)}")
                    }
                }.map { networkResponse ->
                    networkResponse.items.map { it.toDomain() }
                }
            } catch (e: Exception) {
                processApiException(e, "Get new courses")
            }
        }
    }

    /**
     * Get courses by author
     * @param authorId author identifier
     * @param limit maximum number of courses
     * @param token authorization token
     * @return list of courses result
     */
    override suspend fun getCoursesByAuthor(authorId: String, limit: Int, token: String?): DomainResult<List<CourseDomain>> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                client.safeSendWithErrorBody<NetworkCourseList, NetworkCourseErrorResponse>(
                    {
                        url("$apiUrl/courses")
                        method = HttpMethod.Get
                        // Add authorization header if token is provided
                        token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                        parameter("author_id", authorId)
                        parameter("limit", limit)

                        logger.d("Getting courses by author: $authorId, limit: $limit")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Get courses by author failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Author's courses retrieved successfully, count: ${(it.data.items.size)}")
                    }
                }.map { networkResponse ->
                    networkResponse.items.map { it.toDomain() }
                }
            } catch (e: Exception) {
                processApiException(e, "Get courses by author")
            }
        }
    }

    /**
     * Create a new course
     * @param course course data
     * @param token authorization token
     * @return the created course result
     */
    override suspend fun createCourse(course: CourseDomain, token: String): DomainResult<CourseDomain> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                // Проверяем, что токен не пустой
                if (token.isBlank()) {
                    return@executeWithRetry DomainResult.Error(
                        DomainError.authError(
                            message = "error_auth_required",
                            details = "Authorization token is required for course creation"
                        )
                    )
                }

                client.safeSendWithErrorBody<NetworkCourse, NetworkCourseErrorResponse>(
                    {
                        url("$apiUrl/courses")
                        method = HttpMethod.Post
                        contentType(ContentType.Application.Json)
                        setBody(course.toNetwork())
                        // Always add authorization header for this operation
                        header(HttpHeaders.Authorization, "Bearer $token")

                        logger.d("Creating new course with title: ${course.title.content}")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Create course failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Course created successfully with ID: ${it.data.id}")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Create course")
            }
        }
    }

    /**
     * Update a course
     * @param courseId course identifier
     * @param course updated course data
     * @param token authorization token
     * @return the updated course result
     */
    override suspend fun updateCourse(courseId: String, course: CourseDomain, token: String): DomainResult<CourseDomain> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                // Проверяем, что токен не пустой
                if (token.isBlank()) {
                    return@executeWithRetry DomainResult.Error(
                        DomainError.authError(
                            message = "error_auth_required",
                            details = "Authorization token is required for course update"
                        )
                    )
                }

                client.safeSendWithErrorBody<NetworkCourse, NetworkCourseErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId")
                        method = HttpMethod.Put
                        contentType(ContentType.Application.Json)
                        setBody(course.toNetwork())
                        // Always add authorization header for this operation
                        header(HttpHeaders.Authorization, "Bearer $token")

                        logger.d("Updating course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Update course failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Course updated successfully: $courseId")
                    }
                }.map { networkResponse ->
                    networkResponse.toDomain()
                }
            } catch (e: Exception) {
                processApiException(e, "Update course")
            }
        }
    }

    /**
     * Delete a course
     * @param courseId course identifier
     * @param token authorization token
     * @return operation result
     */
    override suspend fun deleteCourse(courseId: String, token: String): DomainResult<Unit> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                // Проверяем, что токен не пустой
                if (token.isBlank()) {
                    return@executeWithRetry DomainResult.Error(
                        DomainError.authError(
                            message = "error_auth_required",
                            details = "Authorization token is required for course deletion"
                        )
                    )
                }

                client.safeSendWithErrorBody<Unit, NetworkCourseErrorResponse>(
                    {
                        url("$apiUrl/courses/$courseId")
                        method = HttpMethod.Delete
                        // Always add authorization header for this operation
                        header(HttpHeaders.Authorization, "Bearer $token")

                        logger.d("Deleting course: $courseId")
                    },
                    logger,
                    { errorResponse ->
                        logger.w("Delete course failed: ${errorResponse.getErrorMessage()}")
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    }
                ).also {
                    if (it is DomainResult.Success) {
                        logger.i("Course deleted successfully: $courseId")
                    }
                }
            } catch (e: Exception) {
                processApiException(e, "Delete course")
            }
        }
    }
}
