package model

/**
 * Коды ошибок приложения
 */
enum class ErrorCode(val value: Int) {
    // Общие ошибки
    UNKNOWN_ERROR(1000),
    NETWORK_ERROR(1001),
    TIMEOUT(1002),
    SERVER_ERROR(1003),

    // Ошибки авторизации
    INVALID_CREDENTIALS(2001),
    UNAUTHORIZED(2002),
    TOKEN_EXPIRED(2003),
    ACCOUNT_LOCKED(2004),

    // Ошибки валидации
    VALIDATION_ERROR(3001),
    EMAIL_ALREADY_EXISTS(3002),
    USERNAME_ALREADY_EXISTS(3003),
    INVALID_EMAIL_FORMAT(3004),
    PASSWORD_TOO_SHORT(3005),
    INVALID_USERNAME(3006);

    companion object {
        /**
         * Получение кода ошибки по числовому значению
         */
        fun fromCode(code: Int): ErrorCode {
            return values().find { it.value == code } ?: UNKNOWN_ERROR
        }
    }
}
