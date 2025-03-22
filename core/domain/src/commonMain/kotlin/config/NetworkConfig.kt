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
}
