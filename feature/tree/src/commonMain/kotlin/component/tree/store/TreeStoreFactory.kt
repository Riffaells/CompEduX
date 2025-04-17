package component.tree.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import logging.Logger
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Фабрика для создания TreeStore
 */
class TreeStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val logger by instance<Logger>()

    fun create(): TreeStore =
        object : TreeStore, Store<TreeStore.Intent, TreeStore.State, Nothing> by storeFactory.create(
            name = "TreeStore",
            initialState = TreeStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data class JsonParsed(val tree: TreeStore.TreeData) : Msg
        data class JsonInputUpdated(val jsonText: String) : Msg
        data class ErrorOccurred(val error: String) : Msg
        data class NodeSelected(val nodeId: String) : Msg
        data class ZoomLevelChanged(val zoomLevel: Float) : Msg
        data class PanPositionChanged(val panX: Float, val panY: Float) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<TreeStore.Intent, Unit, TreeStore.State, Msg, Nothing>(
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
                val tree = json.decodeFromString<TreeStore.TreeData>(jsonText)
                dispatch(Msg.JsonParsed(tree))
            } catch (e: Exception) {
                logger.e("Error parsing JSON: ${e.message}")
                dispatch(Msg.ErrorOccurred("Ошибка разбора JSON: ${e.message}"))
            }
        }

        override fun executeIntent(intent: TreeStore.Intent): Unit =
            try {
                when (intent) {
                    is TreeStore.Intent.Init -> {
                        // Инициализация уже выполнена в executeAction
                    }
                    is TreeStore.Intent.Back -> {
                        // Обработка в компоненте
                    }
                    is TreeStore.Intent.UpdateJsonInput -> {
                        dispatch(Msg.JsonInputUpdated(intent.jsonText))
                    }
                    is TreeStore.Intent.ParseJson -> {
                        parseJsonTree(intent.jsonText)
                    }
                    is TreeStore.Intent.NodeClicked -> {
                        dispatch(Msg.NodeSelected(intent.nodeId))
                    }
                    is TreeStore.Intent.ZoomChanged -> {
                        dispatch(Msg.ZoomLevelChanged(intent.zoomLevel))
                    }
                    is TreeStore.Intent.PanChanged -> {
                        dispatch(Msg.PanPositionChanged(intent.panX, intent.panY))
                    }
                }
            } catch (e: Exception) {
                logger.e("Error in executeIntent: ${e.message}")
                dispatch(Msg.ErrorOccurred("Ошибка: ${e.message}"))
            }
    }

    private object ReducerImpl : Reducer<TreeStore.State, Msg> {
        override fun TreeStore.State.reduce(msg: Msg): TreeStore.State =
            when (msg) {
                is Msg.LoadingData -> copy(isLoading = true, error = null)
                is Msg.JsonParsed -> copy(isLoading = false, parsedTree = msg.tree, error = null)
                is Msg.JsonInputUpdated -> copy(jsonInput = msg.jsonText)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false)
                is Msg.NodeSelected -> copy(selectedNodeId = msg.nodeId)
                is Msg.ZoomLevelChanged -> copy(zoomLevel = msg.zoomLevel)
                is Msg.PanPositionChanged -> copy(panX = msg.panX, panY = msg.panY)
            }
    }
}
