package api.auth

import api.dto.AuthResponseDto
import api.dto.UserResponse
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import model.ApiError
import model.AppError
import model.AuthResult
import model.ErrorCode
import model.User
import model.auth.LoginRequest
import model.auth.RefreshTokenRequest
import model.auth.RegisterRequest
import model.auth.ServerStatusResponse
import kotlinx.serialization.json.*

/**
 * Функция-расширение для конвертации UserResponse в User
 */
private fun UserResponse.toUser(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email
    )
}

/**
 * Функция для извлечения userId из JWT токена
 */
private fun extractUserIdFromToken(token: String): String {
    try {
        // Токен JWT имеет формат: header.payload.signature
        val parts = token.split(".")
        if (parts.size != 3) return ""

        // Декодируем часть payload (Base64)
        val payload = parts[1].decodeBase64()
        val json = Json.parseToJsonElement(payload).jsonObject

        // Получаем sub (subject) из payload - это обычно userId
        return json["sub"]?.jsonPrimitive?.content ?: ""
    } catch (e: Exception) {
        println("Error extracting userId from token: ${e.message}")
        return ""
    }
}

/**
 * Функция для извлечения expiration из JWT токена (в секундах с эпохи)
 */
private fun extractExpirationFromToken(token: String): Long {
    try {
        val parts = token.split(".")
        if (parts.size != 3) return 0

        val payload = parts[1].decodeBase64()
        val json = Json.parseToJsonElement(payload).jsonObject

        // exp - стандартное поле JWT для времени истечения
        return json["exp"]?.jsonPrimitive?.long ?: 0
    } catch (e: Exception) {
        println("Error extracting expiration from token: ${e.message}")
        return 0
    }
}

/**
 * Функция для декодирования Base64Url строки в обычную строку
 */
private fun String.decodeBase64(): String {
    // Добавляем недостающие символы '=' для корректного декодирования
    val padding = when (length % 4) {
        0 -> ""
        1 -> "==="
        2 -> "=="
        else -> "="
    }

    // Заменяем символы URL-safe Base64 на стандартные Base64
    val base64 = this
        .replace('-', '+')
        .replace('_', '/') + padding

    // Декодируем как Base64 и получаем строку
    val decoded = io.ktor.utils.io.core.toByteArray(io.ktor.utils.io.core.String(base64))
    return decoded.decodeToString()
}

/**
 * Реализация API аутентификации
 */
class AuthApiImpl(
    private val client: HttpClient,
    private val networkConfig: NetworkConfig
) : AuthApi {

    private suspend fun getBaseUrl(): String = networkConfig.getBaseUrl()

    override suspend fun register(request: RegisterRequest): AuthResult<AuthResponseDto> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(api.dto.RegisterRequest(
                    email = request.email,
                    password = request.password,
                    username = request.username
                ))
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponseDto>()
                // Извлекаем userId из токена
                val userId = extractUserIdFromToken(authResponse.access_token)
                val expiresIn = extractExpirationFromToken(authResponse.access_token)

                // Получаем информацию о пользователе
                val userResult = getCurrentUser(authResponse.access_token)
                val user = if (userResult is AuthResult.Success) userResult.data else null

                AuthResult.Success(
                    data = authResponse,
                    user = user,
                    token = authResponse.access_token
                )
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Registration failed: ${response.status.description}",
                        code = ErrorCode.REGISTRATION_FAILED,
                        details = errorBody
                    )
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(
                AppError(
                    message = e.message ?: "Unknown registration error",
                    code = ErrorCode.NETWORK_ERROR
                )
            )
        }
    }

    override suspend fun login(request: LoginRequest): AuthResult<AuthResponseDto> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(api.dto.LoginRequest(
                    email = request.email,
                    password = request.password
                ))
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponseDto>()
                // Извлекаем userId из токена
                val userId = extractUserIdFromToken(authResponse.access_token)
                val expiresIn = extractExpirationFromToken(authResponse.access_token)

                // Получаем информацию о пользователе
                val userResult = getCurrentUser(authResponse.access_token)
                val user = if (userResult is AuthResult.Success) userResult.data else null

                AuthResult.Success(
                    data = authResponse,
                    user = user,
                    token = authResponse.access_token
                )
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Login failed: ${response.status.description}",
                        code = ErrorCode.LOGIN_FAILED,
                        details = errorBody
                    )
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(
                AppError(
                    message = e.message ?: "Unknown login error",
                    code = ErrorCode.NETWORK_ERROR
                )
            )
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResult<AuthResponseDto> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(api.dto.RefreshTokenRequest(
                    refresh_token = request.refreshToken
                ))
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponseDto>()
                // Извлекаем userId из токена
                val userId = extractUserIdFromToken(authResponse.access_token)
                val expiresIn = extractExpirationFromToken(authResponse.access_token)

                AuthResult.Success(
                    data = authResponse,
                    user = null, // Пользователя нужно будет получить отдельно при необходимости
                    token = authResponse.access_token
                )
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Token refresh failed: ${response.status.description}",
                        code = ErrorCode.TOKEN_REFRESH_FAILED,
                        details = errorBody
                    )
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(
                AppError(
                    message = e.message ?: "Unknown token refresh error",
                    code = ErrorCode.NETWORK_ERROR
                )
            )
        }
    }

    override suspend fun getCurrentUser(token: String): AuthResult<User> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.get("$baseUrl/auth/me") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                val userResponse = response.body<UserResponse>()
                AuthResult.Success(userResponse.toUser())
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Failed to get user info: ${response.status.description}",
                        code = ErrorCode.USER_INFO_FAILED,
                        details = errorBody
                    )
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(
                AppError(
                    message = e.message ?: "Unknown error getting user info",
                    code = ErrorCode.NETWORK_ERROR
                )
            )
        }
    }

    override suspend fun logout(token: String): AuthResult<Unit> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.post("$baseUrl/auth/logout") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                AuthResult.Success(Unit)
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Logout failed: ${response.status.description}",
                        code = ErrorCode.LOGOUT_FAILED,
                        details = errorBody
                    )
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(
                AppError(
                    message = e.message ?: "Unknown logout error",
                    code = ErrorCode.NETWORK_ERROR
                )
            )
        }
    }

    override suspend fun checkServerStatus(): AuthResult<ServerStatusResponse> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.get("$baseUrl/status")

            if (response.status.isSuccess()) {
                val status = response.body<ServerStatusResponse>()
                AuthResult.Success(status)
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Failed to check server status: ${response.status.description}",
                        code = ErrorCode.SERVER_STATUS_FAILED,
                        details = errorBody
                    )
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(
                AppError(
                    message = e.message ?: "Unknown server status error",
                    code = ErrorCode.NETWORK_ERROR
                )
            )
        }
    }

    override suspend fun updateProfile(token: String, username: String): AuthResult<User> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.put("$baseUrl/users/profile") {
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(api.dto.UpdateProfileRequest(username = username))
            }

            if (response.status.isSuccess()) {
                val userResponse = response.body<UserResponse>()
                AuthResult.Success(userResponse.toUser())
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Failed to update profile: ${response.status.description}",
                        code = ErrorCode.PROFILE_UPDATE_FAILED,
                        details = errorBody
                    )
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(
                AppError(
                    message = e.message ?: "Unknown error updating profile",
                    code = ErrorCode.NETWORK_ERROR
                )
            )
        }
    }
}
