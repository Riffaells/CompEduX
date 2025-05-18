package mapper

import model.course.LocalizedContent
import model.room.*

/**
 * Extension functions for mapping between network and domain room models
 */

// Network to domain mappings
fun NetworkRoom.toDomain(): RoomDomain {
    return RoomDomain(
        id = id,
        name = LocalizedContent(name),
        description = description?.let { LocalizedContent(it) },
        courseId = courseId,
        ownerId = ownerId,
        code = code,
        status = parseRoomStatus(status),
        maxParticipants = maxParticipants,
        settings = settings,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun NetworkRoomList.toDomain(): RoomListDomain {
    return RoomListDomain(
        items = items.map { it.toDomain() },
        total = total,
        page = page,
        pageSize = size
    )
}

fun NetworkRoomParticipant.toDomain(): RoomParticipantDomain {
    return RoomParticipantDomain(
        roomId = roomId,
        userId = userId,
        role = parseParticipantRole(role),
        participantMetadata = participantMetadata,
        joinedAt = joinedAt,
        lastActivityAt = lastActivityAt
    )
}

fun NetworkRoomProgress.toDomain(): RoomProgressDomain {
    return RoomProgressDomain(
        id = id,
        roomId = roomId,
        userId = userId,
        nodeId = nodeId,
        status = status,
        data = data,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun NetworkRoomJoinResponse.toDomain(): RoomJoinResponseDomain {
    return RoomJoinResponseDomain(
        roomId = roomId,
        joined = joined,
        message = message
    )
}

// Domain to network mappings
fun RoomDomain.toNetwork(): NetworkRoom {
    return NetworkRoom(
        id = id,
        name = name.content,
        description = description?.content,
        courseId = courseId,
        ownerId = ownerId,
        code = code,
        status = status.toString(),
        maxParticipants = maxParticipants,
        settings = settings,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun RoomParticipantDomain.toNetwork(): NetworkRoomParticipant {
    return NetworkRoomParticipant(
        roomId = roomId,
        userId = userId,
        role = role.toString(),
        participantMetadata = participantMetadata,
        joinedAt = joinedAt,
        lastActivityAt = lastActivityAt
    )
}

fun RoomProgressDomain.toNetwork(): NetworkRoomProgress {
    return NetworkRoomProgress(
        id = id,
        roomId = roomId,
        userId = userId,
        nodeId = nodeId,
        status = status,
        data = data,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun RoomJoinDomain.toNetwork(): NetworkRoomJoin {
    return NetworkRoomJoin(
        code = code
    )
}

// Helper functions for parsing enums
private fun parseRoomStatus(status: String): RoomStatusDomain {
    return when (status.uppercase()) {
        "ACTIVE" -> RoomStatusDomain.ACTIVE
        "COMPLETED" -> RoomStatusDomain.COMPLETED
        "ARCHIVED" -> RoomStatusDomain.ARCHIVED
        else -> RoomStatusDomain.PENDING
    }
}

private fun parseParticipantRole(role: String): RoomParticipantRoleDomain {
    return when (role.uppercase()) {
        "OWNER" -> RoomParticipantRoleDomain.OWNER
        "TEACHER" -> RoomParticipantRoleDomain.TEACHER
        "OBSERVER" -> RoomParticipantRoleDomain.OBSERVER
        else -> RoomParticipantRoleDomain.STUDENT
    }
}
