package repository.course

import model.DomainResult
import model.course.CourseDomain
import model.course.CourseListDomain
import model.course.CourseQueryParams

/**
 * Repository for course operations
 * The course represents a structure/framework for educational content, not an active entity
 */
interface CourseRepository {
    /**
     * Get a course by its identifier
     * @param courseId the course identifier
     * @return operation result with course data
     */
    suspend fun getCourse(courseId: String): DomainResult<CourseDomain>

    /**
     * Get a list of courses with filtering and pagination
     * @param params query parameters
     * @return operation result with a paginated course list
     */
    suspend fun getCourses(params: CourseQueryParams = CourseQueryParams()): DomainResult<CourseListDomain>

    /**
     * Create a new course
     * @param course course data
     * @return operation result with the created course
     */
    suspend fun createCourse(course: CourseDomain): DomainResult<CourseDomain>

    /**
     * Update a course
     * @param courseId the course identifier
     * @param course updated course data
     * @return operation result with the updated course
     */
    suspend fun updateCourse(courseId: String, course: CourseDomain): DomainResult<CourseDomain>

    /**
     * Delete a course
     * @param courseId the course identifier
     * @return operation result
     */
    suspend fun deleteCourse(courseId: String): DomainResult<Unit>
}
