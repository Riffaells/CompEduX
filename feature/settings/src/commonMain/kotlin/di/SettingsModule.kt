package di

import com.russhwolf.settings.Settings
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import settings.*


val settingsModule = DI.Module("settingsModule") {

    bindSingleton<Settings>() {
        Settings()
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
