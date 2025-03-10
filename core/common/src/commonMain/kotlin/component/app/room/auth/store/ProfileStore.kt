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

interface ProfileStore : Store<ProfileStore.Intent, ProfileStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data class UpdateUsername(val username: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data object SaveProfile : Intent
        data object Logout : Intent
    }

    @Serializable
    data class State(
        val username: String = "User",
        val email: String = "user@example.com",
        val isLoading: Boolean = false,
        val error: String? = null,
        val isEditing: Boolean = false
    )
}

internal class ProfileStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    fun create(): ProfileStore =
        object : ProfileStore, Store<ProfileStore.Intent, ProfileStore.State, Nothing> by storeFactory.create(
            name = "ProfileStore",
            initialState = ProfileStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data class UpdateUsername(val username: String) : Msg
        data class UpdateEmail(val email: String) : Msg
        data class SetError(val error: String?) : Msg
        data object ClearError : Msg
        data class SetEditing(val isEditing: Boolean) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ProfileStore.Intent, Unit, ProfileStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            // Загрузка профиля пользователя
            dispatch(Msg.LoadingData)
            // В реальном приложении здесь был бы запрос к API
            // Для примера просто используем значения по умолчанию
        }

        override fun executeIntent(intent: ProfileStore.Intent) {
            when (intent) {
                is ProfileStore.Intent.Init -> Unit
                is ProfileStore.Intent.UpdateUsername -> {
                    dispatch(Msg.UpdateUsername(intent.username))
                    dispatch(Msg.ClearError)
                }
                is ProfileStore.Intent.UpdateEmail -> {
                    dispatch(Msg.UpdateEmail(intent.email))
                    dispatch(Msg.ClearError)
                }
                is ProfileStore.Intent.SaveProfile -> {
                    scope.launch {
                        dispatch(Msg.LoadingData)

                        // Здесь будет логика сохранения профиля
                        // Для примера просто проверяем, что поля не пустые
                        val state = state()
                        if (state.username.isEmpty() || state.email.isEmpty()) {
                            dispatch(Msg.SetError("Имя пользователя и email не могут быть пустыми"))
                        } else {
                            // Успешное сохранение
                            dispatch(Msg.ClearError)
                            dispatch(Msg.SetEditing(false))
                        }
                    }
                }
                is ProfileStore.Intent.Logout -> {
                    // Логика выхода обрабатывается в компоненте
                    Unit
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ProfileStore.State, Msg> {
        override fun ProfileStore.State.reduce(msg: Msg): ProfileStore.State =
            when (msg) {
                is Msg.LoadingData -> copy(isLoading = true)
                is Msg.UpdateUsername -> copy(username = msg.username)
                is Msg.UpdateEmail -> copy(email = msg.email)
                is Msg.SetError -> copy(error = msg.error, isLoading = false)
                is Msg.ClearError -> copy(error = null)
                is Msg.SetEditing -> copy(isEditing = msg.isEditing, isLoading = false)
            }
    }
}
