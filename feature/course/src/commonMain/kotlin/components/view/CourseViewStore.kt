package components.view

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import logging.Logger
import model.DomainResult
import model.course.CourseDomain
import model.course.CourseStatusDomain
import model.course.LocalizedContent
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.course.CourseUseCases

interface CourseViewStore : Store<CourseViewStore.Intent, CourseViewStore.State, CourseViewStore.Label> {

    /**
     * Intent - действия, которые могут быть выполнены в этом Store
     */
    sealed interface Intent {
        data class LoadCourse(val courseId: String) : Intent
        data class CreateCourse(val course: CourseDomain) : Intent
        data class UpdateCourse(val courseId: String, val course: CourseDomain) : Intent
        data object SwitchToEditMode : Intent
        data object SwitchToViewMode : Intent
        data object ResetError : Intent
        data object NavigateBack : Intent
    }

    /**
     * State - состояние экрана просмотра курса
     */
    data class State(
        val isLoading: Boolean = false,
        val course: CourseDomain? = null,
        val error: String? = null,
        val isEditMode: Boolean = false,
        val isCreateMode: Boolean = false,
        val isSaved: Boolean = false
    )

    sealed interface Label {
        data object NavigateBack : Label
        data class CourseCreated(val courseId: String) : Label
        data class CourseUpdated(val courseId: String) : Label
    }
}

/**
 * Фабрика для создания CourseViewStore
 */
class CourseViewStoreFactory {
    companion object {
        fun create(storeFactory: StoreFactory, di: DI, isCreateMode: Boolean = false): CourseViewStore =
            object : CourseViewStore,
                Store<CourseViewStore.Intent, CourseViewStore.State, CourseViewStore.Label> by storeFactory.create(
                    name = "CourseViewStore",
                    initialState = CourseViewStore.State(isCreateMode = isCreateMode, isEditMode = isCreateMode),
                    bootstrapper = SimpleBootstrapper(Unit),
                    executorFactory = { ExecutorImpl(di) },
                    reducer = ReducerImpl
                ) {}
    }

    // Приватные сообщения для редуктора
    private sealed interface Msg {
        data object Loading : Msg
        data class CourseLoaded(val course: CourseDomain) : Msg
        data class ErrorOccurred(val error: String) : Msg
        data object SwitchToEditMode : Msg
        data object SwitchToViewMode : Msg
        data object ResetError : Msg
        data class CourseSaved(val courseId: String) : Msg
    }

