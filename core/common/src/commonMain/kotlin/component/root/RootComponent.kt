package component.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.webhistory.WebHistoryController
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.AuthComponentParams
import component.app.auth.DefaultAuthComponent
import component.app.main.DefaultMainComponent
import component.app.main.MainComponentParams
import component.app.room.DefaultRoomComponent
import component.app.room.RoomComponentParams
import component.app.settings.DefaultSettingsComponent
import component.DefaultTechnologyTreeComponent
import component.TechnologyTreeComponentParams
import component.root.RootComponent.Child.*
import component.root.store.RootStore
import component.root.store.RootStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import logging.Logger
import navigation.NavigationExecutor
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.factory
import org.kodein.di.instance
import settings.AppearanceSettings
import settings.MultiplatformSettings


/**
 * Параметры для создания корневого компонента
 *
 * @property componentContext Контекст компонента Decompose
 * @property webHistoryController Контроллер веб-истории для управления навигацией в веб-версии
 * @property deepLink Глубокая ссылка для начальной навигации
 */
@OptIn(ExperimentalDecomposeApi::class)
data class RootComponentParams(
    val componentContext: ComponentContext,
    val webHistoryController: WebHistoryController? = null,
    val deepLink: DefaultRootComponent.DeepLink? = null
)


/**
 * Корневой компонент приложения, отвечающий за навигацию между основными экранами
 * и управление общим состоянием приложения.
 *
 * Реализует архитектуру на основе Decompose для организации дерева компонентов
 * и MVI для управления состоянием.
 */
interface RootComponent {
    /**
     * Стек дочерних компонентов, отображаемых в приложении
     */
    val childStack: Value<ChildStack<*, Child>>

    /**
     * Текущее состояние корневого компонента
     */
    val state: StateFlow<RootStore.State>

    /**
     * Глобальные настройки приложения
     */
    val settings: MultiplatformSettings

    /**
     * Обрабатывает события, направленные в RootStore
     *
     * @param event Событие для обработки
     */
    fun onEvent(event: RootStore.Intent)

    /**
     * Переход на главный экран
     */
    fun onMainClicked()

    /**
     * Устанавливает обработчик открытия/закрытия бокового меню
     *
     * @param handler Функция, вызываемая для переключения состояния меню
     */
    fun setDrawerHandler(handler: () -> Unit)

    /**
     * Переход на экран настроек
     */
    fun onSettingsClicked()

    /**
     * Переход на экран карты разработки
     */
    fun onDevelopmentMapClicked()

    /**
     * Переход на экран аутентификации
     */
    fun onAuthClicked()

    /**
     * Переход на экран комнаты
     */
    fun onRoomClicked()

    /**
     * Переход на экран дерева развития
     */
    fun onTreeClicked()

    /**
     * Представляет различные типы дочерних компонентов,
     * которые могут быть активны в приложении
     */
    sealed class Child {
        /**
         * Главный экран приложения
         */
        class MainChild(val component: DefaultMainComponent) : Child()

        /**
         * Экран настроек
         */
        class SettingsChild(val component: DefaultSettingsComponent) : Child()

        /**
         * Экран с Skiko для графики
         */
        class SkikoChild(val component: DefaultTechnologyTreeComponent) : Child()

        /**
         * Экран аутентификации
         */
        class AuthChild(val component: DefaultAuthComponent) : Child()

        /**
         * Экран комнаты (чата/конференции)
         */
        class RoomChild(val component: DefaultRoomComponent) : Child()

        /**
         * Экран дерева развития
         */
//        class TreeChild(val component: DefaultTreeComponent) : Child()
    }

    /**
     * Настройки внешнего вида приложения
     */
    val appearanceSettings: AppearanceSettings
}

/**
 * Реализация корневого компонента приложения
 *
 * @param componentContext Контекст компонента Decompose
 * @param deepLink Глубокая ссылка для начальной навигации
 * @param webHistoryController Контроллер веб-истории для управления навигацией в веб-версии
 * @param storeFactory Фабрика для создания MVI Store
 * @param di Экземпляр Kodein DI для внедрения зависимостей
 */
