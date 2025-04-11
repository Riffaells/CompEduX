package api

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
                contentType(ContentType.Application.Json)
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
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            if (response.status.isSuccess()) {
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
    override suspend fun checkServerStatus(): DomainResult<ServerStatusDomain> {
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
            val requestBody = mapOf("username" to username)

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
        logger.e("API Error: ${e.message}")

        return when (e) {
            is io.ktor.client.plugins.ClientRequestException -> {
                when (e.response.status.value) {
                    401 -> DomainError.authError("Необходима авторизация")
                    403 -> DomainError.authError("Доступ запрещен")
                    404 -> DomainError(
                        code = model.ErrorCode.NOT_FOUND,
                        message = "Ресурс не найден",
                        details = e.message
                    )
                    else -> DomainError(
                        code = model.ErrorCode.UNKNOWN_ERROR,
                        message = "Ошибка запроса: ${e.message}",
                        details = e.stackTraceToString()
                    )
                }
            }
            is io.ktor.client.plugins.ServerResponseException -> {
                DomainError.serverError("Ошибка сервера: ${e.message}")
            }
            is io.ktor.client.plugins.RedirectResponseException -> {
                DomainError(
                    code = model.ErrorCode.UNKNOWN_ERROR,
                    message = "Перенаправление: ${e.message}",
                    details = e.stackTraceToString()
                )
            }
            is kotlinx.coroutines.TimeoutCancellationException -> {
                DomainError(
                    code = model.ErrorCode.TIMEOUT,
                    message = "Превышено время ожидания",
                    details = e.message
                )
            }
            is java.net.UnknownHostException -> {
                DomainError.networkError("Не удается подключиться к серверу", e.message)
            }
            else -> {
                DomainError.unknownError("Неизвестная ошибка: ${e.message}", e.stackTraceToString())
            }
        }
    }
}
