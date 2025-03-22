package repository.mapper

import model.AppError

/**
 * Интерфейс маппера для преобразования ошибок в доменные ошибки
 */
interface ErrorMapper {
    /**
     * Создать доменную ошибку на основе исключения
     */
    fun createNetworkError(throwable: Throwable): AppError

    /**
     * Создать доменную ошибку о таймауте
     */
    fun createTimeoutError(): AppError

    /**
     * Создать доменную ошибку о сбое сервера
     */
    fun createServerError(statusCode: Int): AppError

    /**
     * Создать доменную ошибку об истечении срока токена
     */
    fun createTokenExpiredError(): AppError

    /**
     * Создать доменную ошибку об отсутствии авторизации
     */
    fun createUnauthorizedError(): AppError
}
