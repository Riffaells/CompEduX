package model.auth

/**
 * Модель данных ответа аутентификации для слоя данных
 * Используется как промежуточная модель между domain и network слоями
 */
data class AuthResponseData(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val userId: String = "", // ID пользователя извлекается из токена
    val username: String = "", // Имя пользователя получается в отдельном запросе
    val expiresIn: Long = 0 // Время истечения токена извлекается из JWT payload
)
