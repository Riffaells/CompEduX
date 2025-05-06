package component.app.course.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import model.DomainResult
import model.course.CourseDomain
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import repository.course.CourseRepository

/**
 * Store for course details
 */
interface CourseDetailStore : Store<CourseDetailStore.Intent, CourseDetailStore.State, CourseDetailStore.Label> {

    /**
     * UI intents
     */
    sealed class Intent {
        /**
         * Init with course ID
         */
        data class Init(val courseId: String) : Intent()

        /**
         * Refresh course
         */
        data object Refresh : Intent()

        /**
         * Edit course button clicked
         */
        data object EditCourse : Intent()

        /**
         * Open module button clicked
         */
        data class OpenModule(val moduleId: String) : Intent()
    }

    /**
     * UI state
     */
    @Serializable
    data class State(
        val courseId: String = "",
        val course: CourseDomain? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    /**
     * UI events
     */
    sealed class Label {
        /**
         * Navigate to edit course
         */
        data class NavigateToEditCourse(val courseId: String) : Label()

        /**
         * Navigate to module details
         */
        data class NavigateToModule(val moduleId: String) : Label()
    }

    companion object {
        /**
         * Create store
         */
        fun create(di: DI, storeFactory: StoreFactory): CourseDetailStore = 
            CourseDetailStoreFactory(di, storeFactory).create()
    }
}

/**
 * Factory for creating course detail store
 */
internal class CourseDetailStoreFactory(
    override val di: DI,
    private val storeFactory: StoreFactory
) : DIAware {

    private val courseRepository by instance<CourseRepository>()
    private val logger by instance<Logger>()

    /**
     * Create store
     */
    fun create(): CourseDetailStore =
        object : CourseDetailStore, Store<CourseDetailStore.Intent, CourseDetailStore.State, CourseDetailStore.Label> by storeFactory.create(
            name = "CourseDetailStore",
            initialState = CourseDetailStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    /**
     * Messages for reducer
     */
    private sealed class Msg {
        /**
         * Set loading state
         */
        data class SetLoading(val isLoading: Boolean) : Msg()

        /**
         * Set course ID
         */
        data class SetCourseId(val courseId: String) : Msg()

        /**
         * Set course
         */
        data class SetCourse(val course: CourseDomain) : Msg()

        /**
         * Set error
         */
        data class SetError(val error: String?) : Msg()
    }

    /**
     * Reducer
     */
    private object ReducerImpl : Reducer<CourseDetailStore.State, Msg> {
        override fun CourseDetailStore.State.reduce(msg: Msg): CourseDetailStore.State =
            when (msg) {
                is Msg.SetLoading -> copy(isLoading = msg.isLoading)
                is Msg.SetCourseId -> copy(courseId = msg.courseId)
                is Msg.SetCourse -> copy(course = msg.course)
                is Msg.SetError -> copy(error = msg.error)
            }
    }

    /**
     * Executor
     */
    private inner class ExecutorImpl : CoroutineExecutor<CourseDetailStore.Intent, Unit, CourseDetailStore.State, Msg, CourseDetailStore.Label>() {
        override fun executeAction(action: Unit) {
            // Ничего не делаем при инициализации
        }

        override fun executeIntent(intent: CourseDetailStore.Intent) {
            when (intent) {
                is CourseDetailStore.Intent.Init -> {
                    dispatch(Msg.SetCourseId(intent.courseId))
                    loadCourseDetails(intent.courseId)
                }
                is CourseDetailStore.Intent.Refresh -> {
                    val courseId = state().courseId
                    if (courseId.isNotEmpty()) {
                        loadCourseDetails(courseId)
                    }
                }
                is CourseDetailStore.Intent.EditCourse -> {
                    val courseId = state().courseId
                    if (courseId.isNotEmpty()) {
                        publish(CourseDetailStore.Label.NavigateToEditCourse(courseId))
                    }
                }
                is CourseDetailStore.Intent.OpenModule -> {
                    publish(CourseDetailStore.Label.NavigateToModule(intent.moduleId))
                }
            }
        }

        /**
         * Load course details
         */
        private fun loadCourseDetails(courseId: String) {
            dispatch(Msg.SetLoading(true))
            dispatch(Msg.SetError(null))

            scope.launch {
                try {
                    val result = courseRepository.getCourse(courseId)
                    
                    when (result) {
                        is DomainResult.Success -> {
                            dispatch(Msg.SetCourse(result.data))
                        }
                        is DomainResult.Error -> {
                            dispatch(Msg.SetError(result.error.message))
                        }
                        is DomainResult.Loading -> {
                            // Уже в состоянии загрузки
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Error loading course details", e)
                    dispatch(Msg.SetError(e.message ?: "Unknown error"))
                } finally {
                    dispatch(Msg.SetLoading(false))
                }
            }
        }
    }
}
