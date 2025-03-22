package model

import kotlinx.serialization.Serializable

/**
 * Доменная модель ошибки приложения
 *
 * @property code Код ошибки для идентификации типа ошибки
 * @property message Сообщение об ошибке (может использоваться для отладки)
 * @property details Дополнительные детали ошибки (опционально)
 */
@Serializable
data class AppError(
    val code: ErrorCode = ErrorCode.UNKNOWN_ERROR,
    val message: String,
    val details: String? = null
) {
    /**
     * Получить ключ для локализации сообщения об ошибке
     * Используется в UI модуле для получения локализованного сообщения
     */
    fun localizationKey(): String = "error.${code.name.lowercase()}"
}
