package component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Параметры для создания компонента Skiko
 */
data class TechnologyTreeComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)


interface TechnologyTreeComponent {
    val state: StateFlow<TechnologyTreeStore.State>

    fun onEvent(event: TechnologyTreeStore.Intent)
    fun onBackClicked()
}

class DefaultTechnologyTreeComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    override val di: DI
) : TechnologyTreeComponent, DIAware, ComponentContext by componentContext {

    private val technologyTreeStoreFactory by instance<TechnologyTreeStoreFactory>()

    private val store = instanceKeeper.getStore {
        technologyTreeStoreFactory.create()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<TechnologyTreeStore.State> = store.stateFlow

    override fun onEvent(event: TechnologyTreeStore.Intent) {
        store.accept(event)
    }

    override fun onBackClicked() {
        onBack()
    }
}
