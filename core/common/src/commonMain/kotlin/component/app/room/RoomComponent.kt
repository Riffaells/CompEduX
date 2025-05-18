package component.app.room

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.room.achievement.AchievementComponentParams
import component.app.room.achievement.DefaultAchievementComponent
import component.app.room.detail.DefaultRoomDetailComponent
import component.app.room.detail.RoomDetailComponentParams
import component.app.room.diagram.DefaultDiagramComponent
import component.app.room.diagram.DiagramComponentParams
import component.app.room.list.DefaultRoomListComponent
import component.app.room.list.RoomListComponentParams
import component.app.room.store.RoomStore
import component.app.room.store.RoomStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.factory
import org.kodein.di.instance

/**
 * Параметры для создания RoomComponent
 */
data class RoomComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)

interface RoomComponent {
    val state: StateFlow<RoomStore.State>
    val diagramSlot: Value<ChildSlot<*, DiagramChild>>
    val achievementSlot: Value<ChildSlot<*, AchievementChild>>
    val roomStack: Value<ChildStack<*, RoomChild>>

    fun onEvent(event: RoomStore.Intent)
    fun onBackClicked()

    sealed class DiagramChild {
        data class DiagramContent(val component: DefaultDiagramComponent) : DiagramChild()
    }

    sealed class AchievementChild {
        data class AchievementContent(val component: DefaultAchievementComponent) : AchievementChild()
    }

    sealed class RoomChild {
        data class List(val component: DefaultRoomListComponent) : RoomChild()
        data class Detail(val component: DefaultRoomDetailComponent) : RoomChild()
    }
}

class DefaultRoomComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val onBack: () -> Unit,
    override val di: DI
) : RoomComponent, DIAware, ComponentContext by componentContext {

    private val roomStoreFactory: RoomStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        roomStoreFactory.create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<RoomStore.State> = store.stateFlow

    // Navigation for diagram slot
    private val diagramNavigation = SlotNavigation<DiagramConfig>()

    // Navigation for achievement slot
    private val achievementNavigation = SlotNavigation<AchievementConfig>()

    // Navigation for auth slot
    private val authNavigation = SlotNavigation<AuthConfig>()

    // Navigation for room stack
    private val roomNavigation = StackNavigation<RoomConfig>()

    // Diagram slot
    override val diagramSlot: Value<ChildSlot<*, RoomComponent.DiagramChild>> = childSlot(
        source = diagramNavigation,
        serializer = DiagramConfig.serializer(),
        handleBackButton = true,
        key = "DiagramSlot",
        childFactory = ::createDiagramChild
    )

    // Achievement slot
    override val achievementSlot: Value<ChildSlot<*, RoomComponent.AchievementChild>> = childSlot(
        source = achievementNavigation,
        serializer = AchievementConfig.serializer(),
        handleBackButton = true,
        key = "AchievementSlot",
        childFactory = ::createAchievementChild
    )

    // Room stack
    override val roomStack: Value<ChildStack<*, RoomComponent.RoomChild>> = childStack(
        source = roomNavigation,
        serializer = RoomConfig.serializer(),
        initialConfiguration = RoomConfig.List,
        handleBackButton = true,
        key = "RoomStack",
        childFactory = ::createRoomChild
    )

    init {
        // Инициализируем диаграмму
        diagramNavigation.activate(DiagramConfig.Diagram)

        // Инициализируем достижения в зависимости от начального состояния
        updateAchievementVisibility(store.state.showAchievement)

        // Инициализируем аутентификацию в зависимости от начального состояния
        updateAuthVisibility(store.state.showAuth)

        // Отправляем интент инициализации
        store.accept(RoomStore.Intent.Init)
    }

    override fun onEvent(event: RoomStore.Intent) {
        // Сначала обновляем Store
        store.accept(event)

        // Затем обрабатываем события, связанные с UI
        when (event) {
            is RoomStore.Intent.ToggleAchievement -> {
                // Обновляем UI на основе нового состояния
                updateAchievementVisibility(store.state.showAchievement)
            }

            is RoomStore.Intent.ToggleAuth -> {
                // Обновляем UI на основе нового состояния
                updateAuthVisibility(store.state.showAuth)
            }

            else -> {
                // Другие события не требуют обновления UI
            }
        }
    }

    private fun updateAchievementVisibility(show: Boolean) {
        if (show) {
            achievementNavigation.activate(AchievementConfig.Achievement)
        } else {
            // Используем dismiss() для закрытия слота
            achievementNavigation.dismiss()
        }
    }

    private fun updateAuthVisibility(show: Boolean) {
        if (show) {
            authNavigation.activate(AuthConfig.Auth)
        } else {
            // Используем dismiss() для закрытия слота
            authNavigation.dismiss()
        }
    }

    private fun createDiagramChild(
        config: DiagramConfig,
        componentContext: ComponentContext
    ): RoomComponent.DiagramChild =
        when (config) {
            DiagramConfig.Diagram -> RoomComponent.DiagramChild.DiagramContent(diagramComponent(componentContext))
        }

    private fun createAchievementChild(
        config: AchievementConfig,
        componentContext: ComponentContext
    ): RoomComponent.AchievementChild =
        when (config) {
            AchievementConfig.Achievement -> RoomComponent.AchievementChild.AchievementContent(
                achievementComponent(
                    componentContext
                )
            )
        }

    private fun createRoomChild(
        config: RoomConfig,
        componentContext: ComponentContext
    ): RoomComponent.RoomChild =
        when (config) {
            is RoomConfig.List -> RoomComponent.RoomChild.List(
                roomListComponent(
                    componentContext,
                    ::onRoomSelected
                )
            )
            is RoomConfig.Detail -> RoomComponent.RoomChild.Detail(
                roomDetailComponent(
                    componentContext,
                    config.roomId,
                    ::onRoomDetailBack
                )
            )
        }

    private fun onRoomSelected(roomId: String) {
        roomNavigation.navigate { stack ->
            stack + RoomConfig.Detail(roomId)
        }
    }

    private fun onRoomDetailBack() {
        roomNavigation.pop()
    }

    private fun diagramComponent(componentContext: ComponentContext): DefaultDiagramComponent {
        val diagramComponentFactory: (DiagramComponentParams) -> DefaultDiagramComponent by factory()
        return diagramComponentFactory(
            DiagramComponentParams(
                componentContext = componentContext
            )
        )
    }

    private fun achievementComponent(componentContext: ComponentContext): DefaultAchievementComponent {
        val achievementComponentFactory: (AchievementComponentParams) -> DefaultAchievementComponent by factory()
        return achievementComponentFactory(
            AchievementComponentParams(
                componentContext = componentContext
            )
        )
    }

    private fun roomListComponent(
        componentContext: ComponentContext,
        onRoomSelected: (String) -> Unit
    ): DefaultRoomListComponent {
        val roomListComponentFactory: (RoomListComponentParams) -> DefaultRoomListComponent by factory()
        return roomListComponentFactory(
            RoomListComponentParams(
                componentContext = componentContext,
                onRoomSelected = onRoomSelected
            )
        )
    }

    private fun roomDetailComponent(
        componentContext: ComponentContext,
        roomId: String,
        onBack: () -> Unit
    ): DefaultRoomDetailComponent {
        val roomDetailComponentFactory: (RoomDetailComponentParams) -> DefaultRoomDetailComponent by factory()
        return roomDetailComponentFactory(
            RoomDetailComponentParams(
                componentContext = componentContext,
                roomId = roomId,
                onBack = onBack
            )
        )
    }

    @Serializable
    private sealed interface DiagramConfig {
        @Serializable
        data object Diagram : DiagramConfig
    }

    @Serializable
    private sealed interface AchievementConfig {
        @Serializable
        data object Achievement : AchievementConfig
    }

    @Serializable
    private sealed interface AuthConfig {
        @Serializable
        data object Auth : AuthConfig
    }

    @Serializable
    private sealed interface RoomConfig {
        @Serializable
        data object List : RoomConfig

        @Serializable
        data class Detail(val roomId: String) : RoomConfig
    }

    override fun onBackClicked() {
        onBack()
    }
}
