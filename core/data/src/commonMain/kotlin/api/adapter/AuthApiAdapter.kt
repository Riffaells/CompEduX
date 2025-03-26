package api.adapter

import api.NetworkAuthApi
import model.AppError
import model.AuthResult
import model.ErrorCode
import model.auth.*
import settings.MultiplatformSettings
import api.AuthApi as DomainAuthApi
import api.model.AuthResult as NetworkAuthResult
import model.User as NetworkUser
import model.User as DomainUser

/**
 * Adapter for transformation between domain and network API interfaces.
 * Implements the AuthApi interface from the domain layer using NetworkAuthApi.
 */
class AuthApiAdapter(
    private val networkAuthApi: NetworkAuthApi,
    private val settings: MultiplatformSettings
) : DomainAuthApi {

    /**
     * Creates an error object with specified code and message
     */
    private fun createError(code: ErrorCode, message: String, details: String? = null): AppError {
        return AppError(
            code = code,
            message = message,
            details = details
        )
    }

    /**
     * Handles exceptions and creates an error object
     */
    private fun <T> handleException(e: Exception, errorMessage: String): AuthResult<T> {
        return AuthResult.Error(
            createError(
                code = ErrorCode.NETWORK_ERROR,
                message = errorMessage,
                details = e.message
            )
        )
    }

    /**
     * Creates a domain user object from a network model
     */
    private fun createDomainUser(networkUser: NetworkUser, email: String? = null): DomainUser {
        return DomainUser(
            id = networkUser.id,
            username = networkUser.username,
            email = email ?: networkUser.email
        )
    }

    /**
     * Saves authentication tokens
     */
    private fun saveTokens(token: String, refreshToken: String?) {
        settings.security.saveAuthToken(token)
        refreshToken?.let {
            try {
                settings.security.saveRefreshToken(it)
            } catch (e: NotImplementedError) {
                println("Warning: saveRefreshToken not available in SecuritySettings")
            }
        }
    }

    /**
     * Clears authentication tokens
     */
    private fun clearTokens() {
        settings.security.clearAuthToken()
        try {
            settings.security.clearRefreshToken()
        } catch (e: NotImplementedError) {
            println("Warning: clearRefreshToken not available in SecuritySettings")
        }
    }

    /**
     * Processes network request results and transforms them into the domain model
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T, R> handleNetworkResult(
        result: NetworkAuthResult<T>,
        successHandler: (T) -> AuthResult.Success<R>,
        errorCode: ErrorCode
    ): AuthResult<R> {
        return when (result) {
            is NetworkAuthResult.Success -> successHandler(result.data)
            is NetworkAuthResult.Error -> {
                // Explicit type casting to resolve covariance issues
                AuthResult.Error(
                    createError(
                        code = errorCode,
                        message = result.error.message,
                        details = result.error.code.toString()
                    )
                )
            }
            is NetworkAuthResult.Loading -> {
                // Using explicit type casting with suppressed warning
                AuthResult.Loading as AuthResult<R>
            }
        }
    }

    override suspend fun register(username: String, email: String, password: String): AuthResult<AuthResponseData> {
        try {
            // Create request for network API
            val request = RegisterRequest(username, email, password)

            // Call network API
            val result = networkAuthApi.register(request)

            // Transform result to domain model
            return handleNetworkResult(
                result = result,
                errorCode = ErrorCode.INVALID_CREDENTIALS,
                successHandler = { data ->
                    // Сохраняем токен
                    saveTokens(data.token, data.refreshToken)

                    // Create domain user model from AuthResponseData fields
                    val domainUser = DomainUser(
                        id = data.userId,
                        username = data.username,
                        email = email
                    )

                    AuthResult.Success(data, domainUser, data.token)
                }
            )
        } catch (e: Exception) {
            return handleException(e, "Error during registration")
        }
    }

    override suspend fun login(email: String, password: String): AuthResult<AuthResponseData> {
        try {
            // Create request for network API
            val request = LoginRequest(email, password)

            // Call network API
            val result = networkAuthApi.login(request)

            // Transform result to domain model
            return handleNetworkResult(
                result = result,
                errorCode = ErrorCode.INVALID_CREDENTIALS,
                successHandler = { data ->
                    // Сохраняем токен
                    saveTokens(data.token, data.refreshToken)

                    // Create domain user model from AuthResponseData fields
                    val domainUser = DomainUser(
                        id = data.userId,
                        username = data.username,
                        email = email
                    )

                    AuthResult.Success(data, domainUser, data.token)
                }
            )
        } catch (e: Exception) {
            return handleException(e, "Error during login")
        }
    }

    override suspend fun getCurrentUser(): AuthResult<DomainUser> {
        try {
            // Get token
            val token = settings.security.getAuthToken()
            if (token == null) {
                return AuthResult.Error(
                    createError(
                        code = ErrorCode.UNAUTHORIZED,
                        message = "Authorization token is missing",
                        details = "Login required"
                    )
                )
            }

            // Call network API
            val result = networkAuthApi.getCurrentUser(token)

            // Transform result to domain model
            return handleNetworkResult(
                result = result,
                errorCode = ErrorCode.UNAUTHORIZED,
                successHandler = { data ->
                    // Create domain user model
                    val domainUser = createDomainUser(data)
                    AuthResult.Success(domainUser, domainUser, token)
                }
            )
        } catch (e: Exception) {
            return handleException(e, "Error while getting user data")
        }
    }

    override suspend fun logout(): AuthResult<Unit> {
        try {
            // Get token
            val token = settings.security.getAuthToken()
            if (token == null) {
                return AuthResult.Success(Unit, null, null)
            }

            // Call network API
            val result = networkAuthApi.logout(token)

            // Clear tokens in any case
            clearTokens()

            // On logout, always return success regardless of the request result
            return AuthResult.Success(Unit, null, null)
        } catch (e: Exception) {
            // Clear tokens on error
            clearTokens()

            return AuthResult.Success(Unit, null, null)
        }
    }

    override suspend fun checkServerStatus(): AuthResult<ServerStatusResponse> {
        try {
            // Call network API
            val result = networkAuthApi.checkServerStatus()

            // Transform result to domain model
            return handleNetworkResult(
                result = result,
                errorCode = ErrorCode.SERVER_ERROR,
                successHandler = { data ->
                    AuthResult.Success(data, null, null)
                }
            )
        } catch (e: Exception) {
            return handleException(e, "Error while checking server status")
        }
    }

    override suspend fun updateProfile(username: String): AuthResult<DomainUser> {
        try {
            // Get token
            val token = settings.security.getAuthToken()
            if (token == null) {
                return AuthResult.Error(
                    createError(
                        code = ErrorCode.UNAUTHORIZED,
                        message = "User is not authorized",
                        details = "Login required to update profile"
                    )
                )
            }

            // Call network API
            val result = networkAuthApi.updateProfile(token, username)

            // Transform result to domain model
            return handleNetworkResult(
                result = result,
                errorCode = ErrorCode.UNAUTHORIZED,
                successHandler = { data ->
                    // Create domain user model
                    val domainUser = createDomainUser(data)
                    AuthResult.Success(domainUser, domainUser, token)
                }
            )
        } catch (e: Exception) {
            return handleException(e, "Error while updating profile")
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResult<AuthResponseData> {
        try {
            // Call network API
            val result = networkAuthApi.refreshToken(request)

            // Transform result to domain model
            return handleNetworkResult(
                result = result,
                errorCode = ErrorCode.UNAUTHORIZED,
                successHandler = { data ->
                    // Save new tokens
                    saveTokens(data.token, data.refreshToken)

                    // Create domain user model from AuthResponseData fields
                    val domainUser = DomainUser(
                        id = data.userId,
                        username = data.username,
                        email = data.username // Using username as email since it's not provided in AuthResponseData
                    )

                    AuthResult.Success(data, domainUser, data.token)
                }
            )
        } catch (e: Exception) {
            return handleException(e, "Error while refreshing token")
        }
    }
}
