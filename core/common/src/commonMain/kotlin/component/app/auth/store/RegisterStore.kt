package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import model.AuthResult
import repository.AuthRepository
import utils.rDispatchers

interface RegisterStore : Store<RegisterStore.Intent, RegisterStore.State, Nothing> {
    sealed interface Intent {
        data object Init : Intent
        data class UpdateUsername(val username: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class UpdateConfirmPassword(val confirmPassword: String) : Intent
        data class Register(
            val username: String,
            val email: String,
            val password: String,
            val confirmPassword: String
        ) : Intent
    }

    @Serializable
    data class State(
        val username: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val loading: Boolean = false,
        val error: String? = null,
        val errorDetails: String? = null,
        val registrationSuccess: Boolean = false
    )
}

class RegisterStoreFactory(
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository
) {
    fun create(): RegisterStore =
        object : RegisterStore, Store<RegisterStore.Intent, RegisterStore.State, Nothing> by storeFactory.create(
            name = "RegisterStore",
            initialState = RegisterStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(authRepository) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object Loading : Msg
        data object Loaded : Msg
        data class Error(val message: String, val details: String? = null) : Msg
        data class UpdateUsername(val username: String) : Msg
        data class UpdateEmail(val email: String) : Msg
        data class UpdatePassword(val password: String) : Msg
        data class UpdateConfirmPassword(val confirmPassword: String) : Msg
        data object RegisterSuccess : Msg
    }

    private class ExecutorImpl(
        private val authRepository: AuthRepository
    ) : CoroutineExecutor<RegisterStore.Intent, Unit, RegisterStore.State, Msg, Nothing>(
        rDispatchers.main
    ) {
        override fun executeAction(action: Unit) {
            // Инициализация состояния если необходимо
        }

        override fun executeIntent(intent: RegisterStore.Intent) {
            when (intent) {
                is RegisterStore.Intent.Init -> {
                    // Initialize state
                }
                is RegisterStore.Intent.UpdateUsername -> {
                    dispatch(Msg.UpdateUsername(intent.username))
                }
                is RegisterStore.Intent.UpdateEmail -> {
                    dispatch(Msg.UpdateEmail(intent.email))
                }
                is RegisterStore.Intent.UpdatePassword -> {
                    dispatch(Msg.UpdatePassword(intent.password))
                }
                is RegisterStore.Intent.UpdateConfirmPassword -> {
                    dispatch(Msg.UpdateConfirmPassword(intent.confirmPassword))
                }
                is RegisterStore.Intent.Register -> {
                    scope.launch {
                        try {
                            // Базовая валидация полей
                            if (intent.username.isBlank() || intent.email.isBlank() ||
                                intent.password.isBlank() || intent.confirmPassword.isBlank()) {
                                dispatch(Msg.Error("Заполните все поля", "Все поля должны быть заполнены"))
                                return@launch
                            }

                            if (intent.password != intent.confirmPassword) {
                                dispatch(Msg.Error("Пароли не совпадают", "Введенные пароли должны совпадать"))
                                return@launch
                            }

                            dispatch(Msg.Loading)

                            // Отправляем запрос на регистрацию напрямую в репозиторий
                            val result = authRepository.register(
                                email = intent.email,
                                password = intent.password,
                                username = intent.username
                            )

                            when (result) {
                                is AuthResult.Success -> {
                                    dispatch(Msg.RegisterSuccess)
                                }
                                is AuthResult.Error -> {
                                    dispatch(Msg.Error(result.error.message, result.error.details))
                                }
                                is AuthResult.Loading -> {
                                    // Состояние загрузки уже отправлено
                                }
                                is AuthResult.Unauthenticated -> {
                                    dispatch(Msg.Error("Неудачная регистрация", "Проверьте введенные данные"))
                                }
                            }
                        } catch (e: Exception) {
                            dispatch(Msg.Error(e.message ?: "Ошибка при регистрации"))
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<RegisterStore.State, Msg> {
        override fun RegisterStore.State.reduce(msg: Msg): RegisterStore.State =
            when (msg) {
                is Msg.Loading -> copy(loading = true, error = null, errorDetails = null)
                is Msg.Loaded -> copy(loading = false)
                is Msg.Error -> copy(loading = false, error = msg.message, errorDetails = msg.details)
                is Msg.UpdateUsername -> copy(username = msg.username)
                is Msg.UpdateEmail -> copy(email = msg.email)
                is Msg.UpdatePassword -> copy(password = msg.password)
                is Msg.UpdateConfirmPassword -> copy(confirmPassword = msg.confirmPassword)
                is Msg.RegisterSuccess -> copy(loading = false, error = null, errorDetails = null, registrationSuccess = true)
            }
    }
}
