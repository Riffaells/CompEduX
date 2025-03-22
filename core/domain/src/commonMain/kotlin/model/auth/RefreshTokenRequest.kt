package model.auth

/**
 * Модель запроса для обновления токена
 */
data class RefreshTokenRequest(
    val refreshToken: String
)
