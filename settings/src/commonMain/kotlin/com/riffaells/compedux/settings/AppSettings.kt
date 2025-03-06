package com.riffaells.compedux.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Application settings manager that provides access to user preferences
 */
class AppSettings(private val settings: Settings) {
////    private val flowSettings: FlowSettings = settings.toFlowSettings()
//
//    companion object {
//        private const val KEY_DARK_MODE = "dark_mode"
//        private const val KEY_USERNAME = "username"
//    }
//
//    // Dark mode settings
//    var isDarkMode: Boolean
//        get() = settings.getBoolean(KEY_DARK_MODE, false)
//        set(value) = settings.putBoolean(KEY_DARK_MODE, value)
//
//    fun isDarkModeFlow(): Flow<Boolean> =
//        flowSettings.getBooleanFlow(KEY_DARK_MODE, false)
//
//    // Username settings
//    var username: String
//        get() = settings.getString(KEY_USERNAME, "")
//        set(value) = settings.putString(KEY_USERNAME, value)
//
//    fun usernameFlow(): Flow<String> =
//        flowSettings.getStringFlow(KEY_USERNAME, "")
//
//    // Example of a derived flow
//    fun isUsernameSetFlow(): Flow<Boolean> =
//        usernameFlow().map { it.isNotBlank() }
}
