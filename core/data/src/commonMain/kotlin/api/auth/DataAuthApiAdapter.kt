package api.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logging.Logger
import model.DomainError
import model.DomainResult
import model.UserDomain
import model.auth.*
import repository.auth.TokenRepository

/**
 * Адаптер, связывающий AuthApi с NetworkAuthApi
 * Позволяет абстрагировать доменный слой от деталей реализации сетевых запросов
 */
class DataAuthApiAdapter(
    private val networkAuthApi: NetworkAuthApi,
    private val tokenRepository: TokenRepository,
    private val logger: Logger
) : AuthApi {

    override suspend fun register(
        username: String,
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain> = withContext(Dispatchers.Default) {
        logger.d("DataAuthApiAdapter: register($username, $email, ***)")

        val request = RegisterRequestDomain(
            username = username,
            email = email,
            password = password
        )

        mapApiResult(networkAuthApi.register(request))
    }

    override suspend fun login(
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain> = withContext(Dispatchers.Default) {
        logger.d("DataAuthApiAdapter: login($email, ***)")

        val request = LoginRequestDomain(
            email = email,
            password = password
        )

        mapApiResult(networkAuthApi.login(request))
    }

    override suspend fun getCurrentUser(): DomainResult<UserDomain> = withContext(Dispatchers.Default) {
        logger.d("DataAuthApiAdapter: getCurrentUser()")

        // Получаем сохраненный токен
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot get current user: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Токен доступа не найден"))
        }

        mapApiResult(networkAuthApi.getCurrentUser(token))
    }

    override suspend fun logout(): DomainResult<Unit> = withContext(Dispatchers.Default) {
        logger.d("DataAuthApiAdapter: logout()")

        // Получаем сохраненный токен
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot logout: No access token")
            // Все равно очищаем токены
            tokenRepository.clearTokens()
            return@withContext DomainResult.Success(Unit)
        }

        val result = mapApiResult(networkAuthApi.logout(token))

        // Очищаем токены в любом случае
        tokenRepository.clearTokens()

        result
    }

    override suspend fun refreshToken(request: RefreshTokenRequestDomain): DomainResult<AuthResponseDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataAuthApiAdapter: refreshToken(***)")
            mapApiResult(networkAuthApi.refreshToken(request))
        }

    override suspend fun updateProfile(username: String): DomainResult<UserDomain> = withContext(Dispatchers.Default) {
        logger.d("DataAuthApiAdapter: updateProfile($username)")

        // Получаем сохраненный токен
        val token = tokenRepository.getAccessToken()

        if (token == null) {
            logger.w("Cannot update profile: No access token")
            return@withContext DomainResult.Error(DomainError.authError("Токен доступа не найден"))
        }

        mapApiResult(networkAuthApi.updateProfile(token, username))
    }

    override suspend fun checkServerStatus(): DomainResult<ServerStatusResponseDomain> =
        withContext(Dispatchers.Default) {
            logger.d("DataAuthApiAdapter: checkServerStatus()")
            val result = networkAuthApi.checkServerStatus()

            when (result) {
                is model.DomainResult.Success -> {
                    // Преобразуем ServerStatusDomain в ServerStatusResponseDomain
                    val serverStatus = result.data
                    val responseStatus = ServerStatusResponseDomain(
                        status = serverStatus.status,
                        version = serverStatus.version,
                        uptime = serverStatus.uptime
                    )
                    DomainResult.Success(responseStatus)
                }

                is model.DomainResult.Error -> DomainResult.Error(result.error)
                is model.DomainResult.Loading -> DomainResult.Loading
            }
        }

    /**
     * Преобразует результат API в доменный результат
     */
    private fun <T> mapApiResult(result: model.DomainResult<T>): DomainResult<T> {
        return when (result) {
            is model.DomainResult.Success -> DomainResult.Success(result.data)
            is model.DomainResult.Error -> DomainResult.Error(result.error)
            is model.DomainResult.Loading -> DomainResult.Loading
        }
    }
}
