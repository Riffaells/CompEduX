package client

/**
 * Интерфейс для хранения токенов аутентификации
 */
interface TokenStorage {
    /**
     * Сохранение токена доступа
     * @param token токен доступа
     */
    fun saveAccessToken(token: String)

    /**
     * Получение токена доступа
     * @return токен доступа или null, если токен не найден
     */
    fun getAccessToken(): String?

    /**
     * Сохранение токена обновления
     * @param token токен обновления
     */
    fun saveRefreshToken(token: String)

    /**
     * Получение токена обновления
     * @return токен обновления или null, если токен не найден
     */
    fun getRefreshToken(): String?

    /**
     * Очистка всех токенов
     */
    fun clearTokens()
}

/**
 * Простая реализация хранилища токенов в памяти
 * Для продакшена следует использовать безопасное хранилище
 */
class InMemoryTokenStorage : TokenStorage {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    override fun saveAccessToken(token: String) {
        accessToken = token
    }

    override fun getAccessToken(): String? {
        return accessToken
    }

    override fun saveRefreshToken(token: String) {
        refreshToken = token
    }

    override fun getRefreshToken(): String? {
        return refreshToken
    }

    override fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
}
