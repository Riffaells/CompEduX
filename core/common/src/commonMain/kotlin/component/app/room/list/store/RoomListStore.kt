package component.app.room.list.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import model.DomainResult
import model.room.RoomDomain
import model.room.RoomQueryParams
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import repository.room.RoomRepository

interface RoomListStore : Store<RoomListStore.Intent, RoomListStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object LoadRooms : Intent
        data object LoadMyRooms : Intent
        data object CreateRoom : Intent
        data class FilterRooms(val query: String) : Intent
        data class SelectRoom(val roomId: String) : Intent
        data class JoinRoom(val code: String) : Intent
    }

    @Serializable
    data class State(
        val rooms: List<RoomDomain> = emptyList(),
        val myRooms: List<RoomDomain> = emptyList(),
        val selectedRoomId: String? = null,
        val filterQuery: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
        val showMyRooms: Boolean = false
    )
}

internal class RoomListStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val roomRepository by instance<RoomRepository>()
    private val logger by instance<Logger>()

    fun create(): RoomListStore =
        object : RoomListStore, Store<RoomListStore.Intent, RoomListStore.State, Nothing> by storeFactory.create(
            name = "RoomListStore",
            initialState = RoomListStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(logger.withTag("RoomListStore")) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object StartLoading : Msg
        data class RoomsLoaded(val rooms: List<RoomDomain>) : Msg
        data class MyRoomsLoaded(val rooms: List<RoomDomain>) : Msg
        data class RoomSelected(val roomId: String) : Msg
        data class FilterChanged(val query: String) : Msg
        data class ErrorOccurred(val error: String) : Msg
        data class ToggleMyRooms(val show: Boolean) : Msg
    }

    private inner class ExecutorImpl(
        private val logger: Logger
    ) : CoroutineExecutor<RoomListStore.Intent, Unit, RoomListStore.State, Msg, Nothing>(
        rDispatchers.main
    ) {

        override fun executeAction(action: Unit) {
            loadRooms()
            loadMyRooms()
        }

        private fun loadRooms() {
            scope.launch {
                dispatch(Msg.StartLoading)
                try {
                    val params = RoomQueryParams(
                        page = 0,
                        size = 50,
                        search = state().filterQuery.takeIf { it.isNotBlank() }
                    )
                    
                    when (val result = roomRepository.getRooms(params)) {
                        is DomainResult.Success -> {
                            dispatch(Msg.RoomsLoaded(result.data.items))
                        }
                        is DomainResult.Error -> {
                            dispatch(Msg.ErrorOccurred(result.error.message ?: "Failed to load rooms"))
                        }
                        else -> {
                            // Ignore loading state
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Error loading rooms: ${e.message}")
                    dispatch(Msg.ErrorOccurred(e.message ?: "Unknown error occurred"))
                }
            }
        }

        private fun loadMyRooms() {
            scope.launch {
                try {
                    val params = RoomQueryParams(
                        page = 0,
                        size = 50,
                        search = state().filterQuery.takeIf { it.isNotBlank() }
                    )
                    
                    when (val result = roomRepository.getMyRooms(params)) {
                        is DomainResult.Success -> {
                            dispatch(Msg.MyRoomsLoaded(result.data.items))
                        }
                        is DomainResult.Error -> {
                            logger.w("Failed to load my rooms: ${result.error.message}")
                            // Don't show error for my rooms, just log it
                        }
                        else -> {
                            // Ignore loading state
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Error loading my rooms: ${e.message}")
                }
            }
        }

        override fun executeIntent(intent: RoomListStore.Intent) {
            when (intent) {
                is RoomListStore.Intent.Init -> {
                    loadRooms()
                    loadMyRooms()
                }
                is RoomListStore.Intent.LoadRooms -> {
                    loadRooms()
                }
                is RoomListStore.Intent.LoadMyRooms -> {
                    loadMyRooms()
                }
                is RoomListStore.Intent.FilterRooms -> {
                    dispatch(Msg.FilterChanged(intent.query))
                    loadRooms()
                    loadMyRooms()
                }
                is RoomListStore.Intent.SelectRoom -> {
                    dispatch(Msg.RoomSelected(intent.roomId))
                }
                is RoomListStore.Intent.CreateRoom -> {
                    // This will be handled by the parent component
                    logger.d("Create room intent received")
                }
                is RoomListStore.Intent.JoinRoom -> {
                    joinRoom(intent.code)
                }
            }
        }

        private fun joinRoom(code: String) {
            scope.launch {
                dispatch(Msg.StartLoading)
                try {
                    val joinRequest = model.room.RoomJoinDomain(code)
                    when (val result = roomRepository.joinRoom(joinRequest)) {
                        is DomainResult.Success -> {
                            if (result.data.joined) {
                                // If successfully joined, reload rooms
                                loadRooms()
                                loadMyRooms()
                                // Select the joined room
                                dispatch(Msg.RoomSelected(result.data.roomId))
                            } else {
                                dispatch(Msg.ErrorOccurred(result.data.message))
                            }
                        }
                        is DomainResult.Error -> {
                            dispatch(Msg.ErrorOccurred(result.error.message ?: "Failed to join room"))
                        }
                        else -> {
                            // Ignore loading state
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Error joining room: ${e.message}")
                    dispatch(Msg.ErrorOccurred(e.message ?: "Unknown error occurred"))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<RoomListStore.State, Msg> {
        override fun RoomListStore.State.reduce(msg: Msg): RoomListStore.State =
            when (msg) {
                is Msg.StartLoading -> copy(isLoading = true, error = null)
                is Msg.RoomsLoaded -> copy(rooms = msg.rooms, isLoading = false)
                is Msg.MyRoomsLoaded -> copy(myRooms = msg.rooms)
                is Msg.RoomSelected -> copy(selectedRoomId = msg.roomId)
                is Msg.FilterChanged -> copy(filterQuery = msg.query)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false)
                is Msg.ToggleMyRooms -> copy(showMyRooms = msg.show)
            }
    }
} 