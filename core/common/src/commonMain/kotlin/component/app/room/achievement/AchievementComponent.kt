package component.app.room.achievement

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.room.achievement.store.AchievementStore
import component.app.room.achievement.store.AchievementStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Параметры для создания AchievementComponent
 */
data class AchievementComponentParams(
    val componentContext: ComponentContext
)

interface AchievementComponent {
    val state: StateFlow<AchievementStore.State>

    fun onAction(action: AchievementStore.Intent)
}

class DefaultAchievementComponent(
    componentContext: ComponentContext,
    override val di: DI
) : AchievementComponent, DIAware, ComponentContext by componentContext {

    private val achievementStoreFactory: AchievementStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        achievementStoreFactory.create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<AchievementStore.State> = store.stateFlow

    override fun onAction(action: AchievementStore.Intent) {
        store.accept(action)
    }
}
