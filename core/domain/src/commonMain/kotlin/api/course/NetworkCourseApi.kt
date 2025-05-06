package api.course

import model.DomainResult
import model.course.CourseDomain
import model.course.CourseListDomain

/**
 * Network API for course operations
 */
interface NetworkCourseApi {
    /**
     * Get a course by its identifier
     * @param courseId the course identifier
     * @return course data result
     */
    suspend fun getCourse(courseId: String): DomainResult<CourseDomain>

    /**
     * Get a list of courses with filtering and pagination
     * @param params query parameters as a map
     * @return paginated list of courses result
     */
    suspend fun getCourses(params: Map<String, Any?>): DomainResult<CourseListDomain>

    /**
     * Get a list of popular courses
     * @param limit maximum number of courses
     * @return list of courses result
     */
    suspend fun getPopularCourses(limit: Int = 10): DomainResult<List<CourseDomain>>

    /**
     * Get a list of new courses
     * @param limit maximum number of courses
     * @return list of courses result
     */
    suspend fun getNewCourses(limit: Int = 10): DomainResult<List<CourseDomain>>

    /**
     * Get courses by author
     * @param authorId author identifier
     * @param limit maximum number of courses
     * @return list of courses result
     */
    suspend fun getCoursesByAuthor(authorId: String, limit: Int = 20): DomainResult<List<CourseDomain>>

    /**
     * Create a new course
     * @param course course data
     * @return the created course result
     */
    suspend fun createCourse(course: CourseDomain): DomainResult<CourseDomain>

    /**
     * Update a course
     * @param courseId course identifier
     * @param course updated course data
     * @return the updated course result
     */
    suspend fun updateCourse(courseId: String, course: CourseDomain): DomainResult<CourseDomain>

    /**
     * Delete a course
     * @param courseId course identifier
     * @return operation result
     */
    suspend fun deleteCourse(courseId: String): DomainResult<Unit>
}