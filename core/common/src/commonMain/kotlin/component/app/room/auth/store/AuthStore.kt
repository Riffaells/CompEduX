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

    fun create(): AuthStore =
        object : AuthStore, Store<AuthStore.Intent, AuthStore.State, Nothing> by storeFactory.create(
            name = "AuthStore",
            initialState = AuthStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
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

    private inner class ExecutorImpl :
        CoroutineExecutor<AuthStore.Intent, Unit, AuthStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                // Initial setup if needed
                dispatch(Msg.ClearLoading)
            } catch (e: Exception) {
                println("Error in executeAction: ${e.message}")
            }
        }

        // Safe dispatch method to catch exceptions
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
                        // Initialize the store
                        safeDispatch(Msg.ClearLoading)
                    }
                    is AuthStore.Intent.Login -> {
                        safeDispatch(Msg.SetScreen(AuthStore.State.Screen.LOGIN))
                    }
                    is AuthStore.Intent.Register -> {
                        safeDispatch(Msg.SetScreen(AuthStore.State.Screen.REGISTER))
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

                                // Simulate API call for login
                                // In a real app, you would call your authentication service here
                                kotlinx.coroutines.delay(1000) // Simulate network delay

                                // For demo purposes, we'll just check if username and password are not empty
                                if (intent.username.isNotBlank() && intent.password.isNotBlank()) {
                                    safeDispatch(Msg.LoginSuccess)
                                    safeDispatch(Msg.SetScreen(AuthStore.State.Screen.PROFILE))
                                } else {
                                    safeDispatch(Msg.SetError("Invalid username or password"))
                                }

                                safeDispatch(Msg.ClearLoading)
                            } catch (e: Exception) {
                                safeDispatch(Msg.SetError("Login failed: ${e.message}"))
                                safeDispatch(Msg.ClearLoading)
                                println("Error during login: ${e.message}")
                            }
                        }
                    }
                    is AuthStore.Intent.AttemptRegister -> {
                        scope.launch {
                            try {
                                safeDispatch(Msg.SetLoading)

                                // Simulate API call for registration
                                // In a real app, you would call your registration service here
                                kotlinx.coroutines.delay(1000) // Simulate network delay

                                // For demo purposes, we'll just check if all fields are not empty
                                if (intent.username.isNotBlank() && intent.email.isNotBlank() && intent.password.isNotBlank()) {
                                    safeDispatch(Msg.RegisterSuccess)
                                    safeDispatch(Msg.SetScreen(AuthStore.State.Screen.PROFILE))
                                } else {
                                    safeDispatch(Msg.SetError("All fields are required"))
                                }

                                safeDispatch(Msg.ClearLoading)
                            } catch (e: Exception) {
                                safeDispatch(Msg.SetError("Registration failed: ${e.message}"))
                                safeDispatch(Msg.ClearLoading)
                                println("Error during registration: ${e.message}")
                            }
                        }
                    }
                    is AuthStore.Intent.Logout -> {
                        scope.launch {
                            try {
                                safeDispatch(Msg.SetLoading)

                                // Simulate logout process
                                kotlinx.coroutines.delay(500) // Simulate network delay

                                safeDispatch(Msg.LogoutSuccess)
                                safeDispatch(Msg.SetScreen(AuthStore.State.Screen.LOGIN))
                                safeDispatch(Msg.ClearLoading)
                            } catch (e: Exception) {
                                safeDispatch(Msg.SetError("Logout failed: ${e.message}"))
                                safeDispatch(Msg.ClearLoading)
                                println("Error during logout: ${e.message}")
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
