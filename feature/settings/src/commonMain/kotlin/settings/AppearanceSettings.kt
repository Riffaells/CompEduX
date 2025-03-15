package settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для доступа к настройкам внешнего вида приложения
 */
interface AppearanceSettings {
    /**
     * Тема приложения
     */
    val themeFlow: StateFlow<Int>
    fun saveTheme(value: Int)

    /**
     * Язык приложения
     */
    val langFlow: StateFlow<String>
    fun saveLang(value: String)

    /**
     * Отображение звездного неба
     */
    val starrySkyFlow: StateFlow<Boolean>
    fun saveStarrySky(value: Boolean)

    /**
     * Черный фон
     */
    val blackBackgroundFlow: StateFlow<Boolean>
    fun saveBlackBackground(value: Boolean)

    /**
     * Константы для темы
     */
    object ThemeOption {
        const val KEY = "THEME_OPTION"
        const val THEME_LIGHT = 1
        const val THEME_DARK = 0
        const val THEME_SYSTEM = -1
        const val DEFAULT = THEME_SYSTEM
    }
}

/**
 * Реализация интерфейса AppearanceSettings
 */
internal class AppearanceSettingsImpl(settings: Settings) : BaseSettings(settings), AppearanceSettings {

    private val theme = createIntSetting(
        key = AppearanceSettings.ThemeOption.KEY,
        defaultValue = AppearanceSettings.ThemeOption.DEFAULT
    )

    override val themeFlow: StateFlow<Int> get() = theme.flow
    override fun saveTheme(value: Int) = theme.save(value)

    private val lang = createStringSetting(
        key = "LANGUAGE_WORD_OPTION",
        defaultValue = "ru"
    )

    override val langFlow: StateFlow<String> get() = lang.flow
    override fun saveLang(value: String) = lang.save(value)

    private val starrySky = createBooleanSetting(
        key = "STARRY_SKY_OPTION",
        defaultValue = false
    )

    override val starrySkyFlow: StateFlow<Boolean> get() = starrySky.flow
    override fun saveStarrySky(value: Boolean) = starrySky.save(value)

    private val blackBackground = createBooleanSetting(
        key = "BLACK_BACKGROUND_OPTION",
        defaultValue = false
    )

    override val blackBackgroundFlow: StateFlow<Boolean> get() = blackBackground.flow
    override fun saveBlackBackground(value: Boolean) = blackBackground.save(value)
}
