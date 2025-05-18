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
     * Get courses by author
     * @param authorId author identifier
     * @param page page number (1-based)
     * @param pageSize number of items per page
     * @return operation result with a paginated course list
     */
    suspend fun getCoursesByAuthor(
        authorId: String, 
        page: Int = 1, 
        pageSize: Int = 20
    ): DomainResult<CourseListDomain>
    
    /**
     * Get popular courses
     * @param limit maximum number of courses to return
     * @return operation result with a list of courses
     */
    suspend fun getPopularCourses(limit: Int = 10): DomainResult<List<CourseDomain>>

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
