package api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * DTO запроса авторизации
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * DTO запроса регистрации
 */
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

/**
 * DTO запроса обновления профиля
 */
@Serializable
data class UpdateProfileRequest(
    val username: String
)

/**
 * DTO ответа с пользователем
 */
@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val username: String
)

/**
 * DTO запроса обновления токена
 */
@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

/**
 * DTO ответа авторизации по новому формату
 */
@Serializable
data class AuthResponseDto(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("refresh_token")
    val refreshToken: String,

    @SerialName("token_type")
    val tokenType: String = "bearer"
)

/**
 * DTO ответа с ошибкой
 */
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String
)
