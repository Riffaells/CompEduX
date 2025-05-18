package component.app.room.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.room.detail.store.RoomDetailStore
import component.app.room.detail.store.RoomDetailStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

data class RoomDetailComponentParams(
    val componentContext: ComponentContext,
    val roomId: String,
    val onBack: () -> Unit
)

interface RoomDetailComponent {
    val state: StateFlow<RoomDetailStore.State>
    
    fun onAction(action: RoomDetailStore.Intent)
    fun onBackClicked()
    fun updateRoomName(name: String)
    fun updateRoomDescription(description: String)
    fun saveRoom()
    fun deleteRoom()
}

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultRoomDetailComponent(
    private val params: RoomDetailComponentParams,
    override val di: DI
) : RoomDetailComponent, DIAware, ComponentContext by params.componentContext {

    private val roomDetailStoreFactory by instance<RoomDetailStoreFactory>()
    private val store = instanceKeeper.getStore {
        roomDetailStoreFactory.create(params.roomId)
    }
    
    // Создаем scope, связанный с жизненным циклом компонента
    private val scope = coroutineScope(rDispatchers.main)

    override val state: StateFlow<RoomDetailStore.State> = store.stateFlow

    init {
        // Используем корутину для сбора labels
        scope.launch {
            store.labels.collect { label ->
                // Handle labels if needed
            }
        }
    }

    override fun onAction(action: RoomDetailStore.Intent) {
        store.accept(action)
    }

    override fun onBackClicked() {
        params.onBack()
    }

    override fun updateRoomName(name: String) {
        store.accept(RoomDetailStore.Intent.UpdateName(name))
    }

    override fun updateRoomDescription(description: String) {
        store.accept(RoomDetailStore.Intent.UpdateDescription(description))
    }

    override fun saveRoom() {
        store.accept(RoomDetailStore.Intent.SaveRoom)
    }

    override fun deleteRoom() {
        store.accept(RoomDetailStore.Intent.DeleteRoom)
    }
} 