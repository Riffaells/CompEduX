package config

import kotlinx.coroutines.flow.StateFlow

/**
 * Интерфейс конфигурации сети
 * Предоставляет доступ к настройкам сети, таким как базовый URL API
 */
interface NetworkConfig {
    /**
     * Поток базового URL для API
     * Изменяется, когда пользователь меняет настройки
     */
    val baseUrlFlow: StateFlow<String>

    /**
     * Получает текущий базовый URL
     * Приостанавливающая функция, которая дожидается первого значения из потока
     * @return текущий базовый URL
     */
    suspend fun getBaseUrl(): String

    /**
     * Получает версию API
     * @return строка с версией API (например, "v1")
     */
    suspend fun getApiVersion(): String

    /**
     * Получает полный URL API (baseUrl + apiVersion)
     * @return полный URL API
     */
    suspend fun getFullApiUrl(): String {
        val baseUrl = getBaseUrl()
        val apiVersion = getApiVersion()
        return "$baseUrl/$apiVersion"
    }

    /**
     * Получает таймаут соединения в миллисекундах
     * @return таймаут соединения
     */
    suspend fun getConnectionTimeoutMillis(): Long

    /**
     * Получает таймаут чтения в миллисекундах
     * @return таймаут чтения
     */
    suspend fun getReadTimeoutMillis(): Long
}
