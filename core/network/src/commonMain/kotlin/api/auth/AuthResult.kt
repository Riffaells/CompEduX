package api.auth

import model.AppError

/**
 * Результат операций API авторизации и регистрации в сетевом уровне
 */
sealed class AuthResult<T> {
    /**
     * Успешный результат
     * @property data данные результата операции
     * @property user информация о пользователе (null если пользователь не аутентифицирован)
     * @property token токен доступа (null если пользователь не аутентифицирован)
     */
    data class Success<T>(val data: T, val user: model.User? = null, val token: String? = null) : AuthResult<T>()

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
