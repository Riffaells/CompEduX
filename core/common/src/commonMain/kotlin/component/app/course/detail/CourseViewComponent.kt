package component.app.course.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.course.detail.store.CourseViewStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import model.course.CourseDomain
import org.kodein.di.DI

/**
 * Component for viewing/editing a course
 */
interface CourseViewComponent {

    /**
     * View/Edit mode
     */
    enum class Mode {
        VIEW, EDIT
    }

    /**
     * State flow
     */
    val state: StateFlow<CourseViewStore.State>

    /**
     * Toggle between view and edit modes
     */
    fun toggleEditMode()

    /**
     * Navigate back
     */
    fun onBack()

    /**
     * Select a module for viewing or editing
     */
    fun onModuleSelected(moduleId: String)

    /**
     * Add a new module to the course
     */
    fun onAddModule()

    /**
     * Save changes to the course
     */
    fun onSave()

    /**
     * Methods for direct store interaction
     */
    fun updateTitle(title: Map<String, String>)
    fun updateDescription(description: Map<String, String>)
    fun togglePublish()
    fun cancelEditMode()
    fun confirmDiscard()

    /**
     * Method for accepting direct intents
     */
    fun accept(intent: CourseViewStore.Intent)

    companion object {
        /**
         * Create component
         */
        fun create(
            componentContext: ComponentContext,
            di: DI,
            storeFactory: StoreFactory,
            courseId: String?,
            onBack: () -> Unit,
            onModuleSelected: (String) -> Unit,
            onAddModule: (String) -> Unit
        ): CourseViewComponent =
            DefaultCourseViewComponent(
                componentContext = componentContext,
                di = di,
                storeFactory = storeFactory,
                courseId = courseId,
                onBack = onBack,
                onModuleSelected = onModuleSelected,
                onAddModule = onAddModule
            )
    }
}

/**
 * Default implementation of CourseViewComponent
 */
class DefaultCourseViewComponent(
    componentContext: ComponentContext,
    private val di: DI,
    storeFactory: StoreFactory,
    courseId: String?,
    private val onBack: () -> Unit,
    private val onModuleSelected: (String) -> Unit,
    private val onAddModule: (String) -> Unit
) : CourseViewComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val store = instanceKeeper.getStore {
        CourseViewStore.create(
            di = di,
            storeFactory = storeFactory
        ).also { store ->
            // Initialize the store
            store.accept(CourseViewStore.Intent.Init(courseId))
        }
    }

    override val state: StateFlow<CourseViewStore.State> = store.stateFlow

    init {
        scope.launch {
            store.labels.collectLatest { label ->
                when (label) {
                    is CourseViewStore.Label.NavigateToModule -> {
                        onModuleSelected(label.moduleId)
                    }
                    is CourseViewStore.Label.CreateModule -> {
                        onAddModule(label.courseId)
                    }
                    is CourseViewStore.Label.NavigateBack -> {
                        onBack()
                    }
                }
            }
        }
    }

    override fun toggleEditMode() {
        val currentState = store.state
        if (currentState.mode == CourseViewStore.Mode.VIEW) {
            store.accept(CourseViewStore.Intent.EnableEditMode)
        } else {
            store.accept(CourseViewStore.Intent.DisableEditMode)
        }
    }

    override fun onBack() {
        val currentState = store.state
        if (currentState.mode == CourseViewStore.Mode.EDIT && currentState.isDirty) {
            store.accept(CourseViewStore.Intent.DisableEditMode)
        } else {
            onBack.invoke()
        }
    }

    override fun onModuleSelected(moduleId: String) {
        store.accept(CourseViewStore.Intent.SelectModule(moduleId))
    }

    override fun onAddModule() {
        store.accept(CourseViewStore.Intent.AddModule)
    }

    override fun onSave() {
        store.accept(CourseViewStore.Intent.SaveCourse)
    }

    override fun updateTitle(title: Map<String, String>) {
        store.accept(CourseViewStore.Intent.UpdateTitle(title))
    }

    override fun updateDescription(description: Map<String, String>) {
        store.accept(CourseViewStore.Intent.UpdateDescription(description))
    }

    override fun togglePublish() {
        store.accept(CourseViewStore.Intent.TogglePublish)
    }

    override fun cancelEditMode() {
        store.accept(CourseViewStore.Intent.DisableEditMode)
    }

    override fun confirmDiscard() {
        store.accept(CourseViewStore.Intent.ConfirmDiscard)
    }

    override fun accept(intent: CourseViewStore.Intent) {
        store.accept(intent)
    }

    // Метод для очистки ресурсов при уничтожении компонента
    fun destroy() {
        scope.cancel()
    }
} 