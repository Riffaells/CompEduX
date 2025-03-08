package component.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.router.stack.webhistory.WebHistoryController
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.main.DefaultMainComponent
import component.app.settings.DefaultSettingsComponent
import component.root.RootComponent.Child.*
import component.root.store.RootStore
import component.root.store.RootStoreFactory
import di.MainComponentParams
import di.SettingsComponentParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.factory

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>
    val state: StateFlow<RootStore.State>

    fun onEvent(event: RootStore.Intent)
    fun onMainClicked()
    fun onSettingsClicked()

    sealed class Child {
        class MainChild(val component: DefaultMainComponent) : Child()
        class SettingsChild(val component: DefaultSettingsComponent) : Child()
    }
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultRootComponent(
    componentContext: ComponentContext,
    deepLink: DeepLink = DeepLink.None,
    webHistoryController: WebHistoryController? = null,
    storeFactory: com.arkivanov.mvikotlin.main.store.DefaultStoreFactory,
    override val di: DI
) : RootComponent, DIAware, ComponentContext by componentContext {

    // Получаем фабрики компонентов

    private val navigation = StackNavigation<Config>()

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
        }


    private fun mainComponent(componentContext: ComponentContext): DefaultMainComponent {

        val mainComponentFactory: (MainComponentParams) -> DefaultMainComponent by factory()
        return mainComponentFactory(
            MainComponentParams(
                componentContext = componentContext,
                onSettingsClicked = ::onSettingsClicked
            )
        )
    }

    private fun settingsComponent(componentContext: ComponentContext): DefaultSettingsComponent {

        //val settingsComponentFactory: (SettingsComponentParams) -> DefaultSettingsComponent by factory()
        return DefaultSettingsComponent(
            componentContext = componentContext,
            onBack = navigation::pop,
            di = di

        )
    }

    override fun onMainClicked() {
        navigation.bringToFront(Config.Main)
    }

    override fun onSettingsClicked() {
        navigation.bringToFront(Config.Settings)
    }

    private companion object {
        private const val WEB_PATH_SETTINGS = "settings"

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
            }

        private fun getConfigForPath(path: String): Config =
            when (path.removePrefix("/")) {
                WEB_PATH_SETTINGS -> Config.Settings
                else -> Config.Main
            }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Main : Config

        @Serializable
        data object Settings : Config
    }

    sealed interface DeepLink {
        data object None : DeepLink
        class Web(val path: String) : DeepLink
    }
}
