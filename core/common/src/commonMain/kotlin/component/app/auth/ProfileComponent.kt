package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.store.ProfileStore
import component.app.auth.store.ProfileStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface ProfileComponent {
    val state: StateFlow<ProfileStore.State>
    fun onEvent(event: ProfileStore.Intent)
}

class DefaultProfileComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onLogout: () -> Unit,
    private val onBack: () -> Unit
) : ProfileComponent, DIAware, ComponentContext by componentContext {

    private val profileStoreFactory: ProfileStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        profileStoreFactory.create(
            onLogout = onLogout,
            onBack = onBack
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<ProfileStore.State> = store.stateFlow

    override fun onEvent(event: ProfileStore.Intent) {
        store.accept(event)
    }
}
