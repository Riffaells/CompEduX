package model.room

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Network model for a room
 */
@Serializable
data class NetworkRoom(
    val id: String = "",
    val name: Map<String, String> = emptyMap(),
    val description: Map<String, String>? = null,
    @SerialName("course_id") val courseId: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    val code: String = "",
    val status: String = "PENDING",
    @SerialName("max_participants") val maxParticipants: Int = 0,
    val settings: Map<String, String>? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

/**
 * Network model for room list with pagination
 */
@Serializable
data class NetworkRoomList(
    val items: List<NetworkRoom> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val size: Int = 20,
    val pages: Int = 0
)

/**
 * Network model for room participant
 */
@Serializable
data class NetworkRoomParticipant(
    @SerialName("room_id") val roomId: String = "",
    @SerialName("user_id") val userId: String = "",
    val role: String = "STUDENT",
    @SerialName("participant_metadata") val participantMetadata: Map<String, String>? = null,
    @SerialName("joined_at") val joinedAt: String = "",
    @SerialName("last_activity_at") val lastActivityAt: String = ""
)

/**
 * Network model for room progress
 */
@Serializable
data class NetworkRoomProgress(
    val id: String = "",
    @SerialName("room_id") val roomId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("node_id") val nodeId: String = "",
    val status: String = "",
    val data: Map<String, String>? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

/**
 * Network model for joining a room
 */
@Serializable
data class NetworkRoomJoin(
    val code: String = ""
)

/**
 * Network model for room join response
 */
@Serializable
data class NetworkRoomJoinResponse(
    @SerialName("room_id") val roomId: String = "",
    val joined: Boolean = false,
    val message: String = ""
)

/**
 * Network response for room errors
 */
@Serializable
data class NetworkRoomErrorResponse(
    val status: Int = 400,
    val message: String = "Unknown error",
    val error: String? = null,
    val details: String? = null,
    val path: String? = null,
    val timestamp: Long = 0
) {
    fun getErrorCode(): Int = status
    fun getErrorMessage(): String = message
}
