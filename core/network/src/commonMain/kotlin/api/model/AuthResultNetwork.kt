package api.model

import model.ApiError

/**
 * Результат операций авторизации/регистрации в API слое
 */
sealed class AuthResultNetwork<out T> {
    /**
     * Успешный результат с данными
     */
    data class Success<T>(val data: T) : AuthResultNetwork<T>()

    /**
     * Ошибка операции
     */
    data class Error(val error: ApiError) : AuthResultNetwork<Nothing>()

    /**
     * Состояние загрузки
     */
    data object Loading : AuthResultNetwork<Nothing>()
}
