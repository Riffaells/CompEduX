package repository.auth

import api.auth.AuthApi
import api.auth.AuthState
import kotlinx.coroutines.flow.StateFlow
import model.auth.*

/**
 * Реализация репозитория аутентификации
 */
class AuthRepositoryImpl(
    private val authApi: AuthApi,
) : AuthRepository {

    override val authState: StateFlow<AuthState> = tokenManager.authState

    /**
     * Регистрация нового пользователя
     */
    override suspend fun register(
        username: String,
        email: String,
        password: String,
        firstName: String?,
        lastName: String?
    ): AuthResult<Unit> {
        val request = RegisterRequest(
            username = username,
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName
        )

        return when (val result = authApi.register(request)) {
            is AuthResult.Success -> {
                // Сохраняем данные аутентификации
                tokenManager.saveAuthData(result.data)
                AuthResult.Success(Unit)
            }
            is AuthResult.Error -> result
            is AuthResult.Loading -> AuthResult.Loading
        }
    }

    /**
     * Авторизация пользователя
     */
    override suspend fun login(username: String, password: String): AuthResult<Unit> {
        val request = LoginRequest(
            username = username,
            password = password
        )

        return when (val result = authApi.login(request)) {
            is AuthResult.Success -> {
                // Сохраняем данные аутентификации
                tokenManager.saveAuthData(result.data)
                AuthResult.Success(Unit)
            }
            is AuthResult.Error -> result
            is AuthResult.Loading -> AuthResult.Loading
        }
    }

    /**
     * Выход из системы
     */
    override suspend fun logout(): AuthResult<Unit> {
        val token = tokenManager.getAccessToken() ?: return AuthResult.Success(Unit)

        return when (val result = authApi.logout(token)) {
            is AuthResult.Success -> {
                // Очищаем данные аутентификации
                tokenManager.clearAuthData()
                AuthResult.Success(Unit)
            }
            is AuthResult.Error -> {
                // Даже если запрос завершился с ошибкой, очищаем локальные данные
                tokenManager.clearAuthData()
                result
            }
            is AuthResult.Loading -> AuthResult.Loading
        }
    }

    /**
     * Получение информации о текущем пользователе
     */
    override suspend fun getCurrentUser(): AuthResult<User> {
        // Обновляем токен, если необходимо
        if (!refreshTokenIfNeeded()) {
            return AuthResult.Error(AuthError("auth_error", "Не удалось обновить токен"))
        }

        val token = tokenManager.getAccessToken() ?: return AuthResult.Error(
            AuthError("auth_error", "Пользователь не авторизован")
        )

        return authApi.getCurrentUser(token)
    }

    /**
     * Проверка статуса сервера
     */
    override suspend fun checkServerStatus(): AuthResult<ServerStatusResponse> {
        return authApi.checkServerStatus()
    }

    /**
     * Проверяет, аутентифицирован ли пользователь
     */
    override fun isAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }

    /**
     * Обновляет токен доступа, если он истек
     */
    override suspend fun refreshTokenIfNeeded(): Boolean {
        return tokenManager.refreshTokenIfNeeded()
    }
}
