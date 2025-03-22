package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import model.AuthResult
import repository.auth.AuthRepository
import utils.rDispatchers

interface LoginStore : Store<LoginStore.Intent, LoginStore.State, Nothing> {
    sealed interface Intent {
        data object Init : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class Login(val email: String, val password: String) : Intent
    }

    @Serializable
    data class State(
        val email: String = "",
        val password: String = "",
        val loading: Boolean = false,
        val error: String? = null,
        val errorDetails: String? = null,
        val loginSuccess: Boolean = false
    )
}

class LoginStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository
) {
    fun create(): LoginStore =
        object : LoginStore, Store<LoginStore.Intent, LoginStore.State, Nothing> by storeFactory.create(
            name = "LoginStore",
            initialState = LoginStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(authRepository) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object Loading : Msg
        data object Loaded : Msg
        data class Error(val message: String, val details: String? = null) : Msg
        data class UpdateEmail(val email: String) : Msg
        data class UpdatePassword(val password: String) : Msg
        data object LoginSuccess : Msg
    }

    private class ExecutorImpl(
        private val authRepository: AuthRepository
    ) : CoroutineExecutor<LoginStore.Intent, Unit, LoginStore.State, Msg, Nothing>(
        rDispatchers.main
    ) {
        override fun executeAction(action: Unit) {
            // Инициализация состояния если необходимо
        }

        override fun executeIntent(intent: LoginStore.Intent) {
            when (intent) {
                is LoginStore.Intent.Init -> {
                    // Initialize state
                }
                is LoginStore.Intent.UpdateEmail -> {
                    dispatch(Msg.UpdateEmail(intent.email))
                }
                is LoginStore.Intent.UpdatePassword -> {
                    dispatch(Msg.UpdatePassword(intent.password))
                }
                is LoginStore.Intent.Login -> {
                    scope.launch {
                        try {
                            // Проверяем на пустые значения до отправки
                            if (intent.email.isBlank() || intent.password.isBlank()) {
                                dispatch(Msg.Error("Заполните все поля", "Email и пароль не могут быть пустыми"))
                                return@launch
                            }

                            dispatch(Msg.Loading)

                            // Отправляем запрос на авторизацию напрямую в репозиторий
                            val result = authRepository.login(intent.email, intent.password)

                            when (result) {
                                is AuthResult.Success -> {
                                    if (result.user != null) {
                                        dispatch(Msg.LoginSuccess)
                                    } else {
                                        dispatch(Msg.Error("Неудачная авторизация", "Проверьте логин и пароль"))
                                    }
                                }
                                is AuthResult.Error -> {
                                    dispatch(Msg.Error(result.error.message, result.error.details))
                                }
                            }
                        } catch (e: Exception) {
                            dispatch(Msg.Error(e.message ?: "Ошибка при авторизации"))
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<LoginStore.State, Msg> {
        override fun LoginStore.State.reduce(msg: Msg): LoginStore.State =
            when (msg) {
                is Msg.Loading -> copy(loading = true, error = null, errorDetails = null)
                is Msg.Loaded -> copy(loading = false)
                is Msg.Error -> copy(loading = false, error = msg.message, errorDetails = msg.details)
                is Msg.UpdateEmail -> copy(email = msg.email)
                is Msg.UpdatePassword -> copy(password = msg.password)
                is Msg.LoginSuccess -> copy(loading = false, error = null, errorDetails = null, loginSuccess = true)
            }
    }
}
