package model.course

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network model for a course
 */
@Serializable
data class NetworkCourse(
    val id: String = "",
    val title: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    @SerialName("author_id") val authorId: String = "",
    val tags: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = "",
    val slug: String? = null,
    val visibility: String = "PRIVATE",
    @SerialName("is_published") val isPublished: Boolean = false,
    @SerialName("organization_id") val organizationId: String? = null,
    @SerialName("technology_tree") val technologyTree: String? = null
)

/**
 * Network model for course list with pagination
 */
@Serializable
data class NetworkCourseList(
    val items: List<NetworkCourse> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val size: Int = 20,
    val pages: Int = 0
)

/**
 * Network model for course creation request
 */
@Serializable
data class NetworkCreateCourseRequest(
    val title: Map<String, String>,
    val description: Map<String, String>,
    @SerialName("author_id") val authorId: String? = null,
    val visibility: String? = null,
    @SerialName("organization_id") val organizationId: String? = null,
    val tags: List<String>? = null
)

/**
 * Network model for course update request
 */
@Serializable
data class NetworkUpdateCourseRequest(
    val title: Map<String, String>? = null,
    val description: Map<String, String>? = null,
    val visibility: String? = null,
    @SerialName("organization_id") val organizationId: String? = null,
    val tags: List<String>? = null,
    @SerialName("is_published") val isPublished: Boolean? = null
)

/**
 * Network response for course errors
 */
@Serializable
data class NetworkCourseErrorResponse(
    val status: Int = 400,
    val message: String = "Unknown error",
    val error: String? = null,
    val details: String? = null,
    val path: String? = null,
    val timestamp: Long = 0
) {
    fun getErrorCode(): Int = status
    fun getErrorMessage(): String = message
}
