package component.app.settings.store

import MultiplatformSettings
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import utils.rDispatchers

interface SettingsStore : Store<SettingsStore.Intent, SettingsStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object Back : Intent
        data class UpdateTheme(val theme: Int) : Intent
        data class UpdateLanguage(val language: String) : Intent
        data class UpdateBlackBackground(val enabled: Boolean) : Intent
        data class UpdateServerUrl(val url: String) : Intent
    }

    @Serializable
    data class State(
        val theme: Int = MultiplatformSettings.ThemeOption.THEME_SYSTEM,
        val language: String = "en",
        val blackBackground: Boolean = false,
        val serverUrl: String = "https://api.example.com",
        val loading: Boolean = false
    )
}

internal class SettingsStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val settings by instance<MultiplatformSettings>()

    fun create(): SettingsStore =
        object : SettingsStore, Store<SettingsStore.Intent, SettingsStore.State, Nothing> by storeFactory.create(
            name = "SettingsStore",
            initialState = SettingsStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data object LoadData : Msg
        data class UpdateTheme(val theme: Int) : Msg
        data class UpdateLanguage(val language: String) : Msg
        data class UpdateBlackBackground(val enabled: Boolean) : Msg
        data class UpdateServerUrl(val url: String) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<SettingsStore.Intent, Unit, SettingsStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            dispatch(Msg.LoadData)
            loadThemeSettings()
            loadLanguageSettings()
            loadBlackBackgroundSettings()
            loadServerUrlSettings()
            setupObservers()
        }

        private fun loadThemeSettings() {
            scope.launch {
                val theme = settings.themeFlow.first()
                dispatch(Msg.UpdateTheme(theme))
            }
        }

        private fun loadLanguageSettings() {
            scope.launch {
                val language = settings.langFlow.first()
                dispatch(Msg.UpdateLanguage(language))
            }
        }

        private fun loadBlackBackgroundSettings() {
            scope.launch {
                val blackBackground = settings.blackBackgroundFlow.first()
                dispatch(Msg.UpdateBlackBackground(blackBackground))
            }
        }

        private fun loadServerUrlSettings() {
            scope.launch {
                val serverUrl = settings.serverUrlFlow.first()
                dispatch(Msg.UpdateServerUrl(serverUrl))
            }
        }

        private fun setupObservers() {
            // Наблюдаем за изменениями темы
            scope.launch {
                settings.themeFlow.collectLatest { theme ->
                    dispatch(Msg.UpdateTheme(theme))
                }
            }

            // Наблюдаем за изменениями языка
            scope.launch {
                settings.langFlow.collectLatest { language ->
                    dispatch(Msg.UpdateLanguage(language))
                }
            }

            // Наблюдаем за изменениями настройки черного фона
            scope.launch {
                settings.blackBackgroundFlow.collectLatest { enabled ->
                    dispatch(Msg.UpdateBlackBackground(enabled))
                }
            }

            // Наблюдаем за изменениями URL сервера
            scope.launch {
                settings.serverUrlFlow.collectLatest { url ->
                    dispatch(Msg.UpdateServerUrl(url))
                }
            }
        }

        override fun executeIntent(intent: SettingsStore.Intent): Unit =
            when (intent) {
                is SettingsStore.Intent.Init -> {
                    dispatch(Msg.LoadData)
                    loadThemeSettings()
                    loadLanguageSettings()
                    loadBlackBackgroundSettings()
                    loadServerUrlSettings()
                    setupObservers()
                }
                is SettingsStore.Intent.UpdateTheme -> {
                    // Сохраняем настройку темы
                    settings.saveThemeSettings(intent.theme)
                    dispatch(Msg.UpdateTheme(intent.theme))
                }
                is SettingsStore.Intent.UpdateLanguage -> {
                    // Сохраняем настройку языка
                    settings.saveLangSettings(intent.language)
                    dispatch(Msg.UpdateLanguage(intent.language))
                }
                is SettingsStore.Intent.UpdateBlackBackground -> {
                    // Сохраняем настройку черного фона
                    settings.saveBlackBackgroundSettings(intent.enabled)
                    dispatch(Msg.UpdateBlackBackground(intent.enabled))
                }
                is SettingsStore.Intent.UpdateServerUrl -> {
                    // Сохраняем URL сервера
                    settings.saveServerUrlSettings(intent.url)
                    dispatch(Msg.UpdateServerUrl(intent.url))
                }
                is SettingsStore.Intent.Back -> {
                    // Обработка в компоненте
                }
            }
    }

    private object ReducerImpl : Reducer<SettingsStore.State, Msg> {
        override fun SettingsStore.State.reduce(msg: Msg): SettingsStore.State =
            when (msg) {
                Msg.LoadData -> copy(loading = false)
                Msg.LoadingData -> copy(loading = true)
                is Msg.UpdateTheme -> copy(theme = msg.theme)
                is Msg.UpdateLanguage -> copy(language = msg.language)
                is Msg.UpdateBlackBackground -> copy(blackBackground = msg.enabled)
                is Msg.UpdateServerUrl -> copy(serverUrl = msg.url)
            }
    }
}
