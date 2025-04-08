package api

import model.auth.AuthResponseDomain
import model.auth.LoginRequest
import model.auth.RefreshTokenRequest
import model.auth.RegisterRequest
import model.auth.ServerStatusResponse
import model.User

/**
 * Интерфейс для работы с API аутентификации из домена
 * Определяет контракт для взаимодействия с сетевым API
 */
interface NetworkAuthApi {
    /**
     * Регистрация нового пользователя
     * @param request Данные для регистрации
     * @return Результат операции с данными аутентификации
     */
    suspend fun register(request: RegisterRequest): api.model.AuthResult<AuthResponseDomain>

    /**
     * Авторизация пользователя
     * @param request Данные для авторизации
     * @return Результат операции с данными аутентификации
     */
    suspend fun login(request: LoginRequest): api.model.AuthResult<AuthResponseDomain>

    /**
     * Обновление токена доступа
     * @param request Запрос на обновление токена
     * @return Результат операции с новыми данными аутентификации
     */
    suspend fun refreshToken(request: RefreshTokenRequest): api.model.AuthResult<AuthResponseDomain>

    /**
     * Получение информации о текущем пользователе
     * @param token Токен доступа
     * @return Результат операции с данными пользователя
     */
    suspend fun getCurrentUser(token: String): api.model.AuthResult<User>

    /**
     * Выход из системы (инвалидация токена)
     * @param token Токен доступа
     * @return Результат операции
     */
    suspend fun logout(token: String): api.model.AuthResult<Unit>

    /**
     * Проверка статуса сервера
     * @return Результат операции с данными о статусе сервера
     */
    suspend fun checkServerStatus(): api.model.AuthResult<ServerStatusResponse>

    /**
     * Обновление профиля пользователя
     * @param token Токен доступа
     * @param username Новое имя пользователя
     * @return Результат операции с данными пользователя
     */
    suspend fun updateProfile(token: String, username: String): api.model.AuthResult<User>
}
