package component.app.users

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import logging.Logger
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


interface UsersStore : Store<UsersStore.Intent, UsersStore.State, UsersStore.Label> {


    sealed interface Intent {
        data object Users : Intent
    }


    data class State(
        val title: String = "Users",
    )

    sealed interface Label {
        data object NavigateBack : Label
    }
}

/**
 * Фабрика для создания UsersStore
 */
class UsersStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val logger by instance<Logger>()

    fun create(): UsersStore =
        object : UsersStore, Store<UsersStore.Intent, UsersStore.State, UsersStore.Label> by storeFactory.create(
            name = "UsersStore",
            initialState = UsersStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(logger.withTag("UsersStore")) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {

    }

    private class ExecutorImpl(
        private val logger: Logger
    ) : CoroutineExecutor<UsersStore.Intent, Unit, UsersStore.State, Msg, UsersStore.Label>() {

        override fun executeAction(action: Unit) {

        }

        override fun executeIntent(intent: UsersStore.Intent) {
            when (intent) {
                else -> {}
            }
        }
    }

    private object ReducerImpl : Reducer<UsersStore.State, Msg> {
        override fun UsersStore.State.reduce(msg: Msg): UsersStore.State =
            when (msg) {
                else -> copy()
            }
    }
}
