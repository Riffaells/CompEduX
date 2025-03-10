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

interface LoginStore : Store<LoginStore.Intent, LoginStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data class UpdateUsername(val username: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data object Login : Intent
    }

    @Serializable
    data class State(
        val username: String = "",
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
        data object LoadingData : Msg
        data class UpdateUsername(val username: String) : Msg
        data class UpdatePassword(val password: String) : Msg
        data class SetError(val error: String?) : Msg
        data object ClearError : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<LoginStore.Intent, Unit, LoginStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            // Инициализация, если нужна
        }

        override fun executeIntent(intent: LoginStore.Intent) {
            when (intent) {
                is LoginStore.Intent.Init -> Unit
                is LoginStore.Intent.UpdateUsername -> {
                    dispatch(Msg.UpdateUsername(intent.username))
                    dispatch(Msg.ClearError)
                }
                is LoginStore.Intent.UpdatePassword -> {
                    dispatch(Msg.UpdatePassword(intent.password))
                    dispatch(Msg.ClearError)
                }
                is LoginStore.Intent.Login -> {
                    scope.launch {
                        dispatch(Msg.LoadingData)

                        // Здесь будет логика входа
                        // Для примера просто проверяем, что поля не пустые
                        val state = state()
                        if (state.username.isEmpty() || state.password.isEmpty()) {
                            dispatch(Msg.SetError("Имя пользователя и пароль не могут быть пустыми"))
                        } else {
                            // Успешный вход обрабатывается в компоненте
                            dispatch(Msg.ClearError)
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<LoginStore.State, Msg> {
        override fun LoginStore.State.reduce(msg: Msg): LoginStore.State =
            when (msg) {
                is Msg.LoadingData -> copy(isLoading = true)
                is Msg.UpdateUsername -> copy(username = msg.username)
                is Msg.UpdatePassword -> copy(password = msg.password)
                is Msg.SetError -> copy(error = msg.error, isLoading = false)
                is Msg.ClearError -> copy(error = null)
            }
    }
}
