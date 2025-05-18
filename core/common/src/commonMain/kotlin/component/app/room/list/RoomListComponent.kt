package component.app.room.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.room.list.store.RoomListStore
import component.app.room.list.store.RoomListStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Параметры для создания RoomListComponent
 */
data class RoomListComponentParams(
    val componentContext: ComponentContext,
    val onRoomSelected: (String) -> Unit
)

/**
 * Компонент для отображения списка комнат
 */
interface RoomListComponent {
    /**
     * Текущее состояние компонента
     */
    val state: StateFlow<RoomListStore.State>

    /**
     * Обрабатывает действия пользователя
     * @param action действие пользователя
     */
    fun onAction(action: RoomListStore.Intent)

    /**
     * Обрабатывает выбор комнаты
     * @param roomId идентификатор выбранной комнаты
     */
    fun selectRoom(roomId: String)

    /**
     * Обрабатывает запрос на создание новой комнаты
     */
    fun createRoom()

    /**
     * Обрабатывает запрос на обновление списка комнат
     */
    fun refresh()

    /**
     * Обрабатывает запрос на вход в комнату по коду
     * @param code код комнаты
     */
    fun joinRoom(code: String)

    /**
     * Обрабатывает запрос на фильтрацию комнат
     * @param query поисковый запрос
     */
    fun filterRooms(query: String)
}

/**
 * Реализация компонента списка комнат
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultRoomListComponent(
    private val params: RoomListComponentParams,
    override val di: DI
) : RoomListComponent, DIAware, ComponentContext by params.componentContext {

    private val roomListStoreFactory by instance<RoomListStoreFactory>()
    private val store = instanceKeeper.getStore {
        roomListStoreFactory.create()
    }

    override val state: StateFlow<RoomListStore.State> = store.stateFlow

    init {
        store.accept(RoomListStore.Intent.Init)
    }

    override fun onAction(action: RoomListStore.Intent) {
        store.accept(action)
        
        // Handle specific actions that require navigation
        when (action) {
            is RoomListStore.Intent.SelectRoom -> {
                params.onRoomSelected(action.roomId)
            }
            else -> {
                // Other actions are handled by the store
            }
        }
    }

    override fun selectRoom(roomId: String) {
        store.accept(RoomListStore.Intent.SelectRoom(roomId))
        params.onRoomSelected(roomId)
    }

    override fun createRoom() {
        store.accept(RoomListStore.Intent.CreateRoom)
    }

    override fun joinRoom(code: String) {
        store.accept(RoomListStore.Intent.JoinRoom(code))
    }

    override fun filterRooms(query: String) {
        store.accept(RoomListStore.Intent.FilterRooms(query))
    }

    override fun refresh() {
        store.accept(RoomListStore.Intent.LoadRooms)
        store.accept(RoomListStore.Intent.LoadMyRooms)
    }
} 