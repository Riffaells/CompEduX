package component.app.settings.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import navigation.rDispatchers
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import settings.*

interface SettingsStore : Store<SettingsStore.Intent, SettingsStore.State, SettingsStore.Label> {

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

        // Network settings
        data class UpdateUseExperimentalApi(val enabled: Boolean) : Intent
        data class UpdateEnableBandwidthLimit(val enabled: Boolean) : Intent
        data class UpdateBandwidthLimitKbps(val kbps: Int) : Intent
        data class UpdateUseCustomTimeouts(val enabled: Boolean) : Intent
        data class UpdateConnectionTimeoutSeconds(val seconds: Int) : Intent
        data class UpdateReadTimeoutSeconds(val seconds: Int) : Intent

        // Common actions
        data object ResetToDefaults : Intent
    }

    sealed interface Label {
        data class ThemeChanged(val theme: Int) : Label
        data class LanguageChanged(val language: String) : Label
        data class BlackBackgroundChanged(val enabled: Boolean) : Label
        data class StarrySkyChanged(val enabled: Boolean) : Label
        data class ServerUrlChanged(val url: String) : Label
        data class BiometricChanged(val enabled: Boolean) : Label
        data class AutoLogoutTimeChanged(val minutes: Int) : Label
        data class ProfileChanged(val username: String, val email: String) : Label
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

        val loading: Boolean = false,
        val settingsApplied: Boolean = false,

        // Network settings
        val useExperimentalApi: Boolean = false,
        val enableBandwidthLimit: Boolean = false,
        val bandwidthLimitKbps: Int = 5000,
        val useCustomTimeouts: Boolean = false,
        val connectionTimeoutSeconds: Int = 30,
        val readTimeoutSeconds: Int = 60
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
        object : SettingsStore,
            Store<SettingsStore.Intent, SettingsStore.State, SettingsStore.Label> by storeFactory.create(
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
        data object SettingsApplied : Msg

        // Новые сообщения для профиля
        data class UpdateUsername(val username: String) : Msg
        data class UpdateEmail(val email: String) : Msg
        data class UpdateAvatarUrl(val url: String) : Msg
        data class UpdateStatus(val status: String) : Msg
        data class UpdateProfilePublic(val isPublic: Boolean) : Msg
        data class UpdateProfileNotifications(val enabled: Boolean) : Msg
        data class UpdateProfileComplete(val isComplete: Boolean) : Msg

        // Новые сообщения для сетевых настроек
        data class UpdateUseExperimentalApi(val enabled: Boolean) : Msg
        data class UpdateEnableBandwidthLimit(val enabled: Boolean) : Msg
        data class UpdateBandwidthLimitKbps(val kbps: Int) : Msg
        data class UpdateUseCustomTimeouts(val enabled: Boolean) : Msg
        data class UpdateConnectionTimeoutSeconds(val seconds: Int) : Msg
        data class UpdateReadTimeoutSeconds(val seconds: Int) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<SettingsStore.Intent, Unit, SettingsStore.State, Msg, SettingsStore.Label>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            dispatch(Msg.LoadData)
            loadAllSettings()
            setupObservers()
        }

        private fun loadAllSettings() {
            dispatch(Msg.LoadingData)
            loadAppearanceSettings()
            loadNetworkSettings()
            loadSecuritySettings()
            loadProfileSettings()
            dispatch(Msg.SettingsApplied)
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
                    publish(SettingsStore.Label.ThemeChanged(theme))
                }
            }

            scope.launch {
                appearanceSettings.langFlow.collectLatest { language ->
                    dispatch(Msg.UpdateLanguage(language))
                    publish(SettingsStore.Label.LanguageChanged(language))
                }
            }

            scope.launch {
                appearanceSettings.blackBackgroundFlow.collectLatest { enabled ->
                    dispatch(Msg.UpdateBlackBackground(enabled))
                    publish(SettingsStore.Label.BlackBackgroundChanged(enabled))
                }
            }

            scope.launch {
                appearanceSettings.starrySkyFlow.collectLatest { enabled ->
                    dispatch(Msg.UpdateStarrySky(enabled))
                    publish(SettingsStore.Label.StarrySkyChanged(enabled))
                }
            }

            // Наблюдаем за изменениями настроек сети
            scope.launch {
                networkSettings.serverUrlFlow.collectLatest { url ->
                    dispatch(Msg.UpdateServerUrl(url))
                    publish(SettingsStore.Label.ServerUrlChanged(url))
                }
            }

            // Наблюдаем за изменениями настроек безопасности
            scope.launch {
                securitySettings.useBiometricFlow.collectLatest { enabled ->
                    dispatch(Msg.UpdateUseBiometric(enabled))
                    publish(SettingsStore.Label.BiometricChanged(enabled))
                }
            }

            scope.launch {
                securitySettings.autoLogoutTimeFlow.collectLatest { minutes ->
                    dispatch(Msg.UpdateAutoLogoutTime(minutes))
                    publish(SettingsStore.Label.AutoLogoutTimeChanged(minutes))
                }
            }

            // Наблюдаем за изменениями настроек профиля
            scope.launch {
                profileSettings.usernameFlow.collectLatest { username ->
                    dispatch(Msg.UpdateUsername(username))
                    dispatch(Msg.UpdateProfileComplete(profileSettings.isProfileComplete()))
                    val email = state().email
                    publish(SettingsStore.Label.ProfileChanged(username, email))
                }
            }

            scope.launch {
                profileSettings.emailFlow.collectLatest { email ->
                    dispatch(Msg.UpdateEmail(email))
                    dispatch(Msg.UpdateProfileComplete(profileSettings.isProfileComplete()))
                    val username = state().username
                    publish(SettingsStore.Label.ProfileChanged(username, email))
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

        override fun executeIntent(intent: SettingsStore.Intent) {
            when (intent) {
                is SettingsStore.Intent.Init -> {
                    dispatch(Msg.LoadData)
                    loadAllSettings()
                    setupObservers()
                }

                is SettingsStore.Intent.UpdateTheme -> {
                    scope.launch {
                        appearanceSettings.saveTheme(intent.theme)
                        // Применяем тему немедленно через label
                        publish(SettingsStore.Label.ThemeChanged(intent.theme))
                        // Обновляем состояние хранилища
                        dispatch(Msg.UpdateTheme(intent.theme))
                    }
                }

                is SettingsStore.Intent.UpdateLanguage -> {
                    scope.launch {
                        appearanceSettings.saveLang(intent.language)
                        // Применяем язык немедленно через label
                        publish(SettingsStore.Label.LanguageChanged(intent.language))
                        // Обновляем состояние хранилища
                        dispatch(Msg.UpdateLanguage(intent.language))
                    }
                }

                is SettingsStore.Intent.UpdateBlackBackground -> {
                    scope.launch {
                        appearanceSettings.saveBlackBackground(intent.enabled)
                        // Применяем изменение немедленно через label
                        publish(SettingsStore.Label.BlackBackgroundChanged(intent.enabled))
                        // Обновляем состояние хранилища
                        dispatch(Msg.UpdateBlackBackground(intent.enabled))
                    }
                }

                is SettingsStore.Intent.UpdateStarrySky -> {
                    scope.launch {
                        appearanceSettings.saveStarrySky(intent.enabled)
                        // Применяем изменение немедленно через label
                        publish(SettingsStore.Label.StarrySkyChanged(intent.enabled))
                        // Обновляем состояние хранилища
                        dispatch(Msg.UpdateStarrySky(intent.enabled))
                    }
                }

                is SettingsStore.Intent.UpdateServerUrl -> {
                    scope.launch {
                        networkSettings.saveServerUrl(intent.url)
                        // Применяем изменение немедленно через label
                        publish(SettingsStore.Label.ServerUrlChanged(intent.url))
                        // Обновляем состояние хранилища
                        dispatch(Msg.UpdateServerUrl(intent.url))
                    }
                }

                is SettingsStore.Intent.UpdateUseBiometric -> {
                    scope.launch {
                        securitySettings.saveUseBiometric(intent.enabled)
                        // Применяем изменение немедленно через label
                        publish(SettingsStore.Label.BiometricChanged(intent.enabled))
                        // Обновляем состояние хранилища
                        dispatch(Msg.UpdateUseBiometric(intent.enabled))
                    }
                }

                is SettingsStore.Intent.UpdateAutoLogoutTime -> {
                    scope.launch {
                        securitySettings.saveAutoLogoutTime(intent.minutes)
                        // Применяем изменение немедленно через label
                        publish(SettingsStore.Label.AutoLogoutTimeChanged(intent.minutes))
                        // Обновляем состояние хранилища
                        dispatch(Msg.UpdateAutoLogoutTime(intent.minutes))
                    }
                }

                is SettingsStore.Intent.Back -> {
                    // Обработка в компоненте
                }

                // Обработка интентов профиля
                is SettingsStore.Intent.UpdateUsername -> {
                    scope.launch {
                        profileSettings.saveUsername(intent.username)
                        dispatch(Msg.UpdateUsername(intent.username))
                        dispatch(Msg.UpdateProfileComplete(profileSettings.isProfileComplete()))
                        val email = state().email
                        publish(SettingsStore.Label.ProfileChanged(intent.username, email))
                    }
                }

                is SettingsStore.Intent.UpdateEmail -> {
                    scope.launch {
                        profileSettings.saveEmail(intent.email)
                        dispatch(Msg.UpdateEmail(intent.email))
                        dispatch(Msg.UpdateProfileComplete(profileSettings.isProfileComplete()))
                        val username = state().username
                        publish(SettingsStore.Label.ProfileChanged(username, intent.email))
                    }
                }

                is SettingsStore.Intent.UpdateAvatarUrl -> {
                    scope.launch {
                        profileSettings.saveAvatarUrl(intent.url)
                        dispatch(Msg.UpdateAvatarUrl(intent.url))
                    }
                }

                is SettingsStore.Intent.UpdateStatus -> {
                    scope.launch {
                        profileSettings.saveStatus(intent.status)
                        dispatch(Msg.UpdateStatus(intent.status))
                    }
                }

                is SettingsStore.Intent.UpdateProfilePublic -> {
                    scope.launch {
                        profileSettings.setProfilePublic(intent.isPublic)
                        dispatch(Msg.UpdateProfilePublic(intent.isPublic))
                    }
                }

                is SettingsStore.Intent.UpdateProfileNotifications -> {
                    scope.launch {
                        profileSettings.setProfileNotifications(intent.enabled)
                        dispatch(Msg.UpdateProfileNotifications(intent.enabled))
                    }
                }

                is SettingsStore.Intent.ClearProfileData -> {
                    scope.launch {
                        profileSettings.clearProfileData()
                        loadProfileSettings()
                    }
                }

                // Network settings
                is SettingsStore.Intent.UpdateUseExperimentalApi -> {
                    scope.launch {
                        networkSettings.saveUseExperimentalApi(intent.enabled)
                        dispatch(Msg.UpdateUseExperimentalApi(intent.enabled))
                    }
                }

                is SettingsStore.Intent.UpdateEnableBandwidthLimit -> {
                    scope.launch {
                        networkSettings.saveEnableBandwidthLimit(intent.enabled)
                        dispatch(Msg.UpdateEnableBandwidthLimit(intent.enabled))
                    }
                }

                is SettingsStore.Intent.UpdateBandwidthLimitKbps -> {
                    scope.launch {
                        networkSettings.saveBandwidthLimitKbps(intent.kbps)
                        dispatch(Msg.UpdateBandwidthLimitKbps(intent.kbps))
                    }
                }

                is SettingsStore.Intent.UpdateUseCustomTimeouts -> {
                    scope.launch {
                        networkSettings.saveUseCustomTimeouts(intent.enabled)
                        dispatch(Msg.UpdateUseCustomTimeouts(intent.enabled))
                    }
                }

                is SettingsStore.Intent.UpdateConnectionTimeoutSeconds -> {
                    scope.launch {
                        networkSettings.saveConnectionTimeoutSeconds(intent.seconds)
                        dispatch(Msg.UpdateConnectionTimeoutSeconds(intent.seconds))
                    }
                }

                is SettingsStore.Intent.UpdateReadTimeoutSeconds -> {
                    scope.launch {
                        networkSettings.saveReadTimeoutSeconds(intent.seconds)
                        dispatch(Msg.UpdateReadTimeoutSeconds(intent.seconds))
                    }
                }

                // Common actions
                is SettingsStore.Intent.ResetToDefaults -> {
                    scope.launch {
//                        appearanceSettings.resetToDefaults()
//                        networkSettings.resetToDefaults()
//                        securitySettings.resetToDefaults()
//                        profileSettings.resetToDefaults()
                        loadAllSettings()
                    }
                }

                else -> {
                    // Обработка других интентов
                }
            }
        }
    }

    private object ReducerImpl : Reducer<SettingsStore.State, Msg> {
        override fun SettingsStore.State.reduce(msg: Msg): SettingsStore.State =
            when (msg) {
                Msg.LoadData -> copy(loading = false)
                Msg.LoadingData -> copy(loading = true)
                Msg.SettingsApplied -> copy(settingsApplied = true)
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

                // Network settings
                is Msg.UpdateUseExperimentalApi -> copy(useExperimentalApi = msg.enabled)
                is Msg.UpdateEnableBandwidthLimit -> copy(enableBandwidthLimit = msg.enabled)
                is Msg.UpdateBandwidthLimitKbps -> copy(bandwidthLimitKbps = msg.kbps)
                is Msg.UpdateUseCustomTimeouts -> copy(useCustomTimeouts = msg.enabled)
                is Msg.UpdateConnectionTimeoutSeconds -> copy(connectionTimeoutSeconds = msg.seconds)
                is Msg.UpdateReadTimeoutSeconds -> copy(readTimeoutSeconds = msg.seconds)

                else -> this
            }
    }
}
