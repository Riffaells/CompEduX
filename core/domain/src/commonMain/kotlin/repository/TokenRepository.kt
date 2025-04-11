package repository

/**
 * Interface for managing authentication tokens
 */
interface TokenRepository {
    /**
     * Save access token
     * @param token the access token
     */
    suspend fun saveAccessToken(token: String)

    /**
     * Get access token
     * @return the access token or null if not found
     */
    suspend fun getAccessToken(): String?

    /**
     * Save refresh token
     * @param token the refresh token
     */
    suspend fun saveRefreshToken(token: String)

    /**
     * Get refresh token
     * @return the refresh token or null if not found
     */
    suspend fun getRefreshToken(): String?

    /**
     * Save token type
     * @param type the token type (usually "bearer")
     */
    suspend fun saveTokenType(type: String)

    /**
     * Get token type
     * @return the token type
     */
    suspend fun getTokenType(): String

    /**
     * Check if storage contains access token
     * @return true if access token exists
     */
    suspend fun hasAccessToken(): Boolean

    /**
     * Check if storage contains refresh token
     * @return true if refresh token exists
     */
    suspend fun hasRefreshToken(): Boolean

    /**
     * Clear all tokens
     */
    suspend fun clearTokens()
}
