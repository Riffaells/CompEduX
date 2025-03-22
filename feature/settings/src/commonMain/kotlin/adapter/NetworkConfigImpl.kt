package adapter

import config.NetworkConfig
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import settings.NetworkSettings

/**
 * Реализация интерфейса NetworkConfig, которая адаптирует NetworkSettings
 * Это адаптер, который преобразует NetworkSettings из feature/settings в NetworkConfig для core/domain
 */
class NetworkConfigImpl(
    private val networkSettings: NetworkSettings
) : NetworkConfig {
    /**
     * Поток базового URL для API
     * Делегируется к serverUrlFlow из NetworkSettings
     */
    override val baseUrlFlow: StateFlow<String> = networkSettings.serverUrlFlow

    /**
     * Получает текущий базовый URL
     * @return текущий базовый URL из потока serverUrlFlow
     */
    override suspend fun getBaseUrl(): String = baseUrlFlow.first()
}
