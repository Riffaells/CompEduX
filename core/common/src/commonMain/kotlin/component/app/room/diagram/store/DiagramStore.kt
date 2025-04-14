package component.app.room.diagram.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import utils.rDispatchers

interface DiagramStore : Store<DiagramStore.Intent, DiagramStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data class UpdateDiagramType(val type: DiagramType) : Intent
        data class UpdateDiagramData(val data: List<DataPoint>) : Intent
    }

    @Serializable
    data class State(
        val diagramType: DiagramType = DiagramType.BAR_CHART,
        val diagramData: List<DataPoint> = emptyList(),
        val loading: Boolean = false
    )

    @Serializable
    enum class DiagramType {
        BAR_CHART,
        PIE_CHART,
        LINE_CHART,
        SCATTER_PLOT
    }

    @Serializable
    data class DataPoint(
        val label: String,
        val value: Double
    )
}

internal class DiagramStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val logger by instance<Logger>("DiagramStore")

    fun create(): DiagramStore =
        object : DiagramStore, Store<DiagramStore.Intent, DiagramStore.State, Nothing> by storeFactory.create(
            name = "DiagramStore",
            initialState = DiagramStore.State(
                diagramData = generateSampleData()
            ),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private fun generateSampleData(): List<DiagramStore.DataPoint> {
        return listOf(
            DiagramStore.DataPoint("Jan", 10.0),
            DiagramStore.DataPoint("Feb", 15.0),
            DiagramStore.DataPoint("Mar", 8.0),
            DiagramStore.DataPoint("Apr", 12.0),
            DiagramStore.DataPoint("May", 20.0),
            DiagramStore.DataPoint("Jun", 17.0)
        )
    }

    private sealed interface Msg {
        data object LoadingData : Msg
        data object LoadData : Msg
        data class UpdateDiagramType(val type: DiagramStore.DiagramType) : Msg
        data class UpdateDiagramData(val data: List<DiagramStore.DataPoint>) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<DiagramStore.Intent, Unit, DiagramStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                dispatch(Msg.LoadData)
            } catch (e: Exception) {
                logger.e("Error in executeAction: ${e.message}")
            }
        }

        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                logger.e("Error in dispatch: ${e.message}")
            }
        }

        override fun executeIntent(intent: DiagramStore.Intent): Unit =
            try {
                when (intent) {
                    is DiagramStore.Intent.Init -> {
                        safeDispatch(Msg.LoadData)
                    }
                    is DiagramStore.Intent.UpdateDiagramType -> {
                        scope.launch {
                            try {
                                safeDispatch(Msg.UpdateDiagramType(intent.type))
                            } catch (e: Exception) {
                                logger.e("Error updating diagram type: ${e.message}")
                            }
                        }
                        Unit
                    }
                    is DiagramStore.Intent.UpdateDiagramData -> {
                        scope.launch {
                            try {
                                safeDispatch(Msg.UpdateDiagramData(intent.data))
                            } catch (e: Exception) {
                                logger.e("Error updating diagram data: ${e.message}")
                            }
                        }
                        Unit
                    }
                }
            } catch (e: Exception) {
                logger.e("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<DiagramStore.State, Msg> {
        override fun DiagramStore.State.reduce(msg: Msg): DiagramStore.State =
            when (msg) {
                Msg.LoadData -> copy(loading = false)
                Msg.LoadingData -> copy(loading = true)
                is Msg.UpdateDiagramType -> copy(diagramType = msg.type)
                is Msg.UpdateDiagramData -> copy(diagramData = msg.data)
            }
    }
}
