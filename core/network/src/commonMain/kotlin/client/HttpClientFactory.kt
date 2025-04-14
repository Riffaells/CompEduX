package client

import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.json.Json
import logging.Logger
import platform.Platform
import platform.PlatformInfo
import com.riffaells.compedux.BuildConfig

/**
 * Фабрика для создания HTTP клиента
 */
class HttpClientFactory(
    private val json: Json,
    private val tokenStorage: TokenStorage,
    private val networkConfig: NetworkConfig,
    private val logger: Logger
) {
    /**
     * Создает HTTP клиент с настроенными плагинами
     */
    fun create(): HttpClient {
        return HttpClient {
            // Устанавливаем таймауты
            install(HttpTimeout) {
                connectTimeoutMillis = 15000 // 15 секунд на соединение
                requestTimeoutMillis = 30000 // 30 секунд на запрос
                socketTimeoutMillis = 15000 // 15 секунд на сокет
            }

            // Настройка логирования
            install(Logging) {
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        this@HttpClientFactory.logger.d(message)
                    }
                }
                level = LogLevel.HEADERS
            }

            // Настройка сериализации/десериализации JSON
            install(ContentNegotiation) {
                json(json)
            }

            // Настройка аутентификации Bearer
            install(Auth) {
                bearer {
                    loadTokens {
                        val accessToken = tokenStorage.getAccessToken()
                        val refreshToken = tokenStorage.getRefreshToken()

                        if (accessToken != null && refreshToken != null) {
                            BearerTokens(
                                accessToken = accessToken,
                                refreshToken = refreshToken
                            )
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        val refreshToken = tokenStorage.getRefreshToken() ?: return@refreshTokens null

                        try {
                            // В реальном приложении здесь должен быть запрос к API для обновления токена
                            // Это лишь заглушка
                            BearerTokens(
                                accessToken = "refreshed_access_token",
                                refreshToken = refreshToken
                            )
                        } catch (e: Exception) {
                            logger.e("Failed to refresh token", e)
                            null
                        }
                    }
                }
            }

            // Обработка ошибок
            HttpResponseValidator {
                validateResponse { response ->
                    val statusCode = response.status.value

                    if (statusCode >= 400) {
                        when (statusCode) {
                            401 -> throw ClientRequestException(response, "Unauthorized")
                            403 -> throw ClientRequestException(response, "Forbidden")
                            404 -> throw ClientRequestException(response, "Not Found")
                            422 -> throw ClientRequestException(response, "Unprocessable Entity - Invalid request data")
                            in 500..599 -> throw ServerResponseException(response, "Server Error")
                            else -> throw ResponseException(response, "HTTP Error $statusCode")
                        }
                    }
                }
            }

            // Установка базового URL и заголовков для всех запросов
            defaultRequest {
                // Всегда устанавливаем правильный Content-Type для JSON
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)

                // Добавляем общие заголовки, которые повторялись в каждом запросе
                header("X-App-Version", BuildConfig.APP_VERSION.toString())
                header("X-App-Name", BuildConfig.APP_NAME)
                header("User-Agent", Platform.userAgent(BuildConfig.APP_NAME, BuildConfig.APP_VERSION.toString()))

                // Добавляем заголовки с информацией о приложении
                header("X-Client-Platform", getPlatformName())
                header("X-Client-App", BuildConfig.APP_NAME)
                header("X-Client-Version", BuildConfig.APP_VERSION)
                header("X-Client-Build", BuildConfig.BUILD_TIMESTAMP.toString())
            }
        }
    }

    /**
     * Gets platform name for request headers
     * Uses the Platform utility from utils module
     * @return String representing current platform
     */
    private fun getPlatformName(): String {
        return try {
            platform.Platform.name()
        } catch (e: Exception) {
            logger.e("Failed to get platform information", e)
            "${BuildConfig.APP_NAME}-${BuildConfig.APP_VERSION}"
        }
    }
}
