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

interface LoginStore : Store<LoginStore.Intent, LoginStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data class UpdateIdentifier(val identifier: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data object Login : Intent
        data object NavigateToRegister : Intent
        data object Back : Intent
    }

    @Serializable
    data class State(
        val identifier: String = "",
        val password: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )
}

internal class LoginStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    fun create(
        onLoginSuccess: () -> Unit,
        onNavigateToRegister: () -> Unit,
        onBack: () -> Unit
    ): LoginStore =
        object : LoginStore, Store<LoginStore.Intent, LoginStore.State, Nothing> by storeFactory.create(
            name = "LoginStore",
            initialState = LoginStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(onLoginSuccess, onNavigateToRegister, onBack) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object SetLoading : Msg
        data object ClearLoading : Msg
        data class SetError(val error: String) : Msg
        data class UpdateIdentifier(val identifier: String) : Msg
        data class UpdatePassword(val password: String) : Msg
    }

    private inner class ExecutorImpl(
        private val onLoginSuccess: () -> Unit,
        private val onNavigateToRegister: () -> Unit,
        private val onBack: () -> Unit
    ) : CoroutineExecutor<LoginStore.Intent, Unit, LoginStore.State, Msg, Nothing>(
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

        override fun executeIntent(intent: LoginStore.Intent): Unit {
            when (intent) {
                is LoginStore.Intent.Init -> {
                    // Инициализация, если необходимо
                }

                is LoginStore.Intent.Login -> {
                    scope.launch {
                        try {
                            safeDispatch(Msg.SetLoading)
                            // Здесь будет реальная логика входа
                            kotlinx.coroutines.delay(1000) // Имитация задержки сети
                            onLoginSuccess()
                        } catch (e: Exception) {
                            safeDispatch(Msg.SetError(e.message ?: "Unknown error"))
                        } finally {
                            safeDispatch(Msg.ClearLoading)
                        }
                    }
                }

                is LoginStore.Intent.NavigateToRegister -> {
                    onNavigateToRegister()
                }

                is LoginStore.Intent.Back -> {
                    onBack()
                }

                is LoginStore.Intent.UpdateIdentifier -> {
                    safeDispatch(Msg.UpdateIdentifier(intent.identifier))
                }

                is LoginStore.Intent.UpdatePassword -> {
                    safeDispatch(Msg.UpdatePassword(intent.password))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<LoginStore.State, Msg> {
        override fun LoginStore.State.reduce(msg: Msg): LoginStore.State =
            when (msg) {
                is Msg.SetLoading -> copy(loading = true, error = null)
                is Msg.ClearLoading -> copy(loading = false)
                is Msg.SetError -> copy(error = msg.error)
                is Msg.UpdateIdentifier -> copy(identifier = msg.identifier)
                is Msg.UpdatePassword -> copy(password = msg.password)
            }
    }
}
