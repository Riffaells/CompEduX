package com.riffaells.compedux.settings

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Android implementation of AppSettingsFactory
 */
actual class AppSettingsFactory(private val context: Context) {
    /**
     * Creates an instance of AppSettings using Android's SharedPreferences
     */
    actual fun createSettings(): AppSettings {
        val sharedPreferences = context.getSharedPreferences("compedux_settings", Context.MODE_PRIVATE)
        val settings = SharedPreferencesSettings(sharedPreferences)
        return AppSettings(settings)
    }
}
