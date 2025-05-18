package usecase.room

import model.DomainError
import model.DomainResult
import model.course.LocalizedContent
import model.room.RoomDomain
import model.room.RoomStatusDomain
import model.room.RoomUpdateDomain
import repository.room.RoomRepository

/**
 * Use case for creating a new room
 */
class CreateRoomUseCase(private val roomRepository: RoomRepository) {
    /**
     * Create a new room
     * @param name localized room name
     * @param description localized room description
     * @param courseId ID of the course associated with this room
     * @param status room status
     * @param maxParticipants maximum number of participants (0 for unlimited)
     * @param settings additional room settings
     * @return operation result with the created room
     */
    suspend operator fun invoke(
        name: LocalizedContent,
        courseId: String,
        description: LocalizedContent? = null,
        status: RoomStatusDomain = RoomStatusDomain.PENDING,
        maxParticipants: Int = 0,
        settings: Map<String, String>? = null
    ): DomainResult<RoomDomain> {
        // Validate input data
        if (name.getPreferredString().isBlank()) {
            return DomainResult.Error(DomainError.validationError("Room name cannot be empty"))
        }
        
        if (courseId.isBlank()) {
            return DomainResult.Error(DomainError.validationError("Course ID cannot be empty"))
        }
        
        if (maxParticipants < 0) {
            return DomainResult.Error(DomainError.validationError("Maximum participants cannot be negative"))
        }
        
        // Create room update object for API
        val roomUpdate = RoomUpdateDomain(
            name = name,
            description = description,
            courseId = courseId,
            status = status,
            maxParticipants = maxParticipants,
            settings = settings
        )
        
        return roomRepository.createRoom(roomUpdate)
    }
}
