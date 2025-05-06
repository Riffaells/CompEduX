package usecase.course

import model.DomainError
import model.DomainResult
import repository.course.CourseRepository

/**
 * Use case to delete a course
 */
class DeleteCourseUseCase(private val courseRepository: CourseRepository) {
    /**
     * Delete a course
     * @param courseId the course identifier
     * @return operation result
     */
    suspend operator fun invoke(courseId: String): DomainResult<Unit> {
        // Validate input data
        if (courseId.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Course ID cannot be empty"))
        }

        // Check if the course exists
        val existingCourse = courseRepository.getCourse(courseId)
        if (existingCourse is DomainResult.Error) {
            return existingCourse.map { }
        }

        // Delete the course
        return courseRepository.deleteCourse(courseId)
    }
}
