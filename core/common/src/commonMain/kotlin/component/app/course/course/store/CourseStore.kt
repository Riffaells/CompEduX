package component.app.course.course.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import model.course.CourseDomain
import model.course.CourseModuleDomain
import model.course.CourseVisibilityDomain
import model.course.LocalizedContent
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import repository.course.CourseRepository

/**
 * Store for course management
 */
interface CourseStore : Store<CourseStore.Intent, CourseStore.State, CourseStore.Label> {

    /**
     * UI intents
     */
    sealed interface Intent {
        /**
         * Initialize course
         */
        data object Init : Intent

        /**
         * Load course - reloads the current course data
         */
        data object Load : Intent

        /**
         * Set edit mode for the current course
         */
        data object Edit : Intent

        /**
         * Cancel edit mode
         */
        data object Cancel : Intent

        /**
         * Show discard confirmation dialog
         */
        data object ShowDiscardConfirmation : Intent

        /**
         * Hide discard confirmation dialog
         */
        data object HideDiscardConfirmation : Intent

        /**
         * Save course
         */
        data object SaveCourse : Intent

        /**
         * Delete course
         */
        data object DeleteCourse : Intent

        /**
         * Update course title
         */
        data class UpdateTitle(val title: Map<String, String>) : Intent

        /**
         * Update course description
         */
        data class UpdateDescription(val description: Map<String, String>) : Intent

        /**
         * Update course visibility
         */
        data class UpdateVisibility(val visibility: CourseVisibilityDomain) : Intent

        /**
         * Update course tags
         */
        data class UpdateTags(val tags: List<String>) : Intent

        /**
         * Update course publish state
         */
        data class UpdatePublishState(val isPublished: Boolean) : Intent

        /**
         * Add module to course
         */
        data object AddModule : Intent

        /**
         * Edit course module
         */
        data class EditModule(val moduleId: String) : Intent

        /**
         * Delete course module
         */
        data class DeleteModule(val moduleId: String) : Intent
    }

    /**
     * Course state
     */
    @Serializable
    data class State(
        val courseId: String? = null,
        val course: CourseDomain? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isEditing: Boolean = false,
        val isSaving: Boolean = false,
        val isDeleting: Boolean = false,
        val isDirty: Boolean = false,
        val showDiscardDialog: Boolean = false,

        // Edit fields
        val editTitle: Map<String, String> = emptyMap(),
        val editDescription: Map<String, String> = emptyMap(),
        val editVisibility: CourseVisibilityDomain = CourseVisibilityDomain.PRIVATE,
        val editTags: List<String> = emptyList(),
        val editIsPublished: Boolean = false,

        // Modules
        val modules: List<CourseModuleDomain> = emptyList()
    )

    /**
     * Labels for navigation
     */
    sealed interface Label {
        /**
         * Course saved successfully
         */
        data class CourseSaved(val courseId: String) : Label

        /**
         * Course deleted
         */
        data object CourseDeleted : Label

        /**
         * Navigate to module editor
         */
        data class NavigateToModule(val moduleId: String) : Label
    }
}

/**
 * Factory for creating course Store
 */
