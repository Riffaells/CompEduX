package client

import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import model.auth.NetworkAuthResponse
import model.auth.NetworkRefreshTokenRequest
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

            // Настройка базового URL для всех запросов
            defaultRequest {
                // Будет установлен базовый URL
                url {
                    // URL будет получен из NetworkConfig
                }

                // Настройка заголовков по умолчанию
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Accept, ContentType.Application.Json)
            }

            // Настройка перехвата ошибок и повторных попыток запросов
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }

            // Обработка ошибок HTTP
            HttpResponseValidator {
                validateResponse { response ->
                    val statusCode = response.status.value

                    if (statusCode >= 400) {
                        when (statusCode) {
                            401 -> throw ClientRequestException(response, "Неавторизованный доступ")
                            403 -> throw ClientRequestException(response, "Доступ запрещен")
                            404 -> throw ClientRequestException(response, "Ресурс не найден")
                            in 500..599 -> throw ClientRequestException(response, "Ошибка сервера")
                            else -> throw ClientRequestException(response, "Ошибка клиента")
                        }
                    }
                }
            }

            // В будущем здесь можно добавить:
            // - Поддержку Bearer токенов через Auth плагин
            // - Обработку JWT и refresh токенов
            // - Дополнительные механизмы безопасности
        }
    }

    /**
     * Вспомогательные функции для определения типа ошибки
     */
    companion object {
        fun isUnauthorized(response: HttpResponse): Boolean = response.status.value == 401
        fun isForbidden(response: HttpResponse): Boolean = response.status.value == 403
        fun isNotFound(response: HttpResponse): Boolean = response.status.value == 404
        fun isServerError(response: HttpResponse): Boolean = response.status.value in 500..599
    }
}
