package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import utils.rDispatchers

interface AuthStore : Store<AuthStore.Intent, AuthStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data class Login(val email: String, val password: String) : Intent
        data class Register(val email: String, val password: String, val username: String) : Intent
        data object Logout : Intent
        data class UpdateProfile(val username: String) : Intent
    }

    @Serializable
    data class State(
        val isAuthenticated: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val currentUser: User? = null
    )

    @Serializable
    data class User(
        val id: String,
        val email: String,
        val username: String
    )
}

class AuthStoreFactory(
    private val storeFactory: StoreFactory
) {
    fun create(): AuthStore =
        object : AuthStore, Store<AuthStore.Intent, AuthStore.State, Nothing> by storeFactory.create(
            name = "AuthStore",
            initialState = AuthStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object Loading : Msg
        data object Loaded : Msg
        data class Error(val message: String) : Msg
        data class LoginSuccess(val user: AuthStore.User) : Msg
        data class RegisterSuccess(val user: AuthStore.User) : Msg
        data object LogoutSuccess : Msg
        data class UpdateProfileSuccess(val user: AuthStore.User) : Msg
    }

    private class ExecutorImpl :
        CoroutineExecutor<AuthStore.Intent, Unit, AuthStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            // Initialize auth state if needed
        }

        override fun executeIntent(intent: AuthStore.Intent) {
            when (intent) {
                is AuthStore.Intent.Init -> {
                    // Initialize auth state
                }
                is AuthStore.Intent.Login -> {
                    scope.launch {
                        try {
                            dispatch(Msg.Loading)
                            // TODO: Implement actual API call
                            // Simulating API delay
                            kotlinx.coroutines.delay(1000)
                            dispatch(Msg.LoginSuccess(
                                AuthStore.User(
                                    id = "1",
                                    email = intent.email,
                                    username = "User"
                                )
                            ))
                        } catch (e: Exception) {
                            dispatch(Msg.Error(e.message ?: "Login failed"))
                        }
                    }
                }
                is AuthStore.Intent.Register -> {
                    scope.launch {
                        try {
                            dispatch(Msg.Loading)
                            // TODO: Implement actual API call
                            // Simulating API delay
                            kotlinx.coroutines.delay(1000)
                            dispatch(Msg.RegisterSuccess(
                                AuthStore.User(
                                    id = "1",
                                    email = intent.email,
                                    username = intent.username
                                )
                            ))
                        } catch (e: Exception) {
                            dispatch(Msg.Error(e.message ?: "Registration failed"))
                        }
                    }
                }
                is AuthStore.Intent.Logout -> {
                    scope.launch {
                        try {
                            dispatch(Msg.Loading)
                            // TODO: Implement actual API call
                            // Simulating API delay
                            kotlinx.coroutines.delay(500)
                            dispatch(Msg.LogoutSuccess)
                        } catch (e: Exception) {
                            dispatch(Msg.Error(e.message ?: "Logout failed"))
                        }
                    }
                }
                is AuthStore.Intent.UpdateProfile -> {
                    scope.launch {
                        try {
                            dispatch(Msg.Loading)
                            // TODO: Implement actual API call
                            // Simulating API delay
                            kotlinx.coroutines.delay(1000)

                            // Получаем текущего пользователя из состояния
                            val currentUser = state().currentUser ?: throw IllegalStateException("User not authenticated")

                            dispatch(Msg.UpdateProfileSuccess(
                                AuthStore.User(
                                    id = currentUser.id,
                                    email = currentUser.email,
                                    username = intent.username
                                )
                            ))
                        } catch (e: Exception) {
                            dispatch(Msg.Error(e.message ?: "Profile update failed"))
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<AuthStore.State, Msg> {
        override fun AuthStore.State.reduce(msg: Msg): AuthStore.State =
            when (msg) {
                is Msg.Loading -> copy(isLoading = true, error = null)
                is Msg.Loaded -> copy(isLoading = false)
                is Msg.Error -> copy(isLoading = false, error = msg.message)
                is Msg.LoginSuccess -> copy(
                    isLoading = false,
                    isAuthenticated = true,
                    currentUser = msg.user,
                    error = null
                )
                is Msg.RegisterSuccess -> copy(
                    isLoading = false,
                    isAuthenticated = true,
                    currentUser = msg.user,
                    error = null
                )
                is Msg.LogoutSuccess -> copy(
                    isLoading = false,
                    isAuthenticated = false,
                    currentUser = null,
                    error = null
                )
                is Msg.UpdateProfileSuccess -> copy(
                    isLoading = false,
                    currentUser = msg.user,
                    error = null
                )
            }
    }
}
