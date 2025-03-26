package api.adapter

import api.NetworkAuthApi
import api.auth.AuthApi as KtorAuthApi
import api.dto.AuthResponseDto
import model.AuthResult as KtorAuthResult
import api.model.AuthResult as ApiAuthResult
import model.ApiError
import model.User
import model.auth.AuthResponseData
import model.auth.LoginRequest
import model.auth.RefreshTokenRequest
import model.auth.RegisterRequest
import model.auth.ServerStatusResponse

/**
 * Adapter for transforming between domain and Ktor API interfaces.
 * Implements NetworkAuthApi interface using AuthApi from Ktor.
 */
class NetworkAuthApiAdapter(
    private val ktorAuthApi: KtorAuthApi
) : NetworkAuthApi {

    /**
     * Generic method to handle KtorAuthResult and convert it to ApiAuthResult
     */
    private fun <T, R> handleKtorResult(
        result: KtorAuthResult<T>,
        errorMessage: String,
        transform: (T) -> R
    ): ApiAuthResult<R> {
        return when (result) {
            is KtorAuthResult.Success -> {
                ApiAuthResult.Success(transform(result.data))
            }
            is KtorAuthResult.Error -> {
                ApiAuthResult.Error(
                    ApiError(
                        message = result.error.message,
                        code = result.error.code.hashCode(),
                        details = result.error.details
                    )
                )
            }
            is KtorAuthResult.Loading -> {
                @Suppress("UNCHECKED_CAST")
                ApiAuthResult.Loading as ApiAuthResult<R>
            }
        }
    }

    /**
     * Generic method to handle exceptions in API calls
     */
    private fun <T> handleException(e: Exception, errorMessage: String): ApiAuthResult<T> {
        return ApiAuthResult.Error(
            ApiError(
                message = errorMessage,
                code = -1,
                details = e.message
            )
        )
    }

    /**
     * Convert AuthResponseDto to AuthResponseData
     */
    private fun convertAuthResponseDtoToData(dto: AuthResponseDto): AuthResponseData {
        return AuthResponseData(
            token = dto.token,
            refreshToken = dto.refreshToken,
            userId = dto.user.id,
            username = dto.user.username,
            expiresIn = 0 // ExpiresIn might not be present in DTO
        )
    }

    override suspend fun register(request: RegisterRequest): ApiAuthResult<AuthResponseData> {
        return try {
            val result = ktorAuthApi.register(request)
            handleKtorResult(result, "Error during registration", ::convertAuthResponseDtoToData)
        } catch (e: Exception) {
            handleException(e, "Error during registration")
        }
    }

    override suspend fun login(request: LoginRequest): ApiAuthResult<AuthResponseData> {
        return try {
            val result = ktorAuthApi.login(request)
            handleKtorResult(result, "Error during login", ::convertAuthResponseDtoToData)
        } catch (e: Exception) {
            handleException(e, "Error during login")
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): ApiAuthResult<AuthResponseData> {
        return try {
            val result = ktorAuthApi.refreshToken(request)
            handleKtorResult(result, "Error while refreshing token", ::convertAuthResponseDtoToData)
        } catch (e: Exception) {
            handleException(e, "Error while refreshing token")
        }
    }

    override suspend fun getCurrentUser(token: String): ApiAuthResult<User> {
        return try {
            val result = ktorAuthApi.getCurrentUser(token)
            handleKtorResult(result, "Error while getting user data") { it }
        } catch (e: Exception) {
            handleException(e, "Error while getting user data")
        }
    }

    override suspend fun logout(token: String): ApiAuthResult<Unit> {
        return try {
            val result = ktorAuthApi.logout(token)
            handleKtorResult(result, "Error during logout") { it }
        } catch (e: Exception) {
            handleException(e, "Error during logout")
        }
    }

    override suspend fun checkServerStatus(): ApiAuthResult<ServerStatusResponse> {
        return try {
            val result = ktorAuthApi.checkServerStatus()
            handleKtorResult(result, "Error while checking server status") { it }
        } catch (e: Exception) {
            handleException(e, "Error while checking server status")
        }
    }

    override suspend fun updateProfile(token: String, username: String): ApiAuthResult<User> {
        // The updateProfile method is not available in KtorAuthApi interface
        return ApiAuthResult.Error(
            ApiError(
                message = "Profile update function is not implemented in the current API",
                code = -1,
                details = "This functionality is not available in the KtorAuthApi interface"
            )
        )
    }
}
