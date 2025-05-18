package api.course

import model.DomainResult
import model.course.CourseDomain
import model.course.CourseListDomain

/**
 * Network API interface for course-related operations
 */
interface NetworkCourseApi {
    /**
     * Get a course by its identifier
     * @param courseId the course identifier
     * @param token authorization token
     * @return course data result
     */
    suspend fun getCourse(courseId: String, token: String? = null): DomainResult<CourseDomain>

    /**
     * Get a list of courses with filtering and pagination
     * @param params query parameters as a map
     * @param token authorization token
     * @return paginated list of courses result
     */
    suspend fun getCourses(params: Map<String, Any?>, token: String? = null): DomainResult<CourseListDomain>

    /**
     * Get a list of popular courses
     * @param limit maximum number of courses
     * @param token authorization token
     * @return list of courses result
     */
    suspend fun getPopularCourses(limit: Int, token: String? = null): DomainResult<List<CourseDomain>>

    /**
     * Get a list of new courses
     * @param limit maximum number of courses
     * @param token authorization token
     * @return list of courses result
     */
    suspend fun getNewCourses(limit: Int, token: String? = null): DomainResult<List<CourseDomain>>

    /**
     * Get courses by author
     * @param authorId author identifier
     * @param limit maximum number of courses
     * @param token authorization token
     * @return list of courses result
     */
    suspend fun getCoursesByAuthor(authorId: String, limit: Int, token: String? = null): DomainResult<List<CourseDomain>>

    /**
     * Create a new course
     * @param course course data
     * @param token authorization token
     * @return the created course result
     */
    suspend fun createCourse(course: CourseDomain, token: String): DomainResult<CourseDomain>

    /**
     * Update a course
     * @param courseId course identifier
     * @param course updated course data
     * @param token authorization token
     * @return the updated course result
     */
    suspend fun updateCourse(courseId: String, course: CourseDomain, token: String): DomainResult<CourseDomain>

    /**
     * Delete a course
     * @param courseId course identifier
     * @param token authorization token
     * @return operation result
     */
    suspend fun deleteCourse(courseId: String, token: String): DomainResult<Unit>
}