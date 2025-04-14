package api.auth

import model.DomainResult
import model.UserDomain
import model.auth.AuthResponseDomain
import model.auth.RefreshTokenRequestDomain
import model.auth.ServerStatusResponseDomain

/**
 * Интерфейс для доменного слоя для работы с аутентификацией
 * Абстрагирует логику работы с API и токенами для удобства использования в use case
 */
interface AuthApi {
    /**
     * Регистрация нового пользователя
     * @param username имя пользователя
     * @param email электронная почта
     * @param password пароль
     * @return результат операции с данными аутентификации
     */
    suspend fun register(
        username: String,
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain>

    /**
     * Вход в систему с использованием email и пароля
     * @param email электронная почта
     * @param password пароль
     * @return результат операции с данными аутентификации
     */
    suspend fun login(
        email: String,
        password: String
    ): DomainResult<AuthResponseDomain>

    /**
     * Получение информации о текущем пользователе
     * @return результат операции с данными пользователя
     */
    suspend fun getCurrentUser(): DomainResult<UserDomain>

    /**
     * Выход из системы
     * @return результат операции
     */
    suspend fun logout(): DomainResult<Unit>

    /**
     * Обновление токена доступа
     * @param request запрос на обновление токена
     * @return результат операции с новыми данными аутентификации
     */
    suspend fun refreshToken(request: RefreshTokenRequestDomain): DomainResult<AuthResponseDomain>

    /**
     * Обновление профиля пользователя
     * @param username новое имя пользователя
     * @return результат операции с данными пользователя
     */
    suspend fun updateProfile(username: String): DomainResult<UserDomain>

    /**
     * Проверка статуса сервера
     * @return результат операции с данными о статусе сервера
     */
    suspend fun checkServerStatus(): DomainResult<ServerStatusResponseDomain>
}
