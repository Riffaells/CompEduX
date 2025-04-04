package component.app.room.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import repository.auth.AuthRepository
import usecase.auth.AuthUseCases
import utils.rDispatchers

interface RoomStore : Store<RoomStore.Intent, RoomStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object ToggleAchievement : Intent
        data object ToggleAuth : Intent
        data class UpdateRoomName(val name: String) : Intent
        data class UpdateRoomDescription(val description: String) : Intent
    }

    @Serializable
    data class State(
        val roomName: String = "Default Room",
        val roomDescription: String = "This is a default room description",
        val showAchievement: Boolean = true,
        val showAuth: Boolean = false,
        val isAuthenticated: Boolean = false,
        val loading: Boolean = false
    )
}

internal class RoomStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val authUseCases: AuthUseCases by instance()

    fun create(): RoomStore =
        object : RoomStore, Store<RoomStore.Intent, RoomStore.State, Nothing> by storeFactory.create(
            name = "RoomStore",
            initialState = RoomStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data object LoadData : Msg
        data class UpdateRoomName(val name: String) : Msg
        data class UpdateRoomDescription(val description: String) : Msg
        data class ToggleAchievementVisibility(val show: Boolean) : Msg
        data class ToggleAuthVisibility(val show: Boolean) : Msg
        data class UpdateAuthStatus(val isAuthenticated: Boolean) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<RoomStore.Intent, Unit, RoomStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                dispatch(Msg.LoadData)
                checkAuthStatus()
            } catch (e: Exception) {
                println("Error in executeAction: ${e.message}")
            }
        }

        private fun checkAuthStatus() {
            scope.launch {
                    safeDispatch(Msg.UpdateAuthStatus(authUseCases.isAuthenticated()))

            }
        }

        // Safe dispatch method that catches exceptions
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                println("Error in dispatch: ${e.message}")
            }
        }

        override fun executeIntent(intent: RoomStore.Intent): Unit =
            try {
                when (intent) {
                    is RoomStore.Intent.Init -> {
                        safeDispatch(Msg.LoadData)
                        checkAuthStatus()
                    }
                    is RoomStore.Intent.UpdateRoomName -> {
                        scope.launch {
                            try {
                                // Async operations if needed...
                                safeDispatch(Msg.UpdateRoomName(intent.name))
                            } catch (e: Exception) {
                                println("Error updating room name: ${e.message}")
                            }
                        }
                        Unit
                    }
                    is RoomStore.Intent.UpdateRoomDescription -> {
                        scope.launch {
                            try {
                                // Async operations if needed...
                                safeDispatch(Msg.UpdateRoomDescription(intent.description))
                            } catch (e: Exception) {
                                println("Error updating room description: ${e.message}")
                            }
                        }
                        Unit
                    }
                    is RoomStore.Intent.ToggleAchievement -> {
                        val currentState = state()
                        safeDispatch(Msg.ToggleAchievementVisibility(!currentState.showAchievement))
                    }
                    is RoomStore.Intent.ToggleAuth -> {
                        val currentState = state()
                        safeDispatch(Msg.ToggleAuthVisibility(!currentState.showAuth))
                    }
                }
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<RoomStore.State, Msg> {
        override fun RoomStore.State.reduce(msg: Msg): RoomStore.State =
            when (msg) {
                Msg.LoadData -> copy(loading = false)
                Msg.LoadingData -> copy(loading = true)
                is Msg.UpdateRoomName -> copy(roomName = msg.name)
                is Msg.UpdateRoomDescription -> copy(roomDescription = msg.description)
                is Msg.ToggleAchievementVisibility -> copy(showAchievement = msg.show)
                is Msg.ToggleAuthVisibility -> copy(showAuth = msg.show)
                is Msg.UpdateAuthStatus -> copy(isAuthenticated = msg.isAuthenticated)
            }
    }
}
