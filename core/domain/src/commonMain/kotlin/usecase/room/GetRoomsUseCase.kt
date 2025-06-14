package usecase.room

import model.DomainError
import model.DomainResult
import model.room.RoomListDomain
import model.room.RoomQueryParams
import repository.room.RoomRepository

/**
 * Use case to get a filtered list of rooms
 */
class GetRoomsUseCase(private val roomRepository: RoomRepository) {
    /**
     * Get a list of rooms with filtering and pagination
     * @param params query parameters
     * @return operation result with a paginated list of rooms
     */
    suspend operator fun invoke(params: RoomQueryParams = RoomQueryParams()): DomainResult<RoomListDomain> {
        // Validate input data
        if (params.page < 0) {
            return DomainResult.Error(DomainError.validationError("Page number cannot be negative"))
        }

        if (params.size <= 0) {
            return DomainResult.Error(DomainError.validationError("Page size must be positive"))
        }

        return roomRepository.getRooms(params)
    }

    /**
     * Convenience method to search rooms by text
     * @param searchText text to search for
     * @param page page number
     * @param size page size
     * @return operation result with a list of rooms
     */
    suspend operator fun invoke(searchText: String, page: Int = 0, size: Int = 20): DomainResult<RoomListDomain> {
        val params = RoomQueryParams(
            search = searchText,
            page = page,
            size = size
        )
        return invoke(params)
    }

    /**
     * Convenience method to get rooms by author
     * @param authorId author identifier
     * @param page page number
     * @param size page size
     * @return operation result with a list of rooms
     */
    suspend fun getByAuthor(authorId: String, page: Int = 0, size: Int = 20): DomainResult<RoomListDomain> {
        val params = RoomQueryParams(
            ownerId = authorId,
            page = page,
            size = size
        )
        return invoke(params)
    }
}
