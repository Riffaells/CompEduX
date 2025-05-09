package usecase.room

import model.DomainError
import model.DomainResult
import model.room.RoomJoinDomain
import model.room.RoomJoinResponseDomain
import repository.room.RoomRepository

/**
 * Use case for joining a room using a code
 */
class JoinRoomUseCase(private val roomRepository: RoomRepository) {
    /**
     * Join a room using a code
     * @param code the room code
     * @return operation result with join response
     */
    suspend operator fun invoke(code: String): DomainResult<RoomJoinResponseDomain> {
        // Validate code
        if (code.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Room code cannot be empty"))
        }
        
        if (code.length != 6) {
            return DomainResult.Error(DomainError.validationError("Room code must be 6 characters"))
        }
        
        val joinRequest = RoomJoinDomain(code = code)
        return roomRepository.joinRoom(joinRequest)
    }
} 