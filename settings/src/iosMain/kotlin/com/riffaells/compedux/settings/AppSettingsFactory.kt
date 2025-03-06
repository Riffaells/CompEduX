package com.riffaells.compedux.settings

import com.russhwolf.settings.NSUserDefaultsSettings
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of AppSettingsFactory
 */
actual class AppSettingsFactory {
    /**
     * Creates an instance of AppSettings using NSUserDefaults
     */
    actual fun createSettings(): AppSettings {
        val userDefaults = NSUserDefaults.standardUserDefaults
        val settings = NSUserDefaultsSettings(userDefaults)
        return AppSettings(settings)
    }
}
