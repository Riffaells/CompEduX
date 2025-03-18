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

interface RegisterStore : Store<RegisterStore.Intent, RegisterStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data class UpdateUsername(val username: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdatePassword(val password: String) : Intent
        data class UpdateConfirmPassword(val confirmPassword: String) : Intent
        data object Register : Intent
        data object NavigateToLogin : Intent
        data object Back : Intent
    }

    @Serializable
    data class State(
        val username: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )
}

internal class RegisterStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    fun create(
        onRegisterSuccess: () -> Unit,
        onNavigateToLogin: () -> Unit,
        onBack: () -> Unit
    ): RegisterStore =
        object : RegisterStore, Store<RegisterStore.Intent, RegisterStore.State, Nothing> by storeFactory.create(
            name = "RegisterStore",
            initialState = RegisterStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(onRegisterSuccess, onNavigateToLogin, onBack) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object SetLoading : Msg
        data object ClearLoading : Msg
        data class SetError(val error: String) : Msg
        data class UpdateUsername(val username: String) : Msg
        data class UpdateEmail(val email: String) : Msg
        data class UpdatePassword(val password: String) : Msg
        data class UpdateConfirmPassword(val confirmPassword: String) : Msg
    }

    private inner class ExecutorImpl(
        private val onRegisterSuccess: () -> Unit,
        private val onNavigateToLogin: () -> Unit,
        private val onBack: () -> Unit
    ) : CoroutineExecutor<RegisterStore.Intent, Unit, RegisterStore.State, Msg, Nothing>(
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

        override fun executeIntent(intent: RegisterStore.Intent): Unit {
            when (intent) {
                is RegisterStore.Intent.Init -> {
                    // Инициализация, если необходимо
                }

                is RegisterStore.Intent.Register -> {
                    scope.launch {
                        try {
                            safeDispatch(Msg.SetLoading)
                            // Здесь будет реальная логика регистрации
                            kotlinx.coroutines.delay(1000) // Имитация задержки сети
                            onRegisterSuccess()
                        } catch (e: Exception) {
                            safeDispatch(Msg.SetError(e.message ?: "Unknown error"))
                        } finally {
                            safeDispatch(Msg.ClearLoading)
                        }
                    }
                }

                is RegisterStore.Intent.NavigateToLogin -> {
                    onNavigateToLogin()
                }

                is RegisterStore.Intent.Back -> {
                    onBack()
                }

                is RegisterStore.Intent.UpdateUsername -> {
                    safeDispatch(Msg.UpdateUsername(intent.username))
                }

                is RegisterStore.Intent.UpdateEmail -> {
                    safeDispatch(Msg.UpdateEmail(intent.email))
                }

                is RegisterStore.Intent.UpdatePassword -> {
                    safeDispatch(Msg.UpdatePassword(intent.password))
                }

                is RegisterStore.Intent.UpdateConfirmPassword -> {
                    safeDispatch(Msg.UpdateConfirmPassword(intent.confirmPassword))
                }
            }

        }
    }

    private object ReducerImpl : Reducer<RegisterStore.State, Msg> {
        override fun RegisterStore.State.reduce(msg: Msg): RegisterStore.State =
            when (msg) {
                is Msg.SetLoading -> copy(loading = true, error = null)
                is Msg.ClearLoading -> copy(loading = false)
                is Msg.SetError -> copy(error = msg.error)
                is Msg.UpdateUsername -> copy(username = msg.username)
                is Msg.UpdateEmail -> copy(email = msg.email)
                is Msg.UpdatePassword -> copy(password = msg.password)
                is Msg.UpdateConfirmPassword -> copy(confirmPassword = msg.confirmPassword)
            }
    }
}
