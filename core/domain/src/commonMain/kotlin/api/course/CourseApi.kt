package api.course

import model.DomainResult
import model.course.CourseDomain
import model.course.CourseListDomain

/**
 * API for course operations
 * The course represents a structure/framework for educational content, not an active entity
 */
interface CourseApi {
    /**
     * Get a course by its identifier
     * @param courseId the course identifier
     * @return result containing course data or error
     */
    suspend fun getCourse(courseId: String): DomainResult<CourseDomain>

    /**
     * Get a list of courses with filtering and pagination
     * @param params query parameters as a map
     * @return result containing paginated list of courses or error
     */
    suspend fun getCourses(params: Map<String, Any?>): DomainResult<CourseListDomain>


    /**
     * Create a new course
     * @param course course data
     * @return result containing the created course or error
     */
    suspend fun createCourse(course: CourseDomain): DomainResult<CourseDomain>

    /**
     * Update a course
     * @param courseId course identifier
     * @param course updated course data
     * @return result containing the updated course or error
     */
    suspend fun updateCourse(courseId: String, course: CourseDomain): DomainResult<CourseDomain>

    /**
     * Delete a course
     * @param courseId course identifier
     * @return result containing success or error
     */
    suspend fun deleteCourse(courseId: String): DomainResult<Unit>
}
