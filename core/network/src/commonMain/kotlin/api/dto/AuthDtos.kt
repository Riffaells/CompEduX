package api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * DTO запроса авторизации
 */
@Serializable
data class NetworkLoginRequestDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

/**
 * DTO запроса регистрации
 */
@Serializable
data class NetworkRegisterRequestDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    @SerialName("username") val username: String
)

/**
 * DTO запроса обновления профиля
 */
@Serializable
data class NetworkUpdateProfileRequestDto(
    @SerialName("username") val username: String
)

/**
 * DTO ответа с базовой информацией о пользователе
 */
@Serializable
data class NetworkUserResponseDto(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String,
    @SerialName("username") val username: String
)

/**
 * DTO запроса обновления токена
 */
@Serializable
data class NetworkRefreshTokenRequestDto(
    @SerialName("refresh_token") val refreshToken: String
)

/**
 * DTO ответа авторизации
 */
@Serializable
data class NetworkAuthResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer"
)

/**
 * DTO ответа с ошибкой
 */
@Serializable
data class NetworkErrorResponseDto(
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("details") val details: String? = null
)
