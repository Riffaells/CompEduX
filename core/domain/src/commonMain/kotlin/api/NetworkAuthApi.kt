package api

import model.DomainResult
import model.UserDomain
import model.auth.AuthResponseDomain
import model.auth.LoginRequestDomain
import model.auth.RefreshTokenRequestDomain
import model.auth.RegisterRequestDomain
import model.auth.ServerStatusDomain

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
     * Выход из системы
     * @param token токен доступа
     * @return результат операции
     */
    suspend fun logout(token: String): DomainResult<Unit>

    /**
     * Проверка статуса сервера
     * @return результат операции с данными о статусе сервера
     */
    suspend fun checkServerStatus(): DomainResult<ServerStatusDomain>

    /**
     * Обновление профиля пользователя
     * @param token токен доступа
     * @param username новое имя пользователя
     * @return результат операции с данными пользователя
     */
    suspend fun updateProfile(token: String, username: String): DomainResult<UserDomain>
}
