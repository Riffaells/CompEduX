package model.tree

import kotlinx.serialization.Serializable

/**
 * Сетевая модель ответа с ошибкой
 */
@Serializable
data class NetworkTreeErrorResponse(
    val status: Int = 400,
    val message: String = "Unknown error",
    val error: String? = null,
    val details: Map<String, String> = emptyMap(),
    val path: String? = null,
    val timestamp: Long = 0
) {
    fun getErrorCode(): Int = status
    fun getErrorMessage(): String = message
} 