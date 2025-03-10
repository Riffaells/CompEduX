package component.app.room.diagram

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.room.diagram.store.DiagramStore
import component.app.room.diagram.store.DiagramStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface DiagramComponent {
    val state: StateFlow<DiagramStore.State>

    fun onAction(action: DiagramStore.Intent)
}

class DefaultDiagramComponent(
    componentContext: ComponentContext,
    override val di: DI
) : DiagramComponent, DIAware, ComponentContext by componentContext {

    private val diagramStoreFactory: DiagramStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        diagramStoreFactory.create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<DiagramStore.State> = store.stateFlow

    override fun onAction(action: DiagramStore.Intent) {
        store.accept(action)
    }
}
