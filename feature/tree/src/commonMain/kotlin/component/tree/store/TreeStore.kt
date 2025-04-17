package component.tree.store

import com.arkivanov.mvikotlin.core.store.Store
import kotlinx.serialization.Serializable

/**
 * Store для компонента дерева развития
 */
interface TreeStore : Store<TreeStore.Intent, TreeStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object Back : Intent
        data class UpdateJsonInput(val jsonText: String) : Intent
        data class ParseJson(val jsonText: String) : Intent
        data class NodeClicked(val nodeId: String) : Intent
        data class ZoomChanged(val zoomLevel: Float) : Intent
        data class PanChanged(val panX: Float, val panY: Float) : Intent
    }

    @Serializable
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val jsonInput: String = DEFAULT_JSON,
        val parsedTree: TreeData? = null,
        val selectedNodeId: String? = null,
        val zoomLevel: Float = 1.0f,
        val panX: Float = 0f,
        val panY: Float = 0f
    ) {
        companion object {
            val DEFAULT_JSON = """
            {
              "nodes": [
                {
                  "id": "node1",
                  "titleKey": "course.intro",
                  "descriptionKey": "course.intro.desc",
                  "position": {"x": 100, "y": 150},
                  "style": "circular",
                  "styleClass": "beginner",
                  "state": "AVAILABLE",
                  "difficulty": 1,
                  "estimatedTime": 30,
                  "children": ["node2", "node3"],
                  "contentId": "content123",
                  "requirements": [],
                  "achievements": ["ach1"]
                },
                {
                  "id": "node2",
                  "titleKey": "course.basics",
                  "descriptionKey": "course.basics.desc",
                  "position": {"x": 200, "y": 100},
                  "style": "hexagon",
                  "styleClass": "intermediate",
                  "state": "LOCKED",
                  "difficulty": 2,
                  "estimatedTime": 45,
                  "children": ["node4"],
                  "contentId": "content456",
                  "requirements": ["node1"],
                  "achievements": []
                },
                {
                  "id": "node3",
                  "titleKey": "course.advanced",
                  "descriptionKey": "course.advanced.desc",
                  "position": {"x": 200, "y": 200},
                  "style": "square",
                  "styleClass": "advanced",
                  "state": "LOCKED",
                  "difficulty": 3,
                  "estimatedTime": 60,
                  "children": [],
                  "contentId": "content789",
                  "requirements": ["node1"],
                  "achievements": ["ach2"]
                },
                {
                  "id": "node4",
                  "titleKey": "course.expert",
                  "descriptionKey": "course.expert.desc",
                  "position": {"x": 300, "y": 100},
                  "style": "circular",
                  "styleClass": "expert",
                  "state": "LOCKED",
                  "difficulty": 4,
                  "estimatedTime": 90,
                  "children": [],
                  "contentId": "content101112",
                  "requirements": ["node2"],
                  "achievements": ["ach3"]
                }
              ],
              "connections": [
                {
                  "id": "conn1",
                  "from": "node1",
                  "to": "node2",
                  "style": "solid_arrow",
                  "styleClass": "required",
                  "label": "Базовый курс"
                },
                {
                  "id": "conn2",
                  "from": "node1",
                  "to": "node3",
                  "style": "dashed_line",
                  "styleClass": "optional",
                  "label": "Продвинутый курс"
                },
                {
                  "id": "conn3",
                  "from": "node2",
                  "to": "node4",
                  "style": "solid_arrow",
                  "styleClass": "required",
                  "label": "Экспертный курс"
                }
              ]
            }
            """.trimIndent()

            // Словарь с локализацией для примера
            val TRANSLATIONS = mapOf(
                "course.intro" to "Введение",
                "course.basics" to "Основы",
                "course.advanced" to "Продвинутый",
                "course.expert" to "Эксперт"
            )
        }
    }

    @Serializable
    data class TreeData(
        val nodes: List<TreeNode>,
        val connections: List<TreeConnection>
    )

    @Serializable
    data class TreeNode(
        val id: String,
        val titleKey: String,
        val descriptionKey: String? = null,
        val position: Position,
        val style: String,
        val styleClass: String? = null,
        val state: String = "AVAILABLE",
        val difficulty: Int = 1,
        val estimatedTime: Int = 30,
        val children: List<String>,
        val contentId: String,
        val requirements: List<String>,
        val achievements: List<String> = emptyList()
    )

    @Serializable
    data class Position(
        val x: Int,
        val y: Int
    )

    @Serializable
    data class TreeConnection(
        val id: String? = null,
        val from: String,
        val to: String,
        val style: String,
        val styleClass: String? = null,
        val label: String? = null
    )
}
