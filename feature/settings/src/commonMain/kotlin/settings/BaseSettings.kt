package settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Базовый класс для реализаций настроек
 *
 * // TODO: Добавить поддержку шифрования чувствительных настроек
 * // TODO: Реализовать механизм миграции настроек при обновлении приложения
 * // TODO: Добавить поддержку групп настроек для более сложной организации
 */
abstract class BaseSettings(protected val settings: Settings) {

    /**
     * Класс для управления настройкой любого типа
     *
     * // TODO: Добавить поддержку валидации значений перед сохранением
     * // TODO: Реализовать механизм кэширования для уменьшения обращений к хранилищу
     * // TODO: Добавить поддержку истории изменений для возможности отката
     */
    protected class Setting<T>(
        private val key: String,
        private val defaultValue: T,
        private val settings: Settings,
        private val get: (Settings, String, T) -> T,
        private val put: (Settings, String, T) -> Unit
    ) {
        private val _flow = MutableStateFlow(get(settings, key, defaultValue))
        val flow: StateFlow<T> = _flow

        fun save(value: T) {
            // TODO: Добавить логирование изменений настроек
            put(settings, key, value)
            _flow.value = value
        }

        fun reset() {
            save(defaultValue)
        }

        fun getCurrentValue(): T = _flow.value
    }

    /**
     * Создает настройку целочисленного типа
     */
    protected fun createIntSetting(key: String, defaultValue: Int): Setting<Int> {
        return Setting(
            key = key,
            defaultValue = defaultValue,
            settings = settings,
            get = Settings::getInt,
            put = Settings::putInt
        )
    }

    /**
     * Создает настройку строкового типа
     */
    protected fun createStringSetting(key: String, defaultValue: String): Setting<String> {
        return Setting(
            key = key,
            defaultValue = defaultValue,
            settings = settings,
            get = Settings::getString,
            put = Settings::putString
        )
    }

    /**
     * Создает настройку логического типа
     */
    protected fun createBooleanSetting(key: String, defaultValue: Boolean): Setting<Boolean> {
        return Setting(
            key = key,
            defaultValue = defaultValue,
            settings = settings,
            get = Settings::getBoolean,
            put = Settings::putBoolean
        )
    }

    /**
     * Создает настройку типа Float
     */
    protected fun createFloatSetting(key: String, defaultValue: Float): Setting<Float> {
        return Setting(
            key = key,
            defaultValue = defaultValue,
            settings = settings,
            get = Settings::getFloat,
            put = Settings::putFloat
        )
    }

    /**
     * Создает настройку типа Long
     */
    protected fun createLongSetting(key: String, defaultValue: Long): Setting<Long> {
        return Setting(
            key = key,
            defaultValue = defaultValue,
            settings = settings,
            get = Settings::getLong,
            put = Settings::putLong
        )
    }

    /**
     * Создает настройку типа Double
     */
    protected fun createDoubleSetting(key: String, defaultValue: Double): Setting<Double> {
        return Setting(
            key = key,
            defaultValue = defaultValue,
            settings = settings,
            get = Settings::getDouble,
            put = Settings::putDouble
        )
    }

    /**
     * Создает настройку для хранения списка строк, разделенных запятыми
     *
     * // TODO: Реализовать более эффективную сериализацию списков с поддержкой экранирования запятых
     */
    protected fun createStringListSetting(
        key: String,
        defaultValue: List<String> = emptyList()
    ): Setting<List<String>> {
        return Setting(
            key = key,
            defaultValue = defaultValue,
            settings = settings,
            get = { s, k, d ->
                val value = s.getString(k, "")
                if (value.isEmpty()) d else value.split(",")
            },
            put = { s, k, v ->
                s.putString(k, v.joinToString(","))
            }
        )
    }

    // TODO: Добавить поддержку сложных типов данных через JSON-сериализацию
    // TODO: Реализовать поддержку настроек с ограниченным временем жизни (TTL)

    /**
     * Сбрасывает все настройки
     */
    fun clearAll() {
        settings.clear()
    }

    /**
     * Проверяет, содержит ли настройки указанный ключ
     */
    fun contains(key: String): Boolean {
        return settings.hasKey(key)
    }

    /**
     * Удаляет настройку по ключу
     */
    fun remove(key: String) {
        settings.remove(key)
    }

    // TODO: Добавить метод для экспорта всех настроек в JSON
    // TODO: Добавить метод для импорта настроек из JSON
    // TODO: Реализовать механизм резервного копирования и восстановления настроек
}
