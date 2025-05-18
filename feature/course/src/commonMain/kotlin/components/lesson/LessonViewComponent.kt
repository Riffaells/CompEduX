package components.lesson

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
import model.course.CourseLessonDomain

/**
 * Параметры для создания компонента просмотра урока
 */
data class LessonViewComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit,
    val lessonId: String? = null,
    val courseId: String? = null,
    val moduleId: String? = null
)

/**
 * Компонент просмотра урока
 */
interface LessonViewComponent {
    /**
     * Состояние компонента
     */
    val state: StateFlow<LessonViewStore.State>

    /**
     * Загрузить урок по ID
     */
    fun loadLesson(lessonId: String)

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
 * Реализация компонента просмотра урока
 */
class DefaultLessonViewComponent(
    componentContext: ComponentContext,
    private val di: DI,
    storeFactory: StoreFactory,
    lessonId: String?,
    courseId: String?,
    private val onBackClicked: () -> Unit
) : LessonViewComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + rDispatchers.main)

    private val store = instanceKeeper.getStore {
        LessonViewStoreFactory.create(di = di, storeFactory = storeFactory)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<LessonViewStore.State> = store.stateFlow

    init {
        scope.launch {
            store.labels.collectLatest { label ->
                when (label) {
                    LessonViewStore.Label.NavigateBack -> onBackClicked()
                }
            }
        }
        
        // Если ID урока передан при создании компонента, загружаем его
        lessonId?.let { 
            if (it.isNotBlank()) {
                loadLesson(it)
            }
        }
    }

    override fun loadLesson(lessonId: String) {
        store.accept(LessonViewStore.Intent.LoadLesson(lessonId))
    }

    override fun resetError() {
        store.accept(LessonViewStore.Intent.ResetError)
    }

    override fun onBack() {
        store.accept(LessonViewStore.Intent.NavigateBack)
    }

    fun destroy() {
        scope.cancel()
    }
} 