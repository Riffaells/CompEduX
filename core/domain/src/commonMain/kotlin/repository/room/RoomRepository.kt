package repository.room

import model.DomainResult
import model.room.RoomDomain
import model.room.RoomJoinDomain
import model.room.RoomJoinResponseDomain
import model.room.RoomListDomain
import model.room.RoomQueryParams

/**
 * Repository for room operations
 * The room represents an instance where users can interact with course content
 */
interface RoomRepository {
    /**
     * Get a room by its identifier
     * @param roomId the room identifier
     * @return operation result with room data
     */
    suspend fun getRoom(roomId: String): DomainResult<RoomDomain>

    /**
     * Get a list of rooms with filtering and pagination
     * @param params query parameters
     * @return operation result with a paginated room list
     */
    suspend fun getRooms(params: RoomQueryParams = RoomQueryParams()): DomainResult<RoomListDomain>
    
    /**
     * Get a list of rooms that the current user is participating in
     * @param params query parameters
     * @return operation result with a paginated room list
     */
    suspend fun getMyRooms(params: RoomQueryParams = RoomQueryParams()): DomainResult<RoomListDomain>

    /**
     * Create a new room
     * @param room room data
     * @return operation result with the created room
     */
    suspend fun createRoom(room: RoomDomain): DomainResult<RoomDomain>

    /**
     * Update a room
     * @param roomId the room identifier
     * @param room updated room data
     * @return operation result with the updated room
     */
    suspend fun updateRoom(roomId: String, room: RoomDomain): DomainResult<RoomDomain>

    /**
     * Delete a room
     * @param roomId the room identifier
     * @return operation result
     */
    suspend fun deleteRoom(roomId: String): DomainResult<Unit>
    
    /**
     * Join a room using a code
     * @param joinRequest the join request with room code
     * @return operation result with join response
     */
    suspend fun joinRoom(joinRequest: RoomJoinDomain): DomainResult<RoomJoinResponseDomain>
}
