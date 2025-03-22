package repository.mapper

import api.dto.ApiError
import io.github.aakira.napier.Napier
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.AppError
import model.ErrorCode
import repository.mapper.ErrorMapper as ErrorMapperInterface

/**
 * Реализация маппера для преобразования ошибок API в доменные ошибки приложения
 */
object ErrorMapper : ErrorMapperInterface {
    /**
     * Преобразует HttpResponse с ошибкой в AppError
     *
     * @param response HTTP-ответ с ошибкой
     * @return Доменная модель ошибки
     */
    suspend fun mapHttpResponseToAppError(response: HttpResponse): AppError {
        return try {
            // Пытаемся прочитать ошибку в формате ApiError
            val apiError = response.body<ApiError>()
            mapApiErrorToAppError(apiError)
        } catch (e: Exception) {
            Napier.e("Failed to parse error response: ${e.message}", e)

            // Если не удалось прочитать в формате ApiError, создаем обобщенную ошибку
            val errorCode = mapHttpStatusToErrorCode(response.status)
            AppError(
                code = errorCode,
                message = response.status.description,
                details = "Status: ${response.status.value}"
            )
        }
    }

    /**
     * Преобразует ApiError в AppError
     *
     * @param apiError Ошибка API
     * @return Доменная модель ошибки
     */
    fun mapApiErrorToAppError(apiError: ApiError): AppError {
        val errorCode = ErrorCode.fromCode(apiError.errorCode)
        return AppError(
            code = errorCode,
            message = apiError.message,
            details = apiError.details
        )
    }

    /**
     * Преобразует исключение в AppError
     *
     * @param throwable Исключение
     * @return Доменная модель ошибки
     */
    fun mapThrowableToAppError(throwable: Throwable): AppError {
        return when (throwable) {
            is java.net.UnknownHostException -> AppError(
                code = ErrorCode.NETWORK_ERROR,
                message = "Нет соединения с сервером",
                details = throwable.message
            )
            is java.net.SocketTimeoutException -> AppError(
                code = ErrorCode.CONNECTION_TIMEOUT,
                message = "Превышено время ожидания ответа",
                details = throwable.message
            )
            is kotlinx.serialization.SerializationException -> AppError(
                code = ErrorCode.PARSING_ERROR,
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
     *
     * @param status HTTP-статус
     * @return Код ошибки
     */
    private fun mapHttpStatusToErrorCode(status: HttpStatusCode): ErrorCode {
        return when (status.value) {
            401 -> ErrorCode.UNAUTHORIZED
            403 -> ErrorCode.UNAUTHORIZED
            404 -> ErrorCode.USER_NOT_FOUND
            409 -> ErrorCode.USER_ALREADY_EXISTS
            422 -> ErrorCode.VALIDATION_ERROR
            in 500..599 -> ErrorCode.SERVER_ERROR
            else -> ErrorCode.UNKNOWN_ERROR
        }
    }

    /**
     * Преобразовать ошибку API в доменную ошибку приложения
     */
    fun mapApiErrorToDomain(apiError: ApiError): AppError {
        val errorCode = ErrorCode.fromCode(apiError.errorCode)

        return AppError(
            code = errorCode,
            message = apiError.message.ifEmpty { errorCode.description },
            details = apiError.details
        )
    }

    /**
     * Создать доменную ошибку на основе исключения или другой ошибки
     */
    override fun createNetworkError(throwable: Throwable): AppError {
        return AppError(
            code = ErrorCode.NETWORK_ERROR,
            message = throwable.message ?: ErrorCode.NETWORK_ERROR.description,
            details = throwable.cause?.message
        )
    }

    /**
     * Создать доменную ошибку о таймауте
     */
    override fun createTimeoutError(): AppError {
        return AppError(
            code = ErrorCode.TIMEOUT,
            message = ErrorCode.TIMEOUT.description
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
            message = ErrorCode.TOKEN_EXPIRED.description
        )
    }

    /**
     * Создать доменную ошибку об отсутствии авторизации
     */
    override fun createUnauthorizedError(): AppError {
        return AppError(
            code = ErrorCode.UNAUTHORIZED,
            message = ErrorCode.UNAUTHORIZED.description
        )
    }
}
