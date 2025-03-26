package repository.auth

import api.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import model.AppError
import model.AuthResult
import model.ErrorCode
import model.User
import model.auth.AuthResponseData
import model.auth.ServerStatusResponse
import model.auth.RefreshTokenRequest
import settings.MultiplatformSettings

/**
 * Реализация репозитория для работы с аутентификацией
 * Использует AuthApi из домена для взаимодействия с API
 */
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val settings: MultiplatformSettings
) : AuthRepository {

    private var currentUser: User? = null
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState

    override suspend fun register(
        email: String,
        password: String,
        username: String
    ): AuthResult<AuthResponseData> {
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

            // Обрабатываем результат
            when (result) {
                is AuthResult.Success -> {
                    // Сохраняем токены выполняется в AuthApiAdapter

                    // Обновляем кэшированного пользователя
                    val user = result.user
                    currentUser = user
                    if (user != null) {
                        _authState.value = AuthState.Authenticated(user)
                    }
                }
                is AuthResult.Error -> {
                    // Ничего не делаем, просто возвращаем ошибку
                }
                is AuthResult.Loading -> {
                    // Ничего не делаем, просто возвращаем состояние загрузки
                }
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

    override suspend fun login(email: String, password: String): AuthResult<AuthResponseData> {
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

            // Обрабатываем результат
            when (result) {
                is AuthResult.Success -> {
                    // Сохраняем токены выполняется в AuthApiAdapter

                    // Обновляем кэшированного пользователя
                    val user = result.user
                    currentUser = user
                    if (user != null) {
                        _authState.value = AuthState.Authenticated(user)
                    }
                }
                is AuthResult.Error -> {
                    // Ничего не делаем, просто возвращаем ошибку
                }
                is AuthResult.Loading -> {
                    // Ничего не делаем, просто возвращаем состояние загрузки
                }
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

    override suspend fun logout(): AuthResult<Unit> {
        try {
            // Делегируем выполнение API
            val result = authApi.logout()

            // Очищаем кэш при выходе
            currentUser = null
            _authState.value = AuthState.Unauthenticated

            // Очистка токенов происходит в AuthApiAdapter

            return result
        } catch (e: Exception) {
            // Очищаем кэш при ошибке тоже
            currentUser = null
            _authState.value = AuthState.Unauthenticated

            // Очистка токенов происходит в AuthApiAdapter

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
            if (result is AuthResult.Success) {
                val user = result.data
                currentUser = user
                _authState.value = AuthState.Authenticated(user)
                return user
            }

            return null
        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return settings.security.hasAuthToken()
    }

    override suspend fun updateProfile(username: String): AuthResult<User> {
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

            // Обрабатываем результат
            when (result) {
                is AuthResult.Success -> {
                    // Обновляем кэшированного пользователя
                    val user = result.data
                    currentUser = user
                    if (user != null) {
                        _authState.value = AuthState.Authenticated(user)
                    }
                }
                is AuthResult.Error -> {
                    // Ничего не делаем, просто возвращаем ошибку
                }
                is AuthResult.Loading -> {
                    // Ничего не делаем, просто возвращаем состояние загрузки
                }
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

    override suspend fun checkServerStatus(): AuthResult<ServerStatusResponse> {
        return try {
            // Делегируем выполнение API
            authApi.checkServerStatus()
        } catch (e: Exception) {
            AuthResult.Error(
                AppError(
                    code = ErrorCode.NETWORK_ERROR,
                    message = "Ошибка при проверке статуса сервера",
                    details = e.message
                )
            )
        }
    }

    override suspend fun refreshTokenIfNeeded(): Boolean {
        try {
            val refreshToken = settings.security.getRefreshToken()
            if (refreshToken == null) {
                return false
            }

            val request = RefreshTokenRequest(refreshToken)
            val result = authApi.refreshToken(request)
            return result is AuthResult.Success
        } catch (e: Exception) {
            return false
        }
    }
}
