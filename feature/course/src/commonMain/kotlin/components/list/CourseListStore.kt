package components.list

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import logging.Logger
import model.DomainResult
import model.course.CourseDomain
import model.course.CourseQueryParams
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.course.CourseUseCases

interface CourseListStore : Store<CourseListStore.Intent, CourseListStore.State, CourseListStore.Label> {

    /**
     * Intent - действия, которые могут быть выполнены в этом Store
     */
    sealed interface Intent {
        data object LoadCourses : Intent
        data object RefreshCourses : Intent
        data class FilterCourses(val query: String) : Intent
        data class SelectCourse(val courseId: String) : Intent
        data object NavigateBack : Intent
    }

    /**
     * State - состояние экрана списка курсов
     */
    data class State(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val courses: List<CourseDomain> = emptyList(),
        val filteredCourses: List<CourseDomain> = emptyList(),
        val filterQuery: String = "",
        val error: String? = null,
        val selectedCourseId: String? = null
    )

    sealed interface Label {
        data class NavigateToCourse(val courseId: String) : Label
        data object NavigateBack : Label
    }
}

/**
 * Фабрика для создания CourseListStore
 */
class CourseListStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val logger by instance<Logger>()

    fun create(): CourseListStore =
        object : CourseListStore, Store<CourseListStore.Intent, CourseListStore.State, CourseListStore.Label> by storeFactory.create(
            name = "CourseListStore",
            initialState = CourseListStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl(di) },
            reducer = ReducerImpl
        ) {}

    // Приватные сообщения для редуктора
    private sealed interface Msg {
        data object Loading : Msg
        data object Refreshing : Msg
        data class CoursesLoaded(val courses: List<CourseDomain>) : Msg
        data class FilterChanged(val query: String, val filteredCourses: List<CourseDomain>) : Msg
        data class CourseSelected(val courseId: String) : Msg
        data class ErrorOccurred(val error: String) : Msg
    }

    private class ExecutorImpl(
        override val di: DI
    ) : CoroutineExecutor<CourseListStore.Intent, Unit, CourseListStore.State, Msg, CourseListStore.Label>(
        rDispatchers.main
    ), DIAware {
        
        private val courseUseCases: CourseUseCases by instance()
        private val logger: Logger by instance()

        override fun executeAction(action: Unit) {
            // Загружаем курсы при инициализации
            loadCourses()
        }

        override fun executeIntent(intent: CourseListStore.Intent) {
            when (intent) {
                is CourseListStore.Intent.LoadCourses -> loadCourses()
                is CourseListStore.Intent.RefreshCourses -> refreshCourses()
                is CourseListStore.Intent.FilterCourses -> filterCourses(intent.query)
                is CourseListStore.Intent.SelectCourse -> {
                    dispatch(Msg.CourseSelected(intent.courseId))
                    publish(CourseListStore.Label.NavigateToCourse(intent.courseId))
                }
                is CourseListStore.Intent.NavigateBack -> {
                    publish(CourseListStore.Label.NavigateBack)
                }
            }
        }
        
        private fun loadCourses() {
            dispatch(Msg.Loading)
            fetchCourses()
        }
        
        private fun refreshCourses() {
            dispatch(Msg.Refreshing)
            fetchCourses()
        }
        
        private fun fetchCourses() {
            scope.launch {
                try {
                    when (val result = courseUseCases.getCourses()) {
                        is DomainResult.Success -> {
                            logger.i("Courses loaded successfully: ${result.data.items.size} items")
                            dispatch(Msg.CoursesLoaded(result.data.items))
                        }
                        is DomainResult.Error -> {
                            logger.e("Failed to load courses: ${result.error}")
                            dispatch(Msg.ErrorOccurred(result.error.message ?: "Неизвестная ошибка при загрузке курсов"))
                        }
                        is DomainResult.Loading -> {
                            // Already in loading state
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Exception when loading courses: ${e.message}")
                    dispatch(Msg.ErrorOccurred(e.message ?: "Неизвестная ошибка при загрузке курсов"))
                }
            }
        }
        
        private fun filterCourses(query: String) {
            val currentCourses = state().courses
            val filteredCourses = if (query.isBlank()) {
                currentCourses
            } else {
                currentCourses.filter { course ->
                    course.title.content.values.any { it.contains(query, ignoreCase = true) } ||
                    course.description.content.values.any { it.contains(query, ignoreCase = true) } ||
                    course.tags.any { it.contains(query, ignoreCase = true) }
                }
            }
            
            dispatch(Msg.FilterChanged(query, filteredCourses))
        }
    }

    private object ReducerImpl : Reducer<CourseListStore.State, Msg> {
        override fun CourseListStore.State.reduce(msg: Msg): CourseListStore.State =
            when (msg) {
                is Msg.Loading -> copy(isLoading = true, error = null)
                is Msg.Refreshing -> copy(isRefreshing = true, error = null)
                is Msg.CoursesLoaded -> copy(
                    isLoading = false,
                    isRefreshing = false,
                    courses = msg.courses,
                    filteredCourses = if (filterQuery.isBlank()) msg.courses else filteredCourses,
                    error = null
                )
                is Msg.FilterChanged -> copy(
                    filterQuery = msg.query,
                    filteredCourses = msg.filteredCourses
                )
                is Msg.CourseSelected -> copy(selectedCourseId = msg.courseId)
                is Msg.ErrorOccurred -> copy(isLoading = false, isRefreshing = false, error = msg.error)
            }
    }
}
