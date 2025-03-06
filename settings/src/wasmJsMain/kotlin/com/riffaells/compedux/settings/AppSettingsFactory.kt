package com.riffaells.compedux.settings

import com.russhwolf.settings.StorageSettings

/**
 * WASM implementation of AppSettingsFactory
 */
actual class AppSettingsFactory {
    /**
     * Creates an instance of AppSettings using localStorage
     */
    actual fun createSettings(): AppSettings {
        val settings = StorageSettings()
        return AppSettings(settings)
    }
}
