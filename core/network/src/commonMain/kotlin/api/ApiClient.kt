package api

import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * Базовый клиент API для выполнения сетевых запросов
 */
class ApiClient(
    private val networkConfig: NetworkConfig
) {
    /**
     * Создает и настраивает HTTP клиент с необходимыми плагинами
     */
    fun createHttpClient(): HttpClient {
        return HttpClient {
            // Настройка обработки ошибок
            install(HttpTimeout) {
                requestTimeoutMillis = 30000
                connectTimeoutMillis = 15000
                socketTimeoutMillis = 15000
            }

            // Логирование запросов и ответов
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }

            // Сериализация/десериализация JSON
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // Настройка заголовков по умолчанию
            defaultRequest {
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }
        }
    }

    /**
     * Получает базовый URL сервера из настроек
     */
    suspend fun getBaseUrl(): String {
        return networkConfig.getBaseUrl()
    }

    /**
     * Добавляет токен авторизации к запросу
     */
    fun HttpRequestBuilder.withAuth(token: String) {
        headers {
            append(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}
