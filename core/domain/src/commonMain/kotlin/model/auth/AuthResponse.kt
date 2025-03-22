package model.auth

/**
 * Модель ответа аутентификации
 */
data class AuthResponse(
    val userId: String,
    val username: String,
    val token: String,
    val refreshToken: String? = null
)
