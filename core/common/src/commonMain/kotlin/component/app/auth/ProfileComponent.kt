package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import component.app.auth.store.ProfileStore
import component.app.auth.store.ProfileStoreFactory
import model.User
import model.AuthResult
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import repository.auth.AuthRepository
import usecase.auth.AuthUseCases
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

    private val authUseCases: AuthUseCases by instance()
    private val storeFactory: StoreFactory by instance()

    // Используем coroutineScope из Essenty вместо CoroutineScope
    private val scope = coroutineScope(rDispatchers.main + SupervisorJob())

    private val profileStore = instanceKeeper.getStore {
        ProfileStoreFactory(storeFactory = storeFactory).create()
    }

    // Initialize user data
    init {
        // Launch on the main dispatcher to avoid thread issues
        scope.launch {
            try {
                // If background work is needed for getCurrentUser, use withContext
                val currentUser = withContext(rDispatchers.io) {
                    authUseCases.getCurrentUser()
                }

                if (currentUser != null) {
                    // This will be executed on the main thread
                    profileStore.accept(ProfileStore.Intent.UpdateUsername(currentUser.username))
                }
            } catch (e: Exception) {
                // Ошибка при загрузке данных пользователя
                println("Error loading user data: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override val state: StateFlow<ProfileComponent.State> = profileStore.stateFlow
        .map { storeState ->
            ProfileComponent.State(
                username = storeState.username,
                loading = storeState.loading,
                error = storeState.error,
                user = null // Will be loaded asynchronously
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
        // Make sure we're on the main thread for UI updates
        scope.launch {
            try {
                profileStore.accept(ProfileStore.Intent.SaveProfile)
                // Run the actual network operation on IO dispatcher
                val result = withContext(rDispatchers.io) {
                    authUseCases.updateProfile(currentUsername)
                }
                // Handle result if needed
            } catch (e: Exception) {
                // Handle error if needed
                println("Error updating profile: ${e.message}")
            }
        }
    }

    override fun onLogout() {
        // Make sure we're on the main thread
        scope.launch {
            profileStore.accept(ProfileStore.Intent.Logout)
            // Run the actual logout operation on IO dispatcher
            try {
                withContext(rDispatchers.io) {
                    authUseCases.logout()
                }
                // This callback will also run on the main thread
                onLogout()
            } catch (e: Exception) {
                println("Error during logout: ${e.message}")
            }
        }
    }

    override fun onBackClicked() {
        onBackClicked()
    }
}
