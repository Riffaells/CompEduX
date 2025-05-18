package api.room

import base.BaseNetworkApi
import client.safeSendWithErrorBody
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import logging.Logger
import mapper.toDomain
import mapper.toNetwork
import model.DomainError
import model.DomainResult
import model.room.*

/**
 * Implementation of NetworkRoomApi that uses Ktor HttpClient
 * to perform API requests
 */
class NetworkRoomApiImpl(
    client: HttpClient,
    networkConfig: NetworkConfig,
    logger: Logger
) : BaseNetworkApi(client, networkConfig, logger), NetworkRoomApi {
    

    /**
     * Get a room by its identifier
     * @param token access token
     * @param roomId the room identifier
     * @return room data result
     */
    override suspend fun getRoom(token: String, roomId: String): DomainResult<RoomDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkRoom, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/$roomId")
                method = HttpMethod.Get
                header("Authorization", "Bearer $token")

                logger.d("Getting room with ID: $roomId")
            },
            logger,
            { errorResponse ->
                logger.w("Get room failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Room retrieved successfully: $roomId")
            }
        }.map { networkResponse ->
            networkResponse.toDomain()
        }
    }

    /**
     * Get a list of rooms with filtering and pagination
     * @param token access token
     * @param params query parameters as a map
     * @return paginated list of rooms result
     */
    override suspend fun getRooms(token: String, params: Map<String, Any?>): DomainResult<RoomListDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkRoomList, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms")
                method = HttpMethod.Get
                header("Authorization", "Bearer $token")

                // Add query parameters
                params.forEach { (key, value) ->
                    when (value) {
                        is List<*> -> parameter(key, value.joinToString(","))
                        else -> parameter(key, value)
                    }
                }

                logger.d("Getting rooms list with params: $params")
            },
            logger,
            { errorResponse ->
                logger.w("Get rooms failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Rooms retrieved successfully, count: ${(it.data.items.size)}")
            }
        }.map { networkResponse ->
            networkResponse.toDomain()
        }
    }

    /**
     * Get rooms where the current user is a participant
     * @param token access token
     * @param params query parameters as a map
     * @return paginated list of rooms result
     */
    override suspend fun getMyRooms(token: String, params: Map<String, Any?>): DomainResult<RoomListDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkRoomList, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/my")
                method = HttpMethod.Get
                header("Authorization", "Bearer $token")

                // Add query parameters
                params.forEach { (key, value) ->
                    when (value) {
                        is List<*> -> parameter(key, value.joinToString(","))
                        else -> parameter(key, value)
                    }
                }

                logger.d("Getting my rooms with params: $params")
            },
            logger,
            { errorResponse ->
                logger.w("Get my rooms failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("My rooms retrieved successfully, count: ${(it.data.items.size)}")
            }
        }.map { networkResponse ->
            networkResponse.toDomain()
        }
    }

    /**
     * Create a new room
     * @param token access token
     * @param room room data
     * @return the created room result
     */
    override suspend fun createRoom(token: String, room: RoomDomain): DomainResult<RoomDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkRoom, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms")
                method = HttpMethod.Post
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(room.toNetwork())

                logger.d("Creating new room with name: ${room.name.content}")
            },
            logger,
            { errorResponse ->
                logger.w("Create room failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Room created successfully with ID: ${it.data.id}")
            }
        }.map { networkResponse ->
            networkResponse.toDomain()
        }
    }

    /**
     * Update a room
     * @param token access token
     * @param roomId room identifier
     * @param room updated room data
     * @return the updated room result
     */
    override suspend fun updateRoom(token: String, roomId: String, room: RoomDomain): DomainResult<RoomDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkRoom, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/$roomId")
                method = HttpMethod.Put
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(room.toNetwork())

                logger.d("Updating room: $roomId")
            },
            logger,
            { errorResponse ->
                logger.w("Update room failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Room updated successfully: $roomId")
            }
        }.map { networkResponse ->
            networkResponse.toDomain()
        }
    }

    /**
     * Delete a room
     * @param token access token
     * @param roomId room identifier
     * @return operation result
     */
    override suspend fun deleteRoom(token: String, roomId: String): DomainResult<Unit> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<Unit, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/$roomId")
                method = HttpMethod.Delete
                header("Authorization", "Bearer $token")

                logger.d("Deleting room: $roomId")
            },
            logger,
            { errorResponse ->
                logger.w("Delete room failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Room deleted successfully: $roomId")
            }
        }
    }

    /**
     * Join a room using a code
     * @param token access token
     * @param joinRequest join request with room code
     * @return join response result
     */
    override suspend fun joinRoom(token: String, joinRequest: RoomJoinDomain): DomainResult<RoomJoinResponseDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkRoomJoinResponse, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/join")
                method = HttpMethod.Post
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(joinRequest.toNetwork())

                logger.d("Joining room with code: ${joinRequest.code}")
            },
            logger,
            { errorResponse ->
                logger.w("Join room failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Room joined successfully: ${it.data.roomId}")
            }
        }.map { networkResponse ->
            networkResponse.toDomain()
        }
    }

    /**
     * Get participants of a room
     * @param token access token
     * @param roomId room identifier
     * @return list of participants result
     */
    override suspend fun getRoomParticipants(token: String, roomId: String): DomainResult<List<RoomParticipantDomain>> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<List<NetworkRoomParticipant>, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/$roomId/participants")
                method = HttpMethod.Get
                header("Authorization", "Bearer $token")

                logger.d("Getting participants for room: $roomId")
            },
            logger,
            { errorResponse ->
                logger.w("Get room participants failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Room participants retrieved successfully, count: ${it.data.size}")
            }
        }.map { networkResponse ->
            networkResponse.map { it.toDomain() }
        }
    }

    /**
     * Add a participant to a room
     * @param token access token
     * @param roomId room identifier
     * @param participant participant data
     * @return the added participant result
     */
    override suspend fun addRoomParticipant(token: String, roomId: String, participant: RoomParticipantDomain): DomainResult<RoomParticipantDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkRoomParticipant, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/$roomId/participants")
                method = HttpMethod.Post
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(participant.toNetwork())

                logger.d("Adding participant to room: $roomId, user: ${participant.userId}")
            },
            logger,
            { errorResponse ->
                logger.w("Add room participant failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Participant added successfully to room: $roomId")
            }
        }.map { networkResponse ->
            networkResponse.toDomain()
        }
    }

    /**
     * Update a room participant
     * @param token access token
     * @param roomId room identifier
     * @param userId user identifier
     * @param participant updated participant data
     * @return the updated participant result
     */
    override suspend fun updateRoomParticipant(token: String, roomId: String, userId: String, participant: RoomParticipantDomain): DomainResult<RoomParticipantDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkRoomParticipant, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/$roomId/participants/$userId")
                method = HttpMethod.Put
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(participant.toNetwork())

                logger.d("Updating participant in room: $roomId, user: $userId")
            },
            logger,
            { errorResponse ->
                logger.w("Update room participant failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Participant updated successfully in room: $roomId")
            }
        }.map { networkResponse ->
            networkResponse.toDomain()
        }
    }

    /**
     * Remove a participant from a room
     * @param token access token
     * @param roomId room identifier
     * @param userId user identifier
     * @return operation result
     */
    override suspend fun removeRoomParticipant(token: String, roomId: String, userId: String): DomainResult<Unit> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<Unit, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/$roomId/participants/$userId")
                method = HttpMethod.Delete
                header("Authorization", "Bearer $token")

                logger.d("Removing participant from room: $roomId, user: $userId")
            },
            logger,
            { errorResponse ->
                logger.w("Remove room participant failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Participant removed successfully from room: $roomId")
            }
        }
    }

    /**
     * Get progress records for a room
     * @param token access token
     * @param roomId room identifier
     * @return list of progress records result
     */
    override suspend fun getRoomProgress(token: String, roomId: String): DomainResult<List<RoomProgressDomain>> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<List<NetworkRoomProgress>, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/$roomId/progress")
                method = HttpMethod.Get
                header("Authorization", "Bearer $token")

                logger.d("Getting progress for room: $roomId")
            },
            logger,
            { errorResponse ->
                logger.w("Get room progress failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Room progress retrieved successfully, count: ${it.data.size}")
            }
        }.map { networkResponse ->
            networkResponse.map { it.toDomain() }
        }
    }

    /**
     * Create a progress record
     * @param token access token
     * @param progress progress data
     * @return the created progress record result
     */
    override suspend fun createRoomProgress(token: String, progress: RoomProgressDomain): DomainResult<RoomProgressDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkRoomProgress, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/${progress.roomId}/progress")
                method = HttpMethod.Post
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(progress.toNetwork())

                logger.d("Creating progress for room: ${progress.roomId}, user: ${progress.userId}")
            },
            logger,
            { errorResponse ->
                logger.w("Create room progress failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Progress created successfully for room: ${progress.roomId}")
            }
        }.map { networkResponse ->
            networkResponse.toDomain()
        }
    }

    /**
     * Update a progress record
     * @param token access token
     * @param progressId progress identifier
     * @param progress updated progress data
     * @return the updated progress record result
     */
    override suspend fun updateRoomProgress(token: String, progressId: String, progress: RoomProgressDomain): DomainResult<RoomProgressDomain> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<NetworkRoomProgress, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/${progress.roomId}/progress/$progressId")
                method = HttpMethod.Put
                header("Authorization", "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(progress.toNetwork())

                logger.d("Updating progress: $progressId for room: ${progress.roomId}")
            },
            logger,
            { errorResponse ->
                logger.w("Update room progress failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Progress updated successfully: $progressId")
            }
        }.map { networkResponse ->
            networkResponse.toDomain()
        }
    }

    /**
     * Delete a progress record
     * @param token access token
     * @param progressId progress identifier
     * @return operation result
     */
    override suspend fun deleteRoomProgress(token: String, progressId: String): DomainResult<Unit> {
        val apiUrl = getApiUrl()

        return client.safeSendWithErrorBody<Unit, NetworkRoomErrorResponse>(
            {
                url("$apiUrl/rooms/progress/$progressId")
                method = HttpMethod.Delete
                header("Authorization", "Bearer $token")

                logger.d("Deleting progress: $progressId")
            },
            logger,
            { errorResponse ->
                logger.w("Delete room progress failed: ${errorResponse.getErrorMessage()}")
                DomainError.fromServerCode(
                    serverCode = errorResponse.getErrorCode(),
                    message = errorResponse.getErrorMessage(),
                    details = errorResponse.details
                )
            }
        ).also {
            if (it is DomainResult.Success) {
                logger.i("Progress deleted successfully: $progressId")
            }
        }
    }
}
