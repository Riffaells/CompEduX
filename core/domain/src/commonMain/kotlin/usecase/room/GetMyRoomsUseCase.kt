package usecase.room

import model.DomainError
import model.DomainResult
import model.room.RoomListDomain
import model.room.RoomQueryParams
import repository.room.RoomRepository

/**
 * Use case for getting rooms that the current user is participating in
 */
class GetMyRoomsUseCase(private val roomRepository: RoomRepository) {
    /**
     * Get a list of rooms that the current user is participating in
     * @param params query parameters for filtering and pagination
     * @return operation result with a paginated room list
     */
    suspend operator fun invoke(params: RoomQueryParams = RoomQueryParams()): DomainResult<RoomListDomain> {
        // Validate pagination parameters
        if (params.page < 0) {
            return DomainResult.Error(DomainError.validationError("Page number cannot be negative"))
        }
        
        if (params.size <= 0) {
            return DomainResult.Error(DomainError.validationError("Page size must be positive"))
        }
        
        return roomRepository.getMyRooms(params)
    }
    
    /**
     * Convenience method to filter my rooms by status
     * @param status room status to filter by
     * @param page page number
     * @param size page size
     * @return operation result with a list of rooms
     */
    suspend fun getByStatus(status: String, page: Int = 0, size: Int = 20): DomainResult<RoomListDomain> {
        val params = RoomQueryParams(
            status = status,
            page = page,
            size = size
        )
        return invoke(params)
    }
} 