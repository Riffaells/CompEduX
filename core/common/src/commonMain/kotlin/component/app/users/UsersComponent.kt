package component.app.users

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import navigation.rDispatchers
import org.kodein.di.DI


data class UsersComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit,
)


interface UsersComponent {

    val state: StateFlow<UsersStore.State>


    fun onBack()


}


class DefaultUsersComponent(
    componentContext: ComponentContext,
    private val di: DI,
    storeFactory: StoreFactory,
    courseId: String?,
    private val onBackClicked: () -> Unit
) : UsersComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + rDispatchers.main)


    private val store = instanceKeeper.getStore {
        UsersStoreFactory(
            storeFactory = storeFactory,
            di = di
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<UsersStore.State> = store.stateFlow

    init {
        scope.launch {
            store.labels.collectLatest { label ->
                when (label) {
                    else -> {}
                }
            }
        }
    }

    override fun onBack() {
        onBackClicked()
    }


    fun destroy() {
        scope.cancel()
    }
} 