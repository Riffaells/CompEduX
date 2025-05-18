package client

// Import necessary classes for network error handling
import com.riffaells.compedux.BuildConfig
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import logging.Logger
import model.auth.NetworkAuthResponse
import model.auth.NetworkRefreshTokenRequest
import platform.Platform

/**
 * Factory for creating HTTP client
 */
class HttpClientFactory(
    private val json: Json,
    private val tokenStorage: TokenStorage,
    private val networkConfig: NetworkConfig,
    private val logger: Logger
) {
    /**
     * Error keys for network error handling
     */
    private object ErrorKeys {
        const val CONNECTION_ERROR = "error_connection"
        const val TIMEOUT_ERROR = "error_timeout"
        const val UNAUTHORIZED = "error_unauthorized"
        const val FORBIDDEN = "error_forbidden"
        const val NOT_FOUND = "error_not_found"
        const val VALIDATION_ERROR = "error_validation"
        const val SERVER_ERROR = "error_server"
        const val UNKNOWN_ERROR = "error_unknown"
    }

    /**
     * Creates HTTP client with configured plugins
     */
    fun create(): HttpClient {
        return HttpClient {
            // Set timeouts from configuration
            install(HttpTimeout) {
                connectTimeoutMillis = 15000 // 15 секунд на соединение
                requestTimeoutMillis = 30000 // 30 секунд на запрос
                socketTimeoutMillis = 15000 // 15 секунд на сокет
            }

            // Configure logging
            install(Logging) {
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        this@HttpClientFactory.logger.d(message)
                    }
                }
                level = LogLevel.HEADERS
            }

            // Configure JSON serialization/deserialization
            install(ContentNegotiation) {
                json(json)
            }

            // Configure Bearer authentication
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
                            // Get base API URL - now it's a suspend function call in a suspend context
                            val baseApiUrl = networkConfig.getFullApiUrl()

                            // Execute token refresh request
                            val response = client.post("$baseApiUrl/auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(NetworkRefreshTokenRequest(refreshToken = refreshToken))
                            }

                            if (response.status.isSuccess()) {
                                // Get new tokens from response
                                val authResponse = response.body<NetworkAuthResponse>()

                                // Save new tokens to storage
                                tokenStorage.saveAccessToken(authResponse.accessToken)
                                tokenStorage.saveRefreshToken(authResponse.refreshToken)

                                // Return new tokens for auth mechanism
                                BearerTokens(
                                    accessToken = authResponse.accessToken,
                                    refreshToken = authResponse.refreshToken
                                )
                            } else {
                                // On refresh error - clear tokens and exit
                                logger.e("Failed to refresh token: ${response.status}")
                                tokenStorage.clearTokens()
                                null
                            }
                        } catch (e: Exception) {
                            logger.e("Failed to refresh token", e)
                            tokenStorage.clearTokens()
                            null
                        }
                    }
                }
            }

            // Enhanced error handling
            install(HttpCallValidator) {
                validateResponse { response ->
                    val statusCode = response.status.value

                    when (statusCode) {
                        in 200..299 -> Unit // Success range, do nothing
                        401 -> throw ClientRequestException(
                            response,
                            ErrorKeys.UNAUTHORIZED
                        )

                        403 -> throw ClientRequestException(
                            response,
                            ErrorKeys.FORBIDDEN
                        )

                        404 -> throw ClientRequestException(
                            response,
                            ErrorKeys.NOT_FOUND
                        )

                        422 -> throw ClientRequestException(
                            response,
                            ErrorKeys.VALIDATION_ERROR
                        )

                        in 500..599 -> throw ServerResponseException(
                            response,
                            ErrorKeys.SERVER_ERROR
                        )
                    }
                }

                handleResponseExceptionWithRequest { exception, request ->
                    // Enhanced logging with request details
                    val requestMethod = request.method.value
                    val requestUrl = request.url.toString()

                    // Get appropriate error key based on exception type
                    val errorKey = when (exception) {
                        is ConnectTimeoutException -> ErrorKeys.CONNECTION_ERROR
                        is SocketTimeoutException -> ErrorKeys.TIMEOUT_ERROR
                        is HttpRequestTimeoutException -> ErrorKeys.TIMEOUT_ERROR
                        is IOException -> ErrorKeys.CONNECTION_ERROR
                        else -> exception.message ?: ErrorKeys.UNKNOWN_ERROR
                    }

                    logger.e("[Network Error] $requestMethod $requestUrl - $errorKey", exception)

                    // Transform specific exception types for better handling up the stack
                    when (exception) {
                        is ConnectTimeoutException,
                        is SocketTimeoutException,
                        is HttpRequestTimeoutException ->
                            throw IOException(ErrorKeys.CONNECTION_ERROR, exception)

                        else -> throw exception
                    }
                }
            }

            // Set base URL and headers for all requests
            defaultRequest {
                // Always set the correct Content-Type for JSON
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)

                // Add common headers that were repeated in each request
                header("X-App-Version", BuildConfig.APP_VERSION.toString())
                header("X-App-Name", BuildConfig.APP_NAME)
                header("User-Agent", Platform.userAgent(BuildConfig.APP_NAME, BuildConfig.APP_VERSION.toString()))

                // Add headers with application information
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
