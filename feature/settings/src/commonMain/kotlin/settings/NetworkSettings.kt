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
     * Использование пользовательских тайм-аутов
     */
    val useCustomTimeoutsFlow: StateFlow<Boolean>

    /**
     * Тайм-аут соединения (в секундах)
     */
    val connectionTimeoutSecondsFlow: StateFlow<Int>

    /**
     * Тайм-аут чтения (в секундах)
     */
    val readTimeoutSecondsFlow: StateFlow<Int>

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

    /**
     * Сохраняет настройку использования пользовательских тайм-аутов
     */
    fun saveUseCustomTimeouts(value: Boolean)

    /**
     * Сохраняет тайм-аут соединения в секундах
     */
    fun saveConnectionTimeoutSeconds(value: Int)

    /**
     * Сохраняет тайм-аут чтения в секундах
     */
    fun saveReadTimeoutSeconds(value: Int)
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

    private val useCustomTimeouts = createBooleanSetting(
        key = "USE_CUSTOM_TIMEOUTS",
        defaultValue = false
    )

    private val connectionTimeoutSeconds = createIntSetting(
        key = "CONNECTION_TIMEOUT_SECONDS",
        defaultValue = 30 // 30 секунд по умолчанию
    )

    private val readTimeoutSeconds = createIntSetting(
        key = "READ_TIMEOUT_SECONDS",
        defaultValue = 60 // 60 секунд по умолчанию
    )

    override val serverUrlFlow: StateFlow<String> get() = serverUrl.flow
    override fun saveServerUrl(value: String) = serverUrl.save(value)

    override val useExperimentalApiFlow: StateFlow<Boolean> get() = useExperimentalApi.flow
    override fun saveUseExperimentalApi(value: Boolean) = useExperimentalApi.save(value)

    override val enableBandwidthLimitFlow: StateFlow<Boolean> get() = enableBandwidthLimit.flow
    override fun saveEnableBandwidthLimit(value: Boolean) = enableBandwidthLimit.save(value)

    override val bandwidthLimitKbpsFlow: StateFlow<Int> get() = bandwidthLimitKbps.flow
    override fun saveBandwidthLimitKbps(value: Int) = bandwidthLimitKbps.save(value)

    override val useCustomTimeoutsFlow: StateFlow<Boolean> get() = useCustomTimeouts.flow
    override fun saveUseCustomTimeouts(value: Boolean) = useCustomTimeouts.save(value)

    override val connectionTimeoutSecondsFlow: StateFlow<Int> get() = connectionTimeoutSeconds.flow
    override fun saveConnectionTimeoutSeconds(value: Int) = connectionTimeoutSeconds.save(value)

    override val readTimeoutSecondsFlow: StateFlow<Int> get() = readTimeoutSeconds.flow
    override fun saveReadTimeoutSeconds(value: Int) = readTimeoutSeconds.save(value)
}
