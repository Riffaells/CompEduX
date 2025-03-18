package component.app.auth.store

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

interface ProfileStore : Store<ProfileStore.Intent, ProfileStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object Logout : Intent
        data object Back : Intent
    }

    @Serializable
    data class State(
        val username: String = "",
        val email: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )
}

internal class ProfileStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    fun create(
        onLogout: () -> Unit,
        onBack: () -> Unit
    ): ProfileStore =
        object : ProfileStore, Store<ProfileStore.Intent, ProfileStore.State, Nothing> by storeFactory.create(
            name = "ProfileStore",
            initialState = ProfileStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(onLogout, onBack) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object SetLoading : Msg
        data object ClearLoading : Msg
        data class SetError(val error: String) : Msg
    }

    private inner class ExecutorImpl(
        private val onLogout: () -> Unit,
        private val onBack: () -> Unit
    ) : CoroutineExecutor<ProfileStore.Intent, Unit, ProfileStore.State, Msg, Nothing>(
        rDispatchers.main
    ) {

        override fun executeAction(action: Unit) {
            try {
                // Инициализация, если необходимо
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

        override fun executeIntent(intent: ProfileStore.Intent): Unit {
            when (intent) {
                is ProfileStore.Intent.Init -> {
                    // Инициализация, если необходимо
                }

                is ProfileStore.Intent.Logout -> {
                    scope.launch {
                        try {
                            safeDispatch(Msg.SetLoading)
                            // Здесь будет реальная логика выхода
                            kotlinx.coroutines.delay(500) // Имитация задержки сети
                            onLogout()
                        } catch (e: Exception) {
                            safeDispatch(Msg.SetError(e.message ?: "Unknown error"))
                        } finally {
                            safeDispatch(Msg.ClearLoading)
                        }
                    }
                }

                is ProfileStore.Intent.Back -> {
                    onBack()
                }
            }

        }
    }

    private object ReducerImpl : Reducer<ProfileStore.State, Msg> {
        override fun ProfileStore.State.reduce(msg: Msg): ProfileStore.State =
            when (msg) {
                is Msg.SetLoading -> copy(loading = true, error = null)
                is Msg.ClearLoading -> copy(loading = false)
                is Msg.SetError -> copy(error = msg.error)
            }
    }
}
