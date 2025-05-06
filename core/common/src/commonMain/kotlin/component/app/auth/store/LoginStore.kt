package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import logging.Logger
import model.auth.AuthStateDomain
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Store для экрана входа в систему
 */
interface LoginStore : Store<LoginStore.Intent, LoginStore.State, LoginStore.Label> {

    /**
     * Intent - действия, которые могут быть выполнены в этом Store
     */
    sealed interface Intent {
        data class SetUsername(val username: String) : Intent
        data class SetPassword(val password: String) : Intent
        data object Login : Intent
        data object NavigateToRegister : Intent
        data class ValidationError(val message: String) : Intent
    }

    /**
     * State - состояние экрана входа
     */
    data class State(
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val isNavigateToRegister: Boolean = false,
    )

    sealed interface Label {
        data object NavigateBack : Label
    }
}

/**
 * Фабрика для создания LoginStore
 */
class LoginStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val logger by instance<Logger>()
    private val authStore by instance<AuthStore>()

    fun create(): LoginStore =
        object : LoginStore, Store<LoginStore.Intent, LoginStore.State, LoginStore.Label> by storeFactory.create(
            name = "LoginStore",
            initialState = LoginStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(authStore, logger.withTag("LoginStore")) },
            reducer = ReducerImpl
        ) {}

    // Приватные сообщения для редуктора
    private sealed interface Msg {
        data class SetUsername(val username: String) : Msg
        data class SetPassword(val password: String) : Msg
        data class SetLoading(val isLoading: Boolean) : Msg
        data class SetError(val message: String?) : Msg
        data class SetNavigateToRegister(val isNavigateToRegister: Boolean) : Msg
    }

    private class ExecutorImpl(
        private val authStore: AuthStore,
        private val logger: Logger
    ) : CoroutineExecutor<LoginStore.Intent, Unit, LoginStore.State, Msg, LoginStore.Label>() {

        override fun executeAction(action: Unit) {
            // Subscribe to auth store
            scope.launch {
                authStore.authState.collect { authState ->
                    logger.d("Auth state changed: $authState")
                    if (authState is AuthStateDomain.Authenticated) {
                        // User authenticated, navigate back
                        publish(LoginStore.Label.NavigateBack)
                    }

                    // Set loading state
                    if (authState is AuthStateDomain.Loading) {
                        dispatch(Msg.SetLoading(true))
                    } else {
                        // Clear loading
                        dispatch(Msg.SetLoading(false))
                    }

                    // Set error
                    if (authState is AuthStateDomain.Unauthenticated && authStore.state.error != null) {
                        dispatch(Msg.SetError(authStore.state.error))
                    }
                }
            }
        }

        var d = 0
        override fun executeIntent(intent: LoginStore.Intent) {
            when (intent) {
                is LoginStore.Intent.SetUsername -> {
                    dispatch(Msg.SetUsername(intent.username))
                }

                is LoginStore.Intent.SetPassword -> {
                    dispatch(Msg.SetPassword(intent.password))
                }

                is LoginStore.Intent.Login -> {
                    d += 1
                    logger.d("Login intent with username: ${state().username} ${d++}")
                    // Clear error
                    dispatch(Msg.SetError(null))

                    // Validate
                    if (state().username.isEmpty()) {
                        dispatch(Msg.SetError("Имя пользователя не может быть пустым"))
                        return
                    }

                    if (state().password.isEmpty()) {
                        dispatch(Msg.SetError("Пароль не может быть пустым"))
                        return
                    }

                    // Call auth store
                    authStore.accept(AuthStore.Intent.Login(state().username, state().password))
                }

                is LoginStore.Intent.NavigateToRegister -> {
                    logger.d("NavigateToRegister intent")
                    dispatch(Msg.SetNavigateToRegister(true))
                }

                is LoginStore.Intent.ValidationError -> {
                    logger.w("Validation error: ${intent.message}")
                    dispatch(Msg.SetError(intent.message))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<LoginStore.State, Msg> {
        override fun LoginStore.State.reduce(msg: Msg): LoginStore.State =
            when (msg) {
                is Msg.SetUsername -> copy(username = msg.username)
                is Msg.SetPassword -> copy(password = msg.password)
                is Msg.SetLoading -> copy(isLoading = msg.isLoading)
                is Msg.SetError -> copy(error = msg.message)
                is Msg.SetNavigateToRegister -> copy(isNavigateToRegister = msg.isNavigateToRegister)
            }
    }
}
