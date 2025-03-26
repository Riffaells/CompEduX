package api.auth

import kotlinx.coroutines.flow.StateFlow
import model.auth.AuthResponseData

/**
 * Интерфейс менеджера токенов для хранения и обновления токенов аутентификации
 */
interface ITokenManager {
    /**
     * Текущее состояние аутентификации
     */
    val authState: StateFlow<AuthState>

    /**
     * Сохраняет данные аутентификации
     */
    fun saveAuthData(authResponse: AuthResponseData)

    /**
     * Получает токен доступа
     */
    fun getAccessToken(): String?

    /**
     * Проверяет, истек ли токен доступа
     */
    fun isTokenExpired(): Boolean

    /**
     * Проверяет, аутентифицирован ли пользователь
     */
    fun isAuthenticated(): Boolean

    /**
     * Обновляет токен доступа, если он истек
     * @return true, если токен успешно обновлен, false в противном случае
     */
    suspend fun refreshTokenIfNeeded(): Boolean

    /**
     * Очищает данные аутентификации
     */
    fun clearAuthData()

    /**
     * Инициализирует состояние аутентификации при запуске приложения
     */
    fun initAuthState()
}
