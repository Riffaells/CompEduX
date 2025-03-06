package com.compedu.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings

/**
 * Класс для работы с настройками приложения
 */
class AppSettings(private val settings: Settings) {
//    private val flowSettings: FlowSettings = settings.toFlowSettings()

    companion object {
        private const val KEY_THEME = "theme"
        private const val KEY_LANGUAGE = "language"

        // Значения по умолчанию
        private const val DEFAULT_THEME = "system"
        private const val DEFAULT_LANGUAGE = "system"
    }

    // Геттеры и сеттеры для настроек

    var theme: String
        get() = settings.getString(KEY_THEME, DEFAULT_THEME)
        set(value) = settings.putString(KEY_THEME, value)

    var language: String
        get() = settings.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE)
        set(value) = settings.putString(KEY_LANGUAGE, value)

    // Метод для сброса всех настроек
    fun clearAll() {
        settings.clear()
    }
}
