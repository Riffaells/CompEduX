package model

/**
 * Domain error class
 * @param code error code
 * @param message error message
 * @param details additional error details
 */
data class DomainError(
    val code: ErrorCode,
    val message: String = "",
    val details: String? = null
) {
    /**
     * Checks if the error is related to authentication issues
     * Returns true if the error is one of: UNAUTHORIZED, FORBIDDEN, TOKEN_EXPIRED, INVALID_TOKEN
     * This method is used to determine if token refresh should be attempted
     * @return true if the error is authentication-related
     */
    fun isAuthError(): Boolean {
        return code == ErrorCode.UNAUTHORIZED ||
                code == ErrorCode.FORBIDDEN ||
                code == ErrorCode.TOKEN_EXPIRED ||
                code == ErrorCode.INVALID_TOKEN
    }

    companion object {
        /**
         * Create a network error
         */
        fun networkError(message: String = "error_network", details: String? = null): DomainError {
            return DomainError(
                code = ErrorCode.NETWORK_ERROR,
                message = message,
                details = details
            )
        }

        /**
         * Create a server error
         */
        fun serverError(message: String = "error_server", details: String? = null): DomainError {
            return DomainError(
                code = ErrorCode.SERVER_ERROR,
                message = message,
                details = details
            )
        }

        /**
         * Create an authentication error
         */
        fun authError(message: String = "error_auth", details: String? = null): DomainError {
            return DomainError(
                code = ErrorCode.UNAUTHORIZED,
                message = message,
                details = details
            )
        }

        /**
         * Create an unknown error
         */
        fun unknownError(message: String = "error_unknown", details: String? = null): DomainError {
            return DomainError(
                code = ErrorCode.UNKNOWN_ERROR,
                message = message,
                details = details
            )
        }

        /**
         * Create a validation error
         */
        fun validationError(message: String = "error_validation", details: String? = null): DomainError {
            return DomainError(
                code = ErrorCode.VALIDATION_ERROR,
                message = message,
                details = details
            )
        }
        
        /**
         * Create a not found error
         */
        fun notFoundError(message: String = "error_not_found", details: String? = null): DomainError {
            return DomainError(
                code = ErrorCode.NOT_FOUND,
                message = message,
                details = details
            )
        }

        /**
         * Create an error from server error code
         */
        fun fromServerCode(serverCode: Int, message: String, details: String? = null): DomainError {
            val code = when (serverCode) {
                401, 403 -> ErrorCode.UNAUTHORIZED
                404 -> ErrorCode.NOT_FOUND
                409 -> ErrorCode.CONFLICT
                422 -> ErrorCode.VALIDATION_ERROR
                in 500..599 -> ErrorCode.SERVER_ERROR
                else -> ErrorCode.UNKNOWN_ERROR
            }

            return DomainError(
                code = code,
                message = message,
                details = details
            )
        }
    }
}

/**
 * Enumeration of error codes
 */
enum class ErrorCode(val code: Int) {
    UNKNOWN_ERROR(0),
    NETWORK_ERROR(1000),
    SERVER_ERROR(1001),
    TIMEOUT(1002),
    CONNECTION_REFUSED(1003),
    CONNECTION_FAILED(1004),
    CONNECTION_RESET(1005),
    HOST_UNREACHABLE(1006),
    SOCKET_ERROR(1007),
    UNAUTHORIZED(2000),
    FORBIDDEN(2001),
    NOT_FOUND(2002),
    CONFLICT(2003),
    VALIDATION_ERROR(3000),
    INVALID_CREDENTIALS(3001),
    USER_ALREADY_EXISTS(3002),
    TOKEN_EXPIRED(4000),
    INVALID_TOKEN(4001);

    companion object {
        /**
         * Get ErrorCode from server error code
         */
        fun fromServerCode(serverCode: Int): ErrorCode {
            return when (serverCode) {
                401 -> UNAUTHORIZED
                403 -> FORBIDDEN
                404 -> NOT_FOUND
                409 -> CONFLICT
                422 -> VALIDATION_ERROR
                in 500..599 -> SERVER_ERROR
                else -> UNKNOWN_ERROR
            }
        }
    }
}
