package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import component.app.auth.store.ProfileStore
import component.app.auth.store.ProfileStoreFactory
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.auth.AuthUseCases
import utils.rDispatchers

/**
 * Component for the profile screen
 */
interface ProfileComponent {
    val state: StateFlow<ProfileStore.State>

    fun onUsernameChange(username: String)
    fun onUpdateProfile()
    fun onLogout()
    fun onBackClicked()
}

/**
 * Implementation of the profile component
 */
class DefaultProfileComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onLogout: () -> Unit,
    private val onBackClicked: () -> Unit
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

    override fun onUsernameChange(username: String) {
        profileStore.accept(ProfileStore.Intent.UpdateUsername(username))
    }

    override fun onUpdateProfile() {
        profileStore.accept(ProfileStore.Intent.SaveProfile)
    }

    override fun onLogout() {
        profileStore.accept(ProfileStore.Intent.Logout)
        scope.launch {
            // Wait for the logout to complete (the store will handle the actual API call)
            // Then trigger navigation
            onLogout()
        }
    }

    override fun onBackClicked() {
        onBackClicked()
    }
}
