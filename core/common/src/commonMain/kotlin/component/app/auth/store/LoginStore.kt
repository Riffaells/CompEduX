package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.core.store.create
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import model.AuthResult
import repository.auth.AuthRepository
import usecase.auth.LoginUseCase
import utils.rDispatchers

interface LoginStore : Store<LoginStore.Intent, LoginStore.State, Nothing> {
    sealed interface Intent {
        data class HandleLoginClick(val email: String, val password: String) : Intent
        data object NavigateToRegister : Intent
        data object NavigateToForgotPassword : Intent
    }

    @Serializable
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val errorDetails: String? = null
    )

    sealed interface Message {
        data object StartLoading : Message
        data object StopLoading : Message
        data class SetError(val error: String, val details: String? = null) : Message
        data object ClearError : Message
    }
}

class LoginStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository
) {
    fun create(): LoginStore =
        object : LoginStore, Store<LoginStore.Intent, LoginStore.State, Nothing> by storeFactory.create(
            name = "LoginStore",
            initialState = LoginStore.State(),
            bootstrapper = null,
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private inner class ExecutorImpl : CoroutineExecutor<LoginStore.Intent, Unit, LoginStore.State, LoginStore.Message, Nothing>(
        rDispatchers.main
    ) {
        override fun executeAction(action: Unit) {
            // Пустая реализация
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: LoginStore.Message) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                println("Error in dispatch: ${e.message}")
            }
        }

        override fun executeIntent(intent: LoginStore.Intent): Unit =
            try {
                when (intent) {
                    is LoginStore.Intent.HandleLoginClick -> {
                        scope.launch {
                            safeDispatch(LoginStore.Message.StartLoading)
                            safeDispatch(LoginStore.Message.ClearError)

                            try {
                                val loginUseCase = LoginUseCase(authRepository)
                                val result = loginUseCase(intent.email, intent.password)

                                when (result) {
                                    is AuthResult.Success<*> -> {
                                        // Успех, очищаем ошибки
                                        safeDispatch(LoginStore.Message.ClearError)
                                    }
                                    is AuthResult.Error<*> -> {
                                        safeDispatch(LoginStore.Message.SetError(result.error.message, result.error.details))
                                    }
                                    is AuthResult.Loading -> {
                                        // Состояние загрузки уже установлено
                                    }
                                }
                                safeDispatch(LoginStore.Message.StopLoading)
                            } catch (e: Exception) {
                                safeDispatch(LoginStore.Message.SetError(e.message ?: "Ошибка сети"))
                                safeDispatch(LoginStore.Message.StopLoading)
                            }
                        }
                    }
                    is LoginStore.Intent.NavigateToRegister -> {
                        // В реальном коде здесь будет навигация
                        println("Navigate to Register")
                    }
                    is LoginStore.Intent.NavigateToForgotPassword -> {
                        // В реальном коде здесь будет навигация
                        println("Navigate to Forgot Password")
                    }
                }
                Unit
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<LoginStore.State, LoginStore.Message> {
        override fun LoginStore.State.reduce(msg: LoginStore.Message): LoginStore.State =
            when (msg) {
                is LoginStore.Message.StartLoading -> copy(isLoading = true)
                is LoginStore.Message.StopLoading -> copy(isLoading = false)
                is LoginStore.Message.SetError -> copy(
                    error = msg.error,
                    errorDetails = msg.details
                )
                is LoginStore.Message.ClearError -> copy(
                    error = null,
                    errorDetails = null
                )
            }
    }
}
