package components.view

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.slot.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.TechnologyTreeComponent
import component.TechnologyTreeComponentParams
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import model.course.CourseDomain
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.factory
import org.kodein.di.instance

/**
 * Параметры для создания компонента просмотра курса
 */
data class CourseViewComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit,
    val courseId: String? = null,
    val isCreateMode: Boolean = false
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
     * Слот дерева технологий
     */
    val treeSlot: Value<ChildSlot<TreeSlotConfig, TreeSlotChild>>

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
    fun updateCourse(courseId: String, course: CourseDomain)

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
    fun navigateBack()

    /**
     * Показать дерево технологий
     */
    fun showTechnologyTree()

    /**
     * Скрыть дерево технологий
     */
    fun dismissTechnologyTree()

    /**
     * Создать новое дерево технологий для курса
     */
    fun createTechnologyTree()

    /**
     * Перейти к дереву технологий по ID
     */
    fun navigateToTechnologyTree(courseId: String, technologyTreeId: String)

    /**
     * Принять Intent для обработки
     */
    fun accept(intent: CourseViewStore.Intent)

    /**
     * Дочерние компоненты для слота дерева технологий
     */
    sealed class TreeSlotChild {
        data class Tree(val component: TechnologyTreeComponent) : TreeSlotChild()
    }

    /**
     * Конфигурация для слота дерева технологий
     */
    @Serializable
    sealed class TreeSlotConfig {
        @Serializable
        data class Tree(val courseId: String) : TreeSlotConfig()
    }
}

/**
 * Реализация компонента просмотра курса
 */
class DefaultCourseViewComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onBack: () -> Unit,
    private val isCreateMode: Boolean = false,
    private val initialCourseId: String? = null
) : CourseViewComponent, ComponentContext by componentContext, DIAware {

    private val storeFactory: StoreFactory by instance()
    private val technologyTreeComponentFactory by factory<TechnologyTreeComponentParams, TechnologyTreeComponent>()

    // Корутин скоуп, привязанный к жизненному циклу компонента
    private val scope = coroutineScope(rDispatchers.main)

    // Навигация для слота дерева технологий
    private val treeSlotNavigation = SlotNavigation<CourseViewComponent.TreeSlotConfig>()

    // Слот для дерева технологий
    override val treeSlot: Value<ChildSlot<CourseViewComponent.TreeSlotConfig, CourseViewComponent.TreeSlotChild>> =
        childSlot(
            source = treeSlotNavigation,
            serializer = CourseViewComponent.TreeSlotConfig.serializer(),
            handleBackButton = true,
            key = "TreeSlot"
        ) { config, slotContext ->
            when (config) {
                is CourseViewComponent.TreeSlotConfig.Tree -> CourseViewComponent.TreeSlotChild.Tree(
                    technologyTreeComponentFactory(
                        TechnologyTreeComponentParams(
                            componentContext = slotContext,
                            courseId = config.courseId,
                            onBack = { treeSlotNavigation.dismiss() }
                        )
                    )
                )
            }
        }

    private val store = instanceKeeper.getStore {
        CourseViewStoreFactory.create(storeFactory, di, isCreateMode)
    }

    override val state: StateFlow<CourseViewStore.State> = store.stateFlow

    init {
        // Загружаем курс, если передан ID
        initialCourseId?.let { courseId ->
            if (courseId.isNotBlank() && !isCreateMode) {
                loadCourse(courseId)
            }
        }

        // Подписываемся на метки из стора
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is CourseViewStore.Label.NavigateBack -> onBack()
                    is CourseViewStore.Label.CourseCreated -> {}
                    is CourseViewStore.Label.CourseUpdated -> {}
                    is CourseViewStore.Label.TechnologyTreeCreated -> {
                        // После создания дерева технологий показываем его
                        label.courseId?.let {
                            showTechnologyTreeForCourse(it)
                        }
                    }
                }
            }
        }
    }

    override fun accept(intent: CourseViewStore.Intent) {
        store.accept(intent)
    }

    override fun loadCourse(courseId: String) {
        accept(CourseViewStore.Intent.LoadCourse(courseId))
    }

    override fun createCourse(course: CourseDomain) {
        accept(CourseViewStore.Intent.CreateCourse(course))
    }

    override fun updateCourse(courseId: String, course: CourseDomain) {
        accept(CourseViewStore.Intent.UpdateCourse(courseId, course))
    }

    override fun switchToEditMode() {
        accept(CourseViewStore.Intent.SwitchToEditMode)
    }

    override fun switchToViewMode() {
        accept(CourseViewStore.Intent.SwitchToViewMode)
    }

    override fun resetError() {
        accept(CourseViewStore.Intent.ResetError)
    }

    override fun navigateBack() {
        accept(CourseViewStore.Intent.NavigateBack)
    }

    override fun showTechnologyTree() {
        val currentCourse = state.value.course
        if (currentCourse != null) {
            showTechnologyTreeForCourse(currentCourse.id)
        } else {
            // Обработка ошибки
            accept(CourseViewStore.Intent.Error("Курс не загружен"))
        }
    }

    override fun navigateToTechnologyTree(courseId: String, technologyTreeId: String) {
        // Активируем слот с деревом технологий для указанного курса
        showTechnologyTreeForCourse(courseId)
    }

    private fun showTechnologyTreeForCourse(courseId: String) {
        treeSlotNavigation.activate(CourseViewComponent.TreeSlotConfig.Tree(courseId = courseId))
    }

    override fun dismissTechnologyTree() {
        treeSlotNavigation.dismiss()
    }

    override fun createTechnologyTree() {
        accept(CourseViewStore.Intent.CreateTechnologyTree)
    }
} 