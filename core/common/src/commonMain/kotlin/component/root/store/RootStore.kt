package component.root.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import utils.rDispatchers

interface RootStore : Store<RootStore.Intent, RootStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data class UpdateTheme(val isDarkMode: Boolean) : Intent
        data class UpdateLanguage(val language: String) : Intent
    }

    @Serializable
    data class State(
        val isDarkMode: Boolean = false,
        val language: String = "en",
        val loading: Boolean = false,
    )
}

internal class RootStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI,
) : DIAware {

//    val settings by instance<MultiplatformSettings>()
//    val teamUseCases by instance<TeamUseCases>()
//    val useCasesCategory by instance<CategoryUseCases>()
//    val useCasesWord by instance<WordUseCases>()

    fun create(): RootStore =
        object : RootStore,
            Store<RootStore.Intent, RootStore.State, Nothing> by storeFactory.create(
                name = "RootStore",
                initialState = RootStore.State(),
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
        CoroutineExecutor<RootStore.Intent, Unit, RootStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                dispatch(Msg.LoadData)
                // Здесь можно загрузить настройки из хранилища
                loadInitialSettings()
            } catch (e: Exception) {
                println("Error in executeAction: ${e.message}")
            }
        }

        private fun loadInitialSettings() {
            // Загружаем начальные настройки
            scope.launch {
                try {
                    // Загрузка настроек

                    // Обновление UI
                    // safeDispatch(Msg.UpdateTheme(isDarkMode))
                    // safeDispatch(Msg.UpdateLanguage(language))
                } catch (e: Exception) {
                    println("Error loading initial settings: ${e.message}")
                }
            }
            Unit
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                println("Error in dispatch: ${e.message}")
            }
        }

        override fun executeIntent(intent: RootStore.Intent): Unit =
            try {
                when (intent) {
                    is RootStore.Intent.Init -> {
                        safeDispatch(Msg.LoadData)
                        loadInitialSettings()
                    }
                    is RootStore.Intent.UpdateTheme -> {
                        // Сохранение настроек
                        scope.launch {
                            try {
                                // Асинхронное сохранение настроек
                                // settings.putBoolean("isDarkMode", intent.isDarkMode)

                                // Обновление UI
                                safeDispatch(Msg.UpdateTheme(intent.isDarkMode))
                            } catch (e: Exception) {
                                println("Error updating theme: ${e.message}")
                            }
                        }
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
                            }
                        }
                        Unit
                    }
                }
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<RootStore.State, Msg> {
        override fun RootStore.State.reduce(msg: Msg): RootStore.State =
            when (msg) {
                Msg.LoadData -> copy(loading = false)
                Msg.LoadingData -> copy(loading = true)
                is Msg.UpdateTheme -> copy(isDarkMode = msg.isDarkMode)
                is Msg.UpdateLanguage -> copy(language = msg.language)
            }
    }
}
