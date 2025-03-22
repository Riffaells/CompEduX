package repository.mapper

import api.dto.UserResponse
import model.AppError
import model.AuthResult
import model.ErrorCode
import model.User

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
    fun createSuccessResult(user: User, token: String): AuthResult {
        return AuthResult.Success(user, token)
    }

    /**
     * Создает результат с ошибкой
     *
     * @param error Ошибка приложения
     * @return Результат с ошибкой
     */
    fun createErrorResult(error: AppError): AuthResult {
        return AuthResult.Error(error)
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
    ): AuthResult {
        val appError = AppError(
            code = errorCode,
            message = message,
            details = details
        )
        return AuthResult.Error(appError)
    }

    /**
     * Создает результат загрузки
     *
     * @return Результат загрузки
     */
    fun createLoadingResult(): AuthResult {
        return AuthResult.Loading
    }

    /**
     * Создает результат отсутствия аутентификации
     *
     * @return Результат неавторизованного пользователя (успешный результат с null пользователем)
     */
    fun createUnauthenticatedResult(): AuthResult {
        return AuthResult.Success(null, null)
    }
}
