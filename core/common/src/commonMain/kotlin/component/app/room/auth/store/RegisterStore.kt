package component.app.room.auth.store

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
        data object Init : Intent
        data class UpdateUsername(val username: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class UpdateConfirmPassword(val confirmPassword: String) : Intent
        data object Register : Intent
    }

    @Serializable
    data class State(
        val username: String = "",
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

    fun create(): RegisterStore =
        object : RegisterStore, Store<RegisterStore.Intent, RegisterStore.State, Nothing> by storeFactory.create(
            name = "RegisterStore",
            initialState = RegisterStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data class UpdateUsername(val username: String) : Msg
        data class UpdateEmail(val email: String) : Msg
        data class UpdatePassword(val password: String) : Msg
        data class UpdateConfirmPassword(val confirmPassword: String) : Msg
        data class SetError(val error: String?) : Msg
        data object ClearError : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<RegisterStore.Intent, Unit, RegisterStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            // Инициализация, если нужна
        }

        override fun executeIntent(intent: RegisterStore.Intent) {
            when (intent) {
                is RegisterStore.Intent.Init -> Unit
                is RegisterStore.Intent.UpdateUsername -> {
                    dispatch(Msg.UpdateUsername(intent.username))
                    dispatch(Msg.ClearError)
                }
                is RegisterStore.Intent.UpdateEmail -> {
                    dispatch(Msg.UpdateEmail(intent.email))
                    dispatch(Msg.ClearError)
                }
                is RegisterStore.Intent.UpdatePassword -> {
                    dispatch(Msg.UpdatePassword(intent.password))
                    dispatch(Msg.ClearError)
                }
                is RegisterStore.Intent.UpdateConfirmPassword -> {
                    dispatch(Msg.UpdateConfirmPassword(intent.confirmPassword))
                    dispatch(Msg.ClearError)
                }
                is RegisterStore.Intent.Register -> {
                    scope.launch {
                        dispatch(Msg.LoadingData)

                        // Здесь будет логика регистрации
                        // Для примера просто проверяем, что поля не пустые и пароли совпадают
                        val state = state()
                        when {
                            state.username.isEmpty() || state.email.isEmpty() || state.password.isEmpty() -> {
                                dispatch(Msg.SetError("Все поля должны быть заполнены"))
                            }
                            state.password != state.confirmPassword -> {
                                dispatch(Msg.SetError("Пароли не совпадают"))
                            }
                            else -> {
                                // Успешная регистрация обрабатывается в компоненте
                                dispatch(Msg.ClearError)
                            }
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<RegisterStore.State, Msg> {
        override fun RegisterStore.State.reduce(msg: Msg): RegisterStore.State =
            when (msg) {
                is Msg.LoadingData -> copy(isLoading = true)
                is Msg.UpdateUsername -> copy(username = msg.username)
                is Msg.UpdateEmail -> copy(email = msg.email)
                is Msg.UpdatePassword -> copy(password = msg.password)
                is Msg.UpdateConfirmPassword -> copy(confirmPassword = msg.confirmPassword)
                is Msg.SetError -> copy(error = msg.error, isLoading = false)
                is Msg.ClearError -> copy(error = null)
            }
    }
}
