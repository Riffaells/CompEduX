package api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Расширенная модель ответа с ошибкой API
 */
@Serializable
data class ApiError(
    @SerialName("error_code") val errorCode: Int,
    @SerialName("message") val message: String = "",
    @SerialName("details") val details: String? = null
)
