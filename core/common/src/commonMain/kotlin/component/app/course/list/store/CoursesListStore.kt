package component.app.course.list.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import model.DomainResult
import model.course.CourseDomain
import model.course.CourseListDomain
import model.course.CourseQueryParams
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import repository.course.CourseRepository

/**
 * Store for course list
 */
interface CoursesListStore : Store<CoursesListStore.Intent, CoursesListStore.State, CoursesListStore.Label> {

    /**
     * UI intents
     */
    sealed interface Intent {
        /**
         * Course clicked
         */
        data class CourseClick(val courseId: String) : Intent

        /**
         * Search query changed
         */
        data class SearchQueryChanged(val query: String) : Intent

        /**
         * Search for courses
         */
        data object Search : Intent

        /**
         * Create course
         */
        data object CreateCourseClick : Intent
    }

    /**
     * UI state
     */
    @Serializable
    data class State(
        val isLoading: Boolean = false,
        val courses: List<CourseDomain> = emptyList(),
        val error: String? = null,
        val searchQuery: String = "",
    )

    /**
     * Store events
     */
    sealed interface Message {
        /**
         * Start loading courses
         */
        data object LoadingStarted : Message

        /**
         * Courses loaded successfully
         */
        data class CoursesLoaded(val courses: List<CourseDomain>) : Message

        /**
         * Failed to load courses
         */
        data class LoadingError(val error: String) : Message

        /**
         * Update search query
         */
        data class UpdateSearchQuery(val query: String) : Message
    }

    /**
     * UI events
     */
    sealed interface Label {
        /**
         * Navigate to course details
         */
        data class NavigateToCourseDetails(val courseId: String) : Label

        /**
         * Navigate to course creation
         */
        data object NavigateToCourseCreation : Label
    }

    companion object {
        /**
         * Create instance
         *
         * @param di DI container
         * @param storeFactory Store factory
         */
        fun create(di: DI, storeFactory: StoreFactory): CoursesListStore =
            object : CoursesListStore, Store<CoursesListStore.Intent, CoursesListStore.State, CoursesListStore.Label> by storeFactory.create(
                name = "CoursesListStore",
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(di) },
                reducer = ReducerImpl,
            ) {}
    }

    /**
     * Reducer
     */
    private object ReducerImpl : Reducer<State, Message> {
        override fun State.reduce(message: Message): State =
            when (message) {
                is Message.LoadingStarted -> copy(
                    isLoading = true,
                    error = null,
                )
                is Message.CoursesLoaded -> copy(
                    isLoading = false,
                    courses = message.courses,
                    error = null,
                )
                is Message.LoadingError -> copy(
                    isLoading = false,
                    error = message.error,
                )
                is Message.UpdateSearchQuery -> copy(
                    searchQuery = message.query,
                )
            }
    }

    /**
     * Executor
     */
    private class ExecutorImpl(
        override val di: DI
    ) : CoroutineExecutor<Intent, Unit, State, Message, Label>(), DIAware {
        
        private val courseRepository: CourseRepository by instance()
        private val logger: Logger by instance()

        override fun executeAction(action: Unit) {
            loadCourses()
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.CourseClick -> {
                    publish(Label.NavigateToCourseDetails(intent.courseId))
                }
                is Intent.SearchQueryChanged -> {
                    dispatch(Message.UpdateSearchQuery(intent.query))
                }
                is Intent.Search -> {
                    val query = state().searchQuery
                    loadCourses(query)
                }
                is Intent.CreateCourseClick -> {
                    publish(Label.NavigateToCourseCreation)
                }
            }
        }

        /**
         * Load courses from repository
         */
        private fun loadCourses(searchQuery: String = "") {
            scope.launch {
                dispatch(Message.LoadingStarted)
                try {
                    val params = if (searchQuery.isNotEmpty()) {
                        CourseQueryParams(search = searchQuery)
                    } else {
                        CourseQueryParams()
                    }
                    
                    val result = courseRepository.getCourses(params)
                    when (result) {
                        is DomainResult.Success -> {
                            dispatch(Message.CoursesLoaded(result.data.items))
                        }
                        is DomainResult.Error -> {
                            dispatch(Message.LoadingError(result.error.message))
                        }
                        is DomainResult.Loading -> {
                            // Уже в состоянии загрузки
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Failed to load courses", e)
                    dispatch(Message.LoadingError(e.message ?: "Unknown error"))
                }
            }
        }
    }
}
