package components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import components.list.CourseListComponent
import components.list.CourseListComponentParams
import components.list.DefaultCourseListComponent
import components.view.CourseViewComponent
import components.view.CourseViewComponentParams
import components.view.DefaultCourseViewComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import navigation.NavigationExecutor
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.factory
import org.kodein.di.instance

/**
 * Параметры для создания компонента курсов
 */
data class CourseComponentParams(
    val componentContext: ComponentContext,
    val onBackClicked: () -> Unit
)

/**
 * Компонент для работы с курсами
 */
interface CourseComponent {
    /**
     * Состояние компонента
     */
    val state: StateFlow<CourseStore.State>

    /**
     * Стек дочерних компонентов для навигации
     */
    val childStack: Value<ChildStack<*, Child>>

    /**
     * Дочерние компоненты
     */
    sealed class Child {
        class CourseListChild(val component: CourseListComponent) : Child()
        class CourseViewChild(val component: CourseViewComponent) : Child()
        class CourseCreateChild(val component: CourseViewComponent) : Child()
    }
    
    /**
     * Перейти к списку курсов
     */
    fun navigateToCourseList()
    
    /**
     * Перейти к просмотру курса
     */
    fun navigateToCourseView(courseId: String)
    
    /**
     * Перейти к созданию курса
     */
    fun navigateToCreateCourse()

    /**
     * Вернуться назад
     */
    fun onBackClicked()
}

/**
 * Реализация компонента курсов
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultCourseComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onBack: () -> Unit
) : CourseComponent, DIAware, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    private val scope = coroutineScope(rDispatchers.main)

    private val logger by instance<Logger>()

    private val navigationExecutor = NavigationExecutor(
        navigation = navigation,
        scope = scope,
        mainDispatcher = rDispatchers.main,
        logger = logger.withTag("Course NavigationExecutor")
    )

    private val _store by instance<CourseStore>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _childStack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialStack = {
            listOf(Config.CourseList)
        },
        handleBackButton = true,
        childFactory = ::child
    )

    init {
        scope.launch {
            // Подписываемся на события из дочерних компонентов
            // Например, можно обрабатывать навигационные события
        }
    }

    override val childStack: Value<ChildStack<*, CourseComponent.Child>> = _childStack

    override val state: StateFlow<CourseStore.State> = _store.stateFlow

    override fun navigateToCourseList() {
        navigationExecutor.navigateTo(Config.CourseList)
    }

    override fun navigateToCourseView(courseId: String) {
        navigationExecutor.navigateTo(Config.CourseView(courseId))
    }
    
    override fun navigateToCreateCourse() {
        logger.i("Navigating to course creation screen")
        navigationExecutor.navigateTo(Config.CourseCreate)
    }

    override fun onBackClicked() {
        // Если в стеке только один экран, выходим из фичи
        if (_childStack.items.size <= 1) {
            onBack()
        } else {
            // Иначе возвращаемся на предыдущий экран
            navigation.pop()
        }
    }

    private fun child(config: Config, componentContext: ComponentContext): CourseComponent.Child =
        when (config) {
            Config.CourseList -> CourseComponent.Child.CourseListChild(courseListComponent(componentContext))
            is Config.CourseView -> CourseComponent.Child.CourseViewChild(courseViewComponent(componentContext, config.courseId))
            Config.CourseCreate -> CourseComponent.Child.CourseCreateChild(courseCreateComponent(componentContext))
        }

    private fun courseListComponent(componentContext: ComponentContext): CourseListComponent {
        val courseListComponentFactory by factory<CourseListComponentParams, CourseListComponent>()
        return courseListComponentFactory(
            CourseListComponentParams(
                componentContext = componentContext,
                onBack = ::onBackClicked,
                onCourseSelected = ::navigateToCourseView
            )
        ).also { component ->
            // Подписываемся на события из компонента списка курсов
            scope.launch {
                // Здесь можно обрабатывать события, если они будут добавлены в CourseListComponent
            }
        }
    }

    private fun courseViewComponent(componentContext: ComponentContext, courseId: String): CourseViewComponent {
        val courseViewComponentFactory by factory<CourseViewComponentParams, CourseViewComponent>()
        return courseViewComponentFactory(
            CourseViewComponentParams(
                componentContext = componentContext,
                onBack = ::onBackClicked,
                courseId = courseId,
                onCourseUpdated = { updatedCourseId ->
                    logger.i("Course updated: $updatedCourseId")
                    // Можно выполнить дополнительные действия после обновления курса
                }
            )
        )
    }
    
    private fun courseCreateComponent(componentContext: ComponentContext): CourseViewComponent {
        val courseViewComponentFactory by factory<CourseViewComponentParams, CourseViewComponent>()
        return courseViewComponentFactory(
            CourseViewComponentParams(
                componentContext = componentContext,
                onBack = ::onBackClicked,
                isCreateMode = true,
                onCourseCreated = { courseId ->
                    logger.i("Course created: $courseId")
                    // После создания курса переходим к его просмотру
                    navigateToCourseView(courseId)
                }
            )
        )
    }

    @Serializable
    private sealed class Config {
        @Serializable
        data object CourseList : Config()

        @Serializable
        data class CourseView(val courseId: String) : Config()
        
        @Serializable
        data object CourseCreate : Config()
    }
}
