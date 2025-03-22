package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import utils.rDispatchers

interface ProfileStore : Store<ProfileStore.Intent, ProfileStore.State, Nothing> {
    sealed interface Intent {
        data object Init : Intent
        data class UpdateUsername(val username: String) : Intent
        data object SaveProfile : Intent
        data object Logout : Intent
    }

    @Serializable
    data class State(
        val username: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )
}

class ProfileStoreFactory(private val storeFactory: StoreFactory) {
    fun create(): ProfileStore =
        object : ProfileStore, Store<ProfileStore.Intent, ProfileStore.State, Nothing> by storeFactory.create(
            name = "ProfileStore",
            initialState = ProfileStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object Loading : Msg
        data object Loaded : Msg
        data class Error(val message: String) : Msg
        data class UpdateUsername(val username: String) : Msg
        data object SaveProfileSuccess : Msg
        data object LogoutSuccess : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ProfileStore.Intent, Unit, ProfileStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            // Инициализация состояния если необходимо
        }

        override fun executeIntent(intent: ProfileStore.Intent) {
            when (intent) {
                is ProfileStore.Intent.Init -> {
                    // Initialize state
                }
                is ProfileStore.Intent.UpdateUsername -> {
                    dispatch(Msg.UpdateUsername(intent.username))
                }
                is ProfileStore.Intent.SaveProfile -> {
                    scope.launch {
                        try {
                            dispatch(Msg.Loading)
                            // Имитируем задержку сети
                            kotlinx.coroutines.delay(1000)
                            dispatch(Msg.SaveProfileSuccess)
                        } catch (e: Exception) {
                            dispatch(Msg.Error(e.message ?: "Ошибка при сохранении профиля"))
                        }
                    }
                }
                is ProfileStore.Intent.Logout -> {
                    scope.launch {
                        try {
                            dispatch(Msg.Loading)
                            // Имитируем задержку сети
                            kotlinx.coroutines.delay(500)
                            dispatch(Msg.LogoutSuccess)
                        } catch (e: Exception) {
                            dispatch(Msg.Error(e.message ?: "Ошибка при выходе"))
                        }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ProfileStore.State, Msg> {
        override fun ProfileStore.State.reduce(msg: Msg): ProfileStore.State =
            when (msg) {
                is Msg.Loading -> copy(loading = true, error = null)
                is Msg.Loaded -> copy(loading = false)
                is Msg.Error -> copy(loading = false, error = msg.message)
                is Msg.UpdateUsername -> copy(username = msg.username)
                is Msg.SaveProfileSuccess -> copy(loading = false, error = null)
                is Msg.LogoutSuccess -> copy(loading = false, error = null)
            }
    }
}
