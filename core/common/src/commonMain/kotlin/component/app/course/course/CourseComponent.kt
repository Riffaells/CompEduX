package component.app.course.course

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.course.course.store.CourseStore
import component.app.course.course.store.CourseStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import model.course.CourseDomain
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Unified Component for viewing and editing course content
 */
interface CourseComponent {
    /**
     * Course state
     */
    val state: StateFlow<CourseStore.State>

    /**
     * Handle UI intents
     * @param intent UI intent
     */
    fun accept(intent: CourseStore.Intent)

    /**
     * Handle back button click
     */
    fun onBackClick()

    /**
     * Reload the course data
     */
    fun reload()
    
    /**
     * Toggle between view and edit modes
     */
    fun toggleEditMode()

    /**
     * Save changes to the course
     */
    fun saveCourse()
    
    /**
     * Cancel edit mode and discard changes
     */
    fun cancelEdit()
    
    /**
     * Update course title
     */
    fun updateTitle(title: Map<String, String>)
    
    /**
     * Update course description
     */
    fun updateDescription(description: Map<String, String>)
    
    /**
     * Toggle course publication status
     */
    fun togglePublish()

    /**
     * Navigate to a module
     */
    fun navigateToModule(moduleId: String)
    
    /**
     * Add a new module to the course
     */
    fun addModule()
}

/**
 * Default implementation of unified course component
 */
class DefaultCourseComponent(
    componentContext: ComponentContext,
    courseId: String?,
    private val onBack: () -> Unit,
    override val di: DI
) : CourseComponent, DIAware, ComponentContext by componentContext {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val storeFactory by instance<StoreFactory>()

    /**
     * Store for managing component state
     */
    private val store = instanceKeeper.getStore {
        CourseStoreFactory(
            storeFactory = storeFactory,
            courseId = courseId,
            di = di
        ).create()
    }

    /**
     * Component state
     */
    override val state: StateFlow<CourseStore.State> = store.stateFlow

    init {
        // Initialize the store and load data
        store.accept(CourseStore.Intent.Load)
        
        // Setup label handling
        scope.launch {
            store.labels.collectLatest { label ->
                when (label) {
                    is CourseStore.Label.NavigateToModule -> navigateToModule(label.moduleId)
                    is CourseStore.Label.CourseSaved -> Unit // Обработка успешного сохранения
                    is CourseStore.Label.CourseDeleted -> onBack() // Возврат после удаления
                    // Здесь можно добавить обработку других меток
                }
            }
        }
    }

    /**
     * Handle intents
     */
    override fun accept(intent: CourseStore.Intent) {
        store.accept(intent)
    }

    /**
     * Handle back button click
     */
    override fun onBackClick() {
        // Check if in edit mode with unsaved changes
        val currentState = state.value
        if (currentState.isEditing && currentState.isDirty) {
            store.accept(CourseStore.Intent.ShowDiscardConfirmation)
        } else {
            onBack()
        }
    }

    /**
     * Reload the current course
     */
    override fun reload() {
        store.accept(CourseStore.Intent.Load)
    }

    /**
     * Toggle between view and edit modes
     */
    override fun toggleEditMode() {
        val currentState = state.value
        if (!currentState.isEditing) {
            store.accept(CourseStore.Intent.Edit)
        } else {
            store.accept(CourseStore.Intent.Cancel)
        }
    }

    /**
     * Save changes to the course
     */
    override fun saveCourse() {
        store.accept(CourseStore.Intent.SaveCourse)
    }
    
    /**
     * Cancel edit mode and discard changes
     */
    override fun cancelEdit() {
        store.accept(CourseStore.Intent.Cancel)
    }
    
    /**
     * Update course title
     */
    override fun updateTitle(title: Map<String, String>) {
        store.accept(CourseStore.Intent.UpdateTitle(title))
    }
    
    /**
     * Update course description
     */
    override fun updateDescription(description: Map<String, String>) {
        store.accept(CourseStore.Intent.UpdateDescription(description))
    }
    
    /**
     * Toggle course publication status
     */
    override fun togglePublish() {
        val currentState = state.value
        val isCurrentlyPublished = currentState.editIsPublished
        store.accept(CourseStore.Intent.UpdatePublishState(!isCurrentlyPublished))
    }

    /**
     * Navigate to a module
     */
    override fun navigateToModule(moduleId: String) {
        store.accept(CourseStore.Intent.EditModule(moduleId))
    }
    
    /**
     * Add a new module to the course
     */
    override fun addModule() {
        store.accept(CourseStore.Intent.AddModule)
    }
    
    // Метод вызывается при уничтожении компонента
    fun destroy() {
        scope.cancel()
    }
}
