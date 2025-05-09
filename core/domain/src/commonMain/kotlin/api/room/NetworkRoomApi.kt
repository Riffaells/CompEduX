package api.room

import model.DomainResult
import model.room.RoomDomain
import model.room.RoomJoinDomain
import model.room.RoomJoinResponseDomain
import model.room.RoomListDomain
import model.room.RoomParticipantDomain
import model.room.RoomProgressDomain

/**
 * Network API for room operations
 * Defines the contract for interacting with the room service API
 */
interface NetworkRoomApi {
    /**
     * Get a room by its identifier
     * @param token access token
     * @param roomId the room identifier
     * @return room data result
     */
    suspend fun getRoom(token: String, roomId: String): DomainResult<RoomDomain>

    /**
     * Get a list of rooms with filtering and pagination
     * @param token access token
     * @param params query parameters as a map
     * @return paginated list of rooms result
     */
    suspend fun getRooms(token: String, params: Map<String, Any?>): DomainResult<RoomListDomain>

    /**
     * Get rooms where the current user is a participant
     * @param token access token
     * @param params query parameters as a map
     * @return paginated list of rooms result
     */
    suspend fun getMyRooms(token: String, params: Map<String, Any?>): DomainResult<RoomListDomain>

    /**
     * Create a new room
     * @param token access token
     * @param room room data
     * @return the created room result
     */
    suspend fun createRoom(token: String, room: RoomDomain): DomainResult<RoomDomain>

    /**
     * Update a room
     * @param token access token
     * @param roomId room identifier
     * @param room updated room data
     * @return the updated room result
     */
    suspend fun updateRoom(token: String, roomId: String, room: RoomDomain): DomainResult<RoomDomain>

    /**
     * Delete a room
     * @param token access token
     * @param roomId room identifier
     * @return operation result
     */
    suspend fun deleteRoom(token: String, roomId: String): DomainResult<Unit>

    /**
     * Join a room using a code
     * @param token access token
     * @param joinRequest join request with room code
     * @return join response result
     */
    suspend fun joinRoom(token: String, joinRequest: RoomJoinDomain): DomainResult<RoomJoinResponseDomain>

    /**
     * Get participants of a room
     * @param token access token
     * @param roomId room identifier
     * @return list of participants result
     */
    suspend fun getRoomParticipants(token: String, roomId: String): DomainResult<List<RoomParticipantDomain>>

    /**
     * Add a participant to a room
     * @param token access token
     * @param roomId room identifier
     * @param participant participant data
     * @return the added participant result
     */
    suspend fun addRoomParticipant(token: String, roomId: String, participant: RoomParticipantDomain): DomainResult<RoomParticipantDomain>

    /**
     * Update a room participant
     * @param token access token
     * @param roomId room identifier
     * @param userId user identifier
     * @param participant updated participant data
     * @return the updated participant result
     */
    suspend fun updateRoomParticipant(token: String, roomId: String, userId: String, participant: RoomParticipantDomain): DomainResult<RoomParticipantDomain>

    /**
     * Remove a participant from a room
     * @param token access token
     * @param roomId room identifier
     * @param userId user identifier
     * @return operation result
     */
    suspend fun removeRoomParticipant(token: String, roomId: String, userId: String): DomainResult<Unit>

    /**
     * Get progress records for a room
     * @param token access token
     * @param roomId room identifier
     * @return list of progress records result
     */
    suspend fun getRoomProgress(token: String, roomId: String): DomainResult<List<RoomProgressDomain>>

    /**
     * Create a progress record
     * @param token access token
     * @param progress progress data
     * @return the created progress record result
     */
    suspend fun createRoomProgress(token: String, progress: RoomProgressDomain): DomainResult<RoomProgressDomain>

    /**
     * Update a progress record
     * @param token access token
     * @param progressId progress identifier
     * @param progress updated progress data
     * @return the updated progress record result
     */
    suspend fun updateRoomProgress(token: String, progressId: String, progress: RoomProgressDomain): DomainResult<RoomProgressDomain>

    /**
     * Delete a progress record
     * @param token access token
     * @param progressId progress identifier
     * @return operation result
     */
    suspend fun deleteRoomProgress(token: String, progressId: String): DomainResult<Unit>
} 