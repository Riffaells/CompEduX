package model

/**
 * Класс для представления ошибок API
 *
 * @property code HTTP-код ошибки или внутренний код
 * @property message Сообщение об ошибке
 * @property details Детали ошибки (опционально)
 */
data class ApiError(
    val code: Int,
    val message: String,
    val details: String? = null
)

/**
 * Обертка для результата API-запроса с возможным возвратом ошибки
 */
sealed class AuthResult<out T> {
    /**
     * Успешный результат с данными
     */
    data class Success<T>(val data: T) : AuthResult<T>()

    /**
     * Ошибка с информацией об ошибке
     */
    data class Error(val error: ApiError) : AuthResult<Nothing>()
}
