package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import utils.rDispatchers

interface LoginStore : Store<LoginStore.Intent, LoginStore.State, Nothing> {
    sealed interface Intent {
        data object Init : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class Login(val email: String, val password: String) : Intent
    }

    @Serializable
    data class State(
        val email: String = "",
        val password: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )
}

class LoginStoreFactory(
    private val storeFactory: StoreFactory,
) {

    fun create(): LoginStore =
        object : LoginStore, Store<LoginStore.Intent, LoginStore.State, Nothing> by storeFactory.create(
            name = "LoginStore",
            initialState = LoginStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object Loading : Msg
        data object Loaded : Msg
        data class Error(val message: String) : Msg
        data class UpdateEmail(val email: String) : Msg
        data class UpdatePassword(val password: String) : Msg
        data object LoginSuccess : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<LoginStore.Intent, Unit, LoginStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            // Initialize state if needed
        }

        override fun executeIntent(intent: LoginStore.Intent) {
            when (intent) {
                is LoginStore.Intent.Init -> {
                    // Initialize state
                }
                is LoginStore.Intent.UpdateEmail -> {
                    dispatch(Msg.UpdateEmail(intent.email))
                }
                is LoginStore.Intent.UpdatePassword -> {
                    dispatch(Msg.UpdatePassword(intent.password))
                }
                is LoginStore.Intent.Login -> {
                    scope.launch {
                        try {
                            dispatch(Msg.Loading)
                            // Делегируем аутентификацию общему AuthStore
                            // При успешном входе - обновляем состояние
                            dispatch(Msg.LoginSuccess)
                        } catch (e: Exception) {
                            dispatch(Msg.Error(e.message ?: "Login failed"))
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<LoginStore.State, Msg> {
        override fun LoginStore.State.reduce(msg: Msg): LoginStore.State =
            when (msg) {
                is Msg.Loading -> copy(loading = true, error = null)
                is Msg.Loaded -> copy(loading = false)
                is Msg.Error -> copy(loading = false, error = msg.message)
                is Msg.UpdateEmail -> copy(email = msg.email)
                is Msg.UpdatePassword -> copy(password = msg.password)
                is Msg.LoginSuccess -> copy(loading = false, error = null)
            }
    }
}
