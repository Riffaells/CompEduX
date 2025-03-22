package repository.mapper

import api.dto.UserResponse
import model.AppError
import model.AuthResult as DomainAuthResult
import model.ErrorCode
import model.User
import model.auth.AuthError
import model.auth.AuthResult as DataAuthResult

/**
 * Маппер для преобразования DTO в доменные объекты
 */
object AuthMapper {
    /**
     * Преобразует UserResponse в User
     *
     * @param response DTO пользователя
     * @return Доменная модель пользователя
     */
    fun mapUserResponseToUser(response: UserResponse): User {
        return User(
            id = response.id,
            email = response.email,
            username = response.username
        )
    }

    /**
     * Создает успешный результат авторизации
     *
     * @param user Пользователь
     * @param token Токен аутентификации
     * @return Результат успешной авторизации
     */
    fun createSuccessResult(user: User, token: String): DomainAuthResult {
        return DomainAuthResult.Success(user, token)
    }

    /**
     * Создает результат с ошибкой
     *
     * @param error Ошибка приложения
     * @return Результат с ошибкой
     */
    fun createErrorResult(error: AppError): DomainAuthResult {
        return DomainAuthResult.Error(error)
    }

    /**
     * Создает результат с ошибкой
     *
     * @param message Сообщение об ошибке
     * @param errorCode Код ошибки
     * @return Результат с ошибкой
     */
    fun createErrorResult(
        message: String,
        errorCode: ErrorCode = ErrorCode.UNKNOWN_ERROR,
        details: String? = null
    ): DomainAuthResult {
        val appError = AppError(
            code = errorCode,
            message = message,
            details = details
        )
        return DomainAuthResult.Error(appError)
    }

    /**
     * Создает результат загрузки
     *
     * @return Результат загрузки
     */
    fun createLoadingResult(): DomainAuthResult {
        return DomainAuthResult.Loading
    }

    /**
     * Создает результат отсутствия аутентификации
     *
     * @return Результат неавторизованного пользователя (успешный результат с null пользователем)
     */
    fun createUnauthenticatedResult(): DomainAuthResult {
        return DomainAuthResult.Success(null, null)
    }

    /**
     * Преобразует AuthResult<T> из data в доменный AuthResult
     *
     * @param result Результат из data модуля
     * @param mapSuccess Функция преобразования успешного результата
     * @return Доменная модель результата
     */
    fun <T> mapDataAuthResultToDomain(
        result: DataAuthResult<T>,
        mapSuccess: (T) -> DomainAuthResult
    ): DomainAuthResult {
        return when (result) {
            is DataAuthResult.Success -> mapSuccess(result.data)
            is DataAuthResult.Error -> {
                val appError = AppError(
                    code = ErrorCode.fromCode(result.error.code.hashCode()),
                    message = result.error.message,
                    details = null
                )
                DomainAuthResult.Error(appError)
            }
            is DataAuthResult.Loading -> DomainAuthResult.Loading
        }
    }

    /**
     * Преобразует AuthError в AppError
     *
     * @param error Ошибка аутентификации
     * @return Доменная модель ошибки
     */
    fun mapAuthErrorToAppError(error: AuthError): AppError {
        return AppError(
            code = when (error.code) {
                "invalid_credentials" -> ErrorCode.INVALID_CREDENTIALS
                "unauthorized" -> ErrorCode.UNAUTHORIZED
                "token_expired" -> ErrorCode.TOKEN_EXPIRED
                "network_error" -> ErrorCode.NETWORK_ERROR
                "server_error" -> ErrorCode.SERVER_ERROR
                else -> ErrorCode.UNKNOWN_ERROR
            },
            message = error.message,
            details = null
        )
    }
}
