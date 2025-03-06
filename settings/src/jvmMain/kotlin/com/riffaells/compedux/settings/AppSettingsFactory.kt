package com.riffaells.compedux.settings

import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

/**
 * JVM implementation of AppSettingsFactory
 */
actual class AppSettingsFactory {
    /**
     * Creates an instance of AppSettings using Java Preferences API
     */
    actual fun createSettings(): AppSettings {
        val preferences = Preferences.userRoot().node("com.riffaells.compedux")
        val settings = PreferencesSettings(preferences)
        return AppSettings(settings)
    }
}
