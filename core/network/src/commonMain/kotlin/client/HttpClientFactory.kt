package client

import config.NetworkConfig
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.header
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import repository.mapper.ErrorMapper

/**
 * Фабрика для создания HTTP клиента
 */
class HttpClientFactory(
    private val json: Json,
    private val errorMapper: ErrorMapper,
    private val networkConfig: NetworkConfig
) {
    /**
     * Создает HTTP клиент с настроенными плагинами
     */
    fun create(): HttpClient {
        return HttpClient {
            // Конфигурация логирования
            install(Logging) {
                level = LogLevel.ALL
            }

            // Конфигурация таймаутов
            install(HttpTimeout) {
                requestTimeoutMillis = 30000 // 30 секунд на запрос
                connectTimeoutMillis = 15000 // 15 секунд на соединение
                socketTimeoutMillis = 15000 // 15 секунд на сокет
            }

            // Конфигурация сериализации JSON
            install(ContentNegotiation) {
                json(json)
            }

            // Настройка заголовков по умолчанию для всех запросов
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }

            // Здесь можно добавить дополнительные плагины, например:
            // - Аутентификацию с Bearer токеном
            // - Обработку ошибок
            // - Перенаправления
        }
    }
}
