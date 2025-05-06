package usecase.course

import model.DomainError
import model.DomainResult
import model.course.CourseListDomain
import model.course.CourseQueryParams
import repository.course.CourseRepository

/**
 * Use case to get a filtered list of courses
 */
class GetCoursesUseCase(private val courseRepository: CourseRepository) {
    /**
     * Get a list of courses with filtering and pagination
     * @param params query parameters
     * @return operation result with a paginated list of courses
     */
    suspend operator fun invoke(params: CourseQueryParams = CourseQueryParams()): DomainResult<CourseListDomain> {
        // Validate input data
        if (params.page < 0) {
            return DomainResult.Error(DomainError.validationError("Page number cannot be negative"))
        }

        if (params.size <= 0) {
            return DomainResult.Error(DomainError.validationError("Page size must be positive"))
        }

        return courseRepository.getCourses(params)
    }

    /**
     * Convenience method to search courses by text
     * @param searchText text to search for
     * @param page page number
     * @param size page size
     * @return operation result with a list of courses
     */
    suspend operator fun invoke(searchText: String, page: Int = 0, size: Int = 20): DomainResult<CourseListDomain> {
        val params = CourseQueryParams(
            search = searchText,
            page = page,
            size = size
        )
        return invoke(params)
    }

    /**
     * Convenience method to get courses by author
     * @param authorId author identifier
     * @param page page number
     * @param size page size
     * @return operation result with a list of courses
     */
    suspend fun getByAuthor(authorId: String, page: Int = 0, size: Int = 20): DomainResult<CourseListDomain> {
        val params = CourseQueryParams(
            authorId = authorId,
            page = page,
            size = size
        )
        return invoke(params)
    }
}