@OptIn(ExperimentalDecomposeApi::class)
class DefaultRootComponent(
    componentContext: ComponentContext,
    deepLink: DeepLink = DeepLink.None,
    webHistoryController: WebHistoryController? = null,
    storeFactory: StoreFactory,
    override val di: DI
) : RootComponent, DIAware, ComponentContext by componentContext {

    // Создаем scope, связанный с жизненным циклом компонента
    // Использование coroutineScope из Essenty гарантирует автоматическую отмену
    // корутин при уничтожении компонента
    private val scope = coroutineScope(rDispatchers.main)

    // Навигационный стек для управления переходами между экранами
    private val navigation = StackNavigation<Config>()

    private val logger by instance<Logger>()

    // Создаем навигационный executor, используя scope, связанный с жизненным циклом
    private val navigationExecutor = NavigationExecutor(
        navigation = navigation,
        scope = scope,
        mainDispatcher = rDispatchers.main,
        logger = logger
    )

    private var drawerHandler: (() -> Unit)? = null

    /**
     * Устанавливает обработчик для открытия/закрытия бокового меню
     *
     * @param handler Функция, вызываемая для переключения состояния меню
     */
    override fun setDrawerHandler(handler: () -> Unit) {
        drawerHandler = handler
    }

    /**
     * Глобальные настройки приложения
     */
    override val settings by instance<MultiplatformSettings>()

    /**
     * Настройки внешнего вида приложения
     */
    override val appearanceSettings by instance<AppearanceSettings>()

    /**
     * Стек дочерних компонентов, управляемый через Decompose
     */
    private val stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialStack = {
            getInitialStack(
                webHistoryPaths = webHistoryController?.historyPaths,
                deepLink = deepLink
            )
        },
        handleBackButton = true,
        childFactory = ::child,
    )

    override val childStack: Value<ChildStack<*, RootComponent.Child>> = stack

    private val rootStoreFactory: RootStoreFactory by instance()

    /**
     * Store для управления состоянием корневого компонента
     */
    private val store =
        instanceKeeper.getStore {
            rootStoreFactory.create()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<RootStore.State> = store.stateFlow

    init {
        // Присоединяем контроллер веб-истории к навигации
        webHistoryController?.attach(
            navigator = navigation,
            stack = stack,
            getPath = ::getPathForConfig,
            serializer = Config.serializer(),
            getConfiguration = ::getConfigForPath,
        )
    }

    /**
     * Обрабатывает события, направленные в RootStore
     *
     * @param event Событие для обработки
     */
    override fun onEvent(event: RootStore.Intent) {
        store.accept(event)
    }

    /**
     * Создает дочерний компонент на основе конфигурации
     *
     * @param config Конфигурация, определяющая тип компонента
     * @param componentContext Контекст для создаваемого компонента
     * @return Дочерний компонент
     */
    private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            Config.Main -> MainChild(mainComponent(componentContext))
            Config.Settings -> SettingsChild(settingsComponent(componentContext))
            Config.Skiko -> SkikoChild(skikoComponent(componentContext))
            Config.Auth -> AuthChild(authComponent(componentContext))
            Config.Room -> RoomChild(roomComponent(componentContext))
//            Config.Tree -> TreeChild(treeComponent(componentContext))
        }

    /**
     * Создает компонент главного экрана
     *
     * @param componentContext Контекст для создаваемого компонента
     * @return Компонент главного экрана
     */
    private fun mainComponent(componentContext: ComponentContext): DefaultMainComponent {
        val mainComponentFactory by factory<MainComponentParams, DefaultMainComponent>()
        return mainComponentFactory(
            MainComponentParams(
                componentContext = componentContext,
                onSettingsClicked = {
                    store.accept(RootStore.Intent.NavigateToSettings)
                },
                onDevelopmentMapClicked = {
                    store.accept(RootStore.Intent.NavigateToSkiko)
                },
                onTreeClicked = {
                    store.accept(RootStore.Intent.NavigateToTree)
                },
                onRoomClicked = { roomId ->
                    store.accept(RootStore.Intent.NavigateToRoom(roomId))
                }
            )
        )
    }

    /**
     * Создает компонент экрана настроек
     *
     * @param componentContext Контекст для создаваемого компонента
     * @return Компонент экрана настроек
     */
    private fun settingsComponent(componentContext: ComponentContext): DefaultSettingsComponent {
        return DefaultSettingsComponent(
            componentContext = componentContext,
            onBack = { navigationExecutor.pop() },
            di = di
        )
    }

    /**
     * Создает компонент Skiko для графического экрана
     *
     * @param componentContext Контекст для создаваемого компонента
     * @return Компонент Skiko
     */
    private fun skikoComponent(componentContext: ComponentContext): DefaultTechnologyTreeComponent {
        val skikoComponentFactory by factory<TechnologyTreeComponentParams, DefaultTechnologyTreeComponent>()
        return skikoComponentFactory(
            TechnologyTreeComponentParams(
                componentContext = componentContext,
                onBack = { navigationExecutor.pop() }
            )
        )
    }

    /**
     * Создает компонент экрана аутентификации
     *
     * @param componentContext Контекст для создаваемого компонента
     * @return Компонент экрана аутентификации
     */
    private fun authComponent(componentContext: ComponentContext): DefaultAuthComponent {
        val authComponentFactory by factory<AuthComponentParams, DefaultAuthComponent>()
        return authComponentFactory(
            AuthComponentParams(
                componentContext = componentContext,
                onBack = { navigationExecutor.pop() }
            )
        )
    }

    /**
     * Создает компонент экрана комнаты
     *
     * @param componentContext Контекст для создаваемого компонента
     * @return Компонент экрана комнаты
     */
    private fun roomComponent(componentContext: ComponentContext): DefaultRoomComponent {
        val roomComponentFactory by factory<RoomComponentParams, DefaultRoomComponent>()
        return roomComponentFactory(
            RoomComponentParams(
                componentContext = componentContext,
                onBack = { navigationExecutor.pop() }
            )
        )
    }


    /**
     * Переход на главный экран
     */
    override fun onMainClicked() {
        navigationExecutor.navigateTo(Config.Main)
    }

    /**
     * Переход на экран настроек
     */
    override fun onSettingsClicked() {
        navigationExecutor.navigateTo(Config.Settings)
    }

    /**
     * Переход на экран карты разработки
     */
    override fun onDevelopmentMapClicked() {
        navigationExecutor.navigateTo(Config.Skiko)
    }

    /**
     * Переход на экран аутентификации
     */
    override fun onAuthClicked() {
        navigationExecutor.navigateTo(Config.Auth)
    }

    /**
     * Переход на экран комнаты
     */
    override fun onRoomClicked() {
        navigationExecutor.navigateTo(Config.Room)
    }

    /**
     * Переход на экран дерева развития
     */
    override fun onTreeClicked() {
//        navigationExecutor.navigateTo(Config.Tree)
    }

    private companion object {
        private const val WEB_PATH_SETTINGS = "settings"
        private const val WEB_PATH_SKIKO = "skiko"
        private const val WEB_PATH_AUTH = "auth"
        private const val WEB_PATH_ROOM = "room"
        private const val WEB_PATH_TREE = "tree"

        /**
         * Формирует начальный стек компонентов на основе истории веб-навигации или глубокой ссылки
         *
         * @param webHistoryPaths Пути из истории веб-навигации
         * @param deepLink Глубокая ссылка
         * @return Список конфигураций для начального стека
         */
        private fun getInitialStack(webHistoryPaths: List<String>?, deepLink: DeepLink): List<Config> =
            webHistoryPaths
                ?.takeUnless(List<*>::isEmpty)
                ?.map(Companion::getConfigForPath)
                ?: getInitialStack(deepLink)

        /**
         * Формирует начальный стек компонентов на основе глубокой ссылки
         *
         * @param deepLink Глубокая ссылка
         * @return Список конфигураций для начального стека
         */
        private fun getInitialStack(deepLink: DeepLink): List<Config> =
            when (deepLink) {
                is DeepLink.None -> listOf(Config.Main)
                is DeepLink.Web -> listOf(getConfigForPath(deepLink.path))
            }

        /**
         * Преобразует конфигурацию в веб-путь
         *
         * @param config Конфигурация компонента
         * @return Веб-путь для истории навигации
         */
        private fun getPathForConfig(config: Config): String =
            when (config) {
                Config.Main -> "/"
                Config.Settings -> "/$WEB_PATH_SETTINGS"
                Config.Skiko -> "/$WEB_PATH_SKIKO"
                Config.Auth -> "/$WEB_PATH_AUTH"
                Config.Room -> "/$WEB_PATH_ROOM"
//                Config.Tree -> "/$WEB_PATH_TREE"
            }

        /**
         * Преобразует веб-путь в конфигурацию компонента
         *
         * @param path Веб-путь
         * @return Конфигурация компонента
         */
        private fun getConfigForPath(path: String): Config =
            when (path.removePrefix("/")) {
                WEB_PATH_SETTINGS -> Config.Settings
                WEB_PATH_SKIKO -> Config.Skiko
                WEB_PATH_AUTH -> Config.Auth
                WEB_PATH_ROOM -> Config.Room
                else -> Config.Main
            }
    }

    /**
     * Конфигурация для навигационного стека
     * Определяет тип экрана, который должен быть отображен
     */
    @Serializable
    private sealed interface Config {
        /**
         * Главный экран
         */
        @Serializable
        data object Main : Config

        /**
         * Экран настроек
         */
        @Serializable
        data object Settings : Config

        /**
         * Экран с графикой Skiko
         */
        @Serializable
        data object Skiko : Config

        /**
         * Экран аутентификации
         */
        @Serializable
        data object Auth : Config

        /**
         * Экран комнаты
         */
        @Serializable
        data object Room : Config

    }

    /**
     * Представляет глубокую ссылку для начальной навигации
     */
    sealed interface DeepLink {
        /**
         * Отсутствие глубокой ссылки (стандартная навигация)
         */
        data object None : DeepLink

        /**
         * Веб-путь для глубокой ссылки
         *
         * @property path Путь для навигации
         */
        class Web(val path: String) : DeepLink
    }
}
