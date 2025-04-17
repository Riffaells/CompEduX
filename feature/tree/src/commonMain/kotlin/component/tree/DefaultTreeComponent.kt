package component.tree

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.tree.store.TreeStore
import component.tree.store.TreeStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Реализация компонента дерева развития по умолчанию
 */
class DefaultTreeComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    override val di: DI
) : TreeComponent, DIAware, ComponentContext by componentContext {

    private val treeStoreFactory by instance<TreeStoreFactory>()

    private val store = instanceKeeper.getStore {
        treeStoreFactory.create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<TreeStore.State> = store.stateFlow

    override fun onEvent(event: TreeStore.Intent) {
        store.accept(event)
    }

    override fun onBackClicked() {
        onBack()
    }
}
