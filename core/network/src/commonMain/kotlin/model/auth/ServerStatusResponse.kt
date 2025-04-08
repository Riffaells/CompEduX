package model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Доменная модель ответа о статусе сервера
 */
data class ServerStatusResponse(
    val status: String,
    val version: String,
    val uptime: Long,
    val message: String? = null
)
