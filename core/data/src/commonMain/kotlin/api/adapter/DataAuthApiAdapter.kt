package api.adapter

import api.AuthApi
import api.NetworkAuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.AppError
import model.AuthResult
import model.ErrorCode
import model.User
import model.auth.*
import repository.mapper.DataAuthMapper
import settings.MultiplatformSettings
import api.model.AuthResult as NetworkAuthResult
import api.model.AuthResultNetwork as ApiAuthResult

/**
 * Адаптер для связи сетевого API аутентификации с доменным слоем
 */
class DataAuthApiAdapter(
    private val networkAuthApi: NetworkAuthApi,
    private val settings: MultiplatformSettings
) : AuthApi {

    override suspend fun register(
        username: String,
        email: String,
        password: String
    ): AuthResult<AuthResponseDomain> = withContext(Dispatchers.IO) {
        val request = RegisterRequest(
            username = username,
            email = email,
            password = password
        )

        val result = networkAuthApi.register(request)

        // Преобразуем результат сетевого слоя в доменный
        when (result) {
            is NetworkAuthResult.Success -> {
                // Сохраняем токены
                saveTokens(result.data.accessToken, result.data.refreshToken)

                // Возвращаем успешный результат
                AuthResult.Success(result.data)
            }
            is NetworkAuthResult.Error -> AuthResult.Error(
                AppError(
                    code = ErrorCode.fromCode(result.error.code),
                    message = result.error.message,
                    details = result.error.details
                )
            )
            is NetworkAuthResult.Loading -> AuthResult.Loading as AuthResult<AuthResponseDomain>
        }
    }

    override suspend fun login(
        username: String,
        password: String
    ): AuthResult<AuthResponseDomain> = withContext(Dispatchers.IO) {
        val request = LoginRequest(
            username = username,
            password = password
        )

        val result = networkAuthApi.login(request)

        // Преобразуем результат сетевого слоя в доменный
        when (result) {
            is NetworkAuthResult.Success -> {
                // Сохраняем токены
                saveTokens(result.data.accessToken, result.data.refreshToken)

                // Возвращаем успешный результат
                AuthResult.Success(result.data)
            }
            is NetworkAuthResult.Error -> AuthResult.Error(
                AppError(
                    code = ErrorCode.fromCode(result.error.code),
                    message = result.error.message,
                    details = result.error.details
                )
            )
            is NetworkAuthResult.Loading -> AuthResult.Loading as AuthResult<AuthResponseDomain>
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResult<AuthResponseDomain> = withContext(Dispatchers.IO) {
        // Проверяем, есть ли refresh token
        val refreshToken = settings.security.getRefreshToken() ?: return@withContext AuthResult.Error(
            AppError(
                code = ErrorCode.UNAUTHORIZED,
                message = "Отсутствует refresh token",
                details = "Требуется повторная авторизация"
            )
        )

        // Создаем запрос
        val refreshRequest = RefreshTokenRequest(
            refreshToken = refreshToken
        )

        val result = networkAuthApi.refreshToken(refreshRequest)

        // Преобразуем результат сетевого слоя в доменный
        when (result) {
            is NetworkAuthResult.Success -> {
                // Сохраняем токены
                saveTokens(result.data.accessToken, result.data.refreshToken)

                // Возвращаем успешный результат
                AuthResult.Success(result.data)
            }
            is NetworkAuthResult.Error -> AuthResult.Error(
                AppError(
                    code = ErrorCode.fromCode(result.error.code),
                    message = result.error.message,
                    details = result.error.details
                )
            )
            is NetworkAuthResult.Loading -> AuthResult.Loading as AuthResult<AuthResponseDomain>
        }
    }

    override suspend fun logout(): AuthResult<Unit> = withContext(Dispatchers.IO) {
        // Получаем токен доступа
        val accessToken = settings.security.getAuthToken() ?: return@withContext AuthResult.Error(
            AppError(
                code = ErrorCode.UNAUTHORIZED,
                message = "Отсутствует токен доступа",
                details = "Пользователь не авторизован"
            )
        )

        val result = networkAuthApi.logout(accessToken)

        // В любом случае очищаем токены при выходе
        settings.security.clearAuthToken()
        settings.security.clearRefreshToken()

        // Преобразуем результат сетевого слоя в доменный
        when (result) {
            is NetworkAuthResult.Success -> AuthResult.Success(Unit)
            is NetworkAuthResult.Error -> AuthResult.Error(
                AppError(
                    code = ErrorCode.fromCode(result.error.code),
                    message = result.error.message,
                    details = result.error.details
                )
            )
            is NetworkAuthResult.Loading -> AuthResult.Loading as AuthResult<Unit>
        }
    }

    override suspend fun getCurrentUser(): AuthResult<User> = withContext(Dispatchers.IO) {
        // Получаем токен доступа
        val accessToken = settings.security.getAuthToken() ?: return@withContext AuthResult.Error(
            AppError(
                code = ErrorCode.UNAUTHORIZED,
                message = "Отсутствует токен доступа",
                details = "Пользователь не авторизован"
            )
        )

        val result = networkAuthApi.getCurrentUser(accessToken)

        // Преобразуем результат сетевого слоя в доменный
        when (result) {
            is NetworkAuthResult.Success -> AuthResult.Success(result.data)
            is NetworkAuthResult.Error -> AuthResult.Error(
                AppError(
                    code = ErrorCode.fromCode(result.error.code),
                    message = result.error.message,
                    details = result.error.details
                )
            )
            is NetworkAuthResult.Loading -> AuthResult.Loading as AuthResult<User>
        }
    }

    override suspend fun checkServerStatus(): AuthResult<ServerStatusResponse> = withContext(Dispatchers.IO) {
        val result = networkAuthApi.checkServerStatus()

        // Преобразуем результат сетевого слоя в доменный
        when (result) {
            is NetworkAuthResult.Success -> AuthResult.Success(result.data)
            is NetworkAuthResult.Error -> AuthResult.Error(
                AppError(
                    code = ErrorCode.fromCode(result.error.code),
                    message = result.error.message,
                    details = result.error.details
                )
            )
            is NetworkAuthResult.Loading -> AuthResult.Loading as AuthResult<ServerStatusResponse>
        }
    }

    override suspend fun updateProfile(username: String): AuthResult<User> = withContext(Dispatchers.IO) {
        // Получаем токен доступа
        val accessToken = settings.security.getAuthToken() ?: return@withContext AuthResult.Error(
            AppError(
                code = ErrorCode.UNAUTHORIZED,
                message = "Отсутствует токен доступа",
                details = "Пользователь не авторизован"
            )
        )

        val result = networkAuthApi.updateProfile(accessToken, username)

        // Преобразуем результат сетевого слоя в доменный
        when (result) {
            is NetworkAuthResult.Success -> AuthResult.Success(result.data)
            is NetworkAuthResult.Error -> AuthResult.Error(
                AppError(
                    code = ErrorCode.fromCode(result.error.code),
                    message = result.error.message,
                    details = result.error.details
                )
            )
            is NetworkAuthResult.Loading -> AuthResult.Loading as AuthResult<User>
        }
    }

    /**
     * Сохраняет токены доступа и обновления в хранилище
     */
    private fun saveTokens(accessToken: String, refreshToken: String) {
        settings.security.saveAuthToken(accessToken)
        settings.security.saveRefreshToken(refreshToken)
    }
}
