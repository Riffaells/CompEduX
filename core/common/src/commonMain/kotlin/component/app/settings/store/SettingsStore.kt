package component.app.settings.store

import settings.MultiplatformSettings
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
import settings.AppearanceSettings
import settings.NetworkSettings
import settings.SecuritySettings
import settings.ProfileSettings
import utils.rDispatchers

interface SettingsStore : Store<SettingsStore.Intent, SettingsStore.State, Nothing> {

    sealed interface Intent {
        data object Init : Intent
        data object Back : Intent
        data class UpdateTheme(val theme: Int) : Intent
        data class UpdateLanguage(val language: String) : Intent
        data class UpdateBlackBackground(val enabled: Boolean) : Intent
        data class UpdateServerUrl(val url: String) : Intent
        data class UpdateStarrySky(val enabled: Boolean) : Intent
        data class UpdateUseBiometric(val enabled: Boolean) : Intent
        data class UpdateAutoLogoutTime(val minutes: Int) : Intent

        // Новые интенты для профиля
        data class UpdateUsername(val username: String) : Intent
        data class UpdateEmail(val email: String) : Intent
        data class UpdateAvatarUrl(val url: String) : Intent
        data class UpdateStatus(val status: String) : Intent
        data class UpdateProfilePublic(val isPublic: Boolean) : Intent
        data class UpdateProfileNotifications(val enabled: Boolean) : Intent
        data object ClearProfileData : Intent
    }

    @Serializable
    data class State(
        val theme: Int = AppearanceSettings.ThemeOption.DEFAULT,
        val language: String = "ru",
        val blackBackground: Boolean = false,
        val starrySky: Boolean = false,
        val serverUrl: String = "https://api.example.com",
        val useBiometric: Boolean = false,
        val autoLogoutTime: Int = 15,

        // Новые поля для профиля
        val username: String = "",
        val email: String = "",
        val avatarUrl: String = "",
        val status: String = "",
        val isProfilePublic: Boolean = true,
        val enableProfileNotifications: Boolean = true,
        val isProfileComplete: Boolean = false,

        val loading: Boolean = false
    )
}

internal class SettingsStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {

    private val settings by instance<MultiplatformSettings>()
    private val appearanceSettings by instance<AppearanceSettings>()
    private val networkSettings by instance<NetworkSettings>()
    private val securitySettings by instance<SecuritySettings>()
    private val profileSettings by instance<ProfileSettings>()

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
        data class UpdateStarrySky(val enabled: Boolean) : Msg
        data class UpdateServerUrl(val url: String) : Msg
        data class UpdateUseBiometric(val enabled: Boolean) : Msg
        data class UpdateAutoLogoutTime(val minutes: Int) : Msg

