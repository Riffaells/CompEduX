package repository.auth

import kotlinx.coroutines.flow.StateFlow
import model.AuthResult
import model.User
import model.auth.AuthResponseDomain
import model.auth.ServerStatusResponse

/**
 * Репозиторий для работы с аутентификацией
 * Это интерфейс в domain слое, реализации находятся в data слое
 */
interface AuthRepository {
    /**
     * Текущее состояние аутентификации
     */
    val authState: StateFlow<AuthState>

    /**
     * Регистрация нового пользователя
     * @param username Имя пользователя
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @return Результат операции регистрации
     */
    suspend fun register(
        email: String,
        password: String,
        username: String
    ): AuthResult<AuthResponseDomain>

    /**
     * Авторизация пользователя
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @return Результат операции авторизации
     */
    suspend fun login(email: String, password: String): AuthResult<AuthResponseDomain>

    /**
     * Выход из системы
     * @return Результат операции выхода
     */
    suspend fun logout(): AuthResult<Unit>

    /**
     * Получение информации о текущем пользователе
     * @return Текущий пользователь или null, если пользователь не авторизован
     */
    suspend fun getCurrentUser(): User?

    /**
     * Проверяет, аутентифицирован ли пользователь
     * @return true, если пользователь аутентифицирован, false в противном случае
     */
    suspend fun isAuthenticated(): Boolean

    /**
     * Обновляет профиль пользователя
     * @param username Новое имя пользователя
     * @return Результат операции обновления профиля
     */
    suspend fun updateProfile(username: String): AuthResult<User>

    /**
     * Проверяет статус сервера
     * @return Результат операции проверки статуса
     */
    suspend fun checkServerStatus(): AuthResult<ServerStatusResponse>

    /**
     * Обновляет токен доступа, если он истек
     * @return true, если токен успешно обновлен или не требует обновления, false в противном случае
     */
    suspend fun refreshTokenIfNeeded(): Boolean
}
