package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import model.auth.AuthStateDomain
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import utils.rDispatchers

/**
 * Интерфейс хранилища состояния для экрана регистрации
 */
interface RegisterStore : Store<RegisterStore.Intent, RegisterStore.State, Nothing> {

    /**
     * Возможные действия
     */
    sealed interface Intent {
        data class RegisterClicked(
            val username: String,
            val email: String,
            val password: String
        ) : Intent
        data object NavigateToLogin : Intent
        data object ErrorShown : Intent
    }

    /**
     * Состояние хранилища
     */
    @Serializable
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val shouldNavigateToLogin: Boolean = false,
        val isAuthenticated: Boolean = false
    )
}


/**
 * Фабрика для создания RegisterStore
 */
class RegisterStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val authStore by instance<AuthStore>()
    private val logger by instance<Logger>()

    fun create(): RegisterStore {
        return object : RegisterStore, Store<RegisterStore.Intent, RegisterStore.State, Nothing> by storeFactory.create(
            name = "RegisterStore",
            initialState = RegisterStore.State(),
            executorFactory = { ExecutorImpl(authStore, logger.withTag("RegisterStore")) },
            reducer = ReducerImpl
        ) {}
    }

    // Приватные сообщения для редуктора
    private sealed interface Msg {
        data object StartLoading : Msg
        data object StopLoading : Msg
        data class SetError(val error: String) : Msg
        data object ClearError : Msg
        data object NavigateToLogin : Msg
        data object NavigationHandled : Msg
        data object SetAuthenticated : Msg
    }

    private class ExecutorImpl(
        private val authStore: AuthStore,
        private val logger: Logger
    ) : CoroutineExecutor<RegisterStore.Intent, Nothing, RegisterStore.State, Msg, Nothing>(
        rDispatchers.main
    ) {

        init {
            logger.d("RegisterStore: Initializing")
            scope.launch {
                // Отслеживаем состояние аутентификации из глобального стора
                authStore.authState.collectLatest { authState ->
                    when (authState) {
                        is AuthStateDomain.Loading -> {
                            logger.d("RegisterStore: Auth state changed to Loading")
                            dispatch(Msg.StartLoading)
                        }
                        is AuthStateDomain.Authenticated -> {
                            logger.i("RegisterStore: User authenticated")
                            dispatch(Msg.StopLoading)
                            dispatch(Msg.SetAuthenticated)
                        }
                        is AuthStateDomain.Unauthenticated -> {
                            logger.d("RegisterStore: User unauthenticated")
                            dispatch(Msg.StopLoading)
                            // Если в authStore есть ошибка, отображаем её
                            val error = authStore.state.error
                            if (error != null) {
                                logger.w("RegisterStore: Auth error: $error")
                                dispatch(Msg.SetError(error))
                            }
                        }
                        else -> {
                            logger.w("RegisterStore: Unknown auth state: $authState")
                        }
                    }
                }
            }
        }

        override fun executeIntent(intent: RegisterStore.Intent) {
            when (intent) {
                is RegisterStore.Intent.RegisterClicked -> {
                    logger.i("RegisterStore: Register clicked for username: ${intent.username}, email: ${intent.email}")
                    dispatch(Msg.StartLoading)
                    dispatch(Msg.ClearError)

                    // Делегируем регистрацию глобальному AuthStore
                    authStore.accept(AuthStore.Intent.Register(
                        username = intent.username,
                        email = intent.email,
                        password = intent.password
                    ))
                }
                is RegisterStore.Intent.NavigateToLogin -> {
                    logger.d("RegisterStore: Navigate to login")
                    dispatch(Msg.NavigateToLogin)
                }
                is RegisterStore.Intent.ErrorShown -> {
                    logger.d("RegisterStore: Error shown")
                    dispatch(Msg.ClearError)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<RegisterStore.State, Msg> {
        override fun RegisterStore.State.reduce(msg: Msg): RegisterStore.State =
            when (msg) {
                is Msg.StartLoading -> copy(isLoading = true)
                is Msg.StopLoading -> copy(isLoading = false)
                is Msg.SetError -> copy(error = msg.error)
                is Msg.ClearError -> copy(error = null)
                is Msg.NavigateToLogin -> copy(shouldNavigateToLogin = true)
                is Msg.NavigationHandled -> copy(shouldNavigateToLogin = false)
                is Msg.SetAuthenticated -> copy(isAuthenticated = true)
            }
    }
}
