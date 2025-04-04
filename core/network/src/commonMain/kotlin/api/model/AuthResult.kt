package api.model

import model.ApiError

/**
 * Результат операций авторизации/регистрации в API слое
 */
sealed class AuthResult<out T> {
    /**
     * Успешный результат с данными
     */
    data class Success<T>(val data: T) : AuthResult<T>()

    /**
     * Ошибка операции
     */
    data class Error<T>(val error: ApiError) : AuthResult<T>()

    /**
     * Состояние загрузки
     */
    data object Loading : AuthResult<Nothing>()
}
