package model.tree

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.course.LocalizedContent


/**
 * Domain model for technology tree
 */
@Serializable
data class TechnologyTreeDomain(
    val id: String,
    val version: Int,
    @SerialName("course_id")
    val courseId: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val nodes: List<TreeNodeDomain>,
    val connections: List<TreeConnectionDomain>,
    val groups: List<TreeGroupDomain>,
    val metadata: TreeMetadataDomain
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
 * Domain model for a tree node
 */
@Serializable
data class TreeNodeDomain(
    val id: String,
    val title: LocalizedContent,
    val description: LocalizedContent,
    val type: TreeNodeTypeDomain,
    val position: NodePositionDomain,
    val style: String? = null,
    @SerialName("content_id")
    val contentId: String? = null,
    val requirements: List<String> = emptyList(),
    val state: NodeStateDomain = NodeStateDomain.AVAILABLE,
    val difficulty: Int = 1,
    @SerialName("estimated_time")
    val estimatedTime: Int = 0
)

/**
 * Domain model for a node position
 */
@Serializable
data class NodePositionDomain(
    val x: Float,
    val y: Float
)

/**
 * Domain model for a tree connection
 */
@Serializable
data class TreeConnectionDomain(
    val id: String,
    val from: String,
    val to: String,
    val type: ConnectionTypeDomain,
    val style: String? = null,
    val label: String? = null
)

/**
 * Domain model for a tree group
 */
@Serializable
data class TreeGroupDomain(
    val id: String,
    val name: LocalizedContent,
    val nodes: List<String>,
    val style: String? = null
)

/**
 * Domain model for tree metadata
 */
@Serializable
data class TreeMetadataDomain(
    @SerialName("default_language")
    val defaultLanguage: String,
    @SerialName("available_languages")
    val availableLanguages: List<String>,
    @SerialName("layout_type")
    val layoutType: TreeLayoutTypeDomain,
    @SerialName("layout_direction")
    val layoutDirection: TreeLayoutDirectionDomain,
    @SerialName("canvas_size")
    val canvasSize: CanvasSizeDomain
)

/**
 * Domain model for canvas size
 */
@Serializable
data class CanvasSizeDomain(
    val width: Int,
    val height: Int
)

/**
 * Type of tree node
 */
@Serializable
enum class TreeNodeTypeDomain {
    TOPIC,
    SKILL,
    MODULE,
    ARTICLE
}

/**
 * State of a node
 */
@Serializable
enum class NodeStateDomain {
    AVAILABLE,
    LOCKED,
    COMPLETED,
    IN_PROGRESS
}

/**
 * Type of connection
 */
@Serializable
enum class ConnectionTypeDomain {
    REQUIRED,
    RECOMMENDED,
    OPTIONAL
}

/**
 * Type of tree layout
 */
@Serializable
enum class TreeLayoutTypeDomain {
    TREE,
    MESH,
    RADIAL
}

/**
 * Direction of tree layout
 */
@Serializable
enum class TreeLayoutDirectionDomain {
    HORIZONTAL,
    VERTICAL
}
