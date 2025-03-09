package component.app.auth.store

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

interface ProfileStore : Store<ProfileStore.Intent, ProfileStore.State, Nothing> {

    sealed interface Intent {
        data object EditProfile : Intent
        data object ChangePassword : Intent
        data object SaveProfile : Intent
        data object Logout : Intent
    }

    @Serializable
    data class State(
        val user: User? = null,
        val isEditing: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    @Serializable
    data class User(
        val id: String,
        val name: String,
        val email: String,
        val avatarUrl: String? = null
    )
}

internal class ProfileStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    // Интерфейс для фабрики, используемый в ComponentModule
    interface Factory {
        fun create(): ProfileStore
    }

    fun create(): ProfileStore =
        object : ProfileStore, Store<ProfileStore.Intent, ProfileStore.State, Nothing> by storeFactory.create(
            name = "ProfileStore",
            initialState = ProfileStore.State(
                // Для примера создаем тестового пользователя
                user = ProfileStore.User(
                    id = "1",
                    name = "Тестовый Пользователь",
                    email = "test@example.com",
                    avatarUrl = null
                )
            ),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl
        ) {}

    private sealed interface Msg {
        data object StartEditing : Msg
        data object StopEditing : Msg
        data class ErrorOccurred(val error: String) : Msg
        data object Loading : Msg
        data object SaveSuccess : Msg
        data object LogoutSuccess : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ProfileStore.Intent, Unit, ProfileStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            try {
                // TODO: Загрузить данные профиля пользователя из API или локального хранилища
            } catch (e: Exception) {
                println("Error in executeAction: ${e.message}")
            }
        }

        // Безопасный вызов dispatch, который перехватывает исключения
        private fun safeDispatch(msg: Msg) {
            try {
                dispatch(msg)
            } catch (e: Exception) {
                println("Error in dispatch: ${e.message}")
            }
        }

        override fun executeIntent(intent: ProfileStore.Intent): Unit =
            try {
                when (intent) {
                    is ProfileStore.Intent.EditProfile -> {
                        safeDispatch(Msg.StartEditing)
                    }
                    is ProfileStore.Intent.SaveProfile -> {
                        safeDispatch(Msg.Loading)

                        scope.launch {
                            try {
                                // TODO: Реализовать реальную логику сохранения профиля с использованием API
                                // Для примера просто имитируем задержку
                                kotlinx.coroutines.delay(1000)

                                safeDispatch(Msg.SaveSuccess)
                            } catch (e: Exception) {
                                safeDispatch(Msg.ErrorOccurred(e.message ?: "Неизвестная ошибка"))
                                println("Error in saving profile: ${e.message}")
                            }
                        }
                        Unit
                    }
                    is ProfileStore.Intent.ChangePassword -> {
                        // TODO: Реализовать логику смены пароля
                        // Для примера просто выводим ошибку, что функция не реализована
                        safeDispatch(Msg.ErrorOccurred("Функция смены пароля пока не реализована"))
                        Unit
                    }
                    is ProfileStore.Intent.Logout -> {
                        safeDispatch(Msg.Loading)

                        scope.launch {
                            try {
                                // TODO: Реализовать реальную логику выхода из системы с использованием API
                                // Для примера просто имитируем задержку
                                kotlinx.coroutines.delay(500)

                                safeDispatch(Msg.LogoutSuccess)
                            } catch (e: Exception) {
                                safeDispatch(Msg.ErrorOccurred(e.message ?: "Неизвестная ошибка"))
                                println("Error in logout: ${e.message}")
                            }
                        }
                        Unit
                    }
                }
            } catch (e: Exception) {
                println("Error in executeIntent: ${e.message}")
            }
    }

    private object ReducerImpl : Reducer<ProfileStore.State, Msg> {
        override fun ProfileStore.State.reduce(msg: Msg): ProfileStore.State =
            when (msg) {
                is Msg.StartEditing -> copy(isEditing = true)
                is Msg.StopEditing -> copy(isEditing = false)
                is Msg.ErrorOccurred -> copy(error = msg.error, isLoading = false)
                is Msg.Loading -> copy(isLoading = true, error = null)
                is Msg.SaveSuccess -> copy(isLoading = false, error = null, isEditing = false)
                is Msg.LogoutSuccess -> copy(isLoading = false, error = null, user = null)
            }
    }
}
