package api.dto

import kotlinx.serialization.Serializable

/**
 * DTO запроса авторизации
 */
@Serializable
data class LoginRequest(
    val email: String,
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
    val refresh_token: String
)

/**
 * DTO ответа авторизации по новому формату
 */
@Serializable
data class AuthResponseDto(
    val access_token: String,
    val refresh_token: String,
    val token_type: String = "bearer"
)

/**
 * DTO ответа с ошибкой
 */
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String
)
