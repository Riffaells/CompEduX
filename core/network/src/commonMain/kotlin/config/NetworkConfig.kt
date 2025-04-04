package config

/**
 * Интерфейс конфигурации сетевого уровня
 */
interface NetworkConfig {
    /**
     * Получить базовый URL API
     * @return URL сервера
     */
    suspend fun getBaseUrl(): String

    /**
     * Получить версию API
     * @return строка с версией API (например, "v1")
     */
    suspend fun getApiVersion(): String
}
