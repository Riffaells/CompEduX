package api.course

import base.BaseNetworkApi
import client.safeSendWithErrorBody
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import logging.Logger
import mapper.toDomain
import mapper.toNetwork
import model.DomainError
import model.DomainResult
import model.course.*

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
     * @return course data result
     */
    override suspend fun getCourse(courseId: String): DomainResult<CourseDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkCourse, NetworkCourseErrorResponse>(
            {
                url("$apiUrl/courses/$courseId")
                method = HttpMethod.Get

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
    }

    /**
     * Get a list of courses with filtering and pagination
     * @param params query parameters as a map
     * @return paginated list of courses result
     */
    override suspend fun getCourses(params: Map<String, Any?>): DomainResult<CourseListDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkCourseList, NetworkCourseErrorResponse>(
            {
                url("$apiUrl/courses")
                method = HttpMethod.Get

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
    }

    /**
     * Get a list of popular courses
     * @param limit maximum number of courses
     * @return list of courses result
     */
    override suspend fun getPopularCourses(limit: Int): DomainResult<List<CourseDomain>> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkCourseList, NetworkCourseErrorResponse>(
            {
                url("$apiUrl/courses")
                method = HttpMethod.Get
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
    }

    /**
     * Get a list of new courses
     * @param limit maximum number of courses
     * @return list of courses result
     */
    override suspend fun getNewCourses(limit: Int): DomainResult<List<CourseDomain>> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkCourseList, NetworkCourseErrorResponse>(
            {
                url("$apiUrl/courses")
                method = HttpMethod.Get
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
    }

    /**
     * Get courses by author
     * @param authorId author identifier
     * @param limit maximum number of courses
     * @return list of courses result
     */
    override suspend fun getCoursesByAuthor(authorId: String, limit: Int): DomainResult<List<CourseDomain>> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkCourseList, NetworkCourseErrorResponse>(
            {
                url("$apiUrl/courses")
                method = HttpMethod.Get
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
    }

    /**
     * Create a new course
     * @param course course data
     * @return the created course result
     */
    override suspend fun createCourse(course: CourseDomain): DomainResult<CourseDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkCourse, NetworkCourseErrorResponse>(
            {
                url("$apiUrl/courses")
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                setBody(course.toNetwork())

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
    }

    /**
     * Update a course
     * @param courseId course identifier
     * @param course updated course data
     * @return the updated course result
     */
    override suspend fun updateCourse(courseId: String, course: CourseDomain): DomainResult<CourseDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkCourse, NetworkCourseErrorResponse>(
            {
                url("$apiUrl/courses/$courseId")
                method = HttpMethod.Put
                contentType(ContentType.Application.Json)
                setBody(course.toNetwork())

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
    }

    /**
     * Delete a course
     * @param courseId course identifier
     * @return operation result
     */
    override suspend fun deleteCourse(courseId: String): DomainResult<Unit> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<Unit, NetworkCourseErrorResponse>(
            {
                url("$apiUrl/courses/$courseId")
                method = HttpMethod.Delete

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
    }
}
