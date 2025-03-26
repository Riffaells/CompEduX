package repository.mapper

import api.model.AuthResult as ApiAuthResult
import model.AppError
import model.AuthResult
import model.ErrorCode
import model.User
import model.auth.AuthResponseData

/**
 * Mapper for converting authentication models between layers
 */
object AuthMapper {
    /**
     * Maps User model from data layer to domain User
     *
     * @param user Data layer user model
     * @return Domain user model
     */
    fun mapDataUserToDomainUser(user: model.auth.User): User {
        return User(
            id = user.id,
            username = user.username,
            email = user.email
        )
    }

    /**
     * Creates a successful authentication result
     *
     * @param user User
     * @param token Authentication token
     * @return Successful authentication result
     */
    fun createSuccessResult(user: User?, token: String?): AuthResult<User?> {
        return AuthResult.Success(user, user, token)
    }

    /**
     * Creates an error result
     *
     * @param error Application error
     * @return Error result
     */
    fun <T> createErrorResult(error: AppError): AuthResult<T> {
        return AuthResult.Error(error)
    }

    /**
     * Creates an error result
     *
     * @param message Error message
     * @param errorCode Error code
     * @return Error result
     */
    fun <T> createErrorResult(
        message: String,
        errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR,
        details: String? = null
    ): AuthResult<T> {
        val appError = AppError(
            code = errorCode,
            message = message,
            details = details
        )
        return AuthResult.Error(appError)
    }

    /**
     * Creates a loading result
     *
     * @return Loading result
     */
    fun <T> createLoadingResult(): AuthResult<T> {
        return AuthResult.Loading as AuthResult<T>
    }

    /**
     * Creates an unauthenticated result
     *
     * @return Unauthenticated user result (successful result with null user)
     */
    fun <T> createUnauthenticatedResult(): AuthResult<T> {
        return AuthResult.Success(null, null, null) as AuthResult<T>
    }

    /**
     * Maps ApiAuthResult<T> to domain AuthResult
     *
     * @param result Result from API module
     * @param mapSuccess Function to transform successful result
     * @return Domain model of result
     */
    fun <T, R> mapApiAuthResultToDomain(
        result: ApiAuthResult<T>,
        mapSuccess: (T) -> AuthResult<R>
    ): AuthResult<R> {
        return when (result) {
            is ApiAuthResult.Success -> mapSuccess(result.data)
            is ApiAuthResult.Error -> {
                val appError = AppError(
                    code = ErrorCode.fromCode(result.error.code),
                    message = result.error.message,
                    details = result.error.details
                )
                AuthResult.Error(appError)
            }
            is ApiAuthResult.Loading -> AuthResult.Loading as AuthResult<R>
        }
    }

    /**
     * Maps authentication success response to domain model
     */
    fun mapAuthResponseToDomain(authResponse: AuthResponseData): AuthResult<User> {
        val domainUser = User(
            id = authResponse.userId,
            username = authResponse.username,
            email = authResponse.username // Using username as email since it's not provided in AuthResponse
        )
        return AuthResult.Success(domainUser, domainUser, authResponse.token)
    }

    /**
     * Maps API error to domain error
     */
    fun <T> mapApiErrorToDomain(error: ApiAuthResult.Error<*>): AuthResult<T> {
        val errorCode = when (error.error.code) {
            2001 -> ErrorCode.INVALID_CREDENTIALS
            2002 -> ErrorCode.UNAUTHORIZED
            1003 -> ErrorCode.SERVER_ERROR
            else -> ErrorCode.UNKNOWN_ERROR
        }

        return AuthResult.Error(
            AppError(
                code = errorCode,
                message = error.error.message,
                details = error.error.details
            )
        )
    }
}
