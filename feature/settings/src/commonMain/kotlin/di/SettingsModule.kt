package di

import settings.MultiplatformSettings
import settings.MultiplatformSettingsImpl
import com.russhwolf.settings.Settings
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import settings.AppearanceSettings
import settings.NetworkSettings
import settings.SystemSettings
import settings.SecuritySettings
import settings.NotificationSettings
import settings.StorageSettings
import settings.ProfileSettings

/**
 * Модуль для предоставления настроек приложения
 *
 * // TODO: Добавить поддержку разных реализаций Settings для разных платформ
 * // TODO: Реализовать возможность выбора хранилища настроек (файл, БД, облако)
 * // TODO: Добавить поддержку шифрованного хранилища для чувствительных настроек
 */
val settingsModule = DI.Module("settingsModule") {
    // Основной объект настроек
    bindSingleton {
        try {
            // Используем стандартную реализацию Settings
            Settings()
        } catch (e: Exception) {
            println("Error creating Settings: ${e.message}")
            e.printStackTrace()
            // Создаем резервную реализацию в случае ошибки
            Settings()
        }
    }

    // Главный интерфейс настроек
    bindSingleton<MultiplatformSettings> {
        try {
            MultiplatformSettingsImpl(instance())
        } catch (e: Exception) {
            println("Error creating MultiplatformSettings: ${e.message}")
            e.printStackTrace()
            // Создаем резервную реализацию в случае ошибки
            MultiplatformSettingsImpl(Settings())
        }
    }

    // Отдельные категории настроек для прямого доступа
    bindSingleton<AppearanceSettings> {
        try {
            instance<MultiplatformSettings>().appearance
        } catch (e: Exception) {
            println("Error accessing appearance settings: ${e.message}")
            e.printStackTrace()
            // Создаем резервную реализацию в случае ошибки
            MultiplatformSettingsImpl(Settings()).appearance
        }
    }

    bindSingleton<NetworkSettings> { instance<MultiplatformSettings>().network }
    bindSingleton<SystemSettings> { instance<MultiplatformSettings>().system }
    bindSingleton<SecuritySettings> { instance<MultiplatformSettings>().security }
    bindSingleton<NotificationSettings> { instance<MultiplatformSettings>().notifications }
    bindSingleton<StorageSettings> { instance<MultiplatformSettings>().storage }
    bindSingleton<ProfileSettings> { instance<MultiplatformSettings>().profile }

    // TODO: Добавить фабрику для создания настроек с разными префиксами ключей
    // TODO: Реализовать поддержку профилей настроек для разных пользователей
    // TODO: Добавить возможность динамической регистрации новых категорий настроек
}
