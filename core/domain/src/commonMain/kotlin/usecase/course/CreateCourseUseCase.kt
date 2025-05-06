package usecase.course

import kotlinx.datetime.Clock
import model.DomainError
import model.DomainResult
import model.course.CourseDomain
import model.course.LocalizedContent
import repository.course.CourseRepository
import kotlin.random.Random

/**
 * Use case to create a new course
 */
class CreateCourseUseCase(private val courseRepository: CourseRepository) {
    /**
     * Create a new course
     * @param title localized course title
     * @param description localized course description
     * @param authorId author identifier
     * @return operation result with the created course
     */
    suspend operator fun invoke(
        title: LocalizedContent,
        description: LocalizedContent,
        authorId: String
    ): DomainResult<CourseDomain> {
        // Validate input data
        if (title.content.isEmpty()) {
            return DomainResult.Error(DomainError.validationError("Course title cannot be empty"))
        }

        if (description.content.isEmpty()) {
            return DomainResult.Error(DomainError.validationError("Course description cannot be empty"))
        }

        if (authorId.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Author ID cannot be empty"))
        }

        // Create new course with basic parameters
        val currentTime = Clock.System.now()
        val timeString = currentTime.toString() // ISO-8601 format

        val newCourse = CourseDomain(
            id = generateUUID(),
            title = title,
            description = description,
            authorId = authorId,
            createdAt = timeString,
            updatedAt = timeString,
            slug = generateSlug()
        )

        // Delegate execution to the repository
        return courseRepository.createCourse(newCourse)
    }

    /**
     * Generate a random slug for the course
     * @return an 8-character random slug
     */
    private fun generateSlug(): String {
        val allowedChars = ('a'..'z') + ('0'..'9')
        return (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }

    /**
     * Generate a UUID-like string that works on all platforms
     * @return a UUID-like string
     */
    private fun generateUUID(): String {
        val random = Random.Default
        val hexChars = "0123456789abcdef"
        val uuid = StringBuilder()

        // Generate UUID format: 8-4-4-4-12 (hexadecimal digits)
        for (i in 0 until 36) {
            when (i) {
                8, 13, 18, 23 -> uuid.append('-')
                else -> uuid.append(hexChars[random.nextInt(hexChars.length)])
            }
        }

        return uuid.toString()
    }
}
