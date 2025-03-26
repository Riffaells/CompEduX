package model

/**
 * Модель ошибки API
 * @property message Сообщение об ошибке
 * @property code Код ошибки
 * @property details Детальное описание ошибки (опционально)
 */
data class ApiError(
    val message: String,
    val code: Int,
    val details: String? = null
)
