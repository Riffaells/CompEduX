package model.tree

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Структура дерева технологий для курса
 * В будущем будет расширена для визуализации дерева развития
 */
@Serializable
data class TechnologyTreeDomain(
    val id: String,
    val name: String,
    val description: String? = null,

    @SerialName("course_id")
    val courseId: String? = null,

    val nodes: List<TreeNodeDomain> = emptyList(),
    val connections: List<TreeConnectionDomain> = emptyList(),

    @SerialName("display_settings")
    val displaySettings: TreeDisplaySettingsDomain? = null
)

/**
 * Узел дерева технологий
 */
@Serializable
data class TreeNodeDomain(
    val id: String,

    @SerialName("title_key")
    val titleKey: String,

    @SerialName("description_key")
    val descriptionKey: String,

    val position: PositionDomain,
    val style: String = "",

    @SerialName("style_class")
    val styleClass: String = "",

    val state: NodeStateDomain = NodeStateDomain.LOCKED,
    val difficulty: Int = 1,

    @SerialName("estimated_time")
    val estimatedTime: Int = 0,

    val children: List<String> = emptyList(),

    @SerialName("content_id")
    val contentId: String? = null,

    val requirements: List<String> = emptyList(),
    val achievements: List<String> = emptyList()
)

/**
 * Позиция узла в дереве
 */
@Serializable
data class PositionDomain(
    val x: Float,
    val y: Float
)

/**
 * Соединение между узлами дерева
 */
@Serializable
data class TreeConnectionDomain(
    val id: String,
    val from: String,
    val to: String,
    val style: String = "default",

    @SerialName("style_class")
    val styleClass: String = "default",

    val label: String? = null
)

/**
 * Состояние узла
 */
@Serializable
enum class NodeStateDomain {
    AVAILABLE,
    LOCKED,
    COMPLETED,
    IN_PROGRESS
}

/**
 * Настройки отображения дерева
 */
@Serializable
data class TreeDisplaySettingsDomain(
    val theme: String = "default",

    @SerialName("default_scale")
    val defaultScale: Float = 1.0f,

    @SerialName("grid_size")
    val gridSize: Int = 50,

    val background: String = "grid"
) 