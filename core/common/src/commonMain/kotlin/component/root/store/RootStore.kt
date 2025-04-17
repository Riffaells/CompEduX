package component.root.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import logging.Logger
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import settings.MultiplatformSettings

interface RootStore : Store<RootStore.Intent, RootStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object NavigateToMain : Intent
        data object NavigateToSettings : Intent
        data object NavigateToDevMap : Intent
        data object NavigateToSkiko : Intent
        data object NavigateToTree : Intent
        data object NavigateToAuth : Intent
        data class NavigateToRoom(val roomId: String) : Intent
        data class UpdateTheme(val theme: Int) : Intent
        data class UpdateLanguage(val language: String) : Intent
    }

    @Serializable
    data class State(
        val theme: Int = 0,
        val language: String = "en",
        val loading: Boolean = false,
        val initialized: Boolean = false,
    )
}

@Serializable
sealed class NavigationConfig {
    @Serializable
    data object Main : NavigationConfig()

    @Serializable
    data object Settings : NavigationConfig()

    @Serializable
    data object Skiko : NavigationConfig()

    @Serializable
    data object Tree : NavigationConfig()

    @Serializable
    data object Auth : NavigationConfig()

    @Serializable
    data class Room(val roomId: String) : NavigationConfig()
}

internal class RootStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI,
) : DIAware {

    // Get tagged logger directly
    private val logger by instance<Logger>()
    private val settings by instance<MultiplatformSettings>()

    // Флаг для отслеживания, загружены ли настройки
    private var settingsLoaded = false

    fun create(): RootStore =
        object : RootStore,
            Store<RootStore.Intent, RootStore.State, Nothing> by storeFactory.create(
                name = "RootStore",
                initialState = RootStore.State(),
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = { ExecutorImpl(logger.withTag("RootStore")) },
                reducer = ReducerImpl
            ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data object LoadData : Msg
        data object InitializationComplete : Msg
        data class UpdateTheme(val theme: Int) : Msg
        data class UpdateLanguage(val language: String) : Msg
        data class NavigateTo(val config: NavigationConfig) : Msg
    }

    private inner class ExecutorImpl(
        private val logger: Logger
    ) : CoroutineExecutor<RootStore.Intent, Unit, RootStore.State, Msg, Nothing>(
        rDispatchers.main
    ) {

        // Флаг для предотвращения повторной инициализации
        private var isInitializing = false

        override fun executeAction(action: Unit) {
            // Не делаем ничего в executeAction, чтобы не блокировать запуск
            // Инициализация будет выполнена позже по требованию

            scope.launch {
                settings.appearance.themeFlow.collect { theme ->
                    logger.d("Theme $theme is $theme")
                }
            }
        }

        private suspend fun loadInitialSettings() {
            try {
                // Проверяем, загружены ли настройки
                if (!settingsLoaded) {
                    // Загрузка настроек в фоновом потоке

                    // Обновление UI
                    safeDispatch(Msg.UpdateTheme(settings.appearance.themeFlow.first()))
                    settingsLoaded = true
                }
            } catch (e: Exception) {
                logger.e("Error loading initial settings: ${e.message}")

            }
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                logger.e("Error in dispatch: ${e.message}")

            }
        }

        override fun executeIntent(intent: RootStore.Intent): Unit =
            when (intent) {
                is RootStore.Intent.Init -> {
                    // Проверяем, инициализирован ли уже стор и не выполняется ли инициализация
                    if (!state().initialized && !isInitializing) {
                        isInitializing = true

                        // Не блокируем UI при инициализации
                        scope.launch {
                            // Задержка перед началом загрузки настроек
                            // Это позволит UI полностью отрисоваться
                            kotlinx.coroutines.delay(500)

                            dispatch(Msg.LoadingData)

                            // Загружаем настройки в фоновом потоке
                            withContext(Dispatchers.Default) {
                                loadInitialSettings()
                            }

                            dispatch(Msg.LoadData)
                            dispatch(Msg.InitializationComplete)
                            isInitializing = false
                        }
                    }
                    Unit
                }

                is RootStore.Intent.UpdateTheme -> {
                    readThemeSettings()
                    Unit
                }

                is RootStore.Intent.UpdateLanguage -> {
                    // Сохранение настроек
                    scope.launch {
                        try {
                            // Асинхронное сохранение настроек
                            // settings.putString("language", intent.language)

                            // Обновление UI
                            safeDispatch(Msg.UpdateLanguage(intent.language))
                        } catch (e: Exception) {
                            logger.e("Error updating language: ${e.message}")

                        }
                    }
                    Unit
                }

                is RootStore.Intent.NavigateToMain -> {
                    dispatch(Msg.NavigateTo(NavigationConfig.Main))
                    handleNavigateTo(NavigationConfig.Main)
                }
                is RootStore.Intent.NavigateToSettings -> {
                    dispatch(Msg.NavigateTo(NavigationConfig.Settings))
                    handleNavigateTo(NavigationConfig.Settings)
                }
                is RootStore.Intent.NavigateToDevMap -> {
                    dispatch(Msg.NavigateTo(NavigationConfig.Skiko))
                    handleNavigateTo(NavigationConfig.Skiko)
                }
                is RootStore.Intent.NavigateToSkiko -> {
                    dispatch(Msg.NavigateTo(NavigationConfig.Skiko))
                    handleNavigateTo(NavigationConfig.Skiko)
                }
                is RootStore.Intent.NavigateToTree -> {
                    dispatch(Msg.NavigateTo(NavigationConfig.Tree))
                    handleNavigateTo(NavigationConfig.Tree)
                }
                is RootStore.Intent.NavigateToAuth -> {
                    dispatch(Msg.NavigateTo(NavigationConfig.Auth))
                    handleNavigateTo(NavigationConfig.Auth)
                }
                is RootStore.Intent.NavigateToRoom -> {
                    dispatch(Msg.NavigateTo(NavigationConfig.Room(intent.roomId)))
                    handleNavigateTo(NavigationConfig.Room(intent.roomId))
                }
            }

        private fun saveThemeSettings(value: Int) {
            scope.launch {
                try {
                    // Выполняем сохранение в фоновом потоке
                    withContext(Dispatchers.Default) {
                        settings.appearance.saveTheme(value)
                    }
                    safeDispatch(Msg.UpdateTheme(value))
                } catch (e: Exception) {
                    logger.e("Error saving theme settings: ${e.message}")

                }
            }
        }

        private fun readThemeSettings() {
            scope.launch {
                try {
                    // Читаем настройки в фоновом потоке
                    val theme = withContext(Dispatchers.Default) {
                        settings.appearance.themeFlow.first()
                    }
                    safeDispatch(Msg.UpdateTheme(theme))
                } catch (e: Exception) {
                    logger.e("Error reading theme settings: ${e.message}")

                }
            }
        }

        // Add a handler for NavigateTo messages
        private fun handleNavigateTo(config: NavigationConfig) {
            // As this is an example implementation, we would normally send this to a navigation controller
            // For now, we'll log the navigation request
            logger.d("Navigation request: $config")

            // Actual implementation would be something like:
            // navigationExecutor.navigateTo(mapConfigToNavigationConfig(config))
        }
    }

    private object ReducerImpl : Reducer<RootStore.State, Msg> {
        override fun RootStore.State.reduce(msg: Msg): RootStore.State =
            when (msg) {
                Msg.LoadData -> copy(loading = false)
                Msg.LoadingData -> copy(loading = true)
                Msg.InitializationComplete -> copy(initialized = true)
                is Msg.UpdateTheme -> copy(theme = msg.theme)
                is Msg.UpdateLanguage -> copy(language = msg.language)
                is Msg.NavigateTo -> this // Navigation doesn't affect state
            }
    }
}
