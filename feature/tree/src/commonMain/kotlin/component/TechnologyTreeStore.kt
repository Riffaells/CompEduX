package component

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import logging.Logger
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface TechnologyTreeStore : Store<TechnologyTreeStore.Intent, TechnologyTreeStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object Back : Intent
        data class UpdateJsonInput(val jsonText: String) : Intent
        data class ParseJson(val jsonText: String) : Intent
        data class NodeClicked(val nodeId: String) : Intent
        data class NodeMoved(val nodeId: String, val newPosition: Position) : Intent
    }

    @Serializable
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val jsonInput: String = DEFAULT_JSON,
        val parsedTree: TreeData? = null,
        val selectedNodeId: String? = null
    ) {
        companion object {
            val DEFAULT_JSON = """
            {
              "nodes": [
                {
                  "id": "node1",
                  "titleKey": "course.intro",
                  "position": {"x": 100, "y": 150},
                  "style": "circular",
                  "children": ["node2", "node3"],
                  "contentId": "content123",
                  "requirements": []
                },
                {
                  "id": "node2",
                  "titleKey": "course.basics",
                  "position": {"x": 200, "y": 100},
                  "style": "hexagon",
                  "children": ["node4"],
                  "contentId": "content456",
                  "requirements": ["node1"]
                },
                {
                  "id": "node3",
                  "titleKey": "course.advanced",
                  "position": {"x": 200, "y": 200},
                  "style": "square",
                  "children": [],
                  "contentId": "content789",
                  "requirements": ["node1"]
                },
                {
                  "id": "node4",
                  "titleKey": "course.expert",
                  "position": {"x": 300, "y": 100},
                  "style": "circular",
                  "children": [],
                  "contentId": "content101112",
                  "requirements": ["node2"]
                }
              ],
              "connections": [
                {
                  "from": "node1",
                  "to": "node2",
                  "style": "solid_arrow"
                },
                {
                  "from": "node1",
                  "to": "node3",
                  "style": "dashed_line"
                },
                {
                  "from": "node2",
                  "to": "node4",
                  "style": "solid_arrow"
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
        val position: Position,
        val style: String,
        val children: List<String>,
        val contentId: String,
        val requirements: List<String>
    )

    @Serializable
    data class Position(
        val x: Int,
        val y: Int
    )

    @Serializable
    data class TreeConnection(
        val from: String,
        val to: String,
        val style: String
    )
}

internal class TechnologyTreeStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val logger by instance<Logger>()

    fun create(): TechnologyTreeStore =
        object : TechnologyTreeStore,
            Store<TechnologyTreeStore.Intent, TechnologyTreeStore.State, Nothing> by storeFactory.create(
                name = "SkikoStore",
                initialState = TechnologyTreeStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl
            ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data class JsonParsed(val tree: TechnologyTreeStore.TreeData) : Msg
        data class JsonInputUpdated(val jsonText: String) : Msg
        data class ErrorOccurred(val error: String) : Msg
        data class NodeSelected(val nodeId: String) : Msg
        data class NodePositionUpdated(val nodeId: String, val newPosition: TechnologyTreeStore.Position) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<TechnologyTreeStore.Intent, Unit, TechnologyTreeStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            scope.launch {
                // Парсим JSON по умолчанию при старте
                parseJsonTree(state().jsonInput)
            }
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                logger.e("Error in dispatch: ${e.message}")
                dispatch(Msg.ErrorOccurred("Ошибка: ${e.message}"))
            }
        }

        private fun parseJsonTree(jsonText: String) {
            try {
                dispatch(Msg.LoadingData)

                val json = Json { ignoreUnknownKeys = true }
                val tree = json.decodeFromString<TechnologyTreeStore.TreeData>(jsonText)
                dispatch(Msg.JsonParsed(tree))
            } catch (e: Exception) {
                logger.e("Error parsing JSON: ${e.message}")
                dispatch(Msg.ErrorOccurred("Ошибка разбора JSON: ${e.message}"))
            }
        }

        override fun executeIntent(intent: TechnologyTreeStore.Intent): Unit =
            try {
                when (intent) {
                    is TechnologyTreeStore.Intent.Init -> {
                        // Инициализация уже выполнена в executeAction
                    }

                    is TechnologyTreeStore.Intent.Back -> {
                        // Обработка в компоненте
                    }

                    is TechnologyTreeStore.Intent.UpdateJsonInput -> {
                        dispatch(Msg.JsonInputUpdated(intent.jsonText))
                    }

                    is TechnologyTreeStore.Intent.ParseJson -> {
                        parseJsonTree(intent.jsonText)
                    }

                    is TechnologyTreeStore.Intent.NodeClicked -> {
                        dispatch(Msg.NodeSelected(intent.nodeId))
                    }

                    is TechnologyTreeStore.Intent.NodeMoved -> {
                        dispatch(Msg.NodePositionUpdated(intent.nodeId, intent.newPosition))
                    }
                }
            } catch (e: Exception) {
                logger.e("Error in executeIntent: ${e.message}")
                dispatch(Msg.ErrorOccurred("Ошибка: ${e.message}"))
            }
    }

    private object ReducerImpl : Reducer<TechnologyTreeStore.State, Msg> {
        override fun TechnologyTreeStore.State.reduce(msg: Msg): TechnologyTreeStore.State =
            when (msg) {
                is Msg.LoadingData -> copy(isLoading = true, error = null)
                is Msg.JsonParsed -> copy(isLoading = false, parsedTree = msg.tree, error = null)
                is Msg.JsonInputUpdated -> copy(jsonInput = msg.jsonText)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false)
                is Msg.NodeSelected -> copy(selectedNodeId = msg.nodeId)
                is Msg.NodePositionUpdated -> {
                    // Обновляем позицию узла в дереве
                    parsedTree?.let { currentTree ->
                        val updatedNodes = currentTree.nodes.map { node ->
                            if (node.id == msg.nodeId) {
                                node.copy(position = msg.newPosition)
                            } else {
                                node
                            }
                        }
                        val updatedTree = currentTree.copy(nodes = updatedNodes)

                        // Обновляем JSON-представление
                        val updatedJson = try {
                            val json = Json {
                                prettyPrint = true
                                ignoreUnknownKeys = true
                            }
                            json.encodeToString(updatedTree)
                        } catch (e: Exception) {
                            jsonInput
                        }

                        copy(parsedTree = updatedTree, jsonInput = updatedJson)
                    } ?: this
                }
            }
    }
}
