package model.auth

/**
 * Информация о токенах аутентификации
 *
 * @property accessToken токен доступа JWT
 * @property refreshToken токен обновления
 * @property tokenType тип токена (обычно "bearer")
 */
data class TokenInfo(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)
