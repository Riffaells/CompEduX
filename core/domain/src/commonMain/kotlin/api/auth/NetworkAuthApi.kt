package api.auth

import model.DomainResult
import model.UserDomain
import model.auth.*

/**
 * Интерфейс для работы с API аутентификации из доменного слоя
 * Определяет контракт для взаимодействия с сетевым API
 */
interface NetworkAuthApi {
    /**
     * Регистрация нового пользователя
     * @param request данные для регистрации
     * @return результат операции с данными аутентификации
     */
    suspend fun register(request: RegisterRequestDomain): DomainResult<AuthResponseDomain>

    /**
     * Вход в систему
     * @param request данные для входа
     * @return результат операции с данными аутентификации
     */
    suspend fun login(request: LoginRequestDomain): DomainResult<AuthResponseDomain>

    /**
     * Обновление токена доступа
     * @param request запрос на обновление токена
     * @return результат операции с новыми данными аутентификации
     */
    suspend fun refreshToken(request: RefreshTokenRequestDomain): DomainResult<AuthResponseDomain>

    /**
     * Получение информации о текущем пользователе
     * @param token токен доступа
     * @return результат операции с данными пользователя
     */
    suspend fun getCurrentUser(token: String): DomainResult<UserDomain>

    /**
     * Verifies token validity and gets user information
     * This is a standalone function that doesn't affect automatic token verification
     * @param token access token to verify
     * @return operation result with user data
     */
    suspend fun verifyToken(token: String): DomainResult<UserDomain>

    /**
     * Выход из системы
     * @param token токен доступа
     * @return результат операции
     */
    suspend fun logout(token: String): DomainResult<Unit>

    /**
     * Проверка статуса сервера
     * @return результат операции с данными о статусе сервера
     */
    suspend fun checkServerStatus(): DomainResult<ServerStatusResponseDomain>

    /**
     * Обновление профиля пользователя
     * @param token токен доступа
     * @param username новое имя пользователя
     * @return результат операции с данными пользователя
     */
    suspend fun updateProfile(token: String, username: String): DomainResult<UserDomain>
}
