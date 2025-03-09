package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import utils.rDispatchers

interface LoginStore : Store<LoginStore.Intent, LoginStore.State, Nothing> {

    sealed interface Intent {
        data class UpdateEmail(val email: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data object Login : Intent
    }

    data class State(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )
}

internal class LoginStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    fun create(): LoginStore =
        object : LoginStore, Store<LoginStore.Intent, LoginStore.State, Nothing> by storeFactory.create(
            name = "LoginStore",
            initialState = LoginStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data class EmailChanged(val email: String) : Msg
        data class PasswordChanged(val password: String) : Msg
        data class ErrorOccurred(val error: String) : Msg
        data object Loading : Msg
        data object LoginSuccess : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<LoginStore.Intent, Unit, LoginStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            // TODO: Инициализация, если необходимо (например, загрузка сохраненных данных)
        }

        override fun executeIntent(intent: LoginStore.Intent): Unit =
            when (intent) {
                is LoginStore.Intent.UpdateEmail -> {
                    dispatch(Msg.EmailChanged(intent.email))
                }
                is LoginStore.Intent.UpdatePassword -> {
                    dispatch(Msg.PasswordChanged(intent.password))
                }
                is LoginStore.Intent.Login -> {
                    dispatch(Msg.Loading)

                    scope.launch {
                        try {
                            // TODO: Реализовать реальную логику аутентификации с использованием API
                            // Для примера просто имитируем задержку и успешный вход
                            kotlinx.coroutines.delay(1000)

                            // Проверка на пустые поля
                            if (state().email.isBlank() || state().password.isBlank()) {
                                dispatch(Msg.ErrorOccurred("Email и пароль не могут быть пустыми"))
                                return@launch
                            }

                            // Имитация успешного входа
                            dispatch(Msg.LoginSuccess)
                        } catch (e: Exception) {
                            dispatch(Msg.ErrorOccurred(e.message ?: "Неизвестная ошибка"))
                        }
                    }
                    Unit
                }
            }
    }

    private object ReducerImpl : Reducer<LoginStore.State, Msg> {
        override fun LoginStore.State.reduce(msg: Msg): LoginStore.State =
            when (msg) {
                is Msg.EmailChanged -> copy(email = msg.email)
                is Msg.PasswordChanged -> copy(password = msg.password)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false)
                is Msg.Loading -> copy(isLoading = true, error = null)
                is Msg.LoginSuccess -> copy(isLoading = false, error = null)
            }
    }
}
