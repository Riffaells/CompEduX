package model

/**
 * Результат операций авторизации и регистрации
 */
sealed class AuthResult<T> {
    /**
     * Успешный результат
     * @property data данные результата операции
     */
    data class Success<T>(val data: T) : AuthResult<T>()

    /**
     * Ошибка операции
     * @property error информация об ошибке
     */
    data class Error<T>(val error: AppError) : AuthResult<T>()

    /**
     * Состояние загрузки
     */
    data object Loading : AuthResult<Nothing>()
}