        // Новые сообщения для профиля
        data class UpdateUsername(val username: String) : Msg
        data class UpdateEmail(val email: String) : Msg
        data class UpdateAvatarUrl(val url: String) : Msg
        data class UpdateStatus(val status: String) : Msg
        data class UpdateProfilePublic(val isPublic: Boolean) : Msg
        data class UpdateProfileNotifications(val enabled: Boolean) : Msg
        data class UpdateProfileComplete(val isComplete: Boolean) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<SettingsStore.Intent, Unit, SettingsStore.State, Msg, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            dispatch(Msg.LoadData)
            loadAllSettings()
            setupObservers()
        }

        private fun loadAllSettings() {
            loadAppearanceSettings()
            loadNetworkSettings()
            loadSecuritySettings()
            loadProfileSettings()
        }

        private fun loadAppearanceSettings() {
            scope.launch {
                val theme = appearanceSettings.themeFlow.first()
                dispatch(Msg.UpdateTheme(theme))

                val language = appearanceSettings.langFlow.first()
                dispatch(Msg.UpdateLanguage(language))

                val blackBackground = appearanceSettings.blackBackgroundFlow.first()
                dispatch(Msg.UpdateBlackBackground(blackBackground))

                val starrySky = appearanceSettings.starrySkyFlow.first()
                dispatch(Msg.UpdateStarrySky(starrySky))
            }
        }

        private fun loadNetworkSettings() {
            scope.launch {
                val serverUrl = networkSettings.serverUrlFlow.first()
                dispatch(Msg.UpdateServerUrl(serverUrl))
            }
        }

        private fun loadSecuritySettings() {
            scope.launch {
                val useBiometric = securitySettings.useBiometricFlow.first()
                dispatch(Msg.UpdateUseBiometric(useBiometric))

                val autoLogoutTime = securitySettings.autoLogoutTimeFlow.first()
                dispatch(Msg.UpdateAutoLogoutTime(autoLogoutTime))
            }
        }

        private fun loadProfileSettings() {
            scope.launch {
                val username = profileSettings.usernameFlow.first()
                dispatch(Msg.UpdateUsername(username))

                val email = profileSettings.emailFlow.first()
                dispatch(Msg.UpdateEmail(email))

                val avatarUrl = profileSettings.avatarUrlFlow.first()
                dispatch(Msg.UpdateAvatarUrl(avatarUrl))

                val status = profileSettings.statusFlow.first()
                dispatch(Msg.UpdateStatus(status))

                val isProfilePublic = profileSettings.isProfilePublicFlow.first()
                dispatch(Msg.UpdateProfilePublic(isProfilePublic))

                val enableProfileNotifications = profileSettings.enableProfileNotificationsFlow.first()
                dispatch(Msg.UpdateProfileNotifications(enableProfileNotifications))

                val isProfileComplete = profileSettings.isProfileComplete()
                dispatch(Msg.UpdateProfileComplete(isProfileComplete))
            }
        }

        private fun setupObservers() {
            // Наблюдаем за изменениями настроек внешнего вида
            scope.launch {
                appearanceSettings.themeFlow.collectLatest { theme ->
                    dispatch(Msg.UpdateTheme(theme))
                }
            }

            scope.launch {
                appearanceSettings.langFlow.collectLatest { language ->
                    dispatch(Msg.UpdateLanguage(language))
                }
            }

            scope.launch {
                appearanceSettings.blackBackgroundFlow.collectLatest { enabled ->
                    dispatch(Msg.UpdateBlackBackground(enabled))
                }
            }

            scope.launch {
                appearanceSettings.starrySkyFlow.collectLatest { enabled ->
                    dispatch(Msg.UpdateStarrySky(enabled))
                }
            }

            // Наблюдаем за изменениями настроек сети
            scope.launch {
                networkSettings.serverUrlFlow.collectLatest { url ->
                    dispatch(Msg.UpdateServerUrl(url))
                }
            }

            // Наблюдаем за изменениями настроек безопасности
            scope.launch {
                securitySettings.useBiometricFlow.collectLatest { enabled ->
                    dispatch(Msg.UpdateUseBiometric(enabled))
                }
            }

            scope.launch {
                securitySettings.autoLogoutTimeFlow.collectLatest { minutes ->
                    dispatch(Msg.UpdateAutoLogoutTime(minutes))
                }
            }

            // Наблюдаем за изменениями настроек профиля
            scope.launch {
                profileSettings.usernameFlow.collectLatest { username ->
                    dispatch(Msg.UpdateUsername(username))
                    dispatch(Msg.UpdateProfileComplete(profileSettings.isProfileComplete()))
                }
            }

            scope.launch {
                profileSettings.emailFlow.collectLatest { email ->
                    dispatch(Msg.UpdateEmail(email))
                    dispatch(Msg.UpdateProfileComplete(profileSettings.isProfileComplete()))
                }
            }

            scope.launch {
                profileSettings.avatarUrlFlow.collectLatest { url ->
                    dispatch(Msg.UpdateAvatarUrl(url))
                }
            }

            scope.launch {
                profileSettings.statusFlow.collectLatest { status ->
                    dispatch(Msg.UpdateStatus(status))
                }
            }

            scope.launch {
                profileSettings.isProfilePublicFlow.collectLatest { isPublic ->
                    dispatch(Msg.UpdateProfilePublic(isPublic))
                }
            }

            scope.launch {
                profileSettings.enableProfileNotificationsFlow.collectLatest { enabled ->
                    dispatch(Msg.UpdateProfileNotifications(enabled))
                }
            }
        }

        override fun executeIntent(intent: SettingsStore.Intent): Unit =
            when (intent) {
                is SettingsStore.Intent.Init -> {
                    dispatch(Msg.LoadData)
                    loadAllSettings()
                    setupObservers()
                }
                is SettingsStore.Intent.UpdateTheme -> {
                    appearanceSettings.saveTheme(intent.theme)
                    dispatch(Msg.UpdateTheme(intent.theme))
                }
                is SettingsStore.Intent.UpdateLanguage -> {
                    appearanceSettings.saveLang(intent.language)
                    dispatch(Msg.UpdateLanguage(intent.language))
                }
                is SettingsStore.Intent.UpdateBlackBackground -> {
                    appearanceSettings.saveBlackBackground(intent.enabled)
                    dispatch(Msg.UpdateBlackBackground(intent.enabled))
                }
                is SettingsStore.Intent.UpdateStarrySky -> {
                    appearanceSettings.saveStarrySky(intent.enabled)
                    dispatch(Msg.UpdateStarrySky(intent.enabled))
                }
                is SettingsStore.Intent.UpdateServerUrl -> {
                    networkSettings.saveServerUrl(intent.url)
                    dispatch(Msg.UpdateServerUrl(intent.url))
                }
                is SettingsStore.Intent.UpdateUseBiometric -> {
                    securitySettings.saveUseBiometric(intent.enabled)
                    dispatch(Msg.UpdateUseBiometric(intent.enabled))
                }
                is SettingsStore.Intent.UpdateAutoLogoutTime -> {
                    securitySettings.saveAutoLogoutTime(intent.minutes)
                    dispatch(Msg.UpdateAutoLogoutTime(intent.minutes))
                }
                is SettingsStore.Intent.Back -> {
                    // Обработка в компоненте
                }

                // Обработка интентов профиля
                is SettingsStore.Intent.UpdateUsername -> {
                    profileSettings.saveUsername(intent.username)
                    dispatch(Msg.UpdateUsername(intent.username))
                    dispatch(Msg.UpdateProfileComplete(profileSettings.isProfileComplete()))
                }
                is SettingsStore.Intent.UpdateEmail -> {
                    profileSettings.saveEmail(intent.email)
                    dispatch(Msg.UpdateEmail(intent.email))
                    dispatch(Msg.UpdateProfileComplete(profileSettings.isProfileComplete()))
                }
                is SettingsStore.Intent.UpdateAvatarUrl -> {
                    profileSettings.saveAvatarUrl(intent.url)
                    dispatch(Msg.UpdateAvatarUrl(intent.url))
                }
                is SettingsStore.Intent.UpdateStatus -> {
                    profileSettings.saveStatus(intent.status)
                    dispatch(Msg.UpdateStatus(intent.status))
                }
                is SettingsStore.Intent.UpdateProfilePublic -> {
                    profileSettings.setProfilePublic(intent.isPublic)
                    dispatch(Msg.UpdateProfilePublic(intent.isPublic))
                }
                is SettingsStore.Intent.UpdateProfileNotifications -> {
                    profileSettings.setProfileNotifications(intent.enabled)
                    dispatch(Msg.UpdateProfileNotifications(intent.enabled))
                }
                is SettingsStore.Intent.ClearProfileData -> {
                    profileSettings.clearProfileData()
                    loadProfileSettings()
                }
                else -> {
                    // Обработка других интентов
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
                is Msg.UpdateStarrySky -> copy(starrySky = msg.enabled)
                is Msg.UpdateServerUrl -> copy(serverUrl = msg.url)
                is Msg.UpdateUseBiometric -> copy(useBiometric = msg.enabled)
                is Msg.UpdateAutoLogoutTime -> copy(autoLogoutTime = msg.minutes)

                // Обработка сообщений профиля
                is Msg.UpdateUsername -> copy(username = msg.username)
                is Msg.UpdateEmail -> copy(email = msg.email)
                is Msg.UpdateAvatarUrl -> copy(avatarUrl = msg.url)
                is Msg.UpdateStatus -> copy(status = msg.status)
                is Msg.UpdateProfilePublic -> copy(isProfilePublic = msg.isPublic)
                is Msg.UpdateProfileNotifications -> copy(enableProfileNotifications = msg.enabled)
                is Msg.UpdateProfileComplete -> copy(isProfileComplete = msg.isComplete)
                else -> this
            }
    }
}
