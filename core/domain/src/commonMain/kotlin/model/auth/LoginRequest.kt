package model.auth

/**
 * Модель запроса для авторизации
 */
data class LoginRequest(
    val username: String,
    val password: String
)
