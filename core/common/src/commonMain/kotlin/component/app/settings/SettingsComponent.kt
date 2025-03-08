package component.app.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.app.settings.store.SettingsStore
import component.app.settings.store.SettingsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface SettingsComponent {
    val state: StateFlow<SettingsStore.State>

    fun onAction(action: SettingsStore.Intent)
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    override val di: DI
) : SettingsComponent, DIAware, ComponentContext by componentContext {

    private val settingsStoreFactory by instance<SettingsStoreFactory>()

    private val store = instanceKeeper.getStore {

        SettingsStoreFactory(
            storeFactory = DefaultStoreFactory(),
            di = di
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<SettingsStore.State> = store.stateFlow

    override fun onAction(action: SettingsStore.Intent) {
        when (action) {
            is SettingsStore.Intent.Back -> onBack()
            else -> store.accept(action)
        }
    }
}
