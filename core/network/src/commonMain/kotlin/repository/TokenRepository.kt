package repository

/**
 * Интерфейс репозитория для управления токенами аутентификации
 */
interface TokenRepository {
    /**
     * Получить текущий токен доступа
     * @return токен доступа или null, если пользователь не аутентифицирован
     */
    suspend fun getAccessToken(): String?

    /**
     * Получить токен обновления
     * @return токен обновления или null, если пользователь не аутентифицирован
     */
    suspend fun getRefreshToken(): String?

    /**
     * Сохранить токены аутентификации
     * @param accessToken токен доступа
     * @param refreshToken токен обновления
     * @param expiresIn время жизни токена в секундах (опционально)
     */
    suspend fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long = 0)

    /**
     * Очистить сохраненные токены (при выходе из системы)
     */
    suspend fun clearTokens()

    /**
     * Проверить срок действия токена
     * @return true если токен действителен, false если истек или отсутствует
     */
    suspend fun isTokenValid(): Boolean
}
