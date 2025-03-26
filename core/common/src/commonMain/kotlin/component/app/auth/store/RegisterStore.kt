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
import usecase.auth.RegisterUseCase
import utils.rDispatchers

interface RegisterStore : Store<RegisterStore.Intent, RegisterStore.State, Nothing> {
    sealed interface Intent {
        data class HandleRegisterClick(
            val email: String,
            val password: String,
            val confirmPassword: String,
            val username: String
        ) : Intent
        data object NavigateToLogin : Intent
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

class RegisterStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository
) {
    fun create(): RegisterStore =
        object : RegisterStore, Store<RegisterStore.Intent, RegisterStore.State, Nothing> by storeFactory.create(
            name = "RegisterStore",
            initialState = RegisterStore.State(),
            bootstrapper = null,
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private inner class ExecutorImpl : CoroutineExecutor<RegisterStore.Intent, Unit, RegisterStore.State, RegisterStore.Message, Nothing>(
        rDispatchers.main
    ) {
        override fun executeAction(action: Unit) {
            // Пустая реализация
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: RegisterStore.Message) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                println("Error in dispatch: ${e.message}")
            }
        }

        override fun executeIntent(intent: RegisterStore.Intent): Unit =
            try {
                when (intent) {
                    is RegisterStore.Intent.HandleRegisterClick -> {
                        scope.launch {
                            // Проверка на совпадение паролей
                            if (intent.password != intent.confirmPassword) {
                                safeDispatch(RegisterStore.Message.SetError("Пароли не совпадают"))
                                return@launch
                            }

                            safeDispatch(RegisterStore.Message.StartLoading)
                            safeDispatch(RegisterStore.Message.ClearError)

                            try {
                                val registerUseCase = RegisterUseCase(authRepository)
                                val result = registerUseCase(intent.email, intent.password, intent.username)

                                when (result) {
                                    is AuthResult.Success<*> -> {
                                        // Успех, очищаем ошибки
                                        safeDispatch(RegisterStore.Message.ClearError)
                                    }
                                    is AuthResult.Error<*> -> {
                                        safeDispatch(RegisterStore.Message.SetError(result.error.message, result.error.details))
                                    }
                                    is AuthResult.Loading -> {
                                        // Состояние загрузки уже установлено
                                    }
                                }
                                safeDispatch(RegisterStore.Message.StopLoading)
                            } catch (e: Exception) {
                                safeDispatch(RegisterStore.Message.SetError(e.message ?: "Ошибка сети"))
                                safeDispatch(RegisterStore.Message.StopLoading)
                            }
                        }
                    }
                    is RegisterStore.Intent.NavigateToLogin -> {
                        // В реальном коде здесь будет навигация
                        println("Navigate to Login")
                    }
                }
                Unit
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<RegisterStore.State, RegisterStore.Message> {
        override fun RegisterStore.State.reduce(msg: RegisterStore.Message): RegisterStore.State =
            when (msg) {
                is RegisterStore.Message.StartLoading -> copy(isLoading = true)
                is RegisterStore.Message.StopLoading -> copy(isLoading = false)
                is RegisterStore.Message.SetError -> copy(
                    error = msg.error,
                    errorDetails = msg.details
                )
                is RegisterStore.Message.ClearError -> copy(
                    error = null,
                    errorDetails = null
                )
            }
    }
}
