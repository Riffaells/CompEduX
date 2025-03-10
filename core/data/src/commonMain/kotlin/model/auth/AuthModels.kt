package model.auth

import kotlinx.serialization.Serializable

/**
 * Модели данных для аутентификации и регистрации
 */

/**
 * Запрос на регистрацию пользователя
 */
@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null
)

/**
 * Запрос на авторизацию пользователя
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Ответ на успешную аутентификацию или регистрацию
 */
@Serializable
data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val userId: String,
    val username: String,
    val expiresIn: Long
)

/**
 * Модель пользователя
 */
@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val roles: List<String> = emptyList(),
    val isActive: Boolean = true
)

/**
 * Запрос на обновление токена
 */
@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

/**
 * Ответ на проверку статуса сервера
 */
@Serializable
data class ServerStatusResponse(
    val status: String,
    val version: String,
    val timestamp: Long
)

/**
 * Ошибка аутентификации
 */
@Serializable
data class AuthError(
    val code: String,
    val message: String
)

/**
 * Результат операции аутентификации
 */
sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val error: AuthError) : AuthResult<Nothing>()
    data object Loading : AuthResult<Nothing>()
}
