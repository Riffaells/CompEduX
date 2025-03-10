package api.auth

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.auth.AuthResponse
import model.auth.RefreshTokenRequest

/**
 * Менеджер токенов для хранения и обновления токенов аутентификации
 */
class TokenManager(
    private val settings: Settings,
    private val authApi: AuthApi
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Ключи для хранения токенов
    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USERNAME = "username"
        const val KEY_EXPIRES_AT = "expires_at"
    }

    /**
     * Сохраняет данные аутентификации
     */
    fun saveAuthData(authResponse: AuthResponse) {
        settings.putString(KEY_ACCESS_TOKEN, authResponse.token)
        settings.putString(KEY_REFRESH_TOKEN, authResponse.refreshToken)
        settings.putString(KEY_USER_ID, authResponse.userId)
        settings.putString(KEY_USERNAME, authResponse.username)

        // Вычисляем время истечения токена
        val expiresAt = System.currentTimeMillis() + (authResponse.expiresIn * 1000)
        settings.putLong(KEY_EXPIRES_AT, expiresAt)

        _authState.value = AuthState.Authenticated(
            token = authResponse.token,
            userId = authResponse.userId,
            username = authResponse.username
        )
    }

    /**
     * Получает токен доступа
     */
    fun getAccessToken(): String? {
        return if (settings.hasKey(KEY_ACCESS_TOKEN)) {
            settings.getString(KEY_ACCESS_TOKEN, "")
        } else {
            null
        }
    }

    /**
     * Получает refresh токен
     */
    private fun getRefreshToken(): String? {
        return if (settings.hasKey(KEY_REFRESH_TOKEN)) {
            settings.getString(KEY_REFRESH_TOKEN, "")
        } else {
            null
        }
    }

    /**
     * Проверяет, истек ли токен доступа
     */
    fun isTokenExpired(): Boolean {
        val expiresAt = settings.getLong(KEY_EXPIRES_AT, 0)
        return System.currentTimeMillis() >= expiresAt
    }

    /**
     * Проверяет, аутентифицирован ли пользователь
     */
    fun isAuthenticated(): Boolean {
        return getAccessToken() != null && !isTokenExpired()
    }

    /**
     * Обновляет токен доступа, если он истек
     * @return true, если токен успешно обновлен, false в противном случае
     */
    suspend fun refreshTokenIfNeeded(): Boolean {
        // Если токен не истек, возвращаем true
        if (!isTokenExpired()) {
            return true
        }

        // Если нет refresh токена, возвращаем false
        val refreshToken = getRefreshToken() ?: return false

        // Пытаемся обновить токен
        val result = authApi.refreshToken(RefreshTokenRequest(refreshToken))

        return when (result) {
            is model.auth.AuthResult.Success -> {
                saveAuthData(result.data)
                true
            }
            else -> {
                // Если не удалось обновить токен, очищаем данные аутентификации
                clearAuthData()
                false
            }
        }
    }

    /**
     * Очищает данные аутентификации
     */
    fun clearAuthData() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USERNAME)
        settings.remove(KEY_EXPIRES_AT)

        _authState.value = AuthState.NotAuthenticated
    }

    /**
     * Инициализирует состояние аутентификации при запуске приложения
     */
    fun initAuthState() {
        if (isAuthenticated()) {
            _authState.value = AuthState.Authenticated(
                token = getAccessToken()!!,
                userId = settings.getString(KEY_USER_ID, ""),
                username = settings.getString(KEY_USERNAME, "")
            )
        } else {
            _authState.value = AuthState.NotAuthenticated
        }
    }
}

/**
 * Состояние аутентификации
 */
sealed class AuthState {
    /**
     * Пользователь не аутентифицирован
     */
    data object NotAuthenticated : AuthState()

    /**
     * Пользователь аутентифицирован
     */
    data class Authenticated(
        val token: String,
        val userId: String,
        val username: String
    ) : AuthState()
}
