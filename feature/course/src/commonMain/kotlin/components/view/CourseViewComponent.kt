package components.view

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
import model.course.CourseDomain

/**
 * Параметры для создания компонента просмотра курса
 */
data class CourseViewComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit,
    val courseId: String? = null,
    val isCreateMode: Boolean = false,
    val onCourseCreated: ((String) -> Unit)? = null,
    val onCourseUpdated: ((String) -> Unit)? = null
)

/**
 * Компонент просмотра курса
 */
interface CourseViewComponent {
    /**
     * Состояние компонента
     */
    val state: StateFlow<CourseViewStore.State>

    /**
     * Загрузить курс по ID
     */
    fun loadCourse(courseId: String)

    /**
     * Создать новый курс
     */
    fun createCourse(course: CourseDomain)

    /**
     * Обновить существующий курс
     */
    fun updateCourse(course: CourseDomain)

    /**
     * Переключить в режим редактирования
     */
    fun switchToEditMode()

    /**
     * Переключить в режим просмотра
     */
    fun switchToViewMode()

    /**
     * Сбросить ошибку
     */
    fun resetError()

    /**
     * Вернуться назад
     */
    fun onBack()
}

/**
 * Реализация компонента просмотра курса
 */
class DefaultCourseViewComponent(
    componentContext: ComponentContext,
    private val di: DI,
    storeFactory: StoreFactory,
    courseId: String?,
    private val onBackClicked: () -> Unit,
    private val isCreateMode: Boolean = false,
    private val onCourseCreated: ((String) -> Unit)? = null,
    private val onCourseUpdated: ((String) -> Unit)? = null
) : CourseViewComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + rDispatchers.main)

    private val store = instanceKeeper.getStore {
        CourseViewStoreFactory.create(di = di, storeFactory = storeFactory, isCreateMode = isCreateMode)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<CourseViewStore.State> = store.stateFlow

    init {
        scope.launch {
            store.labels.collectLatest { label ->
                when (label) {
                    CourseViewStore.Label.NavigateBack -> onBackClicked()
                    is CourseViewStore.Label.CourseCreated -> {
                        onCourseCreated?.invoke(label.courseId)
                    }
                    is CourseViewStore.Label.CourseUpdated -> {
                        onCourseUpdated?.invoke(label.courseId)
                    }
                }
            }
        }
        
        // Если ID курса передан при создании компонента и не в режиме создания, загружаем его
        if (!isCreateMode) {
            courseId?.let { 
                if (it.isNotBlank()) {
                    loadCourse(it)
                }
            }
        }
    }

    override fun loadCourse(courseId: String) {
        store.accept(CourseViewStore.Intent.LoadCourse(courseId))
    }

    override fun createCourse(course: CourseDomain) {
        store.accept(CourseViewStore.Intent.CreateCourse(course))
    }

    override fun updateCourse(course: CourseDomain) {
        val currentCourse = state.value.course
        if (currentCourse != null) {
            store.accept(CourseViewStore.Intent.UpdateCourse(currentCourse.id, course))
        } else {
            // Если нет текущего курса, создаем новый
            createCourse(course)
        }
    }

    override fun switchToEditMode() {
        store.accept(CourseViewStore.Intent.SwitchToEditMode)
    }

    override fun switchToViewMode() {
        store.accept(CourseViewStore.Intent.SwitchToViewMode)
    }

    override fun resetError() {
        store.accept(CourseViewStore.Intent.ResetError)
    }

    override fun onBack() {
        store.accept(CourseViewStore.Intent.NavigateBack)
    }

    fun destroy() {
        scope.cancel()
    }
} 