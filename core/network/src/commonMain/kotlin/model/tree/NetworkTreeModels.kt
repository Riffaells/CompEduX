package model.tree

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Сетевая модель технологического дерева
 */
@Serializable
data class NetworkTechnologyTree(
    val id: String = "",
    val version: Int = 1,
    @SerialName("course_id")
    val courseId: String = "",
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = "",
    @SerialName("is_published")
    val isPublished: Boolean = false,
    // Поле data может содержать весь основной контент
    val data: NetworkTreeData? = null,
    // Поддержка альтернативного формата, где nodes и connections находятся на верхнем уровне
    val nodes: List<NetworkTreeNode> = emptyList(),
    val connections: List<NetworkTreeConnection> = emptyList(),
    val groups: List<NetworkTreeGroup> = emptyList(),
    val metadata: NetworkTreeMetadata = NetworkTreeMetadata()
)

/**
 * Модель для содержимого data в JSON ответе
 */
@Serializable
data class NetworkTreeData(
    // Узлы представлены как карта строк к объектам
    val nodes: Map<String, NetworkTreeNode> = emptyMap(),
    // Соединения представлены как список
    val connections: List<NetworkTreeConnection> = emptyList(),
    // Метаданные
    val metadata: NetworkTreeMetadata = NetworkTreeMetadata()
)

/**
 * Сетевая модель узла технологического дерева
 */
@Serializable
data class NetworkTreeNode(
    val id: String = "",
    val title: Map<String, String> = emptyMap(),
    val description: Map<String, String> = emptyMap(),
    val type: String = "TOPIC",
    val position: NetworkNodePosition = NetworkNodePosition(),
    val style: String? = null,
    @SerialName("content_id")
    val contentId: String? = null,
    val requirements: List<String> = emptyList(),
    val status: String = "available"
)

/**
 * Сетевая модель позиции узла
 */
@Serializable
data class NetworkNodePosition(
    val x: Float = 0f,
    val y: Float = 0f
)

/**
 * Сетевая модель связи между узлами
 */
@Serializable
data class NetworkTreeConnection(
    val id: String = "",
    val from: String = "",
    val to: String = "",
    val type: String = "required",
    val style: String? = null,
    val label: String? = null
)

/**
 * Сетевая модель группы узлов
 */
@Serializable
data class NetworkTreeGroup(
    val id: String = "",
    val name: Map<String, String> = emptyMap(),
    val nodes: List<String> = emptyList(),
    val style: String? = null
)

/**
 * Сетевая модель метаданных дерева
 */
@Serializable
data class NetworkTreeMetadata(
    val defaultLanguage: String = "en",
    val availableLanguages: List<String> = listOf("en"),
    val layoutType: String = "tree",
    val layoutDirection: String = "horizontal",
    val canvasSize: NetworkCanvasSize = NetworkCanvasSize()
)

/**
 * Сетевая модель размера холста
 */
@Serializable
data class NetworkCanvasSize(
    val width: Int = 800,
    val height: Int = 600
)
