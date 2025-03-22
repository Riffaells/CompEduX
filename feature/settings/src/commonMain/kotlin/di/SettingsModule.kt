package di

import adapter.NetworkConfigImpl
import com.russhwolf.settings.Settings
import config.NetworkConfig
import org.kodein.di.*
import settings.*

/**
 * Модуль зависимостей для настроек приложения
 */
val settingsModule = DI.Module("settingsModule") {
    // Конфигурация сети, реализация NetworkConfig через адаптер
    bindSingleton<NetworkConfig>() {
        NetworkConfigImpl(instance())
    }

    // Настройки внешнего вида
    bindSingleton<AppearanceSettings> {
        AppearanceSettingsImpl(instance())

    }

    // Настройки сети
    bindSingleton<NetworkSettings>() {
        NetworkSettingsImpl(instance())
    }

    // Системные настройки
    bindSingleton<SystemSettings>() {
        SystemSettingsImpl(instance())
    }

    // Настройки безопасности
    bindSingleton<SecuritySettings>() {
        SecuritySettingsImpl(instance())
    }

    // Настройки уведомлений
    bindSingleton<NotificationSettings>() {
        NotificationSettingsImpl(instance())
    }


    // Общий класс доступа ко всем настройкам
    bindSingleton<MultiplatformSettings>() {
        MultiplatformSettingsImpl(
            Settings()
        )
    }
}
