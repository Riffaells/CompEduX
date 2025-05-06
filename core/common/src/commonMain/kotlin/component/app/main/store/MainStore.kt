package component.app.main.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import model.DomainResult
import model.course.CourseListDomain
import model.course.CourseQueryParams
import model.course.LocalizedContent
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.course.CourseUseCases

interface MainStore : Store<MainStore.Intent, MainStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object OpenSettings : Intent
        data class UpdateTitle(val title: String) : Intent
        data class OpenCourse(val courseId: String) : Intent
        data object RefreshCourses : Intent
    }

    @Serializable
    data class State(
        val title: String = "Main Screen",
        val loading: Boolean = false,
        val allCourses: List<CourseState> = emptyList(),
        val userCourses: List<CourseState> = emptyList(),
        val recommendedCourses: List<CourseState> = emptyList(),
        val error: String? = null
    )

    @Serializable
    data class CourseState(
        val id: String,
        val title: String,
        val description: String,
        val authorName: String,
        val status: String,
        val tags: List<String> = emptyList(),
        val modulesCount: Int = 0
    )
}

internal class MainStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {
    private val logger: Logger by instance()
    private val courseUseCases: CourseUseCases by instance()

    fun create(): MainStore =
        object : MainStore, Store<MainStore.Intent, MainStore.State, Nothing> by storeFactory.create(
            name = "MainStore",
            initialState = MainStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data class DataLoaded(
            val allCourses: List<MainStore.CourseState>,
            val userCourses: List<MainStore.CourseState>,
            val recommendedCourses: List<MainStore.CourseState>
        ) : Msg

        data class ErrorLoading(val error: String) : Msg
        data class UpdateTitle(val title: String) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<MainStore.Intent, Unit, MainStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                loadCourses()
            } catch (e: Exception) {
                logger.e("Error in executeAction: ${e.message}", e, "MainStore")
                safeDispatch(Msg.ErrorLoading("Ошибка при загрузке курсов: ${e.message}"))
            }
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                logger.e("Error in dispatch: ${e.message}", e, "MainStore")
            }
        }

        // Получение локализованного текста
        private fun getLocalizedText(content: LocalizedContent): String {
            // По умолчанию пытаемся получить русский текст, затем английский, 
            // затем первый доступный или пустую строку
            return content.content["ru"]
                ?: content.content["en"]
                ?: content.content.values.firstOrNull()
                ?: ""
        }

        // Загрузка курсов
        private fun loadCourses() {
            safeDispatch(Msg.LoadingData)

            scope.launch {
                try {
                    // Загружаем все курсы одним запросом с увеличенным размером страницы
                    // для оптимизации сетевого взаимодействия
                    val allCoursesResult = courseUseCases.getCourses(CourseQueryParams(page = 0, size = 30))

                    if (allCoursesResult is DomainResult.Success) {
                        val allCourseStates = mapCoursesToState(allCoursesResult.data)

                        // Фильтруем полученные данные для разных категорий
                        val userCourseStates = allCourseStates.filter {
                            it.authorName.contains("Автор", ignoreCase = true)
                        }

                        val recommendedCourseStates = allCourseStates
                            .filter { course -> course.status == "PUBLISHED" }
                            .take(10) // Ограничиваем количество рекомендуемых курсов

                        safeDispatch(
                            Msg.DataLoaded(
                                allCourses = allCourseStates,
                                userCourses = userCourseStates,
                                recommendedCourses = recommendedCourseStates
                            )
                        )
                    } else if (allCoursesResult is DomainResult.Error) {
                        safeDispatch(Msg.ErrorLoading(allCoursesResult.error.message))
                    } else {
                        safeDispatch(Msg.ErrorLoading("Неизвестная ошибка при загрузке курсов"))
                    }
                } catch (e: Exception) {
                    logger.e("Error loading courses: ${e.message}", e, "MainStore")
                    safeDispatch(Msg.ErrorLoading("Ошибка при загрузке курсов: ${e.message}"))
                }
            }
        }

        // Мапинг доменных данных в состояние Store
        private fun mapCoursesToState(courseList: CourseListDomain): List<MainStore.CourseState> {
            return courseList.items.map { course ->
                MainStore.CourseState(
                    id = course.id,
                    title = getLocalizedText(course.title),
                    description = getLocalizedText(course.description),
                    authorName = course.authorName,
                    status = course.status.name,
                    tags = course.tags,
                    modulesCount = course.modules.size
                )
            }
        }

        override fun executeIntent(intent: MainStore.Intent): Unit =
            try {
                when (intent) {
                    is MainStore.Intent.Init -> {
                        loadCourses()
                    }

                    is MainStore.Intent.RefreshCourses -> {
                        loadCourses()
                    }

                    is MainStore.Intent.UpdateTitle -> {
                        // Выполняем асинхронные операции
                        scope.launch {
                            try {
                                // Асинхронные операции...

                                // Обновление UI
                                safeDispatch(Msg.UpdateTitle(intent.title))
                            } catch (e: Exception) {
                                logger.e("Error updating title: ${e.message}", e, "MainStore")
                            }
                        }
                        Unit
                    }

                    is MainStore.Intent.OpenSettings -> {
                        // Обработка в компоненте
                    }

                    is MainStore.Intent.OpenCourse -> {
                        // Обработка в компоненте
                    }
                }
            } catch (e: Exception) {
                logger.e("Error in executeIntent: ${e.message}", e, "MainStore")
            }
    }

    private object ReducerImpl : Reducer<MainStore.State, Msg> {
        override fun MainStore.State.reduce(msg: Msg): MainStore.State =
            when (msg) {
                is Msg.DataLoaded -> copy(
                    loading = false,
                    allCourses = msg.allCourses,
                    userCourses = msg.userCourses,
                    recommendedCourses = msg.recommendedCourses,
                    error = null
                )

                is Msg.LoadingData -> copy(loading = true, error = null)
                is Msg.UpdateTitle -> copy(title = msg.title)
                is Msg.ErrorLoading -> copy(loading = false, error = msg.error)
            }
    }
}
