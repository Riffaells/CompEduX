package model.tree

import kotlinx.serialization.Serializable

/**
 * Данные дерева разработки
 */
@Serializable
data class TreeData(
    val id: String,
    val name: String,
    val description: String? = null,
    val children: List<TreeData> = emptyList(),
    val completed: Boolean = false,
    val progress: Float = 0f,
    val type: NodeType = NodeType.TOPIC
)

/**
 * Тип узла дерева
 */
@Serializable
enum class NodeType {
    ROOT,      // Корневой узел
    TOPIC,     // Тема
    SUBTOPIC,  // Подтема
    LESSON,    // Урок
    EXERCISE,  // Упражнение
    TEST       // Тест
}
