package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.*
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.AppError
import model.AuthResult
import model.ErrorCode
import model.User
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.direct
import org.kodein.di.instance
import usecase.auth.AuthUseCases
import utils.rDispatchers

interface AuthStore : Store<AuthStore.Intent, AuthStore.State, Nothing> {

    sealed interface Intent {
        data class Login(val email: String, val password: String) : Intent
        data class Register(val email: String, val password: String, val username: String) : Intent
        data object Logout : Intent
        data class UpdateProfile(val username: String) : Intent
        data class SetAuthenticated(val authenticated: Boolean) : Intent
    }

    data class State(
        val isLoading: Boolean = false,
        val isAuthenticated: Boolean = false,
        val user: User? = null,
        val error: String? = null,
        val errorDetails: String? = null
    )

    sealed interface Message {
        data object StartLoading : Message
        data object StopLoading : Message
        data class SetUser(val user: User) : Message
        data class SetError(val error: String, val details: String? = null) : Message
        data object ClearUser : Message
        data object ClearError : Message
    }
}

class AuthStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val authUseCases: AuthUseCases by instance()

    fun create(): AuthStore =
        object : AuthStore, Store<AuthStore.Intent, AuthStore.State, Nothing> by storeFactory.create(
            name = "AuthStore",
            initialState = AuthStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object StartLoading : Msg
        data object StopLoading : Msg
        data class SetUser(val user: User) : Msg
        data class SetError(val error: String, val details: String? = null) : Msg
        data object ClearUser : Msg
        data object ClearError : Msg
        data object SetAuthenticated : Msg
    }


    private inner class ExecutorImpl :
        CoroutineExecutor<AuthStore.Intent, Unit, AuthStore.State, Msg, Nothing>(
        rDispatchers.main
    ) {
        override fun executeAction(action: Unit) {
            // Пустая реализация
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                println("Error in dispatch: ${e.message}")
            }
        }

        override fun executeIntent(intent: AuthStore.Intent): Unit =
            try {
                when (intent) {
                    is AuthStore.Intent.Login -> {
                        // Используем scope из CoroutineExecutor
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                // Выполнение тяжелой операции в IO-потоке
                                val result = withContext(rDispatchers.io) {
                                    authUseCases.login(intent.email, intent.password)
                                }
                                handleAuthResult<model.auth.AuthResponseDomain>(result)
                            } catch (e: Exception) {
                                // Обрабатываем неожиданные ошибки
                                val error = AppError(
                                    code = ErrorCode.NETWORK_ERROR,
                                    message = e.message ?: "Ошибка сети",
                                    details = e.cause?.message
                                )
                                safeDispatch(Msg.SetError(error.message, error.details))
                                safeDispatch(Msg.StopLoading)
                            }
                        }
                    }
                    is AuthStore.Intent.Register -> {
                        // Используем scope из CoroutineExecutor
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                // Выполнение тяжелой операции в IO-потоке
                                val result = withContext(rDispatchers.io) {
                                    authUseCases.register(
                                        email = intent.email,
                                        password = intent.password,
                                        username = intent.username
                                    )
                                }
                                handleAuthResult<model.auth.AuthResponseDomain>(result)
                            } catch (e: Exception) {
                                // Обрабатываем неожиданные ошибки
                                val error = AppError(
                                    code = ErrorCode.NETWORK_ERROR,
                                    message = e.message ?: "Ошибка сети",
                                    details = e.cause?.message
                                )
                                safeDispatch(Msg.SetError(error.message, error.details))
                                safeDispatch(Msg.StopLoading)
                            }
                        }
                    }
                    is AuthStore.Intent.Logout -> {
                        // Используем scope из CoroutineExecutor
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                // Выполнение тяжелой операции в IO-потоке
                                val result = withContext(rDispatchers.io) {
                                    authUseCases.logout()
                                }
                                // Исправляем smart cast ошибку
                                when (result) {
                                    is AuthResult.Success<Unit> -> {
                                        // Независимо от результата, считаем пользователя вышедшим
                                        safeDispatch(Msg.ClearUser)
                                    }
                                    else -> {
                                        // Даже при ошибке, выходим из системы локально
                                        safeDispatch(Msg.ClearUser)
                                    }
                                }
                                safeDispatch(Msg.StopLoading)
                            } catch (e: Exception) {
                                // Даже при ошибке, выходим из системы локально
                                safeDispatch(Msg.ClearUser)
                                safeDispatch(Msg.StopLoading)
                            }
                        }
                    }
                    is AuthStore.Intent.UpdateProfile -> {
                        // Используем scope из CoroutineExecutor
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                // Выполнение тяжелой операции в IO-потоке
                                val result = withContext(rDispatchers.io) {
                                    authUseCases.updateProfile(intent.username)
                                }
                                handleAuthResult<User>(result)
                            } catch (e: Exception) {
                                // Обрабатываем неожиданные ошибки
                                val error = AppError(
                                    code = ErrorCode.NETWORK_ERROR,
                                    message = e.message ?: "Ошибка сети",
                                    details = e.cause?.message
                                )
                                safeDispatch(Msg.SetError(error.message, error.details))
                                safeDispatch(Msg.StopLoading)
                            }
                        }
                    }
                    is AuthStore.Intent.SetAuthenticated -> {
                        // Просто обновляем состояние
                        if (intent.authenticated) {
                            safeDispatch(Msg.SetAuthenticated)
                        } else {
                            safeDispatch(Msg.ClearUser)
                        }
                    }
                }
                Unit
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }

        private fun <T> handleAuthResult(result: AuthResult<T>) {
            when (result) {
                is AuthResult.Success -> {
                    // В новой версии AuthResult больше нет поля user
                    // Вместо этого мы запускаем отдельный запрос, чтобы получить пользователя
                    // Запускаем отдельный запрос для получения текущего пользователя
                    scope.launch {
                        try {
                            val userResult = withContext(rDispatchers.io) {
                                authUseCases.getCurrentUser()
                            }
                            if (userResult is AuthResult.Success<User>) {
                                // Пользователь уже не может быть null в новой версии AuthResult
                                safeDispatch(Msg.SetUser(userResult.data))
                            }
                        } catch (e: Exception) {
                            println("Error getting current user: ${e.message}")
                            // Не устанавливаем ошибку, так как авторизация уже прошла успешно
                        }
                    }
                    safeDispatch(Msg.StopLoading)
                }
                is AuthResult.Error -> {
                    safeDispatch(Msg.SetError(result.error.message, result.error.details))
                    safeDispatch(Msg.StopLoading)
                }
                else -> {
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
                    user = msg.user,
                    isAuthenticated = true
                )
                is Msg.SetError -> copy(
                    error = msg.error,
                    errorDetails = msg.details
                )
                is Msg.ClearUser -> copy(
                    user = null,
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
