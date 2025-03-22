package model.auth

/**
 * Модель ответа о статусе сервера
 */
data class ServerStatusResponse(
    val status: String,
    val version: String,
    val timestamp: Long
)
