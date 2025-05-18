package api.room

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logging.Logger
import model.DomainError
import model.DomainResult
import model.room.RoomDomain
import model.room.RoomJoinDomain
import model.room.RoomJoinResponseDomain
import model.room.RoomListDomain
import model.room.RoomParticipantDomain
import model.room.RoomProgressDomain
import model.room.RoomQueryParams
import repository.auth.TokenRepository

/**
 * Adapter connecting RoomApi with NetworkRoomApi
 * Allows abstracting the domain layer from the details of network request implementation
 */
class DataRoomApiAdapter(
    private val networkRoomApi: NetworkRoomApi,
    private val tokenRepository: TokenRepository,
    private val logger: Logger
) : RoomApi {

    override suspend fun getRoom(roomId: String): DomainResult<RoomDomain> = withContext(Dispatchers.Default) {
        logger.d("DataRoomApiAdapter: getRoom($roomId)")

        // Get saved token
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot get room: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
        }

        return@withContext networkRoomApi.getRoom(token, roomId)
    }

    override suspend fun getRooms(params: RoomQueryParams): DomainResult<RoomListDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: getRooms(${params.toMap().keys})")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot get rooms: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.getRooms(token, params.toMap())
        }

    override suspend fun getMyRooms(params: RoomQueryParams): DomainResult<RoomListDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: getMyRooms(${params.toMap().keys})")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot get my rooms: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.getMyRooms(token, params.toMap())
        }

    override suspend fun createRoom(room: RoomDomain): DomainResult<RoomDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: createRoom(${room.name.content})")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot create room: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.createRoom(token, room)
        }

    override suspend fun updateRoom(roomId: String, room: RoomDomain): DomainResult<RoomDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: updateRoom($roomId)")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot update room: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.updateRoom(token, roomId, room)
        }

    override suspend fun deleteRoom(roomId: String): DomainResult<Unit> = withContext(Dispatchers.Default) {
        logger.d("DataRoomApiAdapter: deleteRoom($roomId)")

        // Get saved token
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot delete room: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
        }

        return@withContext networkRoomApi.deleteRoom(token, roomId)
    }

    override suspend fun joinRoom(joinRequest: RoomJoinDomain): DomainResult<RoomJoinResponseDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: joinRoom(${joinRequest.code})")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot join room: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.joinRoom(token, joinRequest)
        }

    override suspend fun getRoomParticipants(roomId: String): DomainResult<List<RoomParticipantDomain>> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: getRoomParticipants($roomId)")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot get room participants: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.getRoomParticipants(token, roomId)
        }

    override suspend fun addRoomParticipant(roomId: String, participant: RoomParticipantDomain): DomainResult<RoomParticipantDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: addRoomParticipant($roomId, ${participant.userId})")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot add room participant: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.addRoomParticipant(token, roomId, participant)
        }

    override suspend fun updateRoomParticipant(roomId: String, userId: String, participant: RoomParticipantDomain): DomainResult<RoomParticipantDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: updateRoomParticipant($roomId, $userId)")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot update room participant: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.updateRoomParticipant(token, roomId, userId, participant)
        }

    override suspend fun removeRoomParticipant(roomId: String, userId: String): DomainResult<Unit> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: removeRoomParticipant($roomId, $userId)")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot remove room participant: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.removeRoomParticipant(token, roomId, userId)
        }

    override suspend fun getRoomProgress(roomId: String): DomainResult<List<RoomProgressDomain>> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: getRoomProgress($roomId)")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot get room progress: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.getRoomProgress(token, roomId)
        }

    override suspend fun createRoomProgress(progress: RoomProgressDomain): DomainResult<RoomProgressDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: createRoomProgress(${progress.roomId}, ${progress.userId})")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot create room progress: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.createRoomProgress(token, progress)
        }

    override suspend fun updateRoomProgress(progressId: String, progress: RoomProgressDomain): DomainResult<RoomProgressDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: updateRoomProgress($progressId)")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot update room progress: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.updateRoomProgress(token, progressId, progress)
        }

    override suspend fun deleteRoomProgress(progressId: String): DomainResult<Unit> =
        withContext(Dispatchers.Default) {
            logger.d("DataRoomApiAdapter: deleteRoomProgress($progressId)")

            // Get saved token
            val token = tokenRepository.getAccessToken()

            if (token == null) {
                logger.w("Cannot delete room progress: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Access token not found"))
            }

            return@withContext networkRoomApi.deleteRoomProgress(token, progressId)
        }
} 