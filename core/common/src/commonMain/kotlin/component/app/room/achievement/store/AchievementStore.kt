package component.app.room.achievement.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import navigation.rDispatchers

interface AchievementStore : Store<AchievementStore.Intent, AchievementStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data class UnlockAchievement(val id: String) : Intent
        data object RefreshAchievements : Intent
    }

    @Serializable
    data class State(
        val achievements: List<Achievement> = emptyList(),
        val loading: Boolean = false
    )

    @Serializable
    data class Achievement(
        val id: String,
        val title: String,
        val description: String,
        val iconUrl: String? = null,
        val isUnlocked: Boolean = false,
        val progress: Float = 0f,
        val maxProgress: Float = 100f
    )
}

internal class AchievementStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val logger by instance<Logger>("AchievementStore")

    fun create(): AchievementStore =
        object : AchievementStore, Store<AchievementStore.Intent, AchievementStore.State, Nothing> by storeFactory.create(
            name = "AchievementStore",
            initialState = AchievementStore.State(
                achievements = generateSampleAchievements()
            ),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private fun generateSampleAchievements(): List<AchievementStore.Achievement> {
        return listOf(
            AchievementStore.Achievement(
                id = "first_login",
                title = "First Login",
                description = "Login to the application for the first time",
                isUnlocked = true,
                progress = 100f
            ),
            AchievementStore.Achievement(
                id = "complete_profile",
                title = "Complete Profile",
                description = "Fill out all profile information",
                isUnlocked = false,
                progress = 50f
            ),
            AchievementStore.Achievement(
                id = "first_diagram",
                title = "First Diagram",
                description = "Create your first diagram",
                isUnlocked = false,
                progress = 0f
            ),
            AchievementStore.Achievement(
                id = "master_creator",
                title = "Master Creator",
                description = "Create 10 different diagrams",
                isUnlocked = false,
                progress = 20f,
                maxProgress = 10f
            )
        )
    }

    private sealed interface Msg {
        data object LoadingData : Msg
        data object LoadData : Msg
        data class UpdateAchievements(val achievements: List<AchievementStore.Achievement>) : Msg
        data class UnlockAchievement(val id: String) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<AchievementStore.Intent, Unit, AchievementStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                dispatch(Msg.LoadData)
            } catch (e: Exception) {
                logger.e("Error in executeAction: ${e.message}")
            }
        }

        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                logger.e("Error in dispatch: ${e.message}")
            }
        }

        override fun executeIntent(intent: AchievementStore.Intent): Unit =
            try {
                when (intent) {
                    is AchievementStore.Intent.Init -> {
                        safeDispatch(Msg.LoadData)
                    }
                    is AchievementStore.Intent.UnlockAchievement -> {
                        scope.launch {
                            try {
                                safeDispatch(Msg.UnlockAchievement(intent.id))
                            } catch (e: Exception) {
                                logger.e("Error unlocking achievement: ${e.message}")
                            }
                        }
                        Unit
                    }
                    is AchievementStore.Intent.RefreshAchievements -> {
                        scope.launch {
                            try {
                                // In a real app, you would fetch achievements from a repository
                                safeDispatch(Msg.UpdateAchievements(generateSampleAchievements()))
                            } catch (e: Exception) {
                                logger.e("Error refreshing achievements: ${e.message}")
                            }
                        }
                        Unit
                    }
                }
            } catch (e: Exception) {
                logger.e("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<AchievementStore.State, Msg> {
        override fun AchievementStore.State.reduce(msg: Msg): AchievementStore.State =
            when (msg) {
                Msg.LoadData -> copy(loading = false)
                Msg.LoadingData -> copy(loading = true)
                is Msg.UpdateAchievements -> copy(achievements = msg.achievements)
                is Msg.UnlockAchievement -> {
                    val updatedAchievements = achievements.map { achievement ->
                        if (achievement.id == msg.id) {
                            achievement.copy(isUnlocked = true, progress = achievement.maxProgress)
                        } else {
                            achievement
                        }
                    }
                    copy(achievements = updatedAchievements)
                }
            }
    }
}
