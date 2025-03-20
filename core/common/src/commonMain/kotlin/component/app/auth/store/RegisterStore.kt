package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import utils.rDispatchers

interface RegisterStore : Store<RegisterStore.Intent, RegisterStore.State, Nothing> {
    sealed interface Intent {
        data object Init : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class UpdateUsername(val username: String) : Intent
        data class Register(val email: String, val password: String, val username: String) : Intent
    }

    @Serializable
    data class State(
        val email: String = "",
        val password: String = "",
        val username: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )
}

class RegisterStoreFactory(
    private val storeFactory: StoreFactory
) {
    fun create(): RegisterStore =
        object : RegisterStore, Store<RegisterStore.Intent, RegisterStore.State, Nothing> by storeFactory.create(
            name = "RegisterStore",
            initialState = RegisterStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object Loading : Msg
        data object Loaded : Msg
        data class Error(val message: String) : Msg
        data class UpdateEmail(val email: String) : Msg
        data class UpdatePassword(val password: String) : Msg
        data class UpdateUsername(val username: String) : Msg
        data object RegisterSuccess : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<RegisterStore.Intent, Unit, RegisterStore.State, Msg, Nothing>(
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
                is RegisterStore.Intent.UpdateEmail -> {
                    dispatch(Msg.UpdateEmail(intent.email))
                }
                is RegisterStore.Intent.UpdatePassword -> {
                    dispatch(Msg.UpdatePassword(intent.password))
                }
                is RegisterStore.Intent.UpdateUsername -> {
                    dispatch(Msg.UpdateUsername(intent.username))
                }
                is RegisterStore.Intent.Register -> {
                    scope.launch {
                        try {
                            dispatch(Msg.Loading)
                            // Имитируем задержку для демонстрации загрузки
                            kotlinx.coroutines.delay(1000)

                            // Фейковая валидация
                            if (intent.email.isEmpty() || intent.password.isEmpty() || intent.username.isEmpty()) {
                                throw Exception("Все поля должны быть заполнены")
                            }

                            if (intent.password.length < 6) {
                                throw Exception("Пароль должен содержать минимум 6 символов")
                            }

                            if (!intent.email.contains("@")) {
                                throw Exception("Некорректный email")
                            }

                            // Фейковая проверка существующего пользователя
                            if (intent.email == "test@test.com") {
                                throw Exception("Пользователь с таким email уже существует")
                            }

                            dispatch(Msg.RegisterSuccess)
                        } catch (e: Exception) {
                            dispatch(Msg.Error(e.message ?: "Ошибка регистрации"))
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<RegisterStore.State, Msg> {
        override fun RegisterStore.State.reduce(msg: Msg): RegisterStore.State =
            when (msg) {
                is Msg.Loading -> copy(loading = true, error = null)
                is Msg.Loaded -> copy(loading = false)
                is Msg.Error -> copy(loading = false, error = msg.message)
                is Msg.UpdateEmail -> copy(email = msg.email)
                is Msg.UpdatePassword -> copy(password = msg.password)
                is Msg.UpdateUsername -> copy(username = msg.username)
                is Msg.RegisterSuccess -> copy(loading = false, error = null)
            }
    }
}
