package usecase.course

import model.DomainError
import model.DomainResult
import model.course.CourseDomain
import repository.course.CourseRepository

/**
 * Use case to update a course
 */
class UpdateCourseUseCase(private val courseRepository: CourseRepository) {
    /**
     * Update a course
     * @param courseId the course identifier
     * @param course updated course data
     * @return operation result with the updated course
     */
    suspend operator fun invoke(courseId: String, course: CourseDomain): DomainResult<CourseDomain> {
        // Validate input data
        if (courseId.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Course ID cannot be empty"))
        }

        // Check if the course exists
        val existingCourse = courseRepository.getCourse(courseId)
        if (existingCourse is DomainResult.Error) {
            return existingCourse
        }

        // Update the course while preserving unchanged fields
        return courseRepository.updateCourse(courseId, course)
    }
}
