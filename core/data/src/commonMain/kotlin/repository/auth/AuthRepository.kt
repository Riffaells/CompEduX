package repository.auth

import api.auth.AuthState
import kotlinx.coroutines.flow.StateFlow
import model.auth.*

/**
 * Репозиторий для работы с аутентификацией
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
     * @param firstName Имя (опционально)
     * @param lastName Фамилия (опционально)
     * @return Результат операции
     */
    suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String? = null,
        lastName: String? = null
    ): AuthResult<Unit>

    /**
     * Авторизация пользователя
     * @param username Имя пользователя
     * @param password Пароль пользователя
     * @return Результат операции
     */
    suspend fun login(username: String, password: String): AuthResult<Unit>

    /**
     * Выход из системы
     * @return Результат операции
     */
    suspend fun logout(): AuthResult<Unit>

    /**
     * Получение информации о текущем пользователе
     * @return Результат операции с данными пользователя
     */
    suspend fun getCurrentUser(): AuthResult<User>

    /**
     * Проверка статуса сервера
     * @return Результат операции с данными о статусе сервера
     */
    suspend fun checkServerStatus(): AuthResult<ServerStatusResponse>

    /**
     * Проверяет, аутентифицирован ли пользователь
     * @return true, если пользователь аутентифицирован, false в противном случае
     */
    fun isAuthenticated(): Boolean

    /**
     * Обновляет токен доступа, если он истек
     * @return true, если токен успешно обновлен или не требует обновления, false в противном случае
     */
    suspend fun refreshTokenIfNeeded(): Boolean
}
