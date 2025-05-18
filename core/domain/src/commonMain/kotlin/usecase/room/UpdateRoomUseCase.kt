package usecase.room

import model.DomainError
import model.DomainResult
import model.course.LocalizedContent
import model.room.RoomDomain
import model.room.RoomStatusDomain
import model.room.RoomUpdateDomain
import repository.room.RoomRepository

/**
 * Use case for updating an existing room
 */
class UpdateRoomUseCase(private val roomRepository: RoomRepository) {
    /**
     * Update a room
     * @param roomId the room identifier
     * @param name updated room name (null to keep current)
     * @param description updated room description (null to keep current)
     * @param status updated room status (null to keep current)
     * @param maxParticipants updated maximum participants (null to keep current)
     * @param settings updated room settings (null to keep current)
     * @return operation result with the updated room
     */
    suspend operator fun invoke(
        roomId: String,
        name: LocalizedContent? = null,
        description: LocalizedContent? = null,
        status: RoomStatusDomain? = null,
        maxParticipants: Int? = null,
        settings: Map<String, String>? = null
    ): DomainResult<RoomDomain> {
        // Validate room ID
        if (roomId.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Room ID cannot be empty"))
        }
        
        // Validate max participants if provided
        if (maxParticipants != null && maxParticipants < 0) {
            return DomainResult.Error(DomainError.validationError("Maximum participants cannot be negative"))
        }
        
        // Get the existing room to verify it exists
        val existingRoomResult = roomRepository.getRoom(roomId)
        if (existingRoomResult is DomainResult.Error) {
            return existingRoomResult
        }
        
        // Create room update object with only the fields to update
        val roomUpdate = RoomUpdateDomain(
            name = name ?: (existingRoomResult as DomainResult.Success).data.name,
            description = description,
            status = status,
            maxParticipants = maxParticipants,
            settings = settings
        )
        
        // Update the room
        return roomRepository.updateRoom(roomId, roomUpdate)
    }
}
