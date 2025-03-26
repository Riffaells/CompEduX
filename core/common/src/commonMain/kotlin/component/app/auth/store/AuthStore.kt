package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.*
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import model.AppError
import model.AuthResult
import model.ErrorCode
import model.User
import repository.auth.AuthRepository
import usecase.auth.AuthUseCases
import utils.rDispatchers

interface AuthStore : Store<AuthStore.Intent, AuthStore.State, Nothing> {

    sealed interface Intent {
        data class Login(val email: String, val password: String) : Intent
        data class Register(val email: String, val password: String, val username: String) : Intent
        data object Logout : Intent
        data class UpdateProfile(val username: String) : Intent
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
    private val authRepository: AuthRepository
) {
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
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                val result = authRepository.login(intent.email, intent.password)
                                handleAuthResult(result)
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
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                val result = authRepository.register(
                                    email = intent.email,
                                    password = intent.password,
                                    username = intent.username
                                )
                                handleAuthResult(result)
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
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                val result = authRepository.logout()
                                // Исправляем smart cast ошибку
                                when (result) {
                                    is AuthResult.Success -> {
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
                        scope.launch {
                            safeDispatch(Msg.StartLoading)
                            safeDispatch(Msg.ClearError)

                            try {
                                val result = authRepository.updateProfile(intent.username)
                                handleAuthResult(result)
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
                }
                Unit
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }

        private fun <T> handleAuthResult(result: AuthResult<T>) {
            when (result) {
                is AuthResult.Success<T> -> {
                    // Исправляем smart cast ошибку
                    val user = result.user
                    if (user != null) {
                        safeDispatch(Msg.SetUser(user))
                    } else {
                        safeDispatch(Msg.ClearUser)
                    }
                    safeDispatch(Msg.StopLoading)
                }
                is AuthResult.Error<T> -> {
                    safeDispatch(Msg.SetError(result.error.message, result.error.details))
                    safeDispatch(Msg.StopLoading)
                }
                is AuthResult.Loading -> {
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
            }
    }
}
