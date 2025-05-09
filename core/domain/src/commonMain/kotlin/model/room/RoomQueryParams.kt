package model.room

/**
 * Query parameters for filtering and sorting rooms
 */
data class RoomQueryParams(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null,
    val language: String? = null,
    val sortBy: String? = null,
    val sortOrder: String? = null,
    val id: String? = null,
    val courseId: String? = null,
    val ownerId: String? = null,
    val status: String? = null,
    val code: String? = null
) {
    /**
     * Convert parameters to a map for API requests
     */
    fun toMap(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()

        // Always include pagination parameters
        map["page"] = page
        map["size"] = size

        // Add optional parameters only if they are not null
        search?.let { map["search"] = it }
        language?.let { map["language"] = it }
        sortBy?.let { map["sort_by"] = it }
        sortOrder?.let { map["sort_order"] = it }
        id?.let { map["id"] = it }
        courseId?.let { map["course_id"] = it }
        ownerId?.let { map["owner_id"] = it }
        status?.let { map["status"] = it }
        code?.let { map["code"] = it }

        return map
    }
}
