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
 *
 * Поддерживает несколько форматов ошибок API:
 * 1. { "code": 400, "message": "Error message" }
 * 2. { "detail": "Error message" }
 * 3. { "code": 400, "message": "Error message", "details": "Additional info" }
 */
@Serializable
data class NetworkErrorResponse(
    @SerialName("code") val code: Int? = null,
    @SerialName("message") val message: String? = null,
    @SerialName("detail") val detail: String? = null,
    @SerialName("details") val details: String? = null
) {
    /**
     * Возвращает сообщение об ошибке, используя message или detail
     */
    fun getErrorMessage(): String {
        return message ?: detail ?: "Unknown error"
    }

    /**
     * Возвращает код ошибки или стандартный код 400
     */
    fun getErrorCode(): Int {
        return code ?: 400
    }
}


@Serializable
data class NetworkLogoutResponse(
    @SerialName("message") val message: String
)
