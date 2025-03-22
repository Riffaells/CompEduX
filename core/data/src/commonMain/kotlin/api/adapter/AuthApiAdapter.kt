package api.adapter

import api.AuthApi as DomainAuthApi
import api.auth.AuthApi as NetworkAuthApi
import model.AppError
import model.AuthResult as DomainAuthResult
import model.User as DomainUser
import model.auth.LoginRequest
import model.auth.RefreshTokenRequest
import model.auth.RegisterRequest
import model.auth.ServerStatusResponse
import repository.mapper.AuthMapper
import settings.MultiplatformSettings

/**
 * Адаптер для преобразования между API интерфейсами домена и сети.
 * Реализует интерфейс AuthApi из домена, используя AuthApi из модуля network.
 */
class AuthApiAdapter(
    private val networkAuthApi: NetworkAuthApi,
    private val settings: MultiplatformSettings
) : DomainAuthApi {

    override suspend fun register(username: String, email: String, password: String): DomainAuthResult {
        try {
            // Создаем запрос для сетевого API
            val request = RegisterRequest(username, email, password)

            // Вызываем сетевое API
            val result = networkAuthApi.register(request)

            // Преобразуем результат в доменную модель
            return when (result) {
                is api.auth.AuthResult.Success -> {
                    // Сохраняем токен
                    settings.security.saveAuthToken(result.data.token)
                    result.data.refreshToken?.let { settings.security.saveRefreshToken(it) }

                    // Создаем доменную модель пользователя
                    val user = DomainUser(
                        id = result.data.userId,
                        username = result.data.username,
                        email = email
                    )

                    DomainAuthResult.Success(user, result.data.token)
                }
                is api.auth.AuthResult.Error -> {
                    DomainAuthResult.Error(
                        AppError(
                            code = model.ErrorCode.AUTH_ERROR,
                            message = result.error.message,
                            details = result.error.code
                        )
                    )
                }
                is api.auth.AuthResult.Loading -> DomainAuthResult.Loading
            }
        } catch (e: Exception) {
            return DomainAuthResult.Error(
                AppError(
                    code = model.ErrorCode.NETWORK_ERROR,
                    message = "Ошибка при регистрации",
                    details = e.message
                )
            )
        }
    }

    override suspend fun login(email: String, password: String): DomainAuthResult {
        try {
            // Создаем запрос для сетевого API
            val request = LoginRequest(email, password)

            // Вызываем сетевое API
            val result = networkAuthApi.login(request)

            // Преобразуем результат в доменную модель
            return when (result) {
                is api.auth.AuthResult.Success -> {
                    // Сохраняем токен
                    settings.security.saveAuthToken(result.data.token)
                    result.data.refreshToken?.let { settings.security.saveRefreshToken(it) }

                    // Создаем доменную модель пользователя
                    val user = DomainUser(
                        id = result.data.userId,
                        username = result.data.username,
                        email = email
                    )

                    DomainAuthResult.Success(user, result.data.token)
                }
                is api.auth.AuthResult.Error -> {
                    DomainAuthResult.Error(
                        AppError(
                            code = model.ErrorCode.AUTH_ERROR,
                            message = result.error.message,
                            details = result.error.code
                        )
                    )
                }
                is api.auth.AuthResult.Loading -> DomainAuthResult.Loading
            }
        } catch (e: Exception) {
            return DomainAuthResult.Error(
                AppError(
                    code = model.ErrorCode.NETWORK_ERROR,
                    message = "Ошибка при авторизации",
                    details = e.message
                )
            )
        }
    }

    override suspend fun getCurrentUser(): DomainAuthResult {
        try {
            // Получаем токен
            val token = settings.security.getAuthToken() ?: return DomainAuthResult.Success(null)

            // Вызываем сетевое API
            val result = networkAuthApi.getCurrentUser(token)

            // Преобразуем результат в доменную модель
            return when (result) {
                is api.auth.AuthResult.Success -> {
                    // Создаем доменную модель пользователя
                    val user = DomainUser(
                        id = result.data.id,
                        username = result.data.username,
                        email = result.data.email
                    )

                    DomainAuthResult.Success(user, token)
                }
                is api.auth.AuthResult.Error -> {
                    DomainAuthResult.Error(
                        AppError(
                            code = model.ErrorCode.AUTH_ERROR,
                            message = result.error.message,
                            details = result.error.code
                        )
                    )
                }
                is api.auth.AuthResult.Loading -> DomainAuthResult.Loading
            }
        } catch (e: Exception) {
            return DomainAuthResult.Error(
                AppError(
                    code = model.ErrorCode.NETWORK_ERROR,
                    message = "Ошибка при получении данных пользователя",
                    details = e.message
                )
            )
        }
    }

    override suspend fun logout(): DomainAuthResult {
        try {
            // Получаем токен
            val token = settings.security.getAuthToken() ?: return DomainAuthResult.Success(null)

            // Вызываем сетевое API
            val result = networkAuthApi.logout(token)

            // Очищаем токены в любом случае
            settings.security.clearAuthToken()
            settings.security.clearRefreshToken()

            // Преобразуем результат в доменную модель
            return when (result) {
                is api.auth.AuthResult.Success -> {
                    DomainAuthResult.Success(null)
                }
                is api.auth.AuthResult.Error -> {
                    // При ошибке выхода все равно считаем пользователя вышедшим
                    DomainAuthResult.Success(null)
                }
                is api.auth.AuthResult.Loading -> DomainAuthResult.Loading
            }
        } catch (e: Exception) {
            // Очищаем токены при ошибке
            settings.security.clearAuthToken()
            settings.security.clearRefreshToken()

            return DomainAuthResult.Success(null)
        }
    }

    override suspend fun checkServerStatus(): DomainAuthResult {
        try {
            // Вызываем сетевое API
            val result = networkAuthApi.checkServerStatus()

            // Преобразуем результат в доменную модель
            return when (result) {
                is api.auth.AuthResult.Success -> {
                    DomainAuthResult.Success(null)
                }
                is api.auth.AuthResult.Error -> {
                    DomainAuthResult.Error(
                        AppError(
                            code = model.ErrorCode.SERVER_ERROR,
                            message = "Ошибка при проверке статуса сервера",
                            details = result.error.message
                        )
                    )
                }
                is api.auth.AuthResult.Loading -> DomainAuthResult.Loading
            }
        } catch (e: Exception) {
            return DomainAuthResult.Error(
                AppError(
                    code = model.ErrorCode.NETWORK_ERROR,
                    message = "Ошибка при проверке статуса сервера",
                    details = e.message
                )
            )
        }
    }

    override suspend fun updateProfile(username: String): DomainAuthResult {
        // TODO: Реализовать обновление профиля через API
        // Пока реализуем заглушку
        val currentUser = getCurrentUser()
        if (currentUser is DomainAuthResult.Success && currentUser.user != null) {
            val updatedUser = currentUser.user.copy(username = username)
            return DomainAuthResult.Success(updatedUser, currentUser.token)
        }

        return DomainAuthResult.Error(
            AppError(
                code = model.ErrorCode.AUTH_ERROR,
                message = "Пользователь не авторизован",
                details = "Невозможно обновить профиль неавторизованного пользователя"
            )
        )
    }
}
