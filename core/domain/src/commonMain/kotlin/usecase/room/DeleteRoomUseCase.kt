package usecase.room

import model.DomainError
import model.DomainResult
import repository.room.RoomRepository

/**
 * Use case for deleting a room
 */
class DeleteRoomUseCase(private val roomRepository: RoomRepository) {
    /**
     * Delete a room
     * @param roomId the room identifier
     * @return operation result
     */
    suspend operator fun invoke(roomId: String): DomainResult<Boolean> {
        // Validate room ID
        if (roomId.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Room ID cannot be empty"))
        }
        
        // Check if the room exists
        val existingRoom = roomRepository.getRoom(roomId)
        if (existingRoom is DomainResult.Error) {
            return DomainResult.Error(existingRoom.error)
        }
        
        // Delete the room
        return roomRepository.deleteRoom(roomId)
    }
}
