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

interface LoginStore : Store<LoginStore.Intent, LoginStore.State, Nothing> {

    sealed interface Intent {
        data class UpdateUsername(val username: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data object Login : Intent
        data object ToggleLoginMethod : Intent
    }

    @Serializable
    data class State(
        val username: String = "",
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val useEmailLogin: Boolean = true
    )
}

internal class LoginStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    // Интерфейс для фабрики, используемый в ComponentModule
    interface Factory {
        fun create(): LoginStore
    }

    fun create(): LoginStore =
        object : LoginStore, Store<LoginStore.Intent, LoginStore.State, Nothing> by storeFactory.create(
            name = "LoginStore",
            initialState = LoginStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data class UsernameChanged(val username: String) : Msg
        data class EmailChanged(val email: String) : Msg
        data class PasswordChanged(val password: String) : Msg
        data class ErrorOccurred(val error: String) : Msg
        data object Loading : Msg
        data object LoginSuccess : Msg
        data object ToggleLoginMethod : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<LoginStore.Intent, Unit, LoginStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                // TODO: Инициализация, если необходимо (например, загрузка сохраненных данных)
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

        override fun executeIntent(intent: LoginStore.Intent): Unit =
            try {
                when (intent) {
                    is LoginStore.Intent.UpdateUsername -> {
                        safeDispatch(Msg.UsernameChanged(intent.username))
                    }
                    is LoginStore.Intent.UpdateEmail -> {
                        safeDispatch(Msg.EmailChanged(intent.email))
                    }
                    is LoginStore.Intent.UpdatePassword -> {
                        safeDispatch(Msg.PasswordChanged(intent.password))
                    }
                    is LoginStore.Intent.ToggleLoginMethod -> {
                        safeDispatch(Msg.ToggleLoginMethod)
                    }
                    is LoginStore.Intent.Login -> {
                        safeDispatch(Msg.Loading)

                        scope.launch {
                            try {
                                // TODO: Реализовать реальную логику аутентификации с использованием API
                                // Для примера просто имитируем задержку и успешный вход
                                kotlinx.coroutines.delay(1000)

                                val state = state()

                                // Проверка в зависимости от выбранного метода входа
                                if (state.useEmailLogin) {
                                    // Вход по email
                                    if (state.email.isBlank() || state.password.isBlank()) {
                                        safeDispatch(Msg.ErrorOccurred("Email и пароль не могут быть пустыми"))
                                        return@launch
                                    }

                                    // Простая проверка формата email
                                    if (!state.email.contains("@")) {
                                        safeDispatch(Msg.ErrorOccurred("Некорректный формат email"))
                                        return@launch
                                    }
                                } else {
                                    // Вход по username
                                    if (state.username.isBlank() || state.password.isBlank()) {
                                        safeDispatch(Msg.ErrorOccurred("Имя пользователя и пароль не могут быть пустыми"))
                                        return@launch
                                    }

                                    // Проверка минимальной длины username
                                    if (state.username.length < 3) {
                                        safeDispatch(Msg.ErrorOccurred("Имя пользователя должно содержать не менее 3 символов"))
                                        return@launch
                                    }
                                }

                                // Имитация успешного входа
                                safeDispatch(Msg.LoginSuccess)
                            } catch (e: Exception) {
                                safeDispatch(Msg.ErrorOccurred(e.message ?: "Неизвестная ошибка"))
                                println("Error in login: ${e.message}")
                            }
                        }
                        Unit
                    }
                }
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<LoginStore.State, Msg> {
        override fun LoginStore.State.reduce(msg: Msg): LoginStore.State =
            when (msg) {
                is Msg.UsernameChanged -> copy(username = msg.username)
                is Msg.EmailChanged -> copy(email = msg.email)
                is Msg.PasswordChanged -> copy(password = msg.password)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false)
                is Msg.Loading -> copy(isLoading = true, error = null)
                is Msg.LoginSuccess -> copy(isLoading = false, error = null)
                is Msg.ToggleLoginMethod -> copy(useEmailLogin = !useEmailLogin)
            }
    }
}
