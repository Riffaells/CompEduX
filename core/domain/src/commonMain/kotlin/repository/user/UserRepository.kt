package repository.user

/**
 * Репозиторий для работы с данными пользователя
 */
interface UserRepository {
    /**
     * Получить ID текущего авторизованного пользователя
     * @return ID пользователя или null, если пользователь не авторизован
     */
    fun getCurrentUserId(): String?

    /**
     * Получить имя текущего авторизованного пользователя
     * @return Имя пользователя или null, если пользователь не авторизован
     */
    fun getCurrentUserName(): String?
} 