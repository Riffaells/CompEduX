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


/**
 * Параметры для создания компонента главного экрана
 */
data class MainComponentParams(
    val componentContext: ComponentContext,
    val onSettingsClicked: () -> Unit,
    val onDevelopmentMapClicked: () -> Unit,
    val onRoomClicked: () -> Unit = {}
)


interface MainComponent {
    val state: StateFlow<MainStore.State>

    fun onAction(action: MainStore.Intent)
    fun onSettingsClicked()
    fun onDevelopmentMapClicked()
    fun onRoomClicked()
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val onSettings: () -> Unit,
    private val onDevelopmentMap: () -> Unit,
    private val onRoom: () -> Unit,
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

    override fun onSettingsClicked() {
        onSettings.invoke()
    }

    override fun onDevelopmentMapClicked() {
        onDevelopmentMap.invoke()
    }

    override fun onRoomClicked() {
        onRoom.invoke()
    }
}
