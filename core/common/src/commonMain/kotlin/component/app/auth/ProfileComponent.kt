package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.Store
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

    fun onEditProfileClicked()
    fun onChangePasswordClicked()
    fun onLogoutClicked()
    fun onBackClicked()
    fun onSaveProfileClicked()
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

    override fun onEditProfileClicked() {
        store.accept(ProfileStore.Intent.EditProfile)
    }

    override fun onChangePasswordClicked() {
        store.accept(ProfileStore.Intent.ChangePassword)
    }

    override fun onLogoutClicked() {
        store.accept(ProfileStore.Intent.Logout)
        onLogout()
    }

    override fun onBackClicked() {
        onBack()
    }

    override fun onSaveProfileClicked() {
        store.accept(ProfileStore.Intent.SaveProfile)
    }
}
