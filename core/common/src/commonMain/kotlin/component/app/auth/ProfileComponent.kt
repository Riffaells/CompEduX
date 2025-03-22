package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import component.app.auth.store.ProfileStore
import component.app.auth.store.ProfileStoreFactory
import utils.rDispatchers

/**
 * Компонент для экрана профиля
 */
interface ProfileComponent {
    val state: StateFlow<State>

    fun onUsernameChange(username: String)
    fun onUpdateProfile()
    fun onLogout()
    fun onBackClicked()

    data class State(
        val username: String = "",
        val loading: Boolean = false,
        val error: String? = null
    )
}

/**
 * Реализация компонента профиля
 */
class DefaultProfileComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val onLogout: () -> Unit,
    private val onUpdateProfile: (String) -> Unit,
    private val onBackClicked: () -> Unit
) : ProfileComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(rDispatchers.main + SupervisorJob())

    private val profileStore = instanceKeeper.getStore {
        ProfileStoreFactory(storeFactory = storeFactory).create()
    }

    override val state: StateFlow<ProfileComponent.State> = profileStore.stateFlow
        .map { storeState ->
            ProfileComponent.State(
                username = storeState.username,
                loading = storeState.loading,
                error = storeState.error
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = ProfileComponent.State()
        )

    override fun onUsernameChange(username: String) {
        profileStore.accept(ProfileStore.Intent.UpdateUsername(username))
    }

    override fun onUpdateProfile() {
        val currentUsername = profileStore.state.username
        onUpdateProfile(currentUsername)
    }

    override fun onLogout() {
        onLogout()
    }

    override fun onBackClicked() {
        onBackClicked()
    }
}