    private class ExecutorImpl(override val di: DI) :
        CoroutineExecutor<CourseViewStore.Intent, Unit, CourseViewStore.State, Msg, CourseViewStore.Label>(rDispatchers.main),
        DIAware {
        val logger: Logger by instance()
        val courseUseCases: CourseUseCases by instance()

        override fun executeAction(action: Unit) {
            // Инициализация, если требуется
        }

        override fun executeIntent(intent: CourseViewStore.Intent) {
            when (intent) {
                is CourseViewStore.Intent.LoadCourse -> loadCourse(intent.courseId)
                is CourseViewStore.Intent.CreateCourse -> createCourse(intent.course)
                is CourseViewStore.Intent.UpdateCourse -> updateCourse(intent.courseId, intent.course)
                is CourseViewStore.Intent.SwitchToEditMode -> dispatch(Msg.SwitchToEditMode)
                is CourseViewStore.Intent.SwitchToViewMode -> dispatch(Msg.SwitchToViewMode)
                is CourseViewStore.Intent.ResetError -> dispatch(Msg.ResetError)
                is CourseViewStore.Intent.NavigateBack -> publish(CourseViewStore.Label.NavigateBack)
            }
        }

        private fun loadCourse(courseId: String) {
            if (courseId.isBlank()) {
                dispatch(Msg.ErrorOccurred("Идентификатор курса не указан"))
                return
            }

            dispatch(Msg.Loading)

            scope.launch {
                try {
                    when (val result = courseUseCases.getCourse(courseId)) {
                        is DomainResult.Success -> {
                            logger.i("Course loaded successfully: ${result.data.id}")
                            dispatch(Msg.CourseLoaded(result.data))
                        }

                        is DomainResult.Error -> {
                            logger.e("Failed to load course: ${result.error}")
                            dispatch(Msg.ErrorOccurred(result.error.message ?: "Неизвестная ошибка при загрузке курса"))
                        }

                        is DomainResult.Loading -> {
                            // Already in loading state
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Exception when loading course: ${e.message}")
                    dispatch(Msg.ErrorOccurred(e.message ?: "Неизвестная ошибка при загрузке курса"))
                }
            }
        }

        private fun createCourse(course: CourseDomain) {
            if (!validateCourse(course)) {
                return
            }

            dispatch(Msg.Loading)

            scope.launch {
                try {
                    // Используем правильную сигнатуру метода из CreateCourseUseCase
                    when (val result = courseUseCases.createCourse(
                        title = course.title,
                        description = course.description
                    )) {
                        is DomainResult.Success -> {
                            val courseId = result.data.id
                            logger.i("Course created successfully: $courseId")
                            dispatch(Msg.CourseSaved(courseId))
                            publish(CourseViewStore.Label.CourseCreated(courseId))
                        }

                        is DomainResult.Error -> {
                            logger.e("Failed to create course: ${result.error}")
                            dispatch(Msg.ErrorOccurred(result.error.message ?: "Неизвестная ошибка при создании курса"))
                        }

                        is DomainResult.Loading -> {
                            // Already in loading state
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Exception when creating course: ${e.message}")
                    dispatch(Msg.ErrorOccurred(e.message ?: "Неизвестная ошибка при создании курса"))
                }
            }
        }

        private fun updateCourse(courseId: String, course: CourseDomain) {
            if (courseId.isBlank()) {
                dispatch(Msg.ErrorOccurred("Идентификатор курса не указан для обновления"))
                return
            }

            if (!validateCourse(course)) {
                return
            }

            dispatch(Msg.Loading)

            scope.launch {
                try {
                    // Убедимся, что мы передаем правильный объект CourseDomain
                    // Используем текущий курс как основу и обновляем его поля
                    val currentState = state()
                    val currentCourse = currentState.course
                    
                    if (currentCourse == null) {
                        dispatch(Msg.ErrorOccurred("Не найден текущий курс для обновления"))
                        return@launch
                    }
                    
                    // Создаем обновленный объект курса, сохраняя id и другие поля
                    val updatedCourse = currentCourse.copy(
                        title = course.title,
                        description = course.description,
                        imageUrl = course.imageUrl,
                        tags = course.tags,
                        visibility = course.visibility,
                        status = course.status
                    )

                    when (val result = courseUseCases.updateCourse(courseId, updatedCourse)) {
                        is DomainResult.Success -> {
                            logger.i("Course updated successfully: $courseId")
                            dispatch(Msg.CourseSaved(courseId))
                            dispatch(Msg.CourseLoaded(result.data))
                            dispatch(Msg.SwitchToViewMode)
                            publish(CourseViewStore.Label.CourseUpdated(courseId))
                        }

                        is DomainResult.Error -> {
                            logger.e("Failed to update course: ${result.error}")
                            dispatch(
                                Msg.ErrorOccurred(
                                    result.error.message ?: "Неизвестная ошибка при обновлении курса"
                                )
                            )
                        }

                        is DomainResult.Loading -> {
                            // Already in loading state
                        }
                    }
                } catch (e: Exception) {
                    logger.e("Exception when updating course: ${e.message}")
                    dispatch(Msg.ErrorOccurred(e.message ?: "Неизвестная ошибка при обновлении курса"))
                }
            }
        }

        /**
         * Проверка валидности данных курса
         */
        private fun validateCourse(course: CourseDomain): Boolean {
            val ruTitle = course.title.content["ru"]
            val ruDescription = course.description.content["ru"]

            if (ruTitle.isNullOrBlank()) {
                dispatch(Msg.ErrorOccurred("Название курса на русском языке обязательно"))
                return false
            }

            if (ruDescription.isNullOrBlank()) {
                dispatch(Msg.ErrorOccurred("Описание курса на русском языке обязательно"))
                return false
            }

            return true
        }
    }

    private object ReducerImpl : Reducer<CourseViewStore.State, Msg> {
        override fun CourseViewStore.State.reduce(msg: Msg): CourseViewStore.State =
            when (msg) {
                is Msg.Loading -> copy(isLoading = true, error = null, isSaved = false)
                is Msg.CourseLoaded -> copy(isLoading = false, course = msg.course, error = null)
                is Msg.ErrorOccurred -> copy(isLoading = false, error = msg.error)
                is Msg.SwitchToEditMode -> copy(isEditMode = true)
                is Msg.SwitchToViewMode -> copy(isEditMode = false)
                is Msg.ResetError -> copy(error = null)
                is Msg.CourseSaved -> copy(isLoading = false, isSaved = true)
            }
    }
}
