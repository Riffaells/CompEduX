package model

/**
 * Доменная модель пользователя системы
 */
data class User(
    val id: String,
    val username: String,
    val email: String
)
