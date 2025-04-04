package model

/**
 * Модель ошибки API слоя
 * @property message сообщение об ошибке
 * @property code код ошибки
 * @property details дополнительные детали ошибки (если есть)
 */
data class ApiError(
    val message: String,
    val code: Int,
    val details: String? = null
)
