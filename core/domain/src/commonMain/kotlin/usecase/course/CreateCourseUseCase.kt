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
        description: LocalizedContent
    ): DomainResult<CourseDomain> {
        // Validate input data
        val validationError = validateCourseData(title, description)
        if (validationError != null) {
            return DomainResult.Error(validationError)
        }

        // Create new course with basic parameters
        val currentTime = Clock.System.now().toString() // ISO-8601 format

        val newCourse = CourseDomain(
            id = "",
            title = title,
            description = description,
            authorId = "",
            createdAt = currentTime,
            updatedAt = currentTime,
            slug = ""
        )

        // Delegate execution to the repository
        return courseRepository.createCourse(newCourse)
    }

    /**
     * Validate course data before creation
     * @return DomainError if validation fails, null if data is valid
     */
    private fun validateCourseData(
        title: LocalizedContent,
        description: LocalizedContent
    ): DomainError? {
        return when {
            title.isEmpty() -> 
                DomainError.validationError("Course title cannot be empty")
            description.isEmpty() -> 
                DomainError.validationError("Course description cannot be empty")
            else -> null
        }
    }

    /**
     * Generate a slug based on the title or a random one if title is not usable
     */
    private fun generateSlug(title: LocalizedContent): String {
        // Try to generate from English title first, then any available title
        val titleText = title.getContent("en") ?: title.getPreferredString()
        
        return if (titleText.isNotBlank()) {
            titleText
                .lowercase()
                .replace(Regex("[^a-z0-9\\s]"), "")
                .replace(Regex("\\s+"), "-")
                .take(20) + "-" + generateRandomString(4)
        } else {
            generateRandomString(8)
        }
    }
    
    /**
     * Generate a random string of specified length
     */
    private fun generateRandomString(length: Int): String {
        val allowedChars = ('a'..'z') + ('0'..'9')
        return (1..length)
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
