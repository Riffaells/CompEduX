package component.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.*
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
import component.app.skiko.DefaultSkikoComponent
import component.app.skiko.SkikoComponentParams
import component.root.RootComponent.Child.*
import component.root.store.RootStore
import component.root.store.RootStoreFactory
import di.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.factory
import org.kodein.di.instance
import settings.AppearanceSettings
import settings.MultiplatformSettings
import utils.NavigationExecutor
import utils.rDispatchers


/**
 * Параметры для создания корневого компонента
 */
@OptIn(ExperimentalDecomposeApi::class)
data class RootComponentParams(
    val componentContext: ComponentContext,
    val webHistoryController: WebHistoryController? = null,
    val deepLink: DefaultRootComponent.DeepLink? = null
)


interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>
    val state: StateFlow<RootStore.State>

    val settings: MultiplatformSettings

    fun onEvent(event: RootStore.Intent)
    fun onMainClicked()
    fun setDrawerHandler(handler: () -> Unit)
    fun onSettingsClicked()
    fun onDevelopmentMapClicked()
    fun onAuthClicked()
    fun onRoomClicked()

    sealed class Child {
        class MainChild(val component: DefaultMainComponent) : Child()
        class SettingsChild(val component: DefaultSettingsComponent) : Child()
        class SkikoChild(val component: DefaultSkikoComponent) : Child()
        class AuthChild(val component: DefaultAuthComponent) : Child()
        class RoomChild(val component: DefaultRoomComponent) : Child()
    }

    val appearanceSettings: AppearanceSettings
}

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

    // Получаем фабрики компонентов

    private val navigation = StackNavigation<Config>()

    // Создаем навигационный executor, используя scope, связанный с жизненным циклом
    private val navigationExecutor = NavigationExecutor(
        navigation = navigation,
        scope = scope,
        mainDispatcher = rDispatchers.main,
        logger = { message -> println("Root Navigation: $message") }
    )

    private var drawerHandler: (() -> Unit)? = null

    override fun setDrawerHandler(handler: () -> Unit) {
        drawerHandler = handler
    }

    override val settings by instance<MultiplatformSettings>()


    override val appearanceSettings by instance<AppearanceSettings>()

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

    private val store =
        instanceKeeper.getStore {
            rootStoreFactory.create()
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<RootStore.State> = store.stateFlow

    init {
        webHistoryController?.attach(
            navigator = navigation,
            stack = stack,
            getPath = ::getPathForConfig,
            serializer = Config.serializer(),
            getConfiguration = ::getConfigForPath,
        )
    }

    override fun onEvent(event: RootStore.Intent) {
        store.accept(event)
    }

    private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            Config.Main -> MainChild(mainComponent(componentContext))
            Config.Settings -> SettingsChild(settingsComponent(componentContext))
            Config.Skiko -> SkikoChild(skikoComponent(componentContext))
            Config.Auth -> AuthChild(authComponent(componentContext))
            Config.Room -> RoomChild(roomComponent(componentContext))
        }


    private fun mainComponent(componentContext: ComponentContext): DefaultMainComponent {
        val mainComponentFactory by factory<MainComponentParams, DefaultMainComponent>()
        return mainComponentFactory(
            MainComponentParams(
                componentContext = componentContext,
                onSettingsClicked = ::onSettingsClicked,
                onDevelopmentMapClicked = ::onDevelopmentMapClicked,
                onRoomClicked = ::onRoomClicked
            )
        )
    }

    private fun settingsComponent(componentContext: ComponentContext): DefaultSettingsComponent {
        return DefaultSettingsComponent(
            componentContext = componentContext,
            onBack = { navigationExecutor.pop() },
            di = di
        )
    }

    private fun skikoComponent(componentContext: ComponentContext): DefaultSkikoComponent {
        val skikoComponentFactory by factory<SkikoComponentParams, DefaultSkikoComponent>()
        return skikoComponentFactory(
            SkikoComponentParams(
                componentContext = componentContext,
                onBack = { navigationExecutor.pop() }
            )
        )
    }

    private fun authComponent(componentContext: ComponentContext): DefaultAuthComponent {
        val authComponentFactory by factory<AuthComponentParams, DefaultAuthComponent>()
        return authComponentFactory(
            AuthComponentParams(
                componentContext = componentContext,
                onBack = { navigationExecutor.pop() }
            )
        )
    }

    private fun roomComponent(componentContext: ComponentContext): DefaultRoomComponent {
        val roomComponentFactory by factory<RoomComponentParams, DefaultRoomComponent>()
        return roomComponentFactory(
            RoomComponentParams(
                componentContext = componentContext,
                onBack = { navigationExecutor.pop() }
            )
        )
    }

    override fun onMainClicked() {
        navigationExecutor.navigateTo(Config.Main)
    }

    override fun onSettingsClicked() {
        navigationExecutor.navigateTo(Config.Settings)
    }

    override fun onDevelopmentMapClicked() {
        navigationExecutor.navigateTo(Config.Skiko)
    }

    override fun onAuthClicked() {
        navigationExecutor.navigateTo(Config.Auth)
    }

    override fun onRoomClicked() {
        navigationExecutor.navigateTo(Config.Room)
    }

    private companion object {
        private const val WEB_PATH_SETTINGS = "settings"
        private const val WEB_PATH_SKIKO = "skiko"
        private const val WEB_PATH_AUTH = "auth"
        private const val WEB_PATH_ROOM = "room"

        private fun getInitialStack(webHistoryPaths: List<String>?, deepLink: DeepLink): List<Config> =
            webHistoryPaths
                ?.takeUnless(List<*>::isEmpty)
                ?.map(Companion::getConfigForPath)
                ?: getInitialStack(deepLink)

        private fun getInitialStack(deepLink: DeepLink): List<Config> =
            when (deepLink) {
                is DeepLink.None -> listOf(Config.Main)
                is DeepLink.Web -> listOf(getConfigForPath(deepLink.path))
            }

        private fun getPathForConfig(config: Config): String =
            when (config) {
                Config.Main -> "/"
                Config.Settings -> "/$WEB_PATH_SETTINGS"
                Config.Skiko -> "/$WEB_PATH_SKIKO"
                Config.Auth -> "/$WEB_PATH_AUTH"
                Config.Room -> "/$WEB_PATH_ROOM"
            }

        private fun getConfigForPath(path: String): Config =
            when (path.removePrefix("/")) {
                WEB_PATH_SETTINGS -> Config.Settings
                WEB_PATH_SKIKO -> Config.Skiko
                WEB_PATH_AUTH -> Config.Auth
                WEB_PATH_ROOM -> Config.Room
                else -> Config.Main
            }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Main : Config

        @Serializable
        data object Settings : Config

        @Serializable
        data object Skiko : Config

        @Serializable
        data object Auth : Config

        @Serializable
        data object Room : Config
    }

    sealed interface DeepLink {
        data object None : DeepLink
        class Web(val path: String) : DeepLink
    }
}
