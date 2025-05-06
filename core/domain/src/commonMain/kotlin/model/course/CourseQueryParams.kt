package model.course

/**
 * Query parameters for filtering and sorting courses
 */
data class CourseQueryParams(
    val page: Int = 0,
    val size: Int = 20,
    val search: String? = null,
    val language: String? = null,
    val sortBy: String? = null,
    val sortOrder: String? = null,
    val slug: String? = null,
    val id: String? = null,
    val fromDate: String? = null,
    val toDate: String? = null,
    val tagIds: List<String> = emptyList(),
    val authorId: String? = null,
    val visibility: String? = null
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
        slug?.let { map["slug"] = it }
        id?.let { map["id"] = it }
        fromDate?.let { map["from_date"] = it }
        toDate?.let { map["to_date"] = it }
        authorId?.let { map["author_id"] = it }
        visibility?.let { map["visibility"] = it }

        // Add collections only if they are not empty
        if (tagIds.isNotEmpty()) {
            map["tag_ids"] = tagIds
        }

        return map
    }
}
