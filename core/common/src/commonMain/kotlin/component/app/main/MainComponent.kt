package component.app.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.main.store.MainStore
import component.app.main.store.MainStoreFactory
import component.root.store.RootStore
import components.CourseComponent
import components.DefaultCourseComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import logging.Logger
import navigation.NavigationExecutor
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

/**
 * Параметры для создания компонента главного экрана
 */
data class MainComponentParams(
    val componentContext: ComponentContext,
    val onSettingsClicked: () -> Unit,
    val onDevelopmentMapClicked: () -> Unit,
    val onTreeClicked: () -> Unit,
    val onRoomClicked: (String) -> Unit
)

/**
 * Конфигурации для навигационного стека MainComponent
 */
@Serializable
sealed interface MainConfig {
    @Serializable
    data object Home : MainConfig

    @Serializable
    data object Course : MainConfig
}

/**
 * Компонент главного экрана приложения
 */
interface MainComponent {
    /**
     * Стек дочерних компонентов
     */
    val childStack: Value<ChildStack<*, Child>>

    /**
     * Состояние главного компонента
     */
    val state: StateFlow<MainStore.State>

    /**
     * Обработка действий
     */
    fun onAction(action: MainStore.Intent)

    /**
     * Переход к настройкам
     */
    fun onSettingsClicked()

    /**
     * Переход к карте разработки
     */
    fun onDevelopmentMapClicked()

    /**
     * Переход к дереву технологий
     */
    fun onTreeClicked()

    /**
     * Переход к комнате
     */
    fun onRoomClicked(roomId: String)

    /**
     * Переход к курсу
     */
    fun navigateToCourse(courseId: String?)

    /**
     * Возврат к предыдущему экрану
     */
    fun navigateBack()

    /**
     * Дочерние компоненты MainComponent
     */
    sealed class Child {
        /**
         * Главная страница с списком курсов
         */
        class HomeChild(val component: HomeComponent) : Child()

        /**
         * Детальная страница курса
         */
        class CourseChild(val component: CourseComponent) : Child()
    }
}

/**
 * Компонент домашней страницы (список курсов и т.д.)
 */
interface HomeComponent {
    val state: StateFlow<MainStore.State>

    fun onAction(action: MainStore.Intent)
    fun onSettingsClicked()
    fun onDevelopmentMapClicked()
    fun onTreeClicked()
    fun onRoomClicked(roomId: String)
    fun onCourseClicked(courseId: String)
}

/**
 * Реализация компонента домашней страницы
 */
class DefaultHomeComponent(
    componentContext: ComponentContext,
    private val onSettings: () -> Unit,
    private val onDevelopmentMap: () -> Unit,
    private val onTree: () -> Unit,
    private val onRoom: (String) -> Unit,
    private val onCourse: (String) -> Unit,
    override val di: DI
) : HomeComponent, DIAware, ComponentContext by componentContext {

    private val mainStoreFactory: MainStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        mainStoreFactory.create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<MainStore.State> = store.stateFlow

    override fun onAction(action: MainStore.Intent) {
        when (action) {
            is MainStore.Intent.OpenSettings -> onSettingsClicked()
            is MainStore.Intent.OpenCourse -> onCourseClicked(action.courseId)
            else -> store.accept(action)
        }
    }

    override fun onSettingsClicked() {
        onSettings.invoke()
    }

    override fun onDevelopmentMapClicked() {
        onDevelopmentMap.invoke()
    }

    override fun onTreeClicked() {
        onTree.invoke()
    }

    override fun onRoomClicked(roomId: String) {
        onRoom.invoke(roomId)
    }

    override fun onCourseClicked(courseId: String) {
        onCourse.invoke(courseId)
    }
}

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val onSettings: () -> Unit,
    private val onDevelopmentMap: () -> Unit,
    private val onTree: () -> Unit,
    private val onRoom: (String) -> Unit,
    override val di: DI
) : MainComponent, DIAware, ComponentContext by componentContext {

    /**
     * Навигационный стек
     */
    private val navigation = StackNavigation<MainConfig>()

    /**
     * Корутин-скоуп, привязанный к жизненному циклу компонента
     */
    private val scope = coroutineScope(rDispatchers.main)

    private val logger by instance<Logger>()

    /**
     * Исполнитель навигационных команд
     */
    private val navigationExecutor = NavigationExecutor(
        navigation = navigation,
        scope = scope,
        mainDispatcher = rDispatchers.main,
        logger = logger.withTag("Main NavigationExecutor")
    )

    /**
     * Стек дочерних компонентов
     */
    override val childStack = childStack(
        source = navigation,
        serializer = MainConfig.serializer(),
        initialStack = { listOf(MainConfig.Course) },
        handleBackButton = true,
        childFactory = ::child
    )

    /**
     * Инициализация
     */
    init {
        // Загружаем список курсов при создании компонента
        lifecycle.doOnCreate {
            scope.launch {
                val homeChild = childStack.value.active.instance as? MainComponent.Child.HomeChild
                homeChild?.component?.onAction(MainStore.Intent.RefreshCourses)
            }
        }
    }

    /**
     * Создание компонента на основе конфигурации
     */
    private fun child(config: MainConfig, componentContext: ComponentContext): MainComponent.Child =
        when (config) {
            MainConfig.Home -> MainComponent.Child.HomeChild(homeComponent(componentContext))
            is MainConfig.Course -> MainComponent.Child.CourseChild(courseComponent(componentContext))
        }

    /**
     * Создание компонента домашней страницы
     */
    private fun homeComponent(componentContext: ComponentContext): HomeComponent {
        return DefaultHomeComponent(
            componentContext = componentContext,
            onSettings = onSettings,
            onDevelopmentMap = onDevelopmentMap,
            onTree = onTree,
            onRoom = onRoom,
            onCourse = ::navigateToCourse,
            di = di
        )
    }

    /**
     * Создание компонента курса
     */
    private fun courseComponent(componentContext: ComponentContext): CourseComponent {
        return DefaultCourseComponent(
            componentContext = componentContext,
            onBack = ::navigateBack,
            di = di
        )
    }

    private val mainStoreFactory: MainStoreFactory by instance()

    private val store =
        instanceKeeper.getStore {
            mainStoreFactory.create()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<MainStore.State> = store.stateFlow

    override fun onAction(action: MainStore.Intent) {
        when (val child = childStack.value.active.instance) {
            is MainComponent.Child.HomeChild -> child.component.onAction(action)
            is MainComponent.Child.CourseChild -> {
                // Возможно, понадобится преобразование action для CourseComponent
            }
        }
    }

    override fun onSettingsClicked() {
        onSettings.invoke()
    }

    override fun onDevelopmentMapClicked() {
        onDevelopmentMap.invoke()
    }

    override fun onTreeClicked() {
        onTree.invoke()
    }

    override fun onRoomClicked(roomId: String) {
        onRoom.invoke(roomId)
    }

    override fun navigateToCourse(courseId: String?) {
        navigationExecutor.navigateTo(MainConfig.Course)
    }

    override fun navigateBack() {
        navigationExecutor.pop()
    }
}
