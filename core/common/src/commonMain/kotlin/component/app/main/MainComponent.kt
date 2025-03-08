package component.app.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.main.store.MainStore
import component.app.main.store.MainStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface MainComponent {
    val state: StateFlow<MainStore.State>

    fun onAction(action: MainStore.Intent)
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val onSettingsClicked: () -> Unit,
    override val di: DI
) : MainComponent, DIAware, ComponentContext by componentContext {

    private val mainStoreFactory: MainStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        mainStoreFactory.create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<MainStore.State> = store.stateFlow

    override fun onAction(action: MainStore.Intent) {
        when (action) {
            is MainStore.Intent.OpenSettings -> onSettingsClicked()
            else -> store.accept(action)
        }
    }
}
