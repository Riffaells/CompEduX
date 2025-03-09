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

interface RegisterStore : Store<RegisterStore.Intent, RegisterStore.State, Nothing> {

    sealed interface Intent {
        data class UpdateName(val name: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class UpdateConfirmPassword(val confirmPassword: String) : Intent
        data object Register : Intent
    }

    @Serializable
    data class State(
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )
}

internal class RegisterStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    // Интерфейс для фабрики, используемый в ComponentModule
    interface Factory {
        fun create(): RegisterStore
    }

    fun create(): RegisterStore =
        object : RegisterStore, Store<RegisterStore.Intent, RegisterStore.State, Nothing> by storeFactory.create(
            name = "RegisterStore",
            initialState = RegisterStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data class NameChanged(val name: String) : Msg
        data class EmailChanged(val email: String) : Msg
        data class PasswordChanged(val password: String) : Msg
        data class ConfirmPasswordChanged(val confirmPassword: String) : Msg
        data class ErrorOccurred(val error: String) : Msg
        data object Loading : Msg
        data object RegisterSuccess : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<RegisterStore.Intent, Unit, RegisterStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                // TODO: Инициализация, если необходимо
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

        override fun executeIntent(intent: RegisterStore.Intent): Unit =
            try {
                when (intent) {
                    is RegisterStore.Intent.UpdateName -> {
                        safeDispatch(Msg.NameChanged(intent.name))
                    }
                    is RegisterStore.Intent.UpdateEmail -> {
                        safeDispatch(Msg.EmailChanged(intent.email))
                    }
                    is RegisterStore.Intent.UpdatePassword -> {
                        safeDispatch(Msg.PasswordChanged(intent.password))
                    }
                    is RegisterStore.Intent.UpdateConfirmPassword -> {
                        safeDispatch(Msg.ConfirmPasswordChanged(intent.confirmPassword))
                    }
                    is RegisterStore.Intent.Register -> {
                        safeDispatch(Msg.Loading)

                        scope.launch {
                            try {
                                // TODO: Реализовать реальную логику регистрации с использованием API
                                // Для примера просто имитируем задержку и проверки
                                kotlinx.coroutines.delay(1000)

                                val state = state()
                                // Проверка на пустые поля
                                if (state.name.isBlank() || state.email.isBlank() ||
                                    state.password.isBlank() || state.confirmPassword.isBlank()) {
                                    safeDispatch(Msg.ErrorOccurred("Все поля должны быть заполнены"))
                                    return@launch
                                }

                                // Проверка совпадения паролей
                                if (state.password != state.confirmPassword) {
                                    safeDispatch(Msg.ErrorOccurred("Пароли не совпадают"))
                                    return@launch
                                }

                                // Имитация успешной регистрации
                                safeDispatch(Msg.RegisterSuccess)
                            } catch (e: Exception) {
                                safeDispatch(Msg.ErrorOccurred(e.message ?: "Неизвестная ошибка"))
                                println("Error in registration: ${e.message}")
                            }
                        }
                        Unit
                    }
                }
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<RegisterStore.State, Msg> {
        override fun RegisterStore.State.reduce(msg: Msg): RegisterStore.State =
            when (msg) {
                is Msg.NameChanged -> copy(name = msg.name)
                is Msg.EmailChanged -> copy(email = msg.email)
                is Msg.PasswordChanged -> copy(password = msg.password)
                is Msg.ConfirmPasswordChanged -> copy(confirmPassword = msg.confirmPassword)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false)
                is Msg.Loading -> copy(isLoading = true, error = null)
                is Msg.RegisterSuccess -> copy(isLoading = false, error = null)
            }
    }
}
