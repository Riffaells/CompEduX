package api

import api.auth.NetworkAuthApi
import com.riffaells.compedux.BuildConfig
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import logging.Logger
import mapper.toDomain
import model.DomainError
import model.DomainResult
import model.UserDomain
import model.auth.*
import model.auth.NetworkLoginRequest
import model.auth.NetworkRefreshTokenRequest
import model.auth.NetworkRegisterRequest
import model.auth.NetworkServerStatusResponse
import model.user.NetworkUserResponse
import platform.Platform

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
     * Gets the full API URL from configuration
     * @return complete API base URL as string
     */
    private suspend fun getApiUrl(): String {
        return networkConfig.getFullApiUrl()
    }

    /**
     * Registers a new user in the system
     *
     * @param request Domain model containing registration information (username, email, password)
     * @return DomainResult with auth tokens on success or error details on failure
     */
    override suspend fun register(request: RegisterRequestDomain): DomainResult<AuthResponseDomain> {
        return try {
            val apiUrl = getApiUrl()

            // Подготовка запроса в формате API
            val networkRequest = NetworkRegisterRequest(
                username = request.username,
                email = request.email,
                password = request.password
            )

            // Выполнение запроса
            logger.d("Registering user: ${request.email}")
            val response = client.post("$apiUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(networkRequest)
            }

            if (response.status.isSuccess()) {
                // Парсинг успешного ответа
                val authResponse = response.body<NetworkAuthResponse>()
                logger.i("User registered successfully: ${request.email}")
                DomainResult.Success(authResponse.toDomain())
            } else {
                // Обработка ошибки
                val errorResponse = response.body<NetworkErrorResponse>()
                logger.w("Registration failed: ${errorResponse.message}")
                DomainResult.Error(
                    DomainError.fromServerCode(
                        serverCode = errorResponse.code,
                        message = errorResponse.message,
                        details = errorResponse.details
                    )
                )
            }
        } catch (e: Exception) {
            logger.e("Register error", e)
            DomainResult.Error(handleException(e))
        }
    }

    /**
     * Authenticates user with email and password
     *
     * @param request Domain model containing login credentials (email, password)
     * @return DomainResult with auth tokens on success or error details on failure
     */
    override suspend fun login(request: LoginRequestDomain): DomainResult<AuthResponseDomain> {
        return try {
            val apiUrl = getApiUrl()

            // Подготовка запроса в формате API
            val networkRequest = NetworkLoginRequest(
                email = request.email,
                password = request.password
            )

            // Выполнение запроса
            logger.d("Logging in user: ${request.email}")
            val response = client.post("$apiUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(networkRequest)
            }

            if (response.status.isSuccess()) {
                // Парсинг успешного ответа
                val authResponse = response.body<NetworkAuthResponse>()
                logger.i("User logged in successfully: ${request.email}")
                DomainResult.Success(authResponse.toDomain())
            } else {
                // Обработка ошибки
                val errorResponse = response.body<NetworkErrorResponse>()
                logger.w("Login failed: ${errorResponse.message}")
                DomainResult.Error(
                    DomainError.fromServerCode(
                        serverCode = errorResponse.code,
                        message = errorResponse.message,
                        details = errorResponse.details
                    )
                )
            }
        } catch (e: Exception) {
            logger.e("Login error", e)
            DomainResult.Error(handleException(e))
        }
    }

    /**
     * Refreshes an expired access token using refresh token
     *
     * @param request Domain model containing refresh token
     * @return DomainResult with new auth tokens on success or error details on failure
     */
    override suspend fun refreshToken(request: RefreshTokenRequestDomain): DomainResult<AuthResponseDomain> {
        return try {
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
                logger.w("Token refresh failed: ${errorResponse.message}")
                DomainResult.Error(
                    DomainError.fromServerCode(
                        serverCode = errorResponse.code,
                        message = errorResponse.message,
                        details = errorResponse.details
                    )
                )
            }
        } catch (e: Exception) {
            logger.e("Refresh token error", e)
            DomainResult.Error(handleException(e))
        }
    }

    /**
     * Retrieves current authenticated user's information
     *
     * @param token Access token for authentication
     * @return DomainResult with user data on success or error details on failure
     */
    override suspend fun getCurrentUser(token: String): DomainResult<UserDomain> {
        return try {
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
                logger.w("Get user info failed: ${errorResponse.message}")
                DomainResult.Error(
                    DomainError.fromServerCode(
                        serverCode = errorResponse.code,
                        message = errorResponse.message,
                        details = errorResponse.details
                    )
                )
            }
        } catch (e: Exception) {
            logger.e("Get current user error", e)
            DomainResult.Error(handleException(e))
        }
    }

    /**
     * Ends user session and invalidates tokens
     *
     * @param token Access token to invalidate
     * @return DomainResult with success status or error details on failure
     */
    override suspend fun logout(token: String): DomainResult<Unit> {
        return try {
            val apiUrl = getApiUrl()

            // Выполнение запроса
            logger.d("Logging out user")
            val response = client.post("$apiUrl/auth/logout") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            if (response.status.isSuccess()) {
                // Если HTTP-статус успешный, считаем операцию успешной
                logger.i("User logged out successfully")
                DomainResult.Success(Unit)
            } else {
                // Обработка ошибки
                val errorResponse = response.body<NetworkErrorResponse>()
                logger.w("Logout failed: ${errorResponse.message}")
                DomainResult.Error(
                    DomainError.fromServerCode(
                        serverCode = errorResponse.code,
                        message = errorResponse.message,
                        details = errorResponse.details
                    )
                )
            }
        } catch (e: Exception) {
            logger.e("Logout error", e)
            DomainResult.Error(handleException(e))
        }
    }

    /**
     * Checks if server is available and returns its status
     *
     * @return DomainResult with server status information or error details on failure
     */
    override suspend fun checkServerStatus(): DomainResult<ServerStatusResponseDomain> {
        return try {
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
                logger.w("Check server status failed: ${errorResponse.message}")
                DomainResult.Error(
                    DomainError.fromServerCode(
                        serverCode = errorResponse.code,
                        message = errorResponse.message,
                        details = errorResponse.details
                    )
                )
            }
        } catch (e: Exception) {
            logger.e("Check server status error", e)
            DomainResult.Error(handleException(e))
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
        return try {
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
                logger.w("Update profile failed: ${errorResponse.message}")
                DomainResult.Error(
                    DomainError.fromServerCode(
                        serverCode = errorResponse.code,
                        message = errorResponse.message,
                        details = errorResponse.details
                    )
                )
            }
        } catch (e: Exception) {
            logger.e("Update profile error", e)
            DomainResult.Error(handleException(e))
        }
    }

    /**
     * Processes exceptions and converts them to domain-specific error models
     *
     * @param e The exception that occurred during API communication
     * @return A domain error model representing the exception
     */
    private fun handleException(e: Exception): DomainError {
        logger.e("API Error in ${BuildConfig.APP_NAME} v${BuildConfig.APP_VERSION}: ${e.message}")

        return when (e) {
            is io.ktor.client.plugins.ClientRequestException -> {
                when (e.response.status.value) {
                    401 -> DomainError(
                        code = model.ErrorCode.UNAUTHORIZED,
                        message = "auth_error_unauthorized",
                        details = e.message
                    )
                    403 -> DomainError(
                        code = model.ErrorCode.FORBIDDEN,
                        message = "auth_error_forbidden",
                        details = e.message
                    )
                    404 -> DomainError(
                        code = model.ErrorCode.NOT_FOUND,
                        message = "error_resource_not_found",
                        details = e.message
                    )
                    422 -> DomainError(
                        code = model.ErrorCode.VALIDATION_ERROR,
                        message = "error_validation",
                        details = e.message
                    )
                    else -> DomainError(
                        code = model.ErrorCode.UNKNOWN_ERROR,
                        message = "error_request",
                        details = e.stackTraceToString()
                    )
                }
            }
            is io.ktor.client.plugins.ServerResponseException -> {
                DomainError(
                    code = model.ErrorCode.SERVER_ERROR,
                    message = "error_server",
                    details = e.message
                )
            }
            is io.ktor.client.plugins.RedirectResponseException -> {
                DomainError(
                    code = model.ErrorCode.UNKNOWN_ERROR,
                    message = "error_redirect",
                    details = e.stackTraceToString()
                )
            }
            is kotlinx.coroutines.TimeoutCancellationException -> {
                DomainError(
                    code = model.ErrorCode.TIMEOUT,
                    message = "error_timeout",
                    details = e.message
                )
            }
            else -> {
                // Универсальная проверка сообщения об ошибке для работы на всех платформах
                val errorMessage = e.message ?: "Unknown error"

                // Проверяем сообщение об ошибке на наличие ключевых слов и определяем код ошибки
                val (errorCode, errorKey) = when {
                    errorMessage.contains("Connection refused", ignoreCase = true) ->
                        Pair(model.ErrorCode.CONNECTION_REFUSED, "error_connection_refused")
                    errorMessage.contains("Failed to connect", ignoreCase = true) ->
                        Pair(model.ErrorCode.CONNECTION_FAILED, "error_connection_failed")
                    errorMessage.contains("Connection reset", ignoreCase = true) ->
                        Pair(model.ErrorCode.CONNECTION_RESET, "error_connection_reset")
                    errorMessage.contains("connect", ignoreCase = true) ->
                        Pair(model.ErrorCode.CONNECTION_FAILED, "error_connection_problem")
                    errorMessage.contains("timeout", ignoreCase = true) ->
                        Pair(model.ErrorCode.TIMEOUT, "error_connection_timeout")
                    errorMessage.contains("socket", ignoreCase = true) ->
                        Pair(model.ErrorCode.SOCKET_ERROR, "error_socket")
                    errorMessage.contains("host", ignoreCase = true) ->
                        Pair(model.ErrorCode.HOST_UNREACHABLE, "error_host_unreachable")
                    errorMessage.contains("network", ignoreCase = true) ->
                        Pair(model.ErrorCode.NETWORK_ERROR, "error_network")
                    else ->
                        Pair(model.ErrorCode.UNKNOWN_ERROR, "error_unknown")
                }

                DomainError(
                    code = errorCode,
                    message = errorKey,
                    details = errorMessage
                )
            }
        }
    }
}
