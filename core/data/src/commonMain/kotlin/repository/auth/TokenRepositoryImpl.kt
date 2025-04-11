package repository.auth

import logging.Logger
import settings.SecuritySettings

/**
 * Реализация репозитория для работы с токенами аутентификации
 * Использует SecuritySettings для сохранения токенов
 */
class TokenRepositoryImpl(
    private val securitySettings: SecuritySettings,
    private val logger: Logger
) : TokenRepository {

    override suspend fun saveAccessToken(token: String) {
        logger.d("Saving access token")
        securitySettings.saveAuthToken(token)
    }

    override suspend fun getAccessToken(): String? {
        val token = securitySettings.getAuthToken()
        logger.d("Getting access token: ${if (token != null) "Found" else "Not found"}")
        return token
    }

    override suspend fun saveRefreshToken(token: String) {
        logger.d("Saving refresh token")
        securitySettings.saveRefreshToken(token)
    }

    override suspend fun getRefreshToken(): String? {
        val token = securitySettings.getRefreshToken()
        logger.d("Getting refresh token: ${if (token != null) "Found" else "Not found"}")
        return token
    }

    override suspend fun saveTokenType(type: String) {
        logger.d("Saving token type: $type")
        // Тип токена не сохраняется в SecuritySettings, поэтому можно добавить его
        // как дополнительную настройку или реализовать сохранение в будущем
    }

    override suspend fun getTokenType(): String {
        // Возвращаем стандартный тип токена, так как в SecuritySettings нет такого поля
        val defaultType = "bearer"
        logger.d("Getting token type: $defaultType")
        return defaultType
    }

    override suspend fun hasAccessToken(): Boolean {
        val hasToken = securitySettings.hasAuthToken()
        logger.d("Checking access token: $hasToken")
        return hasToken
    }

    override suspend fun hasRefreshToken(): Boolean {
        val hasToken = securitySettings.getRefreshToken() != null
        logger.d("Checking refresh token: $hasToken")
        return hasToken
    }

    override suspend fun clearTokens() {
        logger.d("Clearing all tokens")
        securitySettings.clearAuthToken()
        securitySettings.clearRefreshToken()
    }
}
