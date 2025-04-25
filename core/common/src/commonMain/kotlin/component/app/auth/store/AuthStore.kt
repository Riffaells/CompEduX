package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.*
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.arkivanov.mvikotlin.extensions.coroutines.states
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logging.Logger
import model.DomainResult
import model.UserDomain
import model.auth.AuthStateDomain
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.auth.AuthUseCases
import navigation.rDispatchers

/**
 * Глобальное хранилище состояния аутентификации
 * Отвечает за управление аутентификацией пользователя во всем приложении
 */
interface AuthStore : Store<AuthStore.Intent, AuthStore.State, Nothing> {

    /**
     * Действия, которые могут быть выполнены в хранилище
     */
    sealed interface Intent {
        data class Login(val email: String, val password: String) : Intent
        data class Register(val email: String, val password: String, val username: String) : Intent
        data object CheckAuthStatus : Intent
        data object Logout : Intent
        data class SetAuthenticated(val authenticated: Boolean) : Intent
    }

    /**
     * Состояние хранилища
     */
    data class State(
        val isLoading: Boolean = false,
        val isAuthenticated: Boolean = false,
        val userDomain: UserDomain? = null,
        val error: String? = null,
        val errorDetails: String? = null
    )

    /**
     * Доступ к потоку состояния аутентификации
     */
    val authState: StateFlow<AuthStateDomain>
}

/**
 * Фабрика для создания AuthStore
 */
class AuthStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val authUseCases by instance<AuthUseCases>()
    private val logger by instance<Logger>()

    fun create(): AuthStore {
        // Создаем MutableStateFlow для хранения состояния аутентификации
        val authStateFlow = MutableStateFlow<AuthStateDomain>(AuthStateDomain.Unauthenticated)

        // Создаем экземпляр Store
        val store = storeFactory.create(
            name = "AuthStore",
            initialState = AuthStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(authUseCases, logger.withTag("AuthStore")) },
            reducer = ReducerImpl
        )

        // Создаем CoroutineScope для отслеживания изменений в Store
        val scope = CoroutineScope(rDispatchers.main)

        // Подписываемся на изменения в Store и обновляем AuthStateDomain
        scope.launch {
            store.states.collectLatest { state ->
                if (state.isAuthenticated && state.userDomain != null) {
                    authStateFlow.value = AuthStateDomain.Authenticated(state.userDomain)
                } else if (state.isLoading) {
                    authStateFlow.value = AuthStateDomain.Loading
                } else {
                    authStateFlow.value = AuthStateDomain.Unauthenticated
                }
            }
        }

        // Возвращаем анонимный объект, реализующий AuthStore
        return object : AuthStore, Store<AuthStore.Intent, AuthStore.State, Nothing> by store {
            override val authState: StateFlow<AuthStateDomain> = authStateFlow.asStateFlow()
        }
    }

    // Приватные сообщения для редуктора
    private sealed interface Msg {
        data object StartLoading : Msg
        data object StopLoading : Msg
        data class SetUser(val userDomain: UserDomain) : Msg
        data class SetError(val error: String, val details: String? = null) : Msg
        data object ClearUser : Msg
        data object ClearError : Msg
        data object SetAuthenticated : Msg
    }

    private class ExecutorImpl(
        private val authUseCases: AuthUseCases,
        private val logger: Logger
    ) : CoroutineExecutor<AuthStore.Intent, Unit, AuthStore.State, Msg, Nothing>(
        rDispatchers.main
    ) {
        override fun executeAction(action: Unit) {
            // Проверяем статус аутентификации при инициализации
            logger.d("Initializing authentication status check")
            scope.launch {
                checkAuthStatus()
            }
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                logger.e("Error in dispatch: ${e.message}", e)
            }
        }

        private suspend fun checkAuthStatus() {
            try {
                logger.d("Checking authentication status")
                safeDispatch(Msg.StartLoading)

                val isAuthenticated = withContext(rDispatchers.io) {
                    authUseCases.isAuthenticated()
                }

                if (isAuthenticated) {
                    logger.d("User is authenticated, getting current user data")
                    val userResult = withContext(rDispatchers.io) {
                        authUseCases.getCurrentUser()
                    }

                    if (userResult is DomainResult.Success) {
                        logger.d("Successfully retrieved user data: ${userResult.data.username}")
                        safeDispatch(Msg.SetUser(userResult.data))
                    } else {
                        logger.w("Failed to get user data, clearing user")
                        safeDispatch(Msg.ClearUser)
                    }
                } else {
                    logger.d("User is not authenticated")
                    safeDispatch(Msg.ClearUser)
                }
            } catch (e: Exception) {
                logger.e("Error checking auth status: ${e.message}", e)
                safeDispatch(Msg.ClearUser)
            } finally {
                safeDispatch(Msg.StopLoading)
            }
        }

        override fun executeIntent(intent: AuthStore.Intent): Unit =
            try {
                when (intent) {
                    is AuthStore.Intent.Login -> {
                        logger.d("Login intent received for email: ${intent.email}")
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                val result = withContext(rDispatchers.io) {
                                    authUseCases.login(intent.email, intent.password)
                                }
                                handleAuthResult(result)
                            } catch (e: Exception) {
                                logger.e("Login error: ${e.message}", e)
                                safeDispatch(Msg.SetError(e.message ?: "Ошибка сети"))
                                safeDispatch(Msg.StopLoading)
                            }
                        }
                    }
                    is AuthStore.Intent.Register -> {
                        logger.d("Register intent received for username: ${intent.username}, email: ${intent.email}")
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                val result = withContext(rDispatchers.io) {
                                    authUseCases.register(
                                        email = intent.email,
                                        password = intent.password,
                                        username = intent.username
                                    )
                                }
                                handleAuthResult(result)
                            } catch (e: Exception) {
                                logger.e("Register error: ${e.message}", e)
                                safeDispatch(Msg.SetError(e.message ?: "Ошибка сети"))
                                safeDispatch(Msg.StopLoading)
                            }
                        }
                    }
                    is AuthStore.Intent.Logout -> {
                        logger.d("Logout intent received")
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                val result = withContext(rDispatchers.io) {
                                    authUseCases.logout()
                                }

                                // Независимо от результата, очищаем пользователя
                                logger.d("Logout completed, clearing user data")
                                safeDispatch(Msg.ClearUser)
                                safeDispatch(Msg.StopLoading)
                            } catch (e: Exception) {
                                // Даже при ошибке, выходим из системы локально
                                logger.e("Logout error: ${e.message}, clearing user anyway", e)
                                safeDispatch(Msg.ClearUser)
                                safeDispatch(Msg.StopLoading)
                            }
                        }
                    }
                    is AuthStore.Intent.SetAuthenticated -> {
                        logger.d("Set authenticated intent: ${intent.authenticated}")
                        if (intent.authenticated) {
                            safeDispatch(Msg.SetAuthenticated)
                            scope.launch {
                                checkAuthStatus()
                            }
                        } else {
                            safeDispatch(Msg.ClearUser)
                        }
                    }
                    is AuthStore.Intent.CheckAuthStatus -> {
                        logger.d("Check auth status intent received")
                        scope.launch {
                            checkAuthStatus()
                        }
                    }
                }
                Unit
            } catch (e: Exception) {
                logger.e("Error in executeIntent: ${e.message}", e)
            }

        private fun <T> handleAuthResult(result: DomainResult<T>) {
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Authentication successful, retrieving user data")
                    // После успешной авторизации запрашиваем данные пользователя
                    scope.launch {
                        try {
                            val userResult = withContext(rDispatchers.io) {
                                authUseCases.getCurrentUser()
                            }

                            if (userResult is DomainResult.Success) {
                                logger.i("User data retrieved: ${userResult.data.username}")
                                safeDispatch(Msg.SetUser(userResult.data))
                            }
                        } catch (e: Exception) {
                            logger.e("Error getting current user: ${e.message}", e)
                            // Не устанавливаем ошибку, так как авторизация уже прошла успешно
                        }
                    }
                    safeDispatch(Msg.StopLoading)
                }
                is DomainResult.Error -> {
                    logger.w("Authentication error: ${result.error.message}")
                    safeDispatch(Msg.SetError(result.error.message, result.error.details))
                    safeDispatch(Msg.StopLoading)
                }
                is DomainResult.Loading -> {
                    logger.d("Authentication in progress")
                    // Состояние загрузки уже установлено
                }
            }
        }
    }

    private object ReducerImpl : Reducer<AuthStore.State, Msg> {
        override fun AuthStore.State.reduce(msg: Msg): AuthStore.State =
            when (msg) {
                is Msg.StartLoading -> copy(isLoading = true)
                is Msg.StopLoading -> copy(isLoading = false)
                is Msg.SetUser -> copy(
                    userDomain = msg.userDomain,
                    isAuthenticated = true
                )
                is Msg.SetError -> copy(
                    error = msg.error,
                    errorDetails = msg.details
                )
                is Msg.ClearUser -> copy(
                    userDomain = null,
                    isAuthenticated = false
                )
                is Msg.ClearError -> copy(
                    error = null,
                    errorDetails = null
                )
                is Msg.SetAuthenticated -> copy(
                    isAuthenticated = true
                )
            }
    }
}
