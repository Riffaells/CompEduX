package components.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import navigation.rDispatchers
import org.kodein.di.DI

/**
 * Параметры для создания компонента списка курсов
 */
data class CourseListComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit,
    val onCourseSelected: ((String) -> Unit)? = null
)

/**
 * Компонент списка курсов
 */
interface CourseListComponent {
    /**
     * Состояние компонента
     */
    val state: StateFlow<CourseListStore.State>

    /**
     * Загрузить список курсов
     */
    fun loadCourses()

    /**
     * Обновить список курсов (pull-to-refresh)
     */
    fun refreshCourses()

    /**
     * Отфильтровать курсы по запросу
     */
    fun filterCourses(query: String)

    /**
     * Выбрать курс для просмотра
     */
    fun selectCourse(courseId: String)

    /**
     * Вернуться назад
     */
    fun onBack()
}

/**
 * Реализация компонента списка курсов
 */
class DefaultCourseListComponent(
    componentContext: ComponentContext,
    private val di: DI,
    storeFactory: StoreFactory,
    private val onBackClicked: () -> Unit,
    private val onCourseSelected: ((String) -> Unit)? = null
) : CourseListComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + rDispatchers.main)

    private val store = instanceKeeper.getStore {
        CourseListStoreFactory(
            storeFactory = storeFactory,
            di = di
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<CourseListStore.State> = store.stateFlow

    init {
        scope.launch {
            store.labels.collectLatest { label ->
                when (label) {
                    is CourseListStore.Label.NavigateToCourse -> {
                        onCourseSelected?.invoke(label.courseId)
                    }
                    CourseListStore.Label.NavigateBack -> {
                        onBackClicked()
                    }
                }
            }
        }
    }

    override fun loadCourses() {
        store.accept(CourseListStore.Intent.LoadCourses)
    }

    override fun refreshCourses() {
        store.accept(CourseListStore.Intent.RefreshCourses)
    }

    override fun filterCourses(query: String) {
        store.accept(CourseListStore.Intent.FilterCourses(query))
    }

    override fun selectCourse(courseId: String) {
        store.accept(CourseListStore.Intent.SelectCourse(courseId))
    }

    override fun onBack() {
        store.accept(CourseListStore.Intent.NavigateBack)
    }

    fun destroy() {
        scope.cancel()
    }
} 