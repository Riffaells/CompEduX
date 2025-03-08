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
        data class UpdateTheme(val isDarkMode: Boolean) : Intent
        data class UpdateLanguage(val language: String) : Intent
    }

    @Serializable
    data class State(
        val isDarkMode: Boolean = false,
        val language: String = "en",
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
        data class UpdateTheme(val isDarkMode: Boolean) : Msg
        data class UpdateLanguage(val language: String) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<SettingsStore.Intent, Unit, SettingsStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            dispatch(Msg.LoadData)
            loadThemeSettings()
            loadLanguageSettings()
            setupObservers()
        }

        private fun loadThemeSettings() {
            scope.launch {
                val themeValue = settings.themeFlow.first()
                val isDarkMode = themeValue == MultiplatformSettings.ThemeOption.THEME_DARK
                dispatch(Msg.UpdateTheme(isDarkMode))
            }
        }

        private fun loadLanguageSettings() {
            scope.launch {
                val language = settings.langFlow.first()
                dispatch(Msg.UpdateLanguage(language))
            }
        }

        private fun setupObservers() {
            // Наблюдаем за изменениями темы
            scope.launch {
                settings.themeFlow.collectLatest { themeValue ->
                    val isDarkMode = themeValue == MultiplatformSettings.ThemeOption.THEME_DARK
                    dispatch(Msg.UpdateTheme(isDarkMode))
                }
            }

            // Наблюдаем за изменениями языка
            scope.launch {
                settings.langFlow.collectLatest { language ->
                    dispatch(Msg.UpdateLanguage(language))
                }
            }
        }

        override fun executeIntent(intent: SettingsStore.Intent): Unit =
            when (intent) {
                is SettingsStore.Intent.Init -> {
                    dispatch(Msg.LoadData)
                    loadThemeSettings()
                    loadLanguageSettings()
                    setupObservers()
                }
                is SettingsStore.Intent.UpdateTheme -> {
                    // Сохраняем настройку темы
                    val themeValue = if (intent.isDarkMode)
                        MultiplatformSettings.ThemeOption.THEME_DARK
                    else
                        MultiplatformSettings.ThemeOption.THEME_LIGHT

                    settings.saveThemeSettings(themeValue)
                    dispatch(Msg.UpdateTheme(intent.isDarkMode))
                }
                is SettingsStore.Intent.UpdateLanguage -> {
                    // Сохраняем настройку языка
                    settings.saveLangSettings(intent.language)
                    dispatch(Msg.UpdateLanguage(intent.language))
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
                is Msg.UpdateTheme -> copy(isDarkMode = msg.isDarkMode)
                is Msg.UpdateLanguage -> copy(language = msg.language)
            }
    }
}
