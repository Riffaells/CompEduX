package repository.mapper

import model.AppError
import model.ErrorCode

/**
 * Реализация маппера для преобразования ошибок API в доменные ошибки приложения
 */
class DataErrorMapper : repository.mapper.ErrorMapper {
    /**
     * Преобразует ApiError в AppError
     */
    fun mapApiErrorToAppError(errorCode: Int, message: String, details: String?): AppError {
        val domainErrorCode = ErrorCode.fromCode(errorCode)
        return AppError(
            code = domainErrorCode,
            message = message,
            details = details
        )
    }

    /**
     * Преобразует исключение в AppError
     */
    fun mapThrowableToAppError(throwable: Throwable): AppError {
        return when (throwable) {
            is java.net.UnknownHostException -> AppError(
                code = ErrorCode.NETWORK_ERROR,
                message = "Нет соединения с сервером",
                details = throwable.message
            )
            is java.net.SocketTimeoutException -> AppError(
                code = ErrorCode.TIMEOUT,
                message = "Превышено время ожидания ответа",
                details = throwable.message
            )
            is kotlinx.serialization.SerializationException -> AppError(
                code = ErrorCode.UNKNOWN_ERROR,
                message = "Ошибка обработки данных",
                details = throwable.message
            )
            else -> AppError(
                code = ErrorCode.UNKNOWN_ERROR,
                message = throwable.message ?: "Неизвестная ошибка",
                details = throwable.javaClass.simpleName
            )
        }
    }

    /**
     * Преобразует HTTP-статус в ErrorCode
     */
    fun mapHttpStatusToErrorCode(statusCode: Int): ErrorCode {
        return when (statusCode) {
            401 -> ErrorCode.UNAUTHORIZED
            403 -> ErrorCode.UNAUTHORIZED
            404 -> ErrorCode.UNKNOWN_ERROR
            409 -> ErrorCode.USERNAME_ALREADY_EXISTS
            422 -> ErrorCode.VALIDATION_ERROR
            in 500..599 -> ErrorCode.SERVER_ERROR
            else -> ErrorCode.UNKNOWN_ERROR
        }
    }

    /**
     * Создать доменную ошибку на основе исключения или другой ошибки
     */
    override fun createNetworkError(throwable: Throwable): AppError {
        return AppError(
            code = ErrorCode.NETWORK_ERROR,
            message = throwable.message ?: "Сетевая ошибка",
            details = throwable.cause?.message
        )
    }

    /**
     * Создать доменную ошибку о таймауте
     */
    override fun createTimeoutError(): AppError {
        return AppError(
            code = ErrorCode.TIMEOUT,
            message = "Превышено время ожидания"
        )
    }

    /**
     * Создать доменную ошибку о сбое сервера
     */
    override fun createServerError(statusCode: Int): AppError {
        return AppError(
            code = ErrorCode.SERVER_ERROR,
            message = "Ошибка сервера (код $statusCode)",
            details = "Сервер вернул статус $statusCode"
        )
    }

    /**
     * Создать доменную ошибку об истечении срока токена
     */
    override fun createTokenExpiredError(): AppError {
        return AppError(
            code = ErrorCode.TOKEN_EXPIRED,
            message = "Срок действия токена истек"
        )
    }

    /**
     * Создать доменную ошибку об отсутствии авторизации
     */
    override fun createUnauthorizedError(): AppError {
        return AppError(
            code = ErrorCode.UNAUTHORIZED,
            message = "Не авторизован"
        )
    }
}
