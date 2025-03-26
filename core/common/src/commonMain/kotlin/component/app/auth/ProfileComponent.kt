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
import kotlinx.coroutines.launch
import component.app.auth.store.ProfileStore
import component.app.auth.store.ProfileStoreFactory
import model.User
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import repository.auth.AuthRepository
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
        val error: String? = null,
        val user: User? = null
    )
}

/**
 * Реализация компонента профиля
 */
class DefaultProfileComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onLogout: () -> Unit,
    private val onBackClicked: () -> Unit
) : ProfileComponent, DIAware, ComponentContext by componentContext {

    private val authRepository: AuthRepository by instance()
    private val storeFactory: StoreFactory by instance()

    private val scope = CoroutineScope(rDispatchers.main + SupervisorJob())

    private val profileStore = instanceKeeper.getStore {
        ProfileStoreFactory(storeFactory = storeFactory).create()
    }

    // Initialize user data
    init {
        scope.launch {
            val currentUser = authRepository.getCurrentUser()
            currentUser?.let {
                profileStore.accept(ProfileStore.Intent.UpdateUsername(it.username))
            }
        }
    }

    override val state: StateFlow<ProfileComponent.State> = profileStore.stateFlow
        .map { storeState ->
            ProfileComponent.State(
                username = storeState.username,
                loading = storeState.loading,
                error = storeState.error,
                user = authRepository.getCurrentUser()
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
        scope.launch {
            try {
                profileStore.accept(ProfileStore.Intent.SaveProfile)
                authRepository.updateProfile(currentUsername)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    override fun onLogout() {
        scope.launch {
            profileStore.accept(ProfileStore.Intent.Logout)
            authRepository.logout()
            onLogout()
        }
    }

    override fun onBackClicked() {
        onBackClicked()
    }
}
