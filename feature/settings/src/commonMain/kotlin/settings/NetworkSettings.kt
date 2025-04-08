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
     * Использование экспериментального API
     */
    val useExperimentalApiFlow: StateFlow<Boolean>

    /**
     * Включение ограничения пропускной способности
     */
    val enableBandwidthLimitFlow: StateFlow<Boolean>

    /**
     * Ограничение пропускной способности (Кбит/с)
     */
    val bandwidthLimitKbpsFlow: StateFlow<Int>

    /**
     * Сохраняет URL сервера
     */
    fun saveServerUrl(value: String)

    /**
     * Сохраняет настройку использования экспериментального API
     */
    fun saveUseExperimentalApi(value: Boolean)

    /**
     * Сохраняет настройку ограничения пропускной способности
     */
    fun saveEnableBandwidthLimit(value: Boolean)

    /**
     * Сохраняет ограничение пропускной способности в Кбит/с
     */
    fun saveBandwidthLimitKbps(value: Int)
}

/**
 * Реализация интерфейса NetworkSettings
 */
internal class NetworkSettingsImpl(settings: Settings) : BaseSettings(settings), NetworkSettings {

    private val serverUrl = createStringSetting(
        key = "SERVER_URL_OPTION",
        defaultValue = "https://api.example.com"
    )

    private val useExperimentalApi = createBooleanSetting(
        key = "USE_EXPERIMENTAL_API",
        defaultValue = false
    )

    private val enableBandwidthLimit = createBooleanSetting(
        key = "ENABLE_BANDWIDTH_LIMIT",
        defaultValue = false
    )

    private val bandwidthLimitKbps = createIntSetting(
        key = "BANDWIDTH_LIMIT_KBPS",
        defaultValue = 5000 // 5 Мбит/с по умолчанию
    )

    override val serverUrlFlow: StateFlow<String> get() = serverUrl.flow
    override fun saveServerUrl(value: String) = serverUrl.save(value)

    override val useExperimentalApiFlow: StateFlow<Boolean> get() = useExperimentalApi.flow
    override fun saveUseExperimentalApi(value: Boolean) = useExperimentalApi.save(value)

    override val enableBandwidthLimitFlow: StateFlow<Boolean> get() = enableBandwidthLimit.flow
    override fun saveEnableBandwidthLimit(value: Boolean) = enableBandwidthLimit.save(value)

    override val bandwidthLimitKbpsFlow: StateFlow<Int> get() = bandwidthLimitKbps.flow
    override fun saveBandwidthLimitKbps(value: Int) = bandwidthLimitKbps.save(value)
}
