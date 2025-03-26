package api.auth

import api.dto.AuthResponseDto
import api.dto.UserResponse
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import model.AppError
import model.AuthResult
import model.User
import model.auth.LoginRequest
import model.auth.RefreshTokenRequest
import model.auth.RegisterRequest
import model.auth.ServerStatusResponse

/**
 * Функция-расширение для конвертации UserResponse в User
 */
private fun UserResponse.toUser(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email
    )
}

/**
 * Реализация API аутентификации
 */
class AuthApiImpl(
    private val client: HttpClient,
    private val networkConfig: NetworkConfig
) : AuthApi {

    private suspend fun getBaseUrl(): String = networkConfig.getBaseUrl()

    override suspend fun register(request: RegisterRequest): AuthResult<AuthResponseDto> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponseDto>()
                AuthResult.Success(authResponse, authResponse.user.toUser(), authResponse.token)
            } else {
                AuthResult.Error(AppError(message = "Registration failed"))
            }
        } catch (e: Exception) {
            AuthResult.Error(AppError(message = e.message ?: "Unknown error"))
        }
    }

    override suspend fun login(request: LoginRequest): AuthResult<AuthResponseDto> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponseDto>()
                AuthResult.Success(authResponse, authResponse.user.toUser(), authResponse.token)
            } else {
                AuthResult.Error(AppError(message = "Login failed"))
            }
        } catch (e: Exception) {
            AuthResult.Error(AppError(message = e.message ?: "Unknown error"))
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResult<AuthResponseDto> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponseDto>()
                AuthResult.Success(authResponse, authResponse.user.toUser(), authResponse.token)
            } else {
                AuthResult.Error(AppError(message = "Token refresh failed"))
            }
        } catch (e: Exception) {
            AuthResult.Error(AppError(message = e.message ?: "Unknown error"))
        }
    }

    override suspend fun getCurrentUser(token: String): AuthResult<User> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.get("$baseUrl/auth/me") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                val user = response.body<User>()
                AuthResult.Success(user)
            } else {
                AuthResult.Error(AppError(message = "Failed to get user info"))
            }
        } catch (e: Exception) {
            AuthResult.Error(AppError(message = e.message ?: "Unknown error"))
        }
    }

    override suspend fun logout(token: String): AuthResult<Unit> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.post("$baseUrl/auth/logout") {
                header("Authorization", "Bearer $token")
            }

            if (response.status.isSuccess()) {
                AuthResult.Success(Unit)
            } else {
                AuthResult.Error(AppError(message = "Logout failed"))
            }
        } catch (e: Exception) {
            AuthResult.Error(AppError(message = e.message ?: "Unknown error"))
        }
    }

    override suspend fun checkServerStatus(): AuthResult<ServerStatusResponse> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.get("$baseUrl/status")

            if (response.status.isSuccess()) {
                val status = response.body<ServerStatusResponse>()
                AuthResult.Success(status)
            } else {
                AuthResult.Error(AppError(message = "Failed to check server status"))
            }
        } catch (e: Exception) {
            AuthResult.Error(AppError(message = e.message ?: "Unknown error"))
        }
    }
}
