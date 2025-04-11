package model.auth

/**
 * Доменная модель запроса на регистрацию
 */
data class RegisterRequestDomain(
    val username: String,
    val email: String,
    val password: String
)

/**
 * Доменная модель запроса на вход
 */
data class LoginRequestDomain(
    val email: String,
    val password: String
)

/**
 * Доменная модель запроса на обновление токена
 */
data class RefreshTokenRequestDomain(
    val refreshToken: String
)

/**
 * Доменная модель ответа авторизации
 * Содержит токены и их типы
 */
data class AuthResponseDomain(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "bearer"
)

/**
 * Доменная модель ответа о статусе сервера
 */
data class ServerStatusDomain(
    val status: String,
    val version: String,
    val uptime: Long,
    val message: String? = null
)

/**
 * Состояние аутентификации для пользователей
 */
sealed interface AuthStateDomain {
    /**
     * Пользователь не аутентифицирован
     */
    data object Unauthenticated : AuthStateDomain

    /**
     * Пользователь аутентифицирован
     * @property user информация о пользователе
     */
    data class Authenticated(val user: model.UserDomain) : AuthStateDomain

    /**
     * Аутентификация/регистрация в процессе
     */
    data object Loading : AuthStateDomain

    /**
     * Ошибка аутентификации
     * @property message сообщение об ошибке
     */
    data class Error(val message: String) : AuthStateDomain
}
