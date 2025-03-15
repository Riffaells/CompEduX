package api.auth

/**
 * Состояние аутентификации
 */
sealed class AuthState {
    /**
     * Пользователь не аутентифицирован
     */
    data object NotAuthenticated : AuthState()

    /**
     * Пользователь аутентифицирован
     */
    data class Authenticated(
        val token: String,
        val userId: String,
        val username: String
    ) : AuthState()
}
