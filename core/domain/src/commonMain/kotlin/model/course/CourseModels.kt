package model.course

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.tree.TechnologyTreeDomain

/**
 * Domain model for a course
 * The course represents a structure/framework for educational content, not an active entity
 */
@Serializable
data class CourseDomain(
    val id: String,
    val title: LocalizedContent,
    val description: LocalizedContent,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("author_id")
    val authorId: String,
    @SerialName("author_name")
    val authorName: String = "",
    val tags: List<String> = emptyList(),
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    val status: CourseStatusDomain = CourseStatusDomain.DRAFT,
    val slug: String? = null,
    val visibility: CourseVisibilityDomain = CourseVisibilityDomain.PRIVATE,
    @SerialName("is_published")
    val isPublished: Boolean = false,
    @SerialName("organization_id")
    val organizationId: String? = null,
    @SerialName("technology_tree")
    val technologyTree: TechnologyTreeDomain? = null,
    val modules: List<CourseModuleDomain> = emptyList()
) {
    /**
     * Parse created_at timestamp string to Long
     */
    fun getCreatedAtMillis(): Long {
        return try {
            if (createdAt.isNotBlank()) {
                Instant.parse(createdAt).toEpochMilliseconds()
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Parse updated_at timestamp string to Long
     */
    fun getUpdatedAtMillis(): Long {
        return try {
            if (updatedAt.isNotBlank()) {
                Instant.parse(updatedAt).toEpochMilliseconds()
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Course visibility options
 */
@Serializable
enum class CourseVisibilityDomain {
    PRIVATE,   // Only visible to the author and co-authors
    UNLISTED,  // Accessible via link but not visible in public listings
    PUBLIC     // Available to everyone and visible in public listings
}

/**
 * Domain model for a module within a course
 */
@Serializable
data class CourseModuleDomain(
    val id: String,
    @SerialName("course_id")
    val courseId: String,
    val title: LocalizedContent,
    val description: LocalizedContent? = null,
    val order: Int,
    val lessons: List<CourseLessonDomain> = emptyList()
)

/**
 * Domain model for a lesson within a course module
 */
@Serializable
data class CourseLessonDomain(
    val id: String,
    @SerialName("module_id")
    val moduleId: String,
    val title: LocalizedContent,
    val description: LocalizedContent? = null,
    val order: Int,
    @SerialName("content_type")
    val contentType: LessonContentTypeDomain,
    val duration: Int? = null,
    val resources: List<LessonResourceDomain> = emptyList()
)

/**
 * Domain model for a resource attached to a lesson
 */
@Serializable
data class LessonResourceDomain(
    val id: String,
    @SerialName("lesson_id")
    val lessonId: String,
    val title: LocalizedContent,
    val description: LocalizedContent? = null,
    val type: ResourceTypeDomain,
    val url: String,
    val order: Int
)

/**
 * Status of a course
 */
@Serializable
enum class CourseStatusDomain {
    DRAFT,
    PUBLISHED,
    ARCHIVED,
    UNDER_REVIEW
}

/**
 * Type of lesson content
 */
@Serializable
enum class LessonContentTypeDomain {
    VIDEO,
    ARTICLE,
    QUIZ,
    CODE_EXERCISE,
    INTERACTIVE
}

/**
 * Type of lesson resource
 */
@Serializable
enum class ResourceTypeDomain {
    DOCUMENT,
    VIDEO,
    LINK,
    CODE_SAMPLE,
    ASSIGNMENT
}

/**
 * Course sorting options
 */
enum class CourseSortOptionDomain {
    POPULARITY,
    RATING,
    DATE_CREATED,
    DATE_UPDATED
}

/**
 * Sort direction
 */
enum class SortDirectionDomain {
    ASC,
    DESC
}
