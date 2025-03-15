package settings

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс для доступа к сетевым настройкам приложения
 */
interface NetworkSettings {
    /**
     * URL сервера
     */
    val serverUrlFlow: StateFlow<String>

    /**
     * Сохраняет URL сервера
     */
    fun saveServerUrl(value: String)
}

/**
 * Реализация интерфейса NetworkSettings
 */
internal class NetworkSettingsImpl(settings: Settings) : BaseSettings(settings), NetworkSettings {

    private val serverUrl = createStringSetting(
        key = "SERVER_URL_OPTION",
        defaultValue = "https://api.example.com"
    )

    override val serverUrlFlow: StateFlow<String> get() = serverUrl.flow
    override fun saveServerUrl(value: String) = serverUrl.save(value)
}
