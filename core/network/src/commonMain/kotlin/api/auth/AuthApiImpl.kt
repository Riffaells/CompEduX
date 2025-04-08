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
import kotlin.io.encoding.ExperimentalEncodingApi

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
@OptIn(ExperimentalEncodingApi::class)
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

    // Используем более простой способ декодирования
    return kotlin.io.encoding.Base64.decode(base64).decodeToString()
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
            val response = client.post("$baseUrl/api/v1/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(api.dto.RegisterRequest(
                    email = request.email,
                    password = request.password,
                    username = request.username
                ))
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponseDto>()
                // Извлекаем userId и expiresIn из токена - это может быть полезно в логах
                val userId = extractUserIdFromToken(authResponse.accessToken)
                val expiresIn = extractExpirationFromToken(authResponse.accessToken)

                // Получаем информацию о пользователе - это больше не нужно передавать в AuthResult
                val userResult = getCurrentUser(authResponse.accessToken)

                // Возвращаем только данные без user и token
                AuthResult.Success(authResponse)
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Registration failed: ${response.status.description}",
                        code = ErrorCode.UNKNOWN_ERROR,
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
            val response = client.post("$baseUrl/api/v1/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(api.dto.LoginRequest(
                    username = request.username,
                    password = request.password
                ))
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponseDto>()
                // Извлекаем userId и expiresIn из токена - это может быть полезно в логах
                val userId = extractUserIdFromToken(authResponse.accessToken)
                val expiresIn = extractExpirationFromToken(authResponse.accessToken)

                // Получаем информацию о пользователе - это больше не нужно передавать в AuthResult
                val userResult = getCurrentUser(authResponse.accessToken)

                // Возвращаем только данные без user и token
                AuthResult.Success(authResponse)
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Login failed: ${response.status.description}",
                        code = ErrorCode.UNKNOWN_ERROR,
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
            val response = client.post("$baseUrl/api/v1/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(api.dto.RefreshTokenRequest(
                    refreshToken = request.refreshToken
                ))
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponseDto>()
                // Извлекаем userId и expiresIn из токена - это может быть полезно в логах
                val userId = extractUserIdFromToken(authResponse.accessToken)
                val expiresIn = extractExpirationFromToken(authResponse.accessToken)

                // Возвращаем только данные без user и token
                AuthResult.Success(authResponse)
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Token refresh failed: ${response.status.description}",
                        code = ErrorCode.UNKNOWN_ERROR,
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
            val response = client.get("$baseUrl/api/v1/auth/me") {
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
                        code = ErrorCode.UNKNOWN_ERROR,
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
            val response = client.post("$baseUrl/api/v1/auth/logout") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                AuthResult.Success(Unit)
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Logout failed: ${response.status.description}",
                        code = ErrorCode.UNKNOWN_ERROR,
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
            val response = client.get("$baseUrl/api/v1/status")

            if (response.status.isSuccess()) {
                val status = response.body<ServerStatusResponse>()
                AuthResult.Success(status)
            } else {
                val errorBody = response.body<String>()
                AuthResult.Error(
                    AppError(
                        message = "Failed to check server status: ${response.status.description}",
                        code = ErrorCode.UNKNOWN_ERROR,
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
            val response = client.put("$baseUrl/api/v1/users/profile") {
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
                        code = ErrorCode.UNKNOWN_ERROR,
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
