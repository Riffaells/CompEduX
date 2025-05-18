package model.room

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.course.LocalizedContent

/**
 * Domain model for updating a room
 * Contains only the fields that can be updated
 * 
 * @property name Room name (required)
 * @property description Room description (optional)
 * @property courseId ID of the course associated with this room (optional)
 * @property status Room status (optional)
 * @property maxParticipants Maximum number of participants (optional, null means no change, 0 means unlimited)
 * @property settings Room settings (optional)
 */
@Serializable
data class RoomUpdateDomain(
    val name: LocalizedContent,
    val description: LocalizedContent? = null,
    @SerialName("course_id")
    val courseId: String? = null,
    val status: RoomStatusDomain? = null,
    @SerialName("max_participants")
    val maxParticipants: Int? = null,
    val settings: Map<String, String>? = null
) 