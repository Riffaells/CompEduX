package component.app.room.detail.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import model.DomainResult
import model.course.LocalizedContent
import model.room.RoomDomain
import model.room.RoomUpdateDomain
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import repository.room.RoomRepository

interface RoomDetailStore : Store<RoomDetailStore.Intent, RoomDetailStore.State, RoomDetailStore.Label> {

    sealed interface Intent {
        data object Init : Intent
        data object LoadRoom : Intent
        data class UpdateName(val name: String) : Intent
        data class UpdateDescription(val description: String) : Intent
        data object SaveRoom : Intent
        data object DeleteRoom : Intent
    }

    sealed interface Label {
        data object RoomSaved : Label
        data object RoomDeleted : Label
        data class ErrorOccurred(val message: String) : Label
    }

    @Serializable
    data class State(
        val roomId: String = "",
        val roomName: String = "",
        val roomDescription: String = "",
        val roomCode: String = "",
        val participants: Int = 0,
        val isOwner: Boolean = false,
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null
    )
}

internal class RoomDetailStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val roomRepository by instance<RoomRepository>()
    private val logger by instance<Logger>()

    fun create(roomId: String): RoomDetailStore =
        object : RoomDetailStore, Store<RoomDetailStore.Intent, RoomDetailStore.State, RoomDetailStore.Label> by storeFactory.create(
            name = "RoomDetailStore",
            initialState = RoomDetailStore.State(roomId = roomId),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(roomId, logger.withTag("RoomDetailStore")) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object StartLoading : Msg
        data object StartSaving : Msg
        data class RoomLoaded(val room: RoomDomain) : Msg
        data class NameChanged(val name: String) : Msg
        data class DescriptionChanged(val description: String) : Msg
        data object RoomSaved : Msg
        data object RoomDeleted : Msg
        data class ErrorOccurred(val error: String) : Msg
    }

    private inner class ExecutorImpl(
        private val roomId: String,
        private val logger: Logger
    ) : CoroutineExecutor<RoomDetailStore.Intent, Unit, RoomDetailStore.State, Msg, RoomDetailStore.Label>(
        rDispatchers.main
    ) {

        override fun executeAction(action: Unit) {
            loadRoom()
        }

        private fun loadRoom() {
            scope.launch {
                dispatch(Msg.StartLoading)
                try {
                    when (val result = roomRepository.getRoom(roomId)) {
                        is DomainResult.Success -> {
                            dispatch(Msg.RoomLoaded(result.data))
                        }
                        is DomainResult.Error -> {
                            val errorMsg = result.error.message
                            dispatch(Msg.ErrorOccurred(errorMsg))
                            publish(RoomDetailStore.Label.ErrorOccurred(errorMsg))
                        }
                        else -> {
                            // Ignore loading state
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Error loading room: ${e.message}")
                    val errorMsg = e.message ?: "Unknown error occurred"
                    dispatch(Msg.ErrorOccurred(errorMsg))
                    publish(RoomDetailStore.Label.ErrorOccurred(errorMsg))
                }
            }
        }

        override fun executeIntent(intent: RoomDetailStore.Intent) {
            when (intent) {
                is RoomDetailStore.Intent.Init -> {
                    loadRoom()
                }
                is RoomDetailStore.Intent.LoadRoom -> {
                    loadRoom()
                }
                is RoomDetailStore.Intent.UpdateName -> {
                    dispatch(Msg.NameChanged(intent.name))
                }
                is RoomDetailStore.Intent.UpdateDescription -> {
                    dispatch(Msg.DescriptionChanged(intent.description))
                }
                is RoomDetailStore.Intent.SaveRoom -> {
                    saveRoom()
                }
                is RoomDetailStore.Intent.DeleteRoom -> {
                    deleteRoom()
                }
            }
        }

        private fun saveRoom() {
            val currentState = state()
            scope.launch {
                dispatch(Msg.StartSaving)
                try {
                    // Create LocalizedContent from string
                    val nameContent = LocalizedContent.single(currentState.roomName)
                    val descriptionContent = if (currentState.roomDescription.isNotBlank()) {
                        LocalizedContent.single(currentState.roomDescription)
                    } else {
                        null
                    }
                    
                    val roomUpdate = RoomUpdateDomain(
                        name = nameContent,
                        description = descriptionContent
                    )
                    
                    when (val result = roomRepository.updateRoom(roomId, roomUpdate)) {
                        is DomainResult.Success -> {
                            dispatch(Msg.RoomSaved)
                            publish(RoomDetailStore.Label.RoomSaved)
                        }
                        is DomainResult.Error -> {
                            val errorMsg = result.error.message
                            dispatch(Msg.ErrorOccurred(errorMsg))
                            publish(RoomDetailStore.Label.ErrorOccurred(errorMsg))
                        }
                        else -> {
                            // Ignore loading state
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Error saving room: ${e.message}")
                    val errorMsg = e.message ?: "Unknown error occurred"
                    dispatch(Msg.ErrorOccurred(errorMsg))
                    publish(RoomDetailStore.Label.ErrorOccurred(errorMsg))
                }
            }
        }

        private fun deleteRoom() {
            scope.launch {
                dispatch(Msg.StartSaving)
                try {
                    when (val result = roomRepository.deleteRoom(roomId)) {
                        is DomainResult.Success -> {
                            dispatch(Msg.RoomDeleted)
                            publish(RoomDetailStore.Label.RoomDeleted)
                        }
                        is DomainResult.Error -> {
                            val errorMsg = result.error.message
                            dispatch(Msg.ErrorOccurred(errorMsg))
                            publish(RoomDetailStore.Label.ErrorOccurred(errorMsg))
                        }
                        else -> {
                            // Ignore loading state
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Error deleting room: ${e.message}")
                    val errorMsg = e.message ?: "Unknown error occurred"
                    dispatch(Msg.ErrorOccurred(errorMsg))
                    publish(RoomDetailStore.Label.ErrorOccurred(errorMsg))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<RoomDetailStore.State, Msg> {
        override fun RoomDetailStore.State.reduce(msg: Msg): RoomDetailStore.State =
            when (msg) {
                is Msg.StartLoading -> copy(isLoading = true, error = null)
                is Msg.StartSaving -> copy(isSaving = true, error = null)
                is Msg.RoomLoaded -> copy(
                    roomName = msg.room.name.getPreferredString(),
                    roomDescription = msg.room.description?.getPreferredString() ?: "",
                    roomCode = msg.room.code ?: "",
                    // Use safe access for participants size
                    participants = 0, // We don't have participants in RoomDomain
                    isOwner = msg.room.ownerId.isNotBlank(), // Determine ownership by ownerId
                    isLoading = false
                )
                is Msg.NameChanged -> copy(roomName = msg.name)
                is Msg.DescriptionChanged -> copy(roomDescription = msg.description)
                is Msg.RoomSaved -> copy(isSaving = false)
                is Msg.RoomDeleted -> copy(isSaving = false)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false, isSaving = false)
            }
    }
} 