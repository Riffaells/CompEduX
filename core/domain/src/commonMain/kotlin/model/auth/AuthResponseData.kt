package model.auth

/**
 * Модель данных ответа аутентификации для слоя данных
 * Используется как промежуточная модель между domain и network слоями
 */
data class AuthResponseData(
    val token: String,
    val refreshToken: String,
    val userId: String,
    val username: String,
    val expiresIn: Long
)
