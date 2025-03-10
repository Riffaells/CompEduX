package component.app.room

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigation
import com.arkivanov.decompose.router.slot.activate
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.slot.dismiss
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.room.achievement.DefaultAchievementComponent
import component.app.room.diagram.DefaultDiagramComponent
import component.app.room.store.RoomStore
import component.app.room.store.RoomStoreFactory
import di.AchievementComponentParams
import di.DiagramComponentParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.factory
import org.kodein.di.instance

interface RoomComponent {
    val state: StateFlow<RoomStore.State>
    val diagramSlot: Value<ChildSlot<*, DiagramChild>>
    val achievementSlot: Value<ChildSlot<*, AchievementChild>>

    fun onEvent(event: RoomStore.Intent)
    fun onBackClicked()

    sealed class DiagramChild {
        data class DiagramContent(val component: DefaultDiagramComponent) : DiagramChild()
    }

    sealed class AchievementChild {
        data class AchievementContent(val component: DefaultAchievementComponent) : AchievementChild()
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

    init {
        // Инициализируем диаграмму
        diagramNavigation.activate(DiagramConfig.Diagram)

        // Инициализируем достижения в зависимости от начального состояния
        updateAchievementVisibility(store.state.showAchievement)
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

    private fun createDiagramChild(config: DiagramConfig, componentContext: ComponentContext): RoomComponent.DiagramChild =
        when (config) {
            DiagramConfig.Diagram -> RoomComponent.DiagramChild.DiagramContent(diagramComponent(componentContext))
        }

    private fun createAchievementChild(config: AchievementConfig, componentContext: ComponentContext): RoomComponent.AchievementChild =
        when (config) {
            AchievementConfig.Achievement -> RoomComponent.AchievementChild.AchievementContent(achievementComponent(componentContext))
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

    override fun onBackClicked() {
        onBack()
    }
}
