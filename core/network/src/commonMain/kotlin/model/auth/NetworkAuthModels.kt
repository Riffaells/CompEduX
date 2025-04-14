package model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Запрос на регистрацию пользователя (сетевая модель)
 */
@Serializable
data class NetworkRegisterRequest(
    @SerialName("username") val username: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

/**
 * Запрос на вход в систему (сетевая модель)
 */
@Serializable
data class NetworkLoginRequest(
    @SerialName("username") val email: String,
    @SerialName("password") val password: String
)

/**
 * Запрос на обновление токена (сетевая модель)
 */
@Serializable
data class NetworkRefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

/**
 * Ответ на запрос аутентификации (сетевая модель)
 */
@Serializable
data class NetworkAuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer"
)

/**
 * Ответ о статусе сервера (сетевая модель)
 */
@Serializable
data class NetworkServerStatusResponse(
    @SerialName("status") val status: String,
    @SerialName("version") val version: String,
    @SerialName("uptime") val uptime: Long,
    @SerialName("message") val message: String? = null
)

/**
 * Объект ошибки с сервера (сетевая модель)
 */
@Serializable
data class NetworkErrorResponse(
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("details") val details: String? = null
)
