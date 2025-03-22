package repository.auth

import api.AuthApi
import model.AppError
import model.AuthResult
import model.ErrorCode
import model.User
import model.auth.LoginRequest
import model.auth.RefreshTokenRequest
import model.auth.RegisterRequest
import model.auth.ServerStatusResponse
import repository.mapper.AuthMapper
import settings.MultiplatformSettings

/**
 * Реальная реализация репозитория для работы с аутентификацией
 * Использует AuthApi из домена для взаимодействия с API
 */
class DefaultAuthRepository(
    private val authApi: AuthApi,
    private val settings: MultiplatformSettings
) : AuthRepository {

    private var currentUser: User? = null

    override suspend fun register(
        email: String,
        password: String,
        username: String
    ): AuthResult {
        try {
            // Базовая валидация входных параметров
            if (email.isBlank() || password.isBlank() || username.isBlank()) {
                return AuthResult.Error(
                    AppError(
                        code = ErrorCode.VALIDATION_ERROR,
                        message = "Все поля должны быть заполнены",
                        details = "Проверьте заполнение всех полей формы"
                    )
                )
            }

            // Делегируем выполнение API
            val result = authApi.register(username, email, password)

            // Обновляем кэшированного пользователя, если успешно
            if (result is AuthResult.Success && result.user != null) {
                currentUser = result.user
            }

            return result
        } catch (e: Exception) {
            return AuthResult.Error(
                AppError(
                    code = ErrorCode.NETWORK_ERROR,
                    message = "Ошибка при регистрации",
                    details = e.message
                )
            )
        }
    }

    override suspend fun login(email: String, password: String): AuthResult {
        try {
            // Базовая валидация входных параметров
            if (email.isBlank() || password.isBlank()) {
                return AuthResult.Error(
                    AppError(
                        code = ErrorCode.VALIDATION_ERROR,
                        message = "Логин и пароль должны быть заполнены",
                        details = "Проверьте заполнение всех полей формы"
                    )
                )
            }

            // Делегируем выполнение API
            val result = authApi.login(email, password)

            // Обновляем кэшированного пользователя, если успешно
            if (result is AuthResult.Success && result.user != null) {
                currentUser = result.user
            }

            return result
        } catch (e: Exception) {
            return AuthResult.Error(
                AppError(
                    code = ErrorCode.NETWORK_ERROR,
                    message = "Ошибка при авторизации",
                    details = e.message
                )
            )
        }
    }

    override suspend fun logout(): AuthResult {
        try {
            // Делегируем выполнение API
            val result = authApi.logout()

            // Очищаем кэш при выходе
            currentUser = null

            return result
        } catch (e: Exception) {
            // Очищаем кэш при ошибке тоже
            currentUser = null
            return AuthResult.Error(
                AppError(
                    code = ErrorCode.NETWORK_ERROR,
                    message = "Ошибка при выходе из системы",
                    details = e.message
                )
            )
        }
    }

    override suspend fun getCurrentUser(): User? {
        // Если есть кэшированный пользователь, возвращаем его
        if (currentUser != null) {
            return currentUser
        }

        try {
            // Проверяем, авторизован ли пользователь
            if (!isAuthenticated()) {
                return null
            }

            // Получаем пользователя через API
            val result = authApi.getCurrentUser()

            // Если успешно, обновляем кэш и возвращаем пользователя
            if (result is AuthResult.Success && result.user != null) {
                currentUser = result.user
                return result.user
            }

            return null
        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return settings.security.hasAuthToken()
    }

    override suspend fun updateProfile(username: String): AuthResult {
        try {
            // Базовая валидация входных параметров
            if (username.isBlank()) {
                return AuthResult.Error(
                    AppError(
                        code = ErrorCode.VALIDATION_ERROR,
                        message = "Имя пользователя не может быть пустым",
                        details = "Введите имя пользователя"
                    )
                )
            }

            // Делегируем выполнение API
            val result = authApi.updateProfile(username)

            // Обновляем кэшированного пользователя, если успешно
            if (result is AuthResult.Success && result.user != null) {
                currentUser = result.user
            }

            return result
        } catch (e: Exception) {
            return AuthResult.Error(
                AppError(
                    code = ErrorCode.NETWORK_ERROR,
                    message = "Ошибка при обновлении профиля",
                    details = e.message
                )
            )
        }
    }

    override suspend fun checkServerStatus(): AuthResult {
        try {
            // Делегируем выполнение API
            return authApi.checkServerStatus()
        } catch (e: Exception) {
            return AuthResult.Error(
                AppError(
                    code = ErrorCode.NETWORK_ERROR,
                    message = "Ошибка при проверке статуса сервера",
                    details = e.message
                )
            )
        }
    }

    /**
     * Обновляет токен, если он истек
     * @return true, если токен обновлен успешно или не требует обновления
     */
    private suspend fun refreshTokenIfNeeded(): Boolean {
        // Получаем текущий refresh токен
        val refreshToken = settings.security.getRefreshToken() ?: return false

        try {
            // Создаем запрос на обновление токена
            val request = RefreshTokenRequest(refreshToken)

            // Выполняем запрос к API
            val result = authApi.refreshToken(request)

            // Обрабатываем результат
            when (result) {
                is model.auth.AuthResult.Success -> {
                    // Сохраняем новый токен
                    settings.security.saveAuthToken(result.data.token)
                    settings.security.saveRefreshToken(result.data.refreshToken)
                    return true
                }
                else -> {
                    // Очищаем токены в случае ошибки
                    settings.security.clearAuthToken()
                    settings.security.clearRefreshToken()
                    return false
                }
            }
        } catch (e: Exception) {
            // Очищаем токены в случае ошибки
            settings.security.clearAuthToken()
            settings.security.clearRefreshToken()
            return false
        }
    }
}
