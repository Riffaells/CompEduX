package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.store.ProfileStore
import component.app.auth.store.ProfileStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.auth.AuthUseCases

/**
 * Component for the profile screen
 */
interface ProfileComponent {
    val state: StateFlow<ProfileStore.State>

    fun accept(intent: ProfileStore.Intent)
    fun onLogout()
}

/**
 * Implementation of the profile component
 */
class DefaultProfileComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onLogoutClicked: () -> Unit,
) : ProfileComponent, DIAware, ComponentContext by componentContext {

    private val authUseCases: AuthUseCases by instance()
    private val storeFactory: StoreFactory by instance()

    private val scope = CoroutineScope(rDispatchers.main + SupervisorJob())

    private val profileStore = instanceKeeper.getStore {
        ProfileStoreFactory(
            storeFactory = storeFactory,
            di = di
        ).create()
    }

    override val state: StateFlow<ProfileStore.State> = profileStore.stateFlow

    override fun accept(intent: ProfileStore.Intent) {
        profileStore.accept(intent)
    }

    /**
     * Handles logout action
     * This method is designed to guarantee navigation to login screen
     */
    override fun onLogout() {
        // First trigger the callback that will control navigation
        onLogoutClicked()
        
        // Then handle local state cleanup
        scope.launch {
            try {
                // Force logout in auth use cases
                authUseCases.logout()
            } catch (e: Exception) {
                // Ignore exceptions - logout should proceed even with errors
            } finally {
                // Notify store that logout happened
                profileStore.accept(ProfileStore.Intent.Logout)
            }
        }
    }
}
