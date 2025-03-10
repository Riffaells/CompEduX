import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.*

interface MultiplatformSettings {


    val themeFlow: StateFlow<Int>
    fun saveThemeSettings(value: Int)

    val langFlow: StateFlow<String>
    fun saveLangSettings(value: String)

    val starrySkyFlow: StateFlow<Boolean>
    fun saveStarrySkySettings(value: Boolean)

    val blackBackgroundFlow: StateFlow<Boolean>
    fun saveBlackBackgroundSettings(value: Boolean)

    val versionFlow: StateFlow<Int>
    fun saveVersionSettings(value: Int)

    val serverUrlFlow: StateFlow<String>
    fun saveServerUrlSettings(value: String)

    object ThemeOption {
        const val KEY = "THEME_OPTION"
        const val THEME_LIGHT = 1
        const val THEME_DARK = 0
        const val THEME_SYSTEM = -1
        const val DEFAULT = THEME_SYSTEM
    }
}

internal class MultiplatformSettingsImpl(private val settings: Settings) : MultiplatformSettings {

    private class Setting<T>(
        private val key: String,
        private val defaultValue: T,
        private val settings: Settings,
        private val get: (Settings, String, T) -> T,
        private val put: (Settings, String, T) -> Unit
    ) {
        private val _flow = MutableStateFlow(get(settings, key, defaultValue))
        val flow: StateFlow<T> = _flow

        fun save(value: T) {
            put(settings, key, value)
            _flow.value = value
        }
    }

    private val theme = Setting(
        key = MultiplatformSettings.ThemeOption.KEY,
        defaultValue = MultiplatformSettings.ThemeOption.DEFAULT,
        settings = settings,
        get = Settings::getInt,
        put = Settings::putInt
    )

    override val themeFlow: StateFlow<Int> get() = theme.flow
    override fun saveThemeSettings(value: Int) = theme.save(value)

    private val lang = Setting(
        key = "LANGUAGE_WORD_OPTION",
        defaultValue = "ru",
        settings = settings,
        get = Settings::getString,
        put = Settings::putString
    )

    override val langFlow: StateFlow<String> get() = lang.flow
    override fun saveLangSettings(value: String) = lang.save(value)

    private val starrySky = Setting(
        key = "STARRY_SKY_OPTION",
        defaultValue = false,
        settings = settings,
        get = Settings::getBoolean,
        put = Settings::putBoolean
    )

    override val starrySkyFlow: StateFlow<Boolean> get() = starrySky.flow
    override fun saveStarrySkySettings(value: Boolean) = starrySky.save(value)

    private val blackBackground = Setting(
        key = "BLACK_BACKGROUND_OPTION",
        defaultValue = false,
        settings = settings,
        get = Settings::getBoolean,
        put = Settings::putBoolean
    )

    override val blackBackgroundFlow: StateFlow<Boolean> get() = blackBackground.flow
    override fun saveBlackBackgroundSettings(value: Boolean) = blackBackground.save(value)

    private val version = Setting(
        key = "APP_VERSION",
        defaultValue = 0,
        settings = settings,
        get = Settings::getInt,
        put = Settings::putInt
    )

    override val versionFlow: StateFlow<Int> get() = version.flow
    override fun saveVersionSettings(value: Int) = version.save(value)

    private val serverUrl = Setting(
        key = "SERVER_URL_OPTION",
        defaultValue = "https://api.example.com",
        settings = settings,
        get = Settings::getString,
        put = Settings::putString
    )

    override val serverUrlFlow: StateFlow<String> get() = serverUrl.flow
    override fun saveServerUrlSettings(value: String) = serverUrl.save(value)
}
