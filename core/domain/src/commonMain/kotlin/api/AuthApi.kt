package api

import model.AuthResult
import model.User
import model.auth.AuthResponseData
import model.auth.ServerStatusResponse
import model.auth.RefreshTokenRequest

/**
 * Интерфейс API для работы с аутентификацией (домен)
 * Определяет контракт для API в домене, реализация находится в модуле network
 */
interface AuthApi {
    /**
     * Регистрация нового пользователя
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @param username Имя пользователя
     * @return Результат операции с данными аутентификации
     */
    suspend fun register(username: String, email: String, password: String): AuthResult<AuthResponseData>

    /**
     * Авторизация пользователя
     * @param email Email пользователя (или имя пользователя)
     * @param password Пароль пользователя
     * @return Результат операции с данными аутентификации
     */
    suspend fun login(email: String, password: String): AuthResult<AuthResponseData>

    /**
     * Получение информации о текущем пользователе
     * @return Результат операции с данными пользователя
     */
    suspend fun getCurrentUser(): AuthResult<User>

    /**
     * Выход из системы
     * @return Результат операции
     */
    suspend fun logout(): AuthResult<Unit>

    /**
     * Проверка статуса сервера
     * @return Результат операции с данными о статусе сервера
     */
    suspend fun checkServerStatus(): AuthResult<ServerStatusResponse>

    /**
     * Обновление профиля пользователя
     * @param username Новое имя пользователя
     * @return Результат операции с обновленными данными пользователя
     */
    suspend fun updateProfile(username: String): AuthResult<User>

    /**
     * Обновление токена доступа
     * @param refreshToken Токен обновления
     * @return Результат операции с данными аутентификации
     */
    suspend fun refreshToken(request: RefreshTokenRequest): AuthResult<AuthResponseData>
}
