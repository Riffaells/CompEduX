package di

import adapter.NetworkConfigImpl
import com.russhwolf.settings.Settings
import config.NetworkConfig
import org.kodein.di.*
import settings.*


val settingsModule = DI.Module("settingsModule") {

    bindSingleton<Settings>() {
        Settings()
    }

    bindSingleton<NetworkConfig>() {
        NetworkConfigImpl(instance())
    }

    bindSingleton<AppearanceSettings> {
        AppearanceSettingsImpl(instance())

    }

    bindSingleton<NetworkSettings>() {
        NetworkSettingsImpl(instance())
    }

    bindSingleton<SystemSettings>() {
        SystemSettingsImpl(instance())
    }

    bindSingleton<SecuritySettings>() {
        SecuritySettingsImpl(instance())
    }

    bindSingleton<NotificationSettings>() {
        NotificationSettingsImpl(instance())
    }

    bindSingleton<ProfileSettings>() {
        ProfileSettingsImpl(instance())
    }

    bindSingleton<MultiplatformSettings>() {
        MultiplatformSettingsImpl(
            Settings()
        )
    }
}
