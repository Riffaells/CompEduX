package api.auth

import api.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import model.auth.*

/**
 * Реализация API аутентификации
 */
class AuthApiImpl(
    private val apiClient: ApiClient
) : AuthApi {

    private val httpClient = apiClient.createHttpClient()

    /**
     * Регистрация нового пользователя
     */
    override suspend fun register(request: RegisterRequest): AuthResult<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("${apiClient.getBaseUrl()}/auth/register") {
                setBody(request)
            }

            return@withContext when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.Created -> {
                    val authResponse = response.body<AuthResponse>()
                    AuthResult.Success(authResponse)
                }
                else -> {
                    val errorResponse = response.body<AuthError>()
                    AuthResult.Error(errorResponse)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(AuthError("network_error", e.message ?: "Ошибка сети при регистрации"))
        }
    }

    /**
     * Авторизация пользователя
     */
    override suspend fun login(request: LoginRequest): AuthResult<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("${apiClient.getBaseUrl()}/auth/login") {
                setBody(request)
            }

            return@withContext when (response.status) {
                HttpStatusCode.OK -> {
                    val authResponse = response.body<AuthResponse>()
                    AuthResult.Success(authResponse)
                }
                else -> {
                    val errorResponse = response.body<AuthError>()
                    AuthResult.Error(errorResponse)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(AuthError("network_error", e.message ?: "Ошибка сети при авторизации"))
        }
    }

    /**
     * Обновление токена доступа
     */
    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResult<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("${apiClient.getBaseUrl()}/auth/refresh") {
                setBody(request)
            }

            return@withContext when (response.status) {
                HttpStatusCode.OK -> {
                    val authResponse = response.body<AuthResponse>()
                    AuthResult.Success(authResponse)
                }
                else -> {
                    val errorResponse = response.body<AuthError>()
                    AuthResult.Error(errorResponse)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(AuthError("network_error", e.message ?: "Ошибка сети при обновлении токена"))
        }
    }

    /**
     * Получение информации о текущем пользователе
     */
    override suspend fun getCurrentUser(token: String): AuthResult<User> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get("${apiClient.getBaseUrl()}/users/me") {
                with(apiClient) { withAuth(token) }
            }

            return@withContext when (response.status) {
                HttpStatusCode.OK -> {
                    val user = response.body<User>()
                    AuthResult.Success(user)
                }
                else -> {
                    val errorResponse = response.body<AuthError>()
                    AuthResult.Error(errorResponse)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(AuthError("network_error", e.message ?: "Ошибка сети при получении данных пользователя"))
        }
    }

    /**
     * Выход из системы (инвалидация токена)
     */
    override suspend fun logout(token: String): AuthResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.post("${apiClient.getBaseUrl()}/auth/logout") {
                with(apiClient) { withAuth(token) }
            }

            return@withContext when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> {
                    AuthResult.Success(Unit)
                }
                else -> {
                    val errorResponse = response.body<AuthError>()
                    AuthResult.Error(errorResponse)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(AuthError("network_error", e.message ?: "Ошибка сети при выходе из системы"))
        }
    }

    /**
     * Проверка статуса сервера
     */
    override suspend fun checkServerStatus(): AuthResult<ServerStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.get("${apiClient.getBaseUrl()}/status")

            return@withContext when (response.status) {
                HttpStatusCode.OK -> {
                    val statusResponse = response.body<ServerStatusResponse>()
                    AuthResult.Success(statusResponse)
                }
                else -> {
                    AuthResult.Error(AuthError("server_error", "Сервер недоступен или вернул ошибку"))
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(AuthError("network_error", e.message ?: "Ошибка сети при проверке статуса сервера"))
        }
    }
}
