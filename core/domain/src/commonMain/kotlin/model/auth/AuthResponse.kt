package model.auth

import model.User

/**
 * Модель ответа аутентификации
 */
data class AuthResponse(
    val user: User,
    val token: String,
    val refreshToken: String
)
