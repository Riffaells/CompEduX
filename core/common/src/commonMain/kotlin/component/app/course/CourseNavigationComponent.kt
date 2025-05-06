package component.app.course

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import component.app.course.course.CourseComponent
import component.app.course.list.CoursesListComponent
import kotlinx.serialization.Serializable
import org.kodein.di.DI

/**
 * Component to handle course navigation flow
 */
interface CourseNavigationComponent {

    /**
     * Child stack
     */
    val childStack: Value<ChildStack<*, Child>>

    /**
     * Go back (to previous screen or exit)
     */
    fun onBack()

    /**
     * Child (configuration + instance)
     */
    sealed class Child {
        /**
         * List of courses
         */
        data class CoursesList(val component: CoursesListComponent) : Child()

        /**
         * Course detail or view
         */
        data class CourseContent(val component: CourseComponent) : Child()

        /**
         * Module detail
         */
        data class ModuleDetail(val component: Any /* ModuleViewComponent */) : Child()

        /**
         * New module creation
         */
        data class ModuleCreation(val component: Any /* ModuleCreationComponent */) : Child()
    }

    companion object {
        /**
         * Create component
         */
        fun create(
            componentContext: ComponentContext,
            di: DI,
            storeFactory: StoreFactory,
            exitFromFlow: () -> Unit
        ): CourseNavigationComponent =
            DefaultCourseNavigationComponent(
                componentContext = componentContext,
                di = di,
                storeFactory = storeFactory,
                exitFromFlow = exitFromFlow
            )
    }
}

/**
 * Default implementation for course navigation
 */
@OptIn(DelicateDecomposeApi::class)
private class DefaultCourseNavigationComponent(
    componentContext: ComponentContext,
    private val di: DI,
    private val storeFactory: StoreFactory,
    private val exitFromFlow: () -> Unit
) : CourseNavigationComponent, ComponentContext by componentContext {

    /**
     * Navigation configurations
     */
    @Serializable
    private sealed class Config {
        /**
         * Course list
         */
        @Serializable
        data object CoursesList : Config()

        /**
         * Course detail or view
         */
        @Serializable
        data class CourseContent(val courseId: String?) : Config()

        /**
         * Module detail
         */
        @Serializable
        data class ModuleDetail(val moduleId: String) : Config()

        /**
         * Module creation
         */
        @Serializable
        data class ModuleCreation(val courseId: String) : Config()
    }

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, CourseNavigationComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.CoursesList,
            handleBackButton = true,
            childFactory = ::createChild,
        )

    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): CourseNavigationComponent.Child =
        when (config) {
            is Config.CoursesList -> CourseNavigationComponent.Child.CoursesList(
                component = CoursesListComponent.create(
                    componentContext = componentContext,
                    di = di,
                    storeFactory = storeFactory,
                    onBack = ::handleBack,
                    onCourseSelected = ::onCourseSelected
                )
            )
            is Config.CourseContent -> CourseNavigationComponent.Child.CourseContent(
                component = createCourseComponent(
                    componentContext = componentContext,
                    courseId = config.courseId
                )
            )
            is Config.ModuleDetail -> CourseNavigationComponent.Child.ModuleDetail(
                component = createModuleDetailComponent(
                    componentContext = componentContext,
                    moduleId = config.moduleId
                )
            )
            is Config.ModuleCreation -> CourseNavigationComponent.Child.ModuleCreation(
                component = createModuleCreationComponent(
                    componentContext = componentContext,
                    courseId = config.courseId
                )
            )
        }

    /**
     * Create unified course component for both view and edit modes
     */
    private fun createCourseComponent(
        componentContext: ComponentContext,
        courseId: String?
    ): CourseComponent {
        return component.app.course.course.DefaultCourseComponent(
            componentContext = componentContext,
            courseId = courseId,
            onBack = ::handleBack,
            di = di
        )
    }

    /**
     * Temporary stub for module detail component
     */
    private fun createModuleDetailComponent(
        componentContext: ComponentContext,
        moduleId: String
    ): Any {
        return object {}
    }

    /**
     * Temporary stub for module creation component
     */
    private fun createModuleCreationComponent(
        componentContext: ComponentContext,
        courseId: String
    ): Any {
        return object {}
    }

    // Метод для внутренней обработки навигации назад
    private fun handleBack() {
        try {
            navigation.pop()
        } catch (e: Exception) {
            // Если стек пуст, выходим из потока
            exitFromFlow()
        }
    }

    // Имплементация публичного интерфейса
    override fun onBack() {
        handleBack()
    }

    private fun onCourseSelected(courseId: String) {
        navigation.pushToFront(Config.CourseContent(courseId))
    }

    private fun onModuleSelected(moduleId: String) {
        navigation.pushToFront(Config.ModuleDetail(moduleId))
    }

    private fun onAddModule(courseId: String) {
        navigation.pushToFront(Config.ModuleCreation(courseId))
    }
} 