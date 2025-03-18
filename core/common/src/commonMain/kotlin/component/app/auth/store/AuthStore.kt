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
        data object Login : Intent
        data object Register : Intent
        data object Logout : Intent
        data class UpdateUsername(val username: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data class AttemptLogin(val username: String, val password: String) : Intent
        data class AttemptRegister(val username: String, val email: String, val password: String) : Intent
    }

    @Serializable
    data class State(
        val isLoggedIn: Boolean = false,
        val username: String = "",
        val email: String = "",
        val password: String = "",
        val loading: Boolean = false,
        val error: String? = null,
        val currentScreen: Screen = Screen.LOGIN
    ) {
        @Serializable
        enum class Screen {
            LOGIN, REGISTER, PROFILE
        }
    }
}

internal class AuthStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    fun create(
        onLoginSuccess: () -> Unit,
        onRegisterSuccess: () -> Unit,
        onLogout: () -> Unit
    ): AuthStore =
        object : AuthStore, Store<AuthStore.Intent, AuthStore.State, Nothing> by storeFactory.create(
            name = "AuthStore",
            initialState = AuthStore.State(),
            executorFactory = { ExecutorImpl(onLoginSuccess, onRegisterSuccess, onLogout) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object SetLoading : Msg
        data object ClearLoading : Msg
        data object LoginSuccess : Msg
        data object RegisterSuccess : Msg
        data object LogoutSuccess : Msg
        data class SetError(val error: String) : Msg
        data class UpdateUsername(val username: String) : Msg
        data class UpdatePassword(val password: String) : Msg
        data class UpdateEmail(val email: String) : Msg
        data class SetScreen(val screen: AuthStore.State.Screen) : Msg
    }

    private class ExecutorImpl(
        private val onLoginSuccess: () -> Unit,
        private val onRegisterSuccess: () -> Unit,
        private val onLogout: () -> Unit
    ) : CoroutineExecutor<AuthStore.Intent, Unit, AuthStore.State, Msg, Nothing>(
        rDispatchers.main
    ) {

        override fun executeAction(action: Unit) {
            try {
                // Инициализация, если необходимо
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

        override fun executeIntent(intent: AuthStore.Intent) {
            try {
                when (intent) {
                    is AuthStore.Intent.Init -> {
                        // Инициализация, если необходимо
                    }

                    is AuthStore.Intent.Login -> {
                        scope.launch {
                            try {
                                safeDispatch(Msg.SetLoading)
                                // Здесь будет реальная логика входа
                                kotlinx.coroutines.delay(1000) // Имитация задержки сети
                                safeDispatch(Msg.LoginSuccess)
                                onLoginSuccess()
                            } catch (e: Exception) {
                                safeDispatch(Msg.SetError(e.message ?: "Unknown error"))
                            } finally {
                                safeDispatch(Msg.ClearLoading)
                            }
                        }
                    }

                    is AuthStore.Intent.Register -> {
                        scope.launch {
                            try {
                                safeDispatch(Msg.SetLoading)
                                // Здесь будет реальная логика регистрации
                                kotlinx.coroutines.delay(1000) // Имитация задержки сети
                                safeDispatch(Msg.RegisterSuccess)
                                onRegisterSuccess()
                            } catch (e: Exception) {
                                safeDispatch(Msg.SetError(e.message ?: "Unknown error"))
                            } finally {
                                safeDispatch(Msg.ClearLoading)
                            }
                        }
                    }

                    is AuthStore.Intent.Logout -> {
                        scope.launch {
                            try {
                                safeDispatch(Msg.SetLoading)
                                // Здесь будет реальная логика выхода
                                kotlinx.coroutines.delay(500) // Имитация задержки сети
                                safeDispatch(Msg.LogoutSuccess)
                                onLogout()
                            } catch (e: Exception) {
                                safeDispatch(Msg.SetError(e.message ?: "Unknown error"))
                            } finally {
                                safeDispatch(Msg.ClearLoading)
                            }
                        }
                    }

                    is AuthStore.Intent.UpdateUsername -> {
                        safeDispatch(Msg.UpdateUsername(intent.username))
                    }

                    is AuthStore.Intent.UpdatePassword -> {
                        safeDispatch(Msg.UpdatePassword(intent.password))
                    }

                    is AuthStore.Intent.UpdateEmail -> {
                        safeDispatch(Msg.UpdateEmail(intent.email))
                    }

                    is AuthStore.Intent.AttemptLogin -> {
                        scope.launch {
                            try {
                                safeDispatch(Msg.SetLoading)
                                // Здесь будет реальная логика входа
                                kotlinx.coroutines.delay(1000) // Имитация задержки сети
                                safeDispatch(Msg.LoginSuccess)
                                onLoginSuccess()
                            } catch (e: Exception) {
                                safeDispatch(Msg.SetError(e.message ?: "Unknown error"))
                            } finally {
                                safeDispatch(Msg.ClearLoading)
                            }
                        }
                    }

                    is AuthStore.Intent.AttemptRegister -> {
                        scope.launch {
                            try {
                                safeDispatch(Msg.SetLoading)
                                // Здесь будет реальная логика регистрации
                                kotlinx.coroutines.delay(1000) // Имитация задержки сети
                                safeDispatch(Msg.RegisterSuccess)
                                onRegisterSuccess()
                            } catch (e: Exception) {
                                safeDispatch(Msg.SetError(e.message ?: "Unknown error"))
                            } finally {
                                safeDispatch(Msg.ClearLoading)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }
        }
    }

    private object ReducerImpl : Reducer<AuthStore.State, Msg> {
        override fun AuthStore.State.reduce(msg: Msg): AuthStore.State =
            when (msg) {
                is Msg.SetLoading -> copy(loading = true, error = null)
                is Msg.ClearLoading -> copy(loading = false)
                is Msg.LoginSuccess -> copy(isLoggedIn = true, error = null)
                is Msg.RegisterSuccess -> copy(isLoggedIn = true, error = null)
                is Msg.LogoutSuccess -> copy(
                    isLoggedIn = false,
                    username = "",
                    password = "",
                    email = "",
                    error = null
                )
                is Msg.SetError -> copy(error = msg.error)
                is Msg.UpdateUsername -> copy(username = msg.username)
                is Msg.UpdatePassword -> copy(password = msg.password)
                is Msg.UpdateEmail -> copy(email = msg.email)
                is Msg.SetScreen -> copy(currentScreen = msg.screen)
            }
    }
}