class CourseStoreFactory(
    private val storeFactory: StoreFactory,
    private val courseId: String?,
    override val di: DI
) : DIAware {

    private val logger by instance<Logger>()
    private val courseRepository by instance<CourseRepository>()

    /**
     * Create Store
     */
    fun create(): CourseStore =
        object : CourseStore, Store<CourseStore.Intent, CourseStore.State, CourseStore.Label> by storeFactory.create(
            name = "CourseStore",
            initialState = CourseStore.State(courseId = courseId),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl
        ) {}

    /**
     * Messages for Reducer
     */
    private sealed interface Msg {
        /**
         * Set loading state
         */
        data class SetLoading(val isLoading: Boolean) : Msg

        /**
         * Set error
         */
        data class SetError(val error: String?) : Msg

        /**
         * Update course
         */
        data class UpdateCourse(val course: CourseDomain) : Msg

        /**
         * Set edit mode
         */
        data class SetEditMode(val isEditing: Boolean) : Msg

        /**
         * Set saving state
         */
        data class SetSaving(val isSaving: Boolean) : Msg

        /**
         * Set deleting state
         */
        data class SetDeleting(val isDeleting: Boolean) : Msg

        /**
         * Set isDirty flag
         */
        data class SetDirty(val isDirty: Boolean) : Msg

        /**
         * Show discard dialog
         */
        data class SetShowDiscardDialog(val show: Boolean) : Msg

        /**
         * Update edit fields
         */
        data class UpdateEditFields(
            val title: Map<String, String>? = null,
            val description: Map<String, String>? = null,
            val visibility: CourseVisibilityDomain? = null,
            val tags: List<String>? = null,
            val isPublished: Boolean? = null
        ) : Msg

        /**
         * Update modules
         */
        data class UpdateModules(val modules: List<CourseModuleDomain>) : Msg

        /**
         * Add module
         */
        data class AddModule(val module: CourseModuleDomain) : Msg

        /**
         * Remove module
         */
        data class RemoveModule(val moduleId: String) : Msg
    }

    /**
     * Executor for handling intents
     */
    private inner class ExecutorImpl :
        CoroutineExecutor<CourseStore.Intent, Unit, CourseStore.State, Msg, CourseStore.Label>(
            Dispatchers.Main
        ) {

        override fun executeAction(action: Unit) {
            // Initialize when creating store
            executeIntent(CourseStore.Intent.Init)
        }

        override fun executeIntent(intent: CourseStore.Intent) {
            when (intent) {
                is CourseStore.Intent.Init -> {
                    val courseId = state().courseId
                    if (courseId != null && courseId.isNotBlank()) {
                        loadCourse(courseId)
                    } else {
                        // Create new course
                        initNewCourse()
                    }
                }
                is CourseStore.Intent.Load -> {
                    val courseId = state().courseId
                    if (courseId != null && courseId.isNotBlank()) {
                        loadCourse(courseId)
                    } else {
                        initNewCourse()
                    }
                }
                is CourseStore.Intent.Edit -> {
                    if (!state().isEditing) {
                        // Prepare data for editing
                        prepareEditMode()
                    }
                }
                is CourseStore.Intent.Cancel -> {
                    // Reset state to view mode
                    dispatch(Msg.SetEditMode(false))
                    dispatch(Msg.SetDirty(false))
                    dispatch(Msg.SetShowDiscardDialog(false))
                }
                is CourseStore.Intent.ShowDiscardConfirmation -> {
                    if (state().isDirty) {
                        dispatch(Msg.SetShowDiscardDialog(true))
                    } else {
                        dispatch(Msg.SetEditMode(false))
                    }
                }
                is CourseStore.Intent.HideDiscardConfirmation -> {
                    dispatch(Msg.SetShowDiscardDialog(false))
                }
                is CourseStore.Intent.SaveCourse -> {
                    saveCourse()
                }
                is CourseStore.Intent.DeleteCourse -> {
                    deleteCourse()
                }
                is CourseStore.Intent.UpdateTitle -> {
                    dispatch(Msg.UpdateEditFields(title = intent.title))
                    dispatch(Msg.SetDirty(true))
                }
                is CourseStore.Intent.UpdateDescription -> {
                    dispatch(Msg.UpdateEditFields(description = intent.description))
                    dispatch(Msg.SetDirty(true))
                }
                is CourseStore.Intent.UpdateVisibility -> {
                    dispatch(Msg.UpdateEditFields(visibility = intent.visibility))
                    dispatch(Msg.SetDirty(true))
                }
                is CourseStore.Intent.UpdateTags -> {
                    dispatch(Msg.UpdateEditFields(tags = intent.tags))
                    dispatch(Msg.SetDirty(true))
                }
                is CourseStore.Intent.UpdatePublishState -> {
                    dispatch(Msg.UpdateEditFields(isPublished = intent.isPublished))
                    dispatch(Msg.SetDirty(true))
                }
                is CourseStore.Intent.AddModule -> {
                    // Создание модуля
                    addNewModule()
                }
                is CourseStore.Intent.EditModule -> {
                    // Навигация к модулю
                    publish(CourseStore.Label.NavigateToModule(intent.moduleId))
                }
                is CourseStore.Intent.DeleteModule -> {
                    // Удаление модуля
                    deleteModule(intent.moduleId)
                }
            }
        }

        private fun loadCourse(courseId: String) {
            // Здесь нужно реализовать загрузку курса из репозитория
            // Это зависит от реализации CourseRepository
            dispatch(Msg.SetLoading(true))
            scope.launch {
                try {
                    // Для примера - в реальности нужно использовать ваш CourseRepository
                    val result = courseRepository.getCourse(courseId)
                    
                    when (result) {
                        is model.DomainResult.Success -> {
                            dispatch(Msg.UpdateCourse(result.data))
                            loadModules(courseId)
                        }
                        is model.DomainResult.Error -> {
                            dispatch(Msg.SetError(result.error.message))
                        }
                        is model.DomainResult.Loading -> {
                            // Уже в состоянии загрузки
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Error loading course", e)
                    dispatch(Msg.SetError(e.message ?: "Ошибка загрузки курса"))
                } finally {
                    dispatch(Msg.SetLoading(false))
                }
            }
        }

        private fun loadModules(courseId: String) {
            scope.launch {
                try {
                    // Поскольку в репозитории нет отдельного метода для модулей,
                    // просто берем их из последнего загруженного курса
                    val course = state().course
                    if (course != null && course.id == courseId) {
                        // Преобразуем список строк в модули
                        val modules = course.modules.mapIndexed { index, module ->
                            CourseModuleDomain(
                                id = module.id,
                                courseId = courseId,
                                title = module.title,
                                description = module.description ?: LocalizedContent(),
                                order = index,
                                lessons = module.lessons
                            )
                        }
                        dispatch(Msg.UpdateModules(modules))
                    }
                    // В реальном приложении здесь должен быть запрос к API для получения модулей
                } catch (e: Exception) {
                    logger.e("Error loading modules", e)
                    // Не устанавливаем ошибку, так как модули могут загружаться отдельно
                }
            }
        }

        private fun initNewCourse() {
            // Инициализация нового курса
            val emptyTitle = LocalizedContent(mapOf("ru" to "", "en" to ""))
            val emptyDescription = LocalizedContent(mapOf("ru" to "", "en" to ""))
            
            val emptyCourse = CourseDomain(
                id = "",
                title = emptyTitle,
                description = emptyDescription,
                authorId = "",
                authorName = "",
                visibility = CourseVisibilityDomain.PRIVATE,
                isPublished = false,
                tags = emptyList(),
                modules = emptyList()
            )
            
            dispatch(Msg.UpdateCourse(emptyCourse))
            dispatch(Msg.SetEditMode(true)) // Новый курс сразу открывается в режиме редактирования
        }

        private fun prepareEditMode() {
            val course = state().course
            if (course != null) {
                dispatch(Msg.UpdateEditFields(
                    title = course.title.content,
                    description = course.description.content,
                    visibility = course.visibility,
                    tags = course.tags,
                    isPublished = course.isPublished
                ))
            }
            dispatch(Msg.SetEditMode(true))
            dispatch(Msg.SetDirty(false))
        }

        private fun saveCourse() {
            val state = state()
            val courseId = state.courseId
            
            val updatedCourse = state.course?.copy(
                title = LocalizedContent(state.editTitle),
                description = LocalizedContent(state.editDescription),
                visibility = state.editVisibility,
                tags = state.editTags,
                isPublished = state.editIsPublished
            ) ?: CourseDomain(
                id = "",
                title = LocalizedContent(state.editTitle),
                description = LocalizedContent(state.editDescription),
                authorId = "",
                authorName = "",
                visibility = state.editVisibility,
                isPublished = state.editIsPublished,
                tags = state.editTags,
                modules = emptyList()
            )
            
            dispatch(Msg.SetSaving(true))
            scope.launch {
                try {
                    val result = if (courseId.isNullOrBlank()) {
                        // Создание нового курса
                        courseRepository.createCourse(updatedCourse)
                    } else {
                        // Обновление существующего курса
                        courseRepository.updateCourse(courseId, updatedCourse)
                    }
                    
                    when (result) {
                        is model.DomainResult.Success -> {
                            val savedCourse = result.data
                            dispatch(Msg.UpdateCourse(savedCourse))
                            dispatch(Msg.SetEditMode(false))
                            dispatch(Msg.SetDirty(false))
                            publish(CourseStore.Label.CourseSaved(savedCourse.id))
                        }
                        is model.DomainResult.Error -> {
                            dispatch(Msg.SetError(result.error.message))
                        }
                        is model.DomainResult.Loading -> {
                            // Уже в состоянии сохранения
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Error saving course", e)
                    dispatch(Msg.SetError(e.message ?: "Ошибка при сохранении курса"))
                } finally {
                    dispatch(Msg.SetSaving(false))
                }
            }
        }

        private fun deleteCourse() {
            val courseId = state().courseId ?: return
            
            dispatch(Msg.SetDeleting(true))
            scope.launch {
                try {
                    val result = courseRepository.deleteCourse(courseId)
                    
                    when (result) {
                        is model.DomainResult.Success -> {
                            dispatch(Msg.SetDeleting(false))
                            publish(CourseStore.Label.CourseDeleted)
                        }
                        is model.DomainResult.Error -> {
                            dispatch(Msg.SetError(result.error.message))
                            dispatch(Msg.SetDeleting(false))
                        }
                        is model.DomainResult.Loading -> {
                            // Уже в состоянии удаления
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Error deleting course", e)
                    dispatch(Msg.SetError(e.message ?: "Ошибка при удалении курса"))
                    dispatch(Msg.SetDeleting(false))
                }
            }
        }

        private fun addNewModule() {
            val courseId = state().courseId ?: return
            
            // Создаем пустой модуль
            val emptyTitle = LocalizedContent(mapOf("ru" to "Новый модуль", "en" to "New module"))
            val emptyDescription = LocalizedContent(mapOf("ru" to "", "en" to ""))
            
            val newModule = CourseModuleDomain(
                id = "temp_${System.currentTimeMillis()}",
                courseId = courseId,
                title = emptyTitle,
                description = emptyDescription,
                order = state().modules.size,
                lessons = emptyList()
            )
            
            // Добавляем модуль
            dispatch(Msg.AddModule(newModule))
            
            // Переходим к редактированию модуля
            publish(CourseStore.Label.NavigateToModule(newModule.id))
        }

        private fun deleteModule(moduleId: String) {
            // Поскольку в репозитории нет метода для удаления модуля,
            // просто удаляем его из локального состояния
            dispatch(Msg.RemoveModule(moduleId))
            
            // В реальном приложении здесь должен быть запрос к API
            logger.d("Module $moduleId removed from local state")
        }
    }

    /**
     * Reducer for updating state
     */
    private object ReducerImpl : Reducer<CourseStore.State, Msg> {
        override fun CourseStore.State.reduce(msg: Msg): CourseStore.State = when (msg) {
            is Msg.SetLoading -> copy(isLoading = msg.isLoading)
            is Msg.SetError -> copy(error = msg.error)
            is Msg.UpdateCourse -> copy(course = msg.course)
            is Msg.SetEditMode -> copy(isEditing = msg.isEditing)
            is Msg.SetSaving -> copy(isSaving = msg.isSaving)
            is Msg.SetDeleting -> copy(isDeleting = msg.isDeleting)
            is Msg.SetDirty -> copy(isDirty = msg.isDirty)
            is Msg.SetShowDiscardDialog -> copy(showDiscardDialog = msg.show)
            is Msg.UpdateEditFields -> copy(
                editTitle = msg.title ?: editTitle,
                editDescription = msg.description ?: editDescription,
                editVisibility = msg.visibility ?: editVisibility,
                editTags = msg.tags ?: editTags,
                editIsPublished = msg.isPublished ?: editIsPublished
            )
            is Msg.UpdateModules -> copy(modules = msg.modules)
            is Msg.AddModule -> copy(modules = modules + msg.module)
            is Msg.RemoveModule -> copy(modules = modules.filter { it.id != msg.moduleId })
        }
    }
}

