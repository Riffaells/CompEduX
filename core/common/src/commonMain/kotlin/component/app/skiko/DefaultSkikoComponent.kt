package component.app.skiko

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.skiko.store.SkikoStore
import component.app.skiko.store.SkikoStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Параметры для создания компонента Skiko
 */
data class SkikoComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)


interface SkikoComponent {
    val state: StateFlow<SkikoStore.State>

    fun onEvent(event: SkikoStore.Intent)
    fun onBackClicked()
}

class DefaultSkikoComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    override val di: DI
) : SkikoComponent, DIAware, ComponentContext by componentContext {

    private val skikoStoreFactory by instance<SkikoStoreFactory>()

    private val store = instanceKeeper.getStore {
        skikoStoreFactory.create()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<SkikoStore.State> = store.stateFlow

    override fun onEvent(event: SkikoStore.Intent) {
        store.accept(event)
    }

    override fun onBackClicked() {
        onBack()
    }
}
