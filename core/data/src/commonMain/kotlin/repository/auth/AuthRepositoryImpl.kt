package repository.auth

import api.auth.NetworkAuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import logging.Logger
import model.DomainResult
import model.UserDomain
import model.auth.*

/**
 * Реализация репозитория аутентификации
 * Обеспечивает взаимодействие между доменным слоем и сетевым API
 */
class AuthRepositoryImpl(
    private val networkAuthApi: NetworkAuthApi,
    private val tokenRepository: TokenRepository,
    private val logger: Logger
) : AuthRepository {

    // Текущее состояние аутентификации
    private val _authState = MutableStateFlow<AuthStateDomain>(AuthStateDomain.Unauthenticated)
    override val authState: StateFlow<AuthStateDomain> = _authState.asStateFlow()

    override suspend fun register(
        username: String,
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain> {
        logger.d("Register attempt for $email")

        // Создаем запрос для API
        val request = RegisterRequestDomain(
            username = username,
            email = email,
            password = password
        )

        // Выполняем запрос через API
        val result = networkAuthApi.register(request)

        // Обрабатываем результат
        if (result is DomainResult.Success) {
            // Сохраняем токены в TokenRepository
            saveTokens(result.data)

            // Запрашиваем данные пользователя
            getCurrentUser()
        }

        return result
    }

    override suspend fun login(
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain> {
        logger.d("Login attempt for $email")

        // Создаем запрос для API
        val request = LoginRequestDomain(
            email = email,
            password = password
        )

        // Выполняем запрос через API
        val result = networkAuthApi.login(request)

        // Обрабатываем результат
        if (result is DomainResult.Success) {
            // Сохраняем токены в TokenRepository
            saveTokens(result.data)

            // Запрашиваем данные пользователя
            getCurrentUser()
        }

        return result
    }

    override suspend fun logout(): DomainResult<Unit> {
        logger.d("Logout attempt")

        // Получаем токен для запроса
        val accessToken = tokenRepository.getAccessToken()

        if (accessToken == null) {
            logger.w("Cannot logout: No access token")
            _authState.value = AuthStateDomain.Unauthenticated
            return DomainResult.Success(Unit)
        }

        // Выполняем запрос на logout
        val result = networkAuthApi.logout(accessToken)

        // В любом случае очищаем токены локально
        tokenRepository.clearTokens()
        _authState.value = AuthStateDomain.Unauthenticated

        return result
    }

    override suspend fun getCurrentUser(): DomainResult<UserDomain> {
        logger.d("Getting current user")

        // Получаем токен доступа
        val accessToken = tokenRepository.getAccessToken()

        if (accessToken == null) {
            logger.w("Cannot get user: No access token")
            _authState.value = AuthStateDomain.Unauthenticated
            return DomainResult.Error(model.DomainError.authError("Не авторизован"))
        }

        // Выполняем запрос
        val result = networkAuthApi.getCurrentUser(accessToken)

        // Обновляем состояние аутентификации
        when (result) {
            is DomainResult.Success -> {
                _authState.value = AuthStateDomain.Authenticated(result.data)
            }

            is DomainResult.Error -> {
                // Если ошибка связана с токеном, пробуем обновить его
                if (result.error.isAuthError() && refreshTokenIfNeeded()) {
                    // Если токен успешно обновлен, пробуем снова получить пользователя
                    return getCurrentUser()
                } else {
                    _authState.value = AuthStateDomain.Unauthenticated
                }
            }

            is DomainResult.Loading -> {
                // Не меняем состояние при загрузке
            }
        }

        return result
    }

    override suspend fun isAuthenticated(): Boolean {
        // Проверяем наличие access token
        if (!tokenRepository.hasAccessToken()) {
            return false
        }

        // Пытаемся получить пользователя, если токен есть
        val userResult = getCurrentUser()
        return userResult is DomainResult.Success
    }

    override suspend fun updateProfile(username: String): DomainResult<UserDomain> {
        logger.d("Updating profile: $username")

        // Получаем токен доступа
        val accessToken = tokenRepository.getAccessToken() ?: run {
            logger.w("Cannot update profile: No access token")
            return DomainResult.Error(model.DomainError.authError("Не авторизован"))
        }

        // Выполняем запрос
        val result = networkAuthApi.updateProfile(accessToken, username)

        // Обновляем состояние аутентификации при успехе
        if (result is DomainResult.Success) {
            _authState.value = AuthStateDomain.Authenticated(result.data)
        }

        return result
    }

    override suspend fun checkServerStatus(): DomainResult<ServerStatusResponseDomain> {
        logger.d("Checking server status")
        return networkAuthApi.checkServerStatus()
    }

    override suspend fun refreshTokenIfNeeded(): Boolean {
        // Проверяем наличие refresh token
        val refreshToken = tokenRepository.getRefreshToken() ?: run {
            logger.w("Cannot refresh token: No refresh token")
            return false
        }

        logger.d("Attempting to refresh token")

        // Создаем запрос на обновление токена
        val request = RefreshTokenRequestDomain(refreshToken)

        // Выполняем запрос
        val result = networkAuthApi.refreshToken(request)

        return when (result) {
            is DomainResult.Success -> {
                // Сохраняем новые токены
                saveTokens(result.data)
                logger.i("Token refreshed successfully")
                true
            }

            is DomainResult.Error -> {
                logger.e("Token refresh failed: ${result.error.message}")
                // Если не удалось обновить токен, очищаем все токены
                tokenRepository.clearTokens()
                _authState.value = AuthStateDomain.Unauthenticated
                false
            }

            is DomainResult.Loading -> {
                // При загрузке возвращаем false, так как токен еще не обновлен
                false
            }
        }
    }

    /**
     * Сохраняет токены в хранилище
     */
    private suspend fun saveTokens(authResponse: AuthResponseDomain) {
        logger.d("Saving auth tokens")
        tokenRepository.saveAccessToken(authResponse.accessToken)
        tokenRepository.saveRefreshToken(authResponse.refreshToken)
        tokenRepository.saveTokenType(authResponse.tokenType)
    }
}
