package api.adapter

import api.NetworkAuthApi
import api.auth.AuthApi
import api.dto.NetworkAuthResponseDto
import api.model.AuthResultNetwork
import model.ApiError
import model.User
import model.auth.AuthResponseDomain
import model.auth.LoginRequest
import model.auth.RefreshTokenRequest
import model.auth.RegisterRequest
import model.auth.ServerStatusResponse

/**
 * Адаптер для преобразования между доменными и API интерфейсами.
 * Реализует интерфейс NetworkAuthApi, используя AuthApi.
 */
class NetworkAuthApiAdapter(
    private val authApi: AuthApi
) : NetworkAuthApi {

    /**
     * Преобразование NetworkAuthResponseDto в AuthResponseDomain
     */
    private fun convertToAuthResponseDomain(dto: NetworkAuthResponseDto): AuthResponseDomain {
        return AuthResponseDomain(
            accessToken = dto.accessToken,
            refreshToken = dto.refreshToken,
            tokenType = dto.tokenType
        )
    }

    override suspend fun register(request: RegisterRequest): AuthResultNetwork<AuthResponseDomain> {
        return try {
            val result = authApi.register(request)
            when (result) {
                is AuthResultNetwork.Success -> AuthResultNetwork.Success(convertToAuthResponseDomain(result.data))
                is AuthResultNetwork.Error -> AuthResultNetwork.Error(result.error)
                is AuthResultNetwork.Loading -> AuthResultNetwork.Loading
            }
        } catch (e: Exception) {
            AuthResultNetwork.Error(
                ApiError(
                    message = "Ошибка при регистрации: ${e.message}",
                    code = -1,
                    details = e.stackTraceToString()
                )
            )
        }
    }

    override suspend fun login(request: LoginRequest): AuthResultNetwork<AuthResponseDomain> {
        return try {
            val result = authApi.login(request)
            when (result) {
                is AuthResultNetwork.Success -> AuthResultNetwork.Success(convertToAuthResponseDomain(result.data))
                is AuthResultNetwork.Error -> AuthResultNetwork.Error(result.error)
                is AuthResultNetwork.Loading -> AuthResultNetwork.Loading
            }
        } catch (e: Exception) {
            AuthResultNetwork.Error(
                ApiError(
                    message = "Ошибка при входе: ${e.message}",
                    code = -1,
                    details = e.stackTraceToString()
                )
            )
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResultNetwork<AuthResponseDomain> {
        return try {
            val result = authApi.refreshToken(request)
            when (result) {
                is AuthResultNetwork.Success -> AuthResultNetwork.Success(convertToAuthResponseDomain(result.data))
                is AuthResultNetwork.Error -> AuthResultNetwork.Error(result.error)
                is AuthResultNetwork.Loading -> AuthResultNetwork.Loading
            }
        } catch (e: Exception) {
            AuthResultNetwork.Error(
                ApiError(
                    message = "Ошибка при обновлении токена: ${e.message}",
                    code = -1,
                    details = e.stackTraceToString()
                )
            )
        }
    }

    override suspend fun getCurrentUser(token: String): AuthResultNetwork<User> {
        return try {
            authApi.getCurrentUser(token)
        } catch (e: Exception) {
            AuthResultNetwork.Error(
                ApiError(
                    message = "Ошибка при получении данных пользователя: ${e.message}",
                    code = -1,
                    details = e.stackTraceToString()
                )
            )
        }
    }

    override suspend fun logout(token: String): AuthResultNetwork<Unit> {
        return try {
            authApi.logout(token)
        } catch (e: Exception) {
            AuthResultNetwork.Error(
                ApiError(
                    message = "Ошибка при выходе из системы: ${e.message}",
                    code = -1,
                    details = e.stackTraceToString()
                )
            )
        }
    }

    override suspend fun checkServerStatus(): AuthResultNetwork<ServerStatusResponse> {
        return try {
            authApi.checkServerStatus()
        } catch (e: Exception) {
            AuthResultNetwork.Error(
                ApiError(
                    message = "Ошибка при проверке статуса сервера: ${e.message}",
                    code = -1,
                    details = e.stackTraceToString()
                )
            )
        }
    }

    override suspend fun updateProfile(token: String, username: String): AuthResultNetwork<User> {
        return try {
            authApi.updateProfile(token, username)
        } catch (e: Exception) {
            AuthResultNetwork.Error(
                ApiError(
                    message = "Ошибка при обновлении профиля: ${e.message}",
                    code = -1,
                    details = e.stackTraceToString()
                )
            )
        }
    }
}
