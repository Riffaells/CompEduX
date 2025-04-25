package api

import api.auth.NetworkAuthApi
import client.safeSendWithErrorBody
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.delay
import kotlinx.io.IOException
import logging.Logger
import mapper.toDomain
import model.DomainError
import model.DomainResult
import model.UserDomain
import model.auth.*
import model.user.NetworkUserResponse
import kotlin.math.pow
import kotlin.random.Random

/**
 * Implementation of NetworkAuthApi that uses Ktor HttpClient
 * to perform API requests
 */
class NetworkAuthApiImpl(
    private val client: HttpClient,
    private val networkConfig: NetworkConfig,
    private val logger: Logger
) : NetworkAuthApi {

    /**
     * Maximum number of retry attempts for network operations
     */
    private val maxRetries = 3

    /**
     * Base delay in milliseconds for retry backoff strategy
     */
    private val baseRetryDelayMs = 1000L

    /**
     * Jitter factor for random jitter in retry backoff strategy
     */
    private val jitterFactor = 0.25

    /**
     * Gets the full API URL from configuration
     * @return complete API base URL as string
     */
    private suspend fun getApiUrl(): String {
        return networkConfig.getFullApiUrl()
    }

    /**
     * Processes API exceptions and converts them to appropriate DomainError
     *
     * @param e The exception to process
     * @param errorTag A tag to identify the error context in logs
     * @return DomainResult.Error with appropriate error details
     */
    private fun processApiException(e: Exception, errorTag: String): DomainResult.Error {
        logger.e("$errorTag error", e)
        return when (e) {
            is ClientRequestException -> DomainResult.Error(
                DomainError.fromServerCode(
                    serverCode = e.response.status.value,
                    message = e.message,
                    details = null
                )
            )
            is ServerResponseException -> DomainResult.Error(
                DomainError.serverError(
                    message = "error_server_unavailable",
                    details = e.message
                )
            )
            is IOException, is ConnectTimeoutException, is SocketTimeoutException -> DomainResult.Error(
                DomainError.networkError(
                    message = "error_network_connectivity",
                    details = e.message
                )
            )
            else -> DomainResult.Error(
                DomainError.unknownError(
                    message = "error_unknown",
                    details = e.message
                )
            )
        }
    }

    /**
     * Executes a network operation with retry mechanism for handling transient errors.
     * Uses exponential backoff strategy with random jitter to avoid thundering herd problem.
     *
     * @param T The expected return type of the operation.
     * @param operation The suspend function to execute with retry capability.
     * @return The result of the operation if successful.
     */
    private suspend inline fun <T> executeWithRetry(
        crossinline operation: suspend () -> DomainResult<T>
    ): DomainResult<T> {
        var attempt = 0
        var lastException: Exception? = null

        while (attempt < maxRetries) {
            try {
                // Execute the operation and return result if successful
                return operation()
            } catch (e: CancellationException) {
                // Don't retry if the coroutine was cancelled
                throw e
            } catch (e: ClientRequestException) {
                // Don't retry for client errors (4xx) as they are typically not transient
                if (e.response.status.value !in 408..499) {
                    logger.e("Non-retryable client error: ${e.message}", e)
                    return DomainResult.Error(
                        DomainError.fromServerCode(
                            serverCode = e.response.status.value,
                            message = e.message,
                            details = null
                        )
                    )
                }
                lastException = e
                logger.w("Client error detected. Will retry.", e)
            } catch (e: ServerResponseException) {
                // Server errors (5xx) may be transient, so we'll retry
                lastException = e
                logger.w("Server error detected. Will retry.", e)
            } catch (e: HttpRequestTimeoutException) {
                // Timeout errors are typically transient, so we'll retry
                lastException = e
                logger.w("Request timeout detected. Will retry.", e)
            } catch (e: IOException) {
                // I/O errors (network issues) are typically transient, so we'll retry
                lastException = e
                logger.w("Network connectivity issue detected: ${e.message}. Will retry.", e)
            } catch (e: ConnectTimeoutException) {
                // Connection timeout, likely connectivity issue
                lastException = e
                logger.w("Connection timeout detected. Will retry.", e)
            } catch (e: SocketTimeoutException) {
                // Socket timeout, likely connectivity issue
                lastException = e
                logger.w("Socket timeout detected. Will retry.", e)
            } catch (e: Exception) {
                // For any other unexpected exceptions
                logger.e("Unexpected error during network operation: ${e.message}", e)
                return DomainResult.Error(
                    DomainError.unknownError(
                        message = "error_unknown",
                        details = e.message
                    )
                )
            }

            // Log the retry attempt
            attempt++
            if (attempt < maxRetries) {
                // Calculate delay with exponential backoff and jitter
                val jitter = Random.nextDouble(-jitterFactor, jitterFactor)
                val delayWithJitter =
                    (baseRetryDelayMs * (2.0.pow(attempt.toDouble())) + (baseRetryDelayMs * jitter)).toLong()
                logger.d("Retry attempt $attempt/$maxRetries after $delayWithJitter ms")
                delay(delayWithJitter)
            } else {
                logger.e("All retry attempts failed", lastException)
            }
        }

        // If we've exhausted all retries, create appropriate error
        val error = when (lastException) {
            is IOException, is ConnectTimeoutException, is SocketTimeoutException, is HttpRequestTimeoutException -> {
                logger.e("Network connectivity issue persisted after all retries", lastException)
                DomainError.networkError(
                    message = "error_network_connectivity",
                    details = lastException?.message
                )
            }

            is ServerResponseException -> {
                logger.e("Server error persisted after all retries", lastException)
                DomainError.serverError(
                    message = "error_server_unavailable",
                    details = lastException.message
                )
            }

            is ClientRequestException -> {
                logger.e("Client error persisted after all retries", lastException)
                DomainError.fromServerCode(
                    serverCode = lastException.response.status.value,
                    message = lastException.message,
                    details = null
                )
            }

            else -> {
                logger.e("Unknown error type persisted after all retries", lastException)
                DomainError.unknownError(
                    message = "error_unknown_network",
                    details = lastException?.message
                )
            }
        }

        // Return error result
        return DomainResult.Error(error)
    }

    /**
     * Registers a new user in the system
     *
     * @param request Domain model containing registration information (username, email, password)
     * @return DomainResult with auth tokens on success or error details on failure
     */
    override suspend fun register(request: RegisterRequestDomain): DomainResult<AuthResponseDomain> {
        val apiUrl = getApiUrl()

        // Упрощаем метод с помощью расширения safeSendWithErrorBody
        return client.safeSendWithErrorBody<NetworkAuthResponse, NetworkErrorResponse>(
            {
                url("$apiUrl/auth/register")
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                setBody(
                    NetworkRegisterRequest(
                        username = request.username,
                        email = request.email,
                        password = request.password
                    )
                )

                // Log request details
                logger.d("Registering user: ${request.email}")
            },
            logger,
            { errorResponse ->
                // Convert error response to domain error
                logger.w("Registration failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            // Log success if operation succeeded
            it.onSuccess {

                logger.i("User registered successfully: ${request.email}")
            }
        }.map { networkResponse ->
            // Convert network response to domain model
            networkResponse.toDomain()
        }
    }

    /**
     * Authenticates user with email and password
     *
     * @param request Domain model containing login credentials (email, password)
     * @return DomainResult with auth tokens on success or error details on failure
     */
    override suspend fun login(request: LoginRequestDomain): DomainResult<AuthResponseDomain> {
        val apiUrl = getApiUrl()

        // Используем расширение для упрощения метода
        return client.safeSendWithErrorBody<NetworkAuthResponse, NetworkErrorResponse>(
            {
                url("$apiUrl/auth/login")
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                setBody(
                    NetworkLoginRequest(
                        email = request.email,
                        password = request.password
                    )
                )

                // Log request details
                logger.d("Logging in user: ${request.email}")
            },
            logger,
            { errorResponse ->
                // Convert error response to domain error
                logger.w("Login failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            // Log success if operation succeeded
            if (it is DomainResult.Success) {
                logger.i("User logged in successfully: ${request.email}")
            }
        }.map { networkResponse ->
            // Convert network response to domain model
            networkResponse.toDomain()
        }
    }

    /**
     * Refreshes an expired access token using refresh token
     *
     * @param request Domain model containing refresh token
     * @return DomainResult with new auth tokens on success or error details on failure
     */
    override suspend fun refreshToken(request: RefreshTokenRequestDomain): DomainResult<AuthResponseDomain> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                // Подготовка запроса в формате API
                val networkRequest = NetworkRefreshTokenRequest(
                    refreshToken = request.refreshToken
                )

                // Выполнение запроса
                logger.d("Refreshing token")
                val response = client.post("$apiUrl/auth/refresh") {
                    contentType(ContentType.Application.Json)
                    setBody(networkRequest)
                }

                if (response.status.isSuccess()) {
                    // Парсинг успешного ответа
                    val authResponse = response.body<NetworkAuthResponse>()
                    logger.i("Token refreshed successfully")
                    DomainResult.Success(authResponse.toDomain())
                } else {
                    // Обработка ошибки
                    val errorResponse = response.body<NetworkErrorResponse>()
                    logger.w("Token refresh failed: ${errorResponse.getErrorMessage()}")
                    DomainResult.Error(
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    )
                }
            } catch (e: Exception) {
                processApiException(e, "Refresh token")
            }
        }
    }

    /**
     * Verifies access token and retrieves user information
     *
     * @param token Access token to verify
     * @return DomainResult with user data on success or error details on failure
     */
    override suspend fun verifyToken(token: String): DomainResult<UserDomain> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                // Выполнение запроса
                logger.d("Verifying token")
                val response = client.get("$apiUrl/auth/verify") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }

                if (response.status.isSuccess()) {
                    // Парсинг успешного ответа
                    val userResponse = response.body<NetworkUserResponse>()
                    logger.i("Token verified successfully for user: ${userResponse.username}")
                    DomainResult.Success(userResponse.toDomain())
                } else {
                    // Обработка ошибки
                    val errorResponse = response.body<NetworkErrorResponse>()
                    logger.w("Token verification failed: ${errorResponse.getErrorMessage()}")
                    DomainResult.Error(
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    )
                }
            } catch (e: Exception) {
                processApiException(e, "Token verification")
            }
        }
    }

    /**
     * Retrieves current authenticated user's information
     *
     * @param token Access token for authentication
     * @return DomainResult with user data on success or error details on failure
     */
    override suspend fun getCurrentUser(token: String): DomainResult<UserDomain> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                // Выполнение запроса
                logger.d("Getting current user info")
                val response = client.get("$apiUrl/auth/me") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }

                if (response.status.isSuccess()) {
                    // Парсинг успешного ответа
                    val userResponse = response.body<NetworkUserResponse>()
                    logger.i("User info retrieved successfully: ${userResponse.username}")
                    DomainResult.Success(userResponse.toDomain())
                } else {
                    // Обработка ошибки
                    val errorResponse = response.body<NetworkErrorResponse>()
                    logger.w("Get user info failed: ${errorResponse.getErrorMessage()}")
                    DomainResult.Error(
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    )
                }
            } catch (e: Exception) {
                processApiException(e, "Get current user")
            }
        }
    }

    /**
     * Ends user session and invalidates tokens
     *
     * @param token Access token to invalidate
     * @return DomainResult with success status or error details on failure
     */
    override suspend fun logout(token: String): DomainResult<Unit> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                // Выполнение запроса
                logger.d("Logging out user")
                val response = client.post("$apiUrl/auth/logout") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }

                if (response.status.isSuccess()) {
                    // Парсинг успешного ответа с сообщением
                    val logoutResponse = response.body<NetworkLogoutResponse>()
                    logger.i("User logged out successfully: ${logoutResponse.message}")
                    DomainResult.Success(Unit)
                } else {
                    // Обработка ошибки - если токен невалидный (401), считаем что пользователь уже разлогинен
                    if (response.status.value == 401) {
                        logger.i("Token was already invalid, considered logged out")
                        return@executeWithRetry DomainResult.Success(Unit)
                    }

                    // Обработка других ошибок
                    val errorResponse = response.body<NetworkErrorResponse>()
                    logger.w("Logout failed: ${errorResponse.getErrorMessage()}")
                    DomainResult.Error(
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    )
                }
            } catch (e: Exception) {
                processApiException(e, "Logout")
            }
        }
    }

    /**
     * Checks if server is available and returns its status
     *
     * @return DomainResult with server status information or error details on failure
     */
    override suspend fun checkServerStatus(): DomainResult<ServerStatusResponseDomain> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                // Выполнение запроса
                logger.d("Checking server status")
                val response = client.get("$apiUrl/status")

                if (response.status.isSuccess()) {
                    // Парсинг успешного ответа
                    val statusResponse = response.body<NetworkServerStatusResponse>()
                    logger.i("Server status: ${statusResponse.status}, version: ${statusResponse.version}")
                    DomainResult.Success(statusResponse.toDomain())
                } else {
                    // Обработка ошибки
                    val errorResponse = response.body<NetworkErrorResponse>()
                    logger.w("Check server status failed: ${errorResponse.getErrorMessage()}")
                    DomainResult.Error(
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    )
                }
            } catch (e: Exception) {
                processApiException(e, "Check server status")
            }
        }
    }

    /**
     * Updates user profile information
     *
     * @param token Access token for authentication
     * @param username New username to set
     * @return DomainResult with updated user data or error details on failure
     */
    override suspend fun updateProfile(token: String, username: String): DomainResult<UserDomain> {
        return executeWithRetry {
            try {
                val apiUrl = getApiUrl()

                // Подготовка запроса в формате API
                // Важно: для предотвращения ошибки 422 используем data class вместо Map
                data class UpdateProfileRequest(val username: String)

                val requestBody = UpdateProfileRequest(username)

                // Выполнение запроса
                logger.d("Updating profile: $username")
                val response = client.put("$apiUrl/users/me") {
                    contentType(ContentType.Application.Json)
                    header(HttpHeaders.Authorization, "Bearer $token")
                    setBody(requestBody)
                }

                if (response.status.isSuccess()) {
                    // Парсинг успешного ответа
                    val userResponse = response.body<NetworkUserResponse>()
                    logger.i("Profile updated successfully: ${userResponse.username}")
                    DomainResult.Success(userResponse.toDomain())
                } else {
                    // Обработка ошибки
                    val errorResponse = response.body<NetworkErrorResponse>()
                    logger.w("Update profile failed: ${errorResponse.getErrorMessage()}")
                    DomainResult.Error(
                        DomainError.fromServerCode(
                            serverCode = errorResponse.getErrorCode(),
                            message = errorResponse.getErrorMessage(),
                            details = errorResponse.details
                        )
                    )
                }
            } catch (e: Exception) {
                processApiException(e, "Update profile")
            }
        }
    }
}
