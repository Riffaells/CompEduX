package mapper

import model.course.*

/**
 * Extension functions for mapping between network and domain course models
 */

// Network to domain mappings
fun NetworkCourse.toDomain(): CourseDomain {
    return CourseDomain(
        id = id,
        title = LocalizedContent(title),
        description = LocalizedContent(description),
        imageUrl = imageUrl,
        authorId = authorId,
        authorName = "",
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
        status = CourseStatusDomain.DRAFT,
        slug = slug,
        visibility = parseVisibility(visibility),
        isPublished = isPublished,
        organizationId = organizationId,
    )
}

fun NetworkCourseList.toDomain(): CourseListDomain {
    return CourseListDomain(
        items = items.map { it.toDomain() },
        total = total,
        page = page,
        size = size,
        pages = pages
    )
}

// Domain to network mappings
fun CourseDomain.toNetwork(): NetworkCourse {
    return NetworkCourse(
        id = id,
        title = title.content,
        description = description.content,
        authorId = authorId,
        tags = tags,
        createdAt = createdAt,
        updatedAt = updatedAt,
        slug = slug,
        visibility = visibility.toString(),
        isPublished = isPublished,
        organizationId = organizationId,
        imageUrl = imageUrl
    )
}

// Helper functions for parsing enums
private fun parseVisibility(visibility: String): CourseVisibilityDomain {
    return when (visibility.uppercase()) {
        "PUBLIC" -> CourseVisibilityDomain.PUBLIC
        "UNLISTED" -> CourseVisibilityDomain.UNLISTED
        else -> CourseVisibilityDomain.PRIVATE
    }
}

// Data classes for request parameters
data class CreateCourseRequest(
    val title: LocalizedContent,
    val description: LocalizedContent,
    val authorId: String? = null,
    val visibility: CourseVisibilityDomain? = null,
    val organizationId: String? = null,
    val tags: List<String>? = null,
    val imageUrl: String? = null
)

fun CreateCourseRequest.toNetwork(): NetworkCreateCourseRequest {
    return NetworkCreateCourseRequest(
        title = title.content,
        description = description.content,
        authorId = authorId,
        visibility = visibility?.toString(),
        organizationId = organizationId,
        tags = tags,
        imageUrl = imageUrl
    )
}

data class UpdateCourseRequest(
    val title: LocalizedContent? = null,
    val description: LocalizedContent? = null,
    val visibility: CourseVisibilityDomain? = null,
    val organizationId: String? = null,
    val tags: List<String>? = null,
    val isPublished: Boolean? = null,
    val imageUrl: String? = null
)

fun UpdateCourseRequest.toNetwork(): NetworkUpdateCourseRequest {
    return NetworkUpdateCourseRequest(
        title = title?.content,
        description = description?.content,
        visibility = visibility?.toString(),
        organizationId = organizationId,
        tags = tags,
        isPublished = isPublished,
        imageUrl = imageUrl
    )
}
