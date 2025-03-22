package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.store.create
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import model.AppError
import model.AuthResult
import model.User
import repository.auth.AuthRepository
import repository.mapper.ErrorMapper

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
            bootstrapper = BootstrapperImpl(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private inner class BootstrapperImpl : CoroutineBootstrapper<AuthStore.Message>() {
        override fun invoke() {
            scope.launch {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    dispatch(AuthStore.Message.SetUser(user))
                }
            }
        }
    }

    private inner class ExecutorImpl : CoroutineExecutor<AuthStore.Intent, Nothing, AuthStore.State, AuthStore.Message, Nothing>() {
        override fun executeIntent(intent: AuthStore.Intent, getState: () -> AuthStore.State) {
            when (intent) {
                is AuthStore.Intent.Login -> {
                    scope.launch {
                        dispatch(AuthStore.Message.StartLoading)
                        dispatch(AuthStore.Message.ClearError)

                        try {
                            val result = authRepository.login(intent.email, intent.password)
                            handleAuthResult(result)
                        } catch (e: Exception) {
                            // Обрабатываем неожиданные ошибки
                            val error = AppError(
                                code = model.ErrorCode.NETWORK_ERROR,
                                message = e.message ?: "Ошибка сети",
                                details = e.cause?.message
                            )
                            dispatch(AuthStore.Message.SetError(error.message, error.details))
                            dispatch(AuthStore.Message.StopLoading)
                        }
                    }
                }
                is AuthStore.Intent.Register -> {
                    scope.launch {
                        dispatch(AuthStore.Message.StartLoading)
                        dispatch(AuthStore.Message.ClearError)

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
                                code = model.ErrorCode.NETWORK_ERROR,
                                message = e.message ?: "Ошибка сети",
                                details = e.cause?.message
                            )
                            dispatch(AuthStore.Message.SetError(error.message, error.details))
                            dispatch(AuthStore.Message.StopLoading)
                        }
                    }
                }
                is AuthStore.Intent.Logout -> {
                    scope.launch {
                        dispatch(AuthStore.Message.StartLoading)
                        dispatch(AuthStore.Message.ClearError)

                        try {
                            val result = authRepository.logout()
                            if (result is AuthResult.Success && result.user == null) {
                                dispatch(AuthStore.Message.ClearUser)
                            }
                            dispatch(AuthStore.Message.StopLoading)
                        } catch (e: Exception) {
                            // Даже при ошибке, выходим из системы локально
                            dispatch(AuthStore.Message.ClearUser)
                            dispatch(AuthStore.Message.StopLoading)
                        }
                    }
                }
                is AuthStore.Intent.UpdateProfile -> {
                    scope.launch {
                        dispatch(AuthStore.Message.StartLoading)
                        dispatch(AuthStore.Message.ClearError)

                        try {
                            val result = authRepository.updateProfile(intent.username)
                            handleAuthResult(result)
                        } catch (e: Exception) {
                            // Обрабатываем неожиданные ошибки
                            val error = AppError(
                                code = model.ErrorCode.NETWORK_ERROR,
                                message = e.message ?: "Ошибка сети",
                                details = e.cause?.message
                            )
                            dispatch(AuthStore.Message.SetError(error.message, error.details))
                            dispatch(AuthStore.Message.StopLoading)
                        }
                    }
                }
            }
        }

        private fun handleAuthResult(result: AuthResult) {
            when (result) {
                is AuthResult.Success -> {
                    if (result.user != null) {
                        dispatch(AuthStore.Message.SetUser(result.user))
                    } else {
                        dispatch(AuthStore.Message.ClearUser)
                    }
                    dispatch(AuthStore.Message.StopLoading)
                }
                is AuthResult.Error -> {
                    dispatch(AuthStore.Message.SetError(result.error.message, result.error.details))
                    dispatch(AuthStore.Message.StopLoading)
                }
            }
        }
    }

    private object ReducerImpl : Reducer<AuthStore.State, AuthStore.Message> {
        override fun AuthStore.State.reduce(msg: AuthStore.Message): AuthStore.State =
            when (msg) {
                is AuthStore.Message.StartLoading -> copy(isLoading = true)
                is AuthStore.Message.StopLoading -> copy(isLoading = false)
                is AuthStore.Message.SetUser -> copy(
                    user = msg.user,
                    isAuthenticated = true
                )
                is AuthStore.Message.SetError -> copy(
                    error = msg.error,
                    errorDetails = msg.details
                )
                is AuthStore.Message.ClearUser -> copy(
                    user = null,
                    isAuthenticated = false
                )
                is AuthStore.Message.ClearError -> copy(
                    error = null,
                    errorDetails = null
                )
            }
    }
}
