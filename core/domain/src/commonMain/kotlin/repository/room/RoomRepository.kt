package repository.room

import model.DomainResult
import model.room.*

/**
 * Репозиторий для работы с комнатами
 */
interface RoomRepository {
    /**
     * Получить комнату по идентификатору
     *
     * @param roomId Идентификатор комнаты
     * @return Результат операции с данными комнаты или ошибкой
     */
    suspend fun getRoom(roomId: String): DomainResult<RoomDomain>

    /**
     * Получить список комнат с пагинацией и фильтрацией
     *
     * @param params Параметры запроса
     * @return Результат операции со списком комнат или ошибкой
     */
    suspend fun getRooms(params: RoomQueryParams): DomainResult<RoomListDomain>

    /**
     * Получить список комнат текущего пользователя
     *
     * @param params Параметры запроса
     * @return Результат операции со списком комнат или ошибкой
     */
    suspend fun getMyRooms(params: RoomQueryParams): DomainResult<RoomListDomain>

    /**
     * Создать новую комнату
     *
     * @param room Данные для создания комнаты
     * @return Результат операции с созданной комнатой или ошибкой
     */
    suspend fun createRoom(room: RoomUpdateDomain): DomainResult<RoomDomain>

    /**
     * Обновить информацию о комнате
     *
     * @param roomId Идентификатор комнаты
     * @param room Данные для обновления
     * @return Результат операции с обновленной комнатой или ошибкой
     */
    suspend fun updateRoom(roomId: String, room: RoomUpdateDomain): DomainResult<RoomDomain>

    /**
     * Удалить комнату
     *
     * @param roomId Идентификатор комнаты
     * @return Результат операции с информацией об успешном удалении или ошибкой
     */
    suspend fun deleteRoom(roomId: String): DomainResult<Boolean>

    /**
     * Присоединиться к комнате по коду
     *
     * @param joinRequest Запрос на присоединение к комнате
     * @return Результат операции с информацией о присоединении или ошибкой
     */
    suspend fun joinRoom(joinRequest: RoomJoinDomain): DomainResult<RoomJoinResponseDomain>
}
