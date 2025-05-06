package component.app.course.detail.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.DomainResult
import model.course.CourseDomain
import model.course.LocalizedContent
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import repository.course.CourseRepository

/**
 * Store for course viewing and editing
 */
interface CourseViewStore : Store<CourseViewStore.Intent, CourseViewStore.State, CourseViewStore.Label> {

    /**
     * View/Edit mode
     */
    enum class Mode {
        VIEW, EDIT
    }

    sealed class Intent {
        data class Init(val courseId: String?) : Intent()
        object EnableEditMode : Intent()
        object DisableEditMode : Intent()
        object ConfirmDiscard : Intent()
        data class SelectModule(val moduleId: String) : Intent()
        object AddModule : Intent()
        object SaveCourse : Intent()
        data class UpdateTitle(val title: Map<String, String>) : Intent()
        data class UpdateDescription(val description: Map<String, String>) : Intent()
        object TogglePublish : Intent()
    }

    data class State(
        val courseId: String? = null,
        val originalCourse: CourseDomain? = null,
        val editedCourse: CourseDomain? = null,
        val mode: Mode = Mode.VIEW,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isDirty: Boolean = false,
        val showDiscardDialog: Boolean = false
    )

    sealed class Label {
        data class NavigateToModule(val moduleId: String) : Label()
        data class CreateModule(val courseId: String) : Label()
        object NavigateBack : Label()
    }

    companion object {
        fun create(di: DI, storeFactory: StoreFactory): CourseViewStore =
            CourseViewStoreFactory(di, storeFactory).create()
    }
}

/**
 * Factory class for CourseViewStore
 */
