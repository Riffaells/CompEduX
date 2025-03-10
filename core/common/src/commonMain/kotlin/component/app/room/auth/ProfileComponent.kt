package component.app.room.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.room.auth.store.ProfileStore
import component.app.room.auth.store.ProfileStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface ProfileComponent {
    val state: StateFlow<ProfileStore.State>

    fun onAction(action: ProfileStore.Intent)
    fun onLogout()
    fun onBackClicked()
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    private val onLogout: () -> Unit,
    private val onBack: () -> Unit,
    override val di: DI
) : ProfileComponent, DIAware, ComponentContext by componentContext {

    private val profileStoreFactory: ProfileStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        profileStoreFactory.create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<ProfileStore.State> = store.stateFlow

    override fun onAction(action: ProfileStore.Intent) {
        when (action) {
            is ProfileStore.Intent.Logout -> {
                store.accept(action)
                onLogout()
            }
            else -> store.accept(action)
        }
    }

    override fun onLogout() {
        onLogout()
    }

    override fun onBackClicked() {
        onBack()
    }
}
