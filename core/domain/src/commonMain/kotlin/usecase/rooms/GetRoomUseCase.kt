package usecase.rooms

import model.DomainError
import model.DomainResult
import model.rooms.RoomDomain
import repository.rooms.RoomRepository

/**
 * Use case to get a room by its identifier
 */
class GetRoomUseCase(
    private val roomRepository: RoomRepository
) {

    /**
     * Get a room by its identifier
     * @param roomId the room identifier
     * @return operation result with room data
     */
    suspend operator fun invoke(roomId: String): DomainResult<RoomDomain> {
        if (roomId.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Room ID cannot be blank"))
        }

        return roomRepository.getRoom(roomId)
    }
}
