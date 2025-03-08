package component.app.main.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import utils.rDispatchers

interface MainStore : Store<MainStore.Intent, MainStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object OpenSettings : Intent
        data class UpdateTitle(val title: String) : Intent
    }

    @Serializable
    data class State(
        val title: String = "Main Screen",
        val loading: Boolean = false
    )
}

internal class MainStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    fun create(): MainStore =
        object : MainStore, Store<MainStore.Intent, MainStore.State, Nothing> by storeFactory.create(
            name = "MainStore",
            initialState = MainStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data object LoadData : Msg
        data class UpdateTitle(val title: String) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<MainStore.Intent, Unit, MainStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                dispatch(Msg.LoadData)
            } catch (e: Exception) {
                println("Error in executeAction: ${e.message}")
            }
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                println("Error in dispatch: ${e.message}")
            }
        }

        override fun executeIntent(intent: MainStore.Intent): Unit =
            try {
                when (intent) {
                    is MainStore.Intent.Init -> {
                        safeDispatch(Msg.LoadData)
                    }
                    is MainStore.Intent.UpdateTitle -> {
                        // Выполняем асинхронные операции
                        scope.launch {
                            try {
                                // Асинхронные операции...

                                // Обновление UI
                                safeDispatch(Msg.UpdateTitle(intent.title))
                            } catch (e: Exception) {
                                println("Error updating title: ${e.message}")
                            }
                        }
                        Unit
                    }
                    is MainStore.Intent.OpenSettings -> {
                        // Обработка в компоненте
                    }
                }
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<MainStore.State, Msg> {
        override fun MainStore.State.reduce(msg: Msg): MainStore.State =
            when (msg) {
                Msg.LoadData -> copy(loading = false)
                Msg.LoadingData -> copy(loading = true)
                is Msg.UpdateTitle -> copy(title = msg.title)
            }
    }
}
