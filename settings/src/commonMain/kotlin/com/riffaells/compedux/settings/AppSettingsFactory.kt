package com.riffaells.compedux.settings

import com.russhwolf.settings.Settings

/**
 * Factory for creating AppSettings instances
 */
expect class AppSettingsFactory() {
    /**
     * Creates an instance of AppSettings
     */
    fun createSettings(): AppSettings
}
