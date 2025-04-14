package config

import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import settings.NetworkSettings

/**
 * Реализация интерфейса конфигурации сети,
 * использующая NetworkSettings для получения настроек
 */
class DataNetworkConfig(
    override val di: DI
) : NetworkConfig, DIAware {

    private val networkSettings by instance<NetworkSettings>()

    /**
     * Получаем поток базового URL из настроек
     */
    override val baseUrlFlow: StateFlow<String>
        get() = networkSettings.serverUrlFlow

    /**
     * Получаем текущий базовый URL
     */
    override suspend fun getBaseUrl(): String {
        return networkSettings.serverUrlFlow.value
    }

    /**
     * Версия API
     * В будущем можно добавить настройку для выбора версии API
     */
    override suspend fun getApiVersion(): String {
        return "api/v1"
    }

    /**
     * Получаем таймаут соединения
     */
    override suspend fun getConnectionTimeoutMillis(): Long {
        return if (networkSettings.useCustomTimeoutsFlow.value) {
            networkSettings.connectionTimeoutSecondsFlow.value * 1000L
        } else {
            DEFAULT_CONNECTION_TIMEOUT
        }
    }

    /**
     * Получаем таймаут чтения
     */
    override suspend fun getReadTimeoutMillis(): Long {
        return if (networkSettings.useCustomTimeoutsFlow.value) {
            networkSettings.readTimeoutSecondsFlow.value * 1000L
        } else {
            DEFAULT_READ_TIMEOUT
        }
    }

    companion object {
        private const val DEFAULT_CONNECTION_TIMEOUT = 30_000L // 30 секунд
        private const val DEFAULT_READ_TIMEOUT = 60_000L // 60 секунд
    }
}
