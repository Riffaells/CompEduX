package model.auth

/**
 * Доменная модель данных ответа аутентификации
 * Содержит только основные данные о токенах, необходимые для аутентификации
 */
data class AuthResponseDomain(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)
