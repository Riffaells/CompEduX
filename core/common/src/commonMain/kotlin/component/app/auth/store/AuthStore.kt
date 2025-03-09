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

interface AuthStore : Store<AuthStore.Intent, AuthStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object NavigateToLogin : Intent
        data object NavigateToRegister : Intent
        data object NavigateToProfile : Intent
    }

    @Serializable
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null,
        val isAuthenticated: Boolean = false
    )
}

internal class AuthStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    fun create(): AuthStore =
        object : AuthStore, Store<AuthStore.Intent, AuthStore.State, Nothing> by storeFactory.create(
            name = "AuthStore",
            initialState = AuthStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data object LoadData : Msg
        data class ErrorOccurred(val error: String) : Msg
        data object SetAuthenticated : Msg
        data object SetUnauthenticated : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<AuthStore.Intent, Unit, AuthStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                dispatch(Msg.LoadData)
                // Здесь можно проверить, авторизован ли пользователь
                checkAuthStatus()
            } catch (e: Exception) {
                println("Error in executeAction: ${e.message}")
            }
        }

        private fun checkAuthStatus() {
            scope.launch {
                try {
                    // TODO: Проверить статус авторизации пользователя
                    // Для примера просто имитируем неавторизованного пользователя
                    safeDispatch(Msg.SetUnauthenticated)
                } catch (e: Exception) {
                    safeDispatch(Msg.ErrorOccurred(e.message ?: "Неизвестная ошибка"))
                    println("Error checking auth status: ${e.message}")
                }
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

        override fun executeIntent(intent: AuthStore.Intent): Unit =
            try {
                when (intent) {
                    is AuthStore.Intent.Init -> {
                        safeDispatch(Msg.LoadData)
                        checkAuthStatus()
                    }
                    is AuthStore.Intent.NavigateToLogin -> {
                        // Обработка в компоненте
                    }
                    is AuthStore.Intent.NavigateToRegister -> {
                        // Обработка в компоненте
                    }
                    is AuthStore.Intent.NavigateToProfile -> {
                        // Обработка в компоненте
                    }
                }
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<AuthStore.State, Msg> {
        override fun AuthStore.State.reduce(msg: Msg): AuthStore.State =
            when (msg) {
                is Msg.LoadData -> copy(isLoading = false)
                is Msg.LoadingData -> copy(isLoading = true)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false)
                is Msg.SetAuthenticated -> copy(isAuthenticated = true)
                is Msg.SetUnauthenticated -> copy(isAuthenticated = false)
            }
    }
}
