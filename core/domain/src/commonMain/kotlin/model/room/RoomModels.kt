package model.room

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.course.LocalizedContent

/**
 * Domain model for a room
 * A room represents an instance where users can interact with course content
 */
@Serializable
data class RoomDomain(
    val id: String,
    val name: LocalizedContent,
    val description: LocalizedContent? = null,
    @SerialName("course_id")
    val courseId: String,
    @SerialName("owner_id")
    val ownerId: String,
    val code: String,
    val status: RoomStatusDomain = RoomStatusDomain.PENDING,
    @SerialName("max_participants")
    val maxParticipants: Int = 0,
    val settings: Map<String, String>? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
) {
    /**
     * Parse created_at timestamp string to Long
     */
    fun getCreatedAtMillis(): Long {
        return try {
            if (createdAt.isNotBlank()) {
                Instant.parse(createdAt).toEpochMilliseconds()
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Parse updated_at timestamp string to Long
     */
    fun getUpdatedAtMillis(): Long {
        return try {
            if (updatedAt.isNotBlank()) {
                Instant.parse(updatedAt).toEpochMilliseconds()
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Room status options
 */
@Serializable
enum class RoomStatusDomain {
    @SerialName("PENDING")
    PENDING,    // Room is created but not yet active
    @SerialName("ACTIVE")
    ACTIVE,     // Room is currently active
    @SerialName("COMPLETED")
    COMPLETED,  // Room has been completed
    @SerialName("ARCHIVED")
    ARCHIVED    // Room is archived and no longer active
}

/**
 * Domain model for room participant
 */
@Serializable
data class RoomParticipantDomain(
    @SerialName("room_id")
    val roomId: String,
    @SerialName("user_id")
    val userId: String,
    val role: RoomParticipantRoleDomain,
    @SerialName("participant_metadata")
    val participantMetadata: Map<String, String>? = null,
    @SerialName("joined_at")
    val joinedAt: String = "",
    @SerialName("last_activity_at")
    val lastActivityAt: String = ""
)

/**
 * Role of a room participant
 */
@Serializable
enum class RoomParticipantRoleDomain {
    @SerialName("OWNER")
    OWNER,
    @SerialName("TEACHER")
    TEACHER,
    @SerialName("STUDENT")
    STUDENT,
    @SerialName("OBSERVER")
    OBSERVER
}

/**
 * Domain model for room progress
 */
@Serializable
data class RoomProgressDomain(
    val id: String,
    @SerialName("room_id")
    val roomId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("node_id")
    val nodeId: String,
    val status: String,
    val data: Map<String, String>? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
)

/**
 * Domain model for joining a room
 */
@Serializable
data class RoomJoinDomain(
    val code: String
)

/**
 * Domain model for room join response
 */
@Serializable
data class RoomJoinResponseDomain(
    @SerialName("room_id")
    val roomId: String,
    val joined: Boolean,
    val message: String
)

/**
 * Room sorting options
 */
enum class RoomSortOptionDomain {
    CREATED_AT,
    UPDATED_AT,
    NAME,
    STATUS
}

/**
 * Sort direction
 */
enum class SortDirectionDomain {
    ASC,
    DESC
}
