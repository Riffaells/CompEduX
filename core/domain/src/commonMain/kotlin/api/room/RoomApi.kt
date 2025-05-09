package api.room

import model.DomainResult
import model.room.RoomDomain
import model.room.RoomJoinDomain
import model.room.RoomJoinResponseDomain
import model.room.RoomListDomain
import model.room.RoomParticipantDomain
import model.room.RoomProgressDomain
import model.room.RoomQueryParams

/**
 * API for room operations
 * The room represents an active instance where users can interact with course content
 */
interface RoomApi {
    /**
     * Get a room by its identifier
     * @param roomId the room identifier
     * @return result containing room data or error
     */
    suspend fun getRoom(roomId: String): DomainResult<RoomDomain>

    /**
     * Get a list of rooms with filtering and pagination
     * @param params query parameters for filtering and pagination
     * @return result containing paginated list of rooms or error
     */
    suspend fun getRooms(params: RoomQueryParams): DomainResult<RoomListDomain>

    /**
     * Get rooms where the current user is a participant
     * @param params query parameters for filtering and pagination
     * @return result containing paginated list of rooms or error
     */
    suspend fun getMyRooms(params: RoomQueryParams): DomainResult<RoomListDomain>

    /**
     * Create a new room
     * @param room room data
     * @return result containing the created room or error
     */
    suspend fun createRoom(room: RoomDomain): DomainResult<RoomDomain>

    /**
     * Update a room
     * @param roomId room identifier
     * @param room updated room data
     * @return result containing the updated room or error
     */
    suspend fun updateRoom(roomId: String, room: RoomDomain): DomainResult<RoomDomain>

    /**
     * Delete a room
     * @param roomId room identifier
     * @return result containing success or error
     */
    suspend fun deleteRoom(roomId: String): DomainResult<Unit>

    /**
     * Join a room using a code
     * @param joinRequest join request with room code
     * @return result containing join response or error
     */
    suspend fun joinRoom(joinRequest: RoomJoinDomain): DomainResult<RoomJoinResponseDomain>

    /**
     * Get participants of a room
     * @param roomId room identifier
     * @return result containing list of participants or error
     */
    suspend fun getRoomParticipants(roomId: String): DomainResult<List<RoomParticipantDomain>>

    /**
     * Add a participant to a room
     * @param roomId room identifier
     * @param participant participant data
     * @return result containing the added participant or error
     */
    suspend fun addRoomParticipant(roomId: String, participant: RoomParticipantDomain): DomainResult<RoomParticipantDomain>

    /**
     * Update a room participant
     * @param roomId room identifier
     * @param userId user identifier
     * @param participant updated participant data
     * @return result containing the updated participant or error
     */
    suspend fun updateRoomParticipant(roomId: String, userId: String, participant: RoomParticipantDomain): DomainResult<RoomParticipantDomain>

    /**
     * Remove a participant from a room
     * @param roomId room identifier
     * @param userId user identifier
     * @return result containing success or error
     */
    suspend fun removeRoomParticipant(roomId: String, userId: String): DomainResult<Unit>

    /**
     * Get progress records for a room
     * @param roomId room identifier
     * @return result containing list of progress records or error
     */
    suspend fun getRoomProgress(roomId: String): DomainResult<List<RoomProgressDomain>>

    /**
     * Create a progress record
     * @param progress progress data
     * @return result containing the created progress record or error
     */
    suspend fun createRoomProgress(progress: RoomProgressDomain): DomainResult<RoomProgressDomain>

    /**
     * Update a progress record
     * @param progressId progress identifier
     * @param progress updated progress data
     * @return result containing the updated progress record or error
     */
    suspend fun updateRoomProgress(progressId: String, progress: RoomProgressDomain): DomainResult<RoomProgressDomain>

    /**
     * Delete a progress record
     * @param progressId progress identifier
     * @return result containing success or error
     */
    suspend fun deleteRoomProgress(progressId: String): DomainResult<Unit>
} 