package usecase.course

import model.DomainError
import model.DomainResult
import model.course.CourseDomain
import repository.course.CourseRepository

/**
 * Use case to get a course by its identifier
 */
class GetCourseUseCase(
    private val courseRepository: CourseRepository
) {

    /**
     * Get a course by its identifier
     * @param courseId the course identifier
     * @return operation result with course data
     */
    suspend operator fun invoke(courseId: String): DomainResult<CourseDomain> {
        if (courseId.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Course ID cannot be blank"))
        }

        return courseRepository.getCourse(courseId)
    }
}
