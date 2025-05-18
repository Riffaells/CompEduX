package components.lesson

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import logging.Logger
import model.DomainResult
import model.course.CourseLessonDomain
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.lesson.LessonUseCases

interface LessonViewStore : Store<LessonViewStore.Intent, LessonViewStore.State, LessonViewStore.Label> {

    /**
     * Intent - действия, которые могут быть выполнены в этом Store
     */
    sealed interface Intent {
        data class LoadLesson(val lessonId: String) : Intent
        data object ResetError : Intent
        data object NavigateBack : Intent
    }

    /**
     * State - состояние экрана просмотра урока
     */
    data class State(
        val isLoading: Boolean = false,
        val lesson: CourseLessonDomain? = null,
        val error: String? = null
    )

    sealed interface Label {
        data object NavigateBack : Label
    }
}

/**
 * Фабрика для создания LessonViewStore
 */
object LessonViewStoreFactory {
    fun create(
        di: DI,
        storeFactory: StoreFactory
    ): LessonViewStore =
        object : LessonViewStore, DIAware {
            override val di: DI = di

            private val logger by instance<Logger>()
            private val lessonUseCases by instance<LessonUseCases>()

            override val store: LessonViewStore = storeFactory.create(
                name = "LessonViewStore",
                initialState = LessonViewStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl
            )

            private inner class ExecutorImpl : CoroutineExecutor<LessonViewStore.Intent, Unit, LessonViewStore.State, Result, LessonViewStore.Label>(
                rDispatchers.main
            ) {
                override fun executeAction(action: Unit, getState: () -> LessonViewStore.State) {
                    // Инициализация, если нужна
                }

                override fun executeIntent(intent: LessonViewStore.Intent, getState: () -> LessonViewStore.State) {
                    when (intent) {
                        is LessonViewStore.Intent.LoadLesson -> loadLesson(intent.lessonId)
                        LessonViewStore.Intent.ResetError -> dispatch(Result.ErrorReset)
                        LessonViewStore.Intent.NavigateBack -> publish(LessonViewStore.Label.NavigateBack)
                    }
                }

                private fun loadLesson(lessonId: String) {
                    scope.launch {
                        dispatch(Result.Loading)
                        try {
                            when (val result = lessonUseCases.getLesson(lessonId)) {
                                is DomainResult.Success -> {
                                    dispatch(Result.LessonLoaded(result.data))
                                }
                                is DomainResult.Error -> {
                                    logger.e("Failed to load lesson: ${result.message}")
                                    dispatch(Result.Error(result.message))
                                }
                            }
                        } catch (e: Exception) {
                            logger.e("Exception loading lesson: ${e.message}")
                            dispatch(Result.Error(e.message ?: "Неизвестная ошибка"))
                        }
                    }
                }
            }

            private sealed interface Result {
                data object Loading : Result
                data class LessonLoaded(val lesson: CourseLessonDomain) : Result
                data class Error(val message: String) : Result
                data object ErrorReset : Result
            }

            private object ReducerImpl : Reducer<LessonViewStore.State, Result> {
                override fun LessonViewStore.State.reduce(result: Result): LessonViewStore.State =
                    when (result) {
                        is Result.Loading -> copy(isLoading = true, error = null)
                        is Result.LessonLoaded -> copy(isLoading = false, lesson = result.lesson, error = null)
                        is Result.Error -> copy(isLoading = false, error = result.message)
                        is Result.ErrorReset -> copy(error = null)
                    }
            }
        }.store 