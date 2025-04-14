package component.app.skiko.store

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

interface SkikoStore : Store<SkikoStore.Intent, SkikoStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object Back : Intent
    }

    @Serializable
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null
    )
}

internal class SkikoStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val logger by instance<Logger>("SkikoStore")

    fun create(): SkikoStore =
        object : SkikoStore, Store<SkikoStore.Intent, SkikoStore.State, Nothing> by storeFactory.create(
            name = "SkikoStore",
            initialState = SkikoStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data object LoadData : Msg
        data class ErrorOccurred(val error: String) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<SkikoStore.Intent, Unit, SkikoStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                dispatch(Msg.LoadData)
            } catch (e: Exception) {
                logger.e("Error in executeAction: ${e.message}")
            }
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                logger.e("Error in dispatch: ${e.message}")
            }
        }

        override fun executeIntent(intent: SkikoStore.Intent): Unit =
            try {
                when (intent) {
                    is SkikoStore.Intent.Init -> {
                        safeDispatch(Msg.LoadData)
                    }
                    is SkikoStore.Intent.Back -> {
                        // Обработка в компоненте
                    }
                }
            } catch (e: Exception) {
                logger.e("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<SkikoStore.State, Msg> {
        override fun SkikoStore.State.reduce(msg: Msg): SkikoStore.State =
            when (msg) {
                is Msg.LoadData -> copy(isLoading = false)
                is Msg.LoadingData -> copy(isLoading = true)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false)
            }
    }
}
