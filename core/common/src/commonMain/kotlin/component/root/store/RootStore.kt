package component.root.store

import settings.MultiplatformSettings
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
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import utils.rDispatchers

interface RootStore : Store<RootStore.Intent, RootStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
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

internal class RootStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI,
) : DIAware {

    // Получаем настройки через DI
    private val settings: MultiplatformSettings by di.instance()

    // Флаг для отслеживания, загружены ли настройки
    private var settingsLoaded = false

    fun create(): RootStore =
        object : RootStore,
            Store<RootStore.Intent, RootStore.State, Nothing> by storeFactory.create(
                name = "RootStore",
                initialState = RootStore.State(),
                // Отключаем автоматический bootstrapper для ускорения запуска
                bootstrapper = SimpleBootstrapper(Unit),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl
            ) {}

    private sealed interface Msg {
        data object LoadingData : Msg
        data object LoadData : Msg
        data object InitializationComplete : Msg
        data class UpdateTheme(val theme: Int) : Msg
        data class UpdateLanguage(val language: String) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<RootStore.Intent, Unit, RootStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        // Флаг для предотвращения повторной инициализации
        private var isInitializing = false

        override fun executeAction(action: Unit) {
            // Не делаем ничего в executeAction, чтобы не блокировать запуск
            // Инициализация будет выполнена позже по требованию
        }

        private suspend fun loadInitialSettings() {
            try {
                // Проверяем, загружены ли настройки
                if (!settingsLoaded) {
                    // Загрузка настроек в фоновом потоке
                    val theme = withContext(Dispatchers.Default) {
                        // Безопасный доступ к настройкам

                        settings.appearance.themeFlow.first()
                    }
                    // Обновление UI
                    safeDispatch(Msg.UpdateTheme(theme))
                    settingsLoaded = true
                }
            } catch (e: Exception) {
                println("Error loading initial settings: ${e.message}")
                e.printStackTrace()
            }
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                println("Error in dispatch: ${e.message}")
                e.printStackTrace()
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
                            println("Error updating language: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                    Unit
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
                    println("Error saving theme settings: ${e.message}")
                    e.printStackTrace()
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
                    println("Error reading theme settings: ${e.message}")
                    e.printStackTrace()
                }
            }
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
            }
    }
}