private class CourseViewStoreFactory(
    override val di: DI,
    private val storeFactory: StoreFactory
) : DIAware {

    private val courseRepository: CourseRepository by instance()

    /**
     * Create the store
     */
    fun create(): CourseViewStore =
        object : CourseViewStore, Store<CourseViewStore.Intent, CourseViewStore.State, CourseViewStore.Label> by storeFactory.create(
            name = "CourseViewStore",
            initialState = CourseViewStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed class Msg {
        data class CourseLoaded(val course: CourseDomain) : Msg()
        data class ErrorLoading(val error: String) : Msg()
        object LoadingStarted : Msg()
        object SwitchToEditMode : Msg()
        object SwitchToViewMode : Msg()
        object ShowDiscardDialog : Msg()
        object HideDiscardDialog : Msg()
        data class TitleUpdated(val title: Map<String, String>) : Msg()
        data class DescriptionUpdated(val description: Map<String, String>) : Msg()
        object PublishToggled : Msg()
        object CourseUpdated : Msg()
    }

    private inner class ExecutorImpl : CoroutineExecutor<CourseViewStore.Intent, Unit, CourseViewStore.State, Msg, CourseViewStore.Label>(Dispatchers.Main) {
        override fun executeAction(action: Unit) {
            // No action on bootstrap
        }

        override fun executeIntent(intent: CourseViewStore.Intent) {
            when (intent) {
                is CourseViewStore.Intent.Init -> initCourse(intent.courseId)
                is CourseViewStore.Intent.EnableEditMode -> dispatch(Msg.SwitchToEditMode)
                is CourseViewStore.Intent.DisableEditMode -> {
                    val state = state()
                    if (state.isDirty) {
                        dispatch(Msg.ShowDiscardDialog)
                    } else {
                        dispatch(Msg.SwitchToViewMode)
                    }
                }
                is CourseViewStore.Intent.ConfirmDiscard -> {
                    dispatch(Msg.HideDiscardDialog)
                    dispatch(Msg.SwitchToViewMode)
                }
                is CourseViewStore.Intent.SelectModule -> {
                    val state = state()
                    if (state.courseId != null) {
                        publish(CourseViewStore.Label.NavigateToModule(intent.moduleId))
                    }
                }
                is CourseViewStore.Intent.AddModule -> {
                    val state = state()
                    if (state.courseId != null) {
                        publish(CourseViewStore.Label.CreateModule(state.courseId))
                    }
                }
                is CourseViewStore.Intent.SaveCourse -> saveCourse()
                is CourseViewStore.Intent.UpdateTitle -> dispatch(Msg.TitleUpdated(intent.title))
                is CourseViewStore.Intent.UpdateDescription -> dispatch(Msg.DescriptionUpdated(intent.description))
                is CourseViewStore.Intent.TogglePublish -> dispatch(Msg.PublishToggled)
            }
        }

        private fun initCourse(courseId: String?) {
            if (courseId == null) {
                // Create new course mode - not implemented yet
                return
            }

            scope.launch {
                dispatch(Msg.LoadingStarted)
                try {
                    val result = courseRepository.getCourse(courseId)
                    when (result) {
                        is DomainResult.Success -> {
                            dispatch(Msg.CourseLoaded(result.data))
                        }
                        is DomainResult.Error -> {
                            dispatch(Msg.ErrorLoading(result.error.message))
                        }
                        is DomainResult.Loading -> {
                            // Уже находимся в состоянии загрузки
                        }
                    }
                } catch (e: Exception) {
                    dispatch(Msg.ErrorLoading(e.message ?: "Error loading course"))
                }
            }
        }

        private fun saveCourse() {
            val state = state()
            val editedCourse = state.editedCourse ?: return

            scope.launch {
                dispatch(Msg.LoadingStarted)
                try {
                    val result = courseRepository.updateCourse(editedCourse.id, editedCourse)
                    when (result) {
                        is DomainResult.Success -> {
                            dispatch(Msg.CourseUpdated)
                            dispatch(Msg.SwitchToViewMode)
                        }
                        is DomainResult.Error -> {
                            dispatch(Msg.ErrorLoading(result.error.message))
                        }
                        is DomainResult.Loading -> {
                            // Уже находимся в состоянии загрузки
                        }
                    }
                } catch (e: Exception) {
                    dispatch(Msg.ErrorLoading(e.message ?: "Error saving course"))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<CourseViewStore.State, Msg> {
        override fun CourseViewStore.State.reduce(msg: Msg): CourseViewStore.State =
            when (msg) {
                is Msg.LoadingStarted -> copy(isLoading = true, error = null)
                is Msg.ErrorLoading -> copy(isLoading = false, error = msg.error)
                is Msg.CourseLoaded -> copy(
                    isLoading = false,
                    error = null,
                    courseId = msg.course.id,
                    originalCourse = msg.course,
                    editedCourse = msg.course,
                    isDirty = false
                )
                is Msg.SwitchToEditMode -> copy(mode = CourseViewStore.Mode.EDIT)
                is Msg.SwitchToViewMode -> copy(
                    mode = CourseViewStore.Mode.VIEW,
                    editedCourse = originalCourse,
                    isDirty = false
                )
                is Msg.ShowDiscardDialog -> copy(showDiscardDialog = true)
                is Msg.HideDiscardDialog -> copy(showDiscardDialog = false)
                is Msg.TitleUpdated -> {
                    val updatedCourse = editedCourse?.copy(title = LocalizedContent(msg.title)) ?: return this
                    copy(
                        editedCourse = updatedCourse,
                        isDirty = true
                    )
                }
                is Msg.DescriptionUpdated -> {
                    val updatedCourse = editedCourse?.copy(description = LocalizedContent(msg.description)) ?: return this
                    copy(
                        editedCourse = updatedCourse,
                        isDirty = true
                    )
                }
                is Msg.PublishToggled -> {
                    val current = editedCourse ?: return this
                    val updatedCourse = current.copy(isPublished = !current.isPublished)
                    copy(
                        editedCourse = updatedCourse,
                        isDirty = true
                    )
                }
                is Msg.CourseUpdated -> copy(
                    isLoading = false,
                    error = null,
                    originalCourse = editedCourse,
                    isDirty = false
                )
            }
    }
} 