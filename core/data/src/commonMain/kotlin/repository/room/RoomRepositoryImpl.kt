package repository.room

import api.room.NetworkRoomApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logging.Logger
import model.DomainError
import model.DomainResult
import model.room.RoomDomain
import model.room.RoomJoinDomain
import model.room.RoomJoinResponseDomain
import model.room.RoomListDomain
import model.room.RoomQueryParams
import model.room.RoomUpdateDomain
import repository.auth.TokenRepository

/**
 * Реализация репозитория комнат
 * Обеспечивает взаимодействие между доменным слоем и сетевым API
 */
class RoomRepositoryImpl(
    private val networkRoomApi: NetworkRoomApi,
    private val tokenRepository: TokenRepository,
    private val logger: Logger
) : RoomRepository {

    override suspend fun getRoom(roomId: String): DomainResult<RoomDomain> = withContext(Dispatchers.Default) {
        logger.d("RoomRepositoryImpl: getRoom($roomId)")

        // Проверяем наличие токена доступа
        val accessToken = tokenRepository.getAccessToken()
        if (accessToken == null) {
            logger.w("Cannot get room: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
        }

        // Выполняем запрос
        val result = networkRoomApi.getRoom(accessToken, roomId)

        // Обрабатываем результат
        when (result) {
            is DomainResult.Success -> {
                logger.i("Room retrieved successfully: $roomId")
            }

            is DomainResult.Error -> {
                logger.e("Failed to get room: ${result.error.message}")

                // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                    return@withContext getRoom(roomId)
                }
            }

            is DomainResult.Loading -> {
                // Не выполняем действий в состоянии загрузки
            }
        }

        result
    }

    override suspend fun getRooms(params: RoomQueryParams): DomainResult<RoomListDomain> =
        withContext(Dispatchers.Default) {
            logger.d("RoomRepositoryImpl: getRooms(${params.toMap().keys})")

            // Проверяем наличие токена доступа
            val accessToken = tokenRepository.getAccessToken()
            if (accessToken == null) {
                logger.w("Cannot get rooms: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
            }

            // Преобразуем параметры запроса в Map для API
            val queryParams = params.toMap()

            // Выполняем запрос
            val result = networkRoomApi.getRooms(accessToken, queryParams)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Rooms retrieved successfully, count: ${result.data.items.size}")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to get rooms: ${result.error.message}")

                    // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                    if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                        return@withContext getRooms(params)
                    }
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }
        
    override suspend fun getMyRooms(params: RoomQueryParams): DomainResult<RoomListDomain> =
        withContext(Dispatchers.Default) {
            logger.d("RoomRepositoryImpl: getMyRooms(${params.toMap().keys})")

            // Проверяем наличие токена доступа
            val accessToken = tokenRepository.getAccessToken()
            if (accessToken == null) {
                logger.w("Cannot get my rooms: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
            }

            // Преобразуем параметры запроса в Map для API
            val queryParams = params.toMap()

            // Выполняем запрос
            val result = networkRoomApi.getMyRooms(accessToken, queryParams)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("My rooms retrieved successfully, count: ${result.data.items.size}")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to get my rooms: ${result.error.message}")

                    // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                    if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                        return@withContext getMyRooms(params)
                    }
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    override suspend fun createRoom(room: RoomUpdateDomain): DomainResult<RoomDomain> =
        withContext(Dispatchers.Default) {
            logger.d("RoomRepositoryImpl: createRoom(${room.name.content})")

            // Проверяем наличие токена доступа
            val accessToken = tokenRepository.getAccessToken()
            if (accessToken == null) {
                logger.w("Cannot create room: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
            }

            // Создаем базовую модель комнаты из данных обновления
            val roomDomain = RoomDomain(
                id = "",  // ID будет присвоен сервером
                name = room.name,
                description = room.description,
                courseId = room.courseId ?: "",  // Получаем courseId из объекта обновления
                ownerId = "",   // Будет установлено сервером
                code = "",      // Будет сгенерировано сервером
                status = room.status ?: model.room.RoomStatusDomain.PENDING,
                maxParticipants = room.maxParticipants ?: 0,
                settings = room.settings
            )

            // Выполняем запрос
            val result = networkRoomApi.createRoom(accessToken, roomDomain)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Room created successfully: ${result.data.id}")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to create room: ${result.error.message}")

                    // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                    if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                        return@withContext createRoom(room)
                    }
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    override suspend fun updateRoom(roomId: String, room: RoomUpdateDomain): DomainResult<RoomDomain> =
        withContext(Dispatchers.Default) {
            logger.d("RoomRepositoryImpl: updateRoom($roomId)")

            // Проверяем наличие токена доступа
            val accessToken = tokenRepository.getAccessToken()
            if (accessToken == null) {
                logger.w("Cannot update room: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
            }

            // Сначала получаем текущую комнату для обновления только нужных полей
            val currentRoomResult = getRoom(roomId)
            if (currentRoomResult is DomainResult.Error) {
                return@withContext currentRoomResult
            }
            
            val currentRoom = (currentRoomResult as DomainResult.Success).data
            
            // Создаем обновленную модель комнаты
            val updatedRoom = currentRoom.copy(
                name = room.name,
                description = room.description ?: currentRoom.description,
                status = room.status ?: currentRoom.status,
                maxParticipants = room.maxParticipants ?: currentRoom.maxParticipants,
                settings = room.settings ?: currentRoom.settings
            )

            // Выполняем запрос
            val result = networkRoomApi.updateRoom(accessToken, roomId, updatedRoom)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Room updated successfully: $roomId")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to update room: ${result.error.message}")

                    // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                    if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                        return@withContext updateRoom(roomId, room)
                    }
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    override suspend fun deleteRoom(roomId: String): DomainResult<Boolean> = withContext(Dispatchers.Default) {
        logger.d("RoomRepositoryImpl: deleteRoom($roomId)")

        // Проверяем наличие токена доступа
        val accessToken = tokenRepository.getAccessToken()
        if (accessToken == null) {
            logger.w("Cannot delete room: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
        }

        // Выполняем запрос
        val result = networkRoomApi.deleteRoom(accessToken, roomId)

        // Обрабатываем результат и преобразуем в нужный тип
        when (result) {
            is DomainResult.Success -> {
                logger.i("Room deleted successfully: $roomId")
                DomainResult.Success(true)
            }

            is DomainResult.Error -> {
                logger.e("Failed to delete room: ${result.error.message}")

                // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                    return@withContext deleteRoom(roomId)
                }
                
                DomainResult.Error(result.error)
            }

            is DomainResult.Loading -> {
                DomainResult.Loading
            }
        }
    }
    
    override suspend fun joinRoom(joinRequest: RoomJoinDomain): DomainResult<RoomJoinResponseDomain> = 
        withContext(Dispatchers.Default) {
            logger.d("RoomRepositoryImpl: joinRoom(${joinRequest.code})")

            // Проверяем наличие токена доступа
            val accessToken = tokenRepository.getAccessToken()
            if (accessToken == null) {
                logger.w("Cannot join room: No access token")
                return@withContext DomainResult.Error(DomainError.authError("Не авторизован"))
            }

            // Выполняем запрос
            val result = networkRoomApi.joinRoom(accessToken, joinRequest)

            // Обрабатываем результат
            when (result) {
                is DomainResult.Success -> {
                    logger.i("Room joined successfully: ${result.data.roomId}")
                }

                is DomainResult.Error -> {
                    logger.e("Failed to join room: ${result.error.message}")

                    // Если ошибка связана с токеном и токен можно обновить, пробуем снова
                    if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                        return@withContext joinRoom(joinRequest)
                    }
                }

                is DomainResult.Loading -> {
                    // Не выполняем действий в состоянии загрузки
                }
            }

            result
        }

    /**
     * Обновляет токен доступа, если это необходимо и возможно
     * @return true, если токен был успешно обновлен
     */
    private suspend fun refreshTokenIfNeeded(): Boolean {
        logger.d("Attempting to refresh token")

        // Проверяем наличие refresh token
        val refreshToken = tokenRepository.getRefreshToken() ?: run {
            logger.w("Cannot refresh token: No refresh token")
            return false
        }

        // Здесь должен быть вызов метода обновления токена из AuthRepository
        // Для простоты и независимости от реализации AuthRepository,
        // пока просто возвращаем false
        logger.w("Token refresh not implemented in RoomRepository")
        return false
    }
} 