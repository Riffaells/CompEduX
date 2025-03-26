package repository.auth

import model.User

/**
 * Состояние авторизации пользователя
 */
sealed interface AuthState {
    /**
     * Пользователь не авторизован
     */
    data object Unauthenticated : AuthState

    /**
     * Пользователь авторизован
     * @property user информация о пользователе
     */
    data class Authenticated(val user: User) : AuthState

    /**
     * Идет процесс авторизации/регистрации
     */
    data object Loading : AuthState

    /**
     * Ошибка авторизации
     * @property message сообщение об ошибке
     */
    data class Error(val message: String) : AuthState
}
