package settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для доступа к системным настройкам приложения
 */
interface SystemSettings {
    /**
     * Версия приложения
     */
    val versionFlow: StateFlow<Int>
    fun saveVersion(value: Int)
}

/**
 * Реализация интерфейса SystemSettings
 */
internal class SystemSettingsImpl(settings: Settings) : BaseSettings(settings), SystemSettings {

    private val version = createIntSetting(
        key = "APP_VERSION",
        defaultValue = 0
    )

    override val versionFlow: StateFlow<Int> get() = version.flow
    override fun saveVersion(value: Int) = version.save(value)
}
