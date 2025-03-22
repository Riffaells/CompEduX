package model

/**
 * Результат операций авторизации и регистрации
 */
sealed class AuthResult {
    /**
     * Успешный результат
     * @property user информация о пользователе (null если пользователь не аутентифицирован)
     * @property token токен доступа (null если пользователь не аутентифицирован)
     */
    data class Success(val user: User?, val token: String? = null) : AuthResult()

    /**
     * Ошибка операции
     * @property error информация об ошибке
     */
    data class Error(val error: AppError) : AuthResult()

    /**
     * Состояние загрузки
     */
    data object Loading : AuthResult()
}
