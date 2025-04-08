package api.auth

import api.NetworkAuthApi
import api.model.AuthResultNetwork
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import model.User
import model.auth.*
import repository.mapper.ErrorMapper
import repository.mapper.UserMapper

/**
 * Реализация API аутентификации для работы с сетью
 */
class NetworkAuthApiImpl(
    private val client: HttpClient,
    private val networkConfig: NetworkConfig,
    private val errorMapper: ErrorMapper,
    private val userMapper: UserMapper
) : NetworkAuthApi {

    /**
     * Получение базового URL API с учетом версии API
     */
    private suspend fun getApiUrl(): String {
        val baseUrl = networkConfig.getBaseUrl()
        val apiVersion = networkConfig.getApiVersion()
        return "$baseUrl/$apiVersion"
    }

    override suspend fun register(request: RegisterRequest): AuthResultNetwork<AuthResponseDomain> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = getApiUrl()

            // Конвертируем domain-запрос в network-запрос
            val networkRequest = NetworkRegisterRequest(
                username = request.username,
                email = request.email,
                password = request.password
            )

            // Выполняем запрос
            val response = client.post("$apiUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(networkRequest)
            }

            // Обрабатываем ответ
            if (response.status.isSuccess()) {
                val authResponse = response.body<NetworkAuthResponse>()
                // Конвертируем network-ответ в domain-ответ
                val domainResponse = AuthResponseDomain(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    tokenType = authResponse.tokenType
                )
                AuthResultNetwork.Success(domainResponse)
            } else {
                val errorBody = response.body<NetworkErrorResponse>()
                val error = errorMapper.mapToAppError(
                    errorCode = errorBody.code,
                    message = errorBody.message,
                    details = errorBody.details
                )
                AuthResultNetwork.Error(error)
            }
        } catch (e: IOException) {
            // Ошибка сети
            val error = errorMapper.mapToAppError(
                errorCode = -1,
                message = "Ошибка сети: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        } catch (e: Exception) {
            // Другие ошибки
            val error = errorMapper.mapToAppError(
                errorCode = -2,
                message = "Ошибка: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        }
    }

    override suspend fun login(request: LoginRequest): AuthResultNetwork<AuthResponseDomain> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = getApiUrl()

            // Конвертируем domain-запрос в network-запрос
            val networkRequest = NetworkLoginRequest(
                username = request.username,
                password = request.password
            )

            // Выполняем запрос
            val response = client.post("$apiUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(networkRequest)
            }

            // Обрабатываем ответ
            if (response.status.isSuccess()) {
                val authResponse = response.body<NetworkAuthResponse>()
                // Конвертируем network-ответ в domain-ответ
                val domainResponse = AuthResponseDomain(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    tokenType = authResponse.tokenType
                )
                AuthResultNetwork.Success(domainResponse)
            } else {
                val errorBody = response.body<NetworkErrorResponse>()
                val error = errorMapper.mapToAppError(
                    errorCode = errorBody.code,
                    message = errorBody.message,
                    details = errorBody.details
                )
                AuthResultNetwork.Error(error)
            }
        } catch (e: IOException) {
            // Ошибка сети
            val error = errorMapper.mapToAppError(
                errorCode = -1,
                message = "Ошибка сети: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        } catch (e: Exception) {
            // Другие ошибки
            val error = errorMapper.mapToAppError(
                errorCode = -2,
                message = "Ошибка: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResultNetwork<AuthResponseDomain> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = getApiUrl()

            // Конвертируем domain-запрос в network-запрос
            val networkRequest = NetworkRefreshTokenRequest(
                refreshToken = request.refreshToken
            )

            // Выполняем запрос
            val response = client.post("$apiUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(networkRequest)
            }

            // Обрабатываем ответ
            if (response.status.isSuccess()) {
                val authResponse = response.body<NetworkAuthResponse>()
                // Конвертируем network-ответ в domain-ответ
                val domainResponse = AuthResponseDomain(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    tokenType = authResponse.tokenType
                )
                AuthResultNetwork.Success(domainResponse)
            } else {
                val errorBody = response.body<NetworkErrorResponse>()
                val error = errorMapper.mapToAppError(
                    errorCode = errorBody.code,
                    message = errorBody.message,
                    details = errorBody.details
                )
                AuthResultNetwork.Error(error)
            }
        } catch (e: IOException) {
            // Ошибка сети
            val error = errorMapper.mapToAppError(
                errorCode = -1,
                message = "Ошибка сети: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        } catch (e: Exception) {
            // Другие ошибки
            val error = errorMapper.mapToAppError(
                errorCode = -2,
                message = "Ошибка: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        }
    }

    override suspend fun getCurrentUser(token: String): AuthResultNetwork<User> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = getApiUrl()

            // Выполняем запрос
            val response = client.get("$apiUrl/users/me") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            // Обрабатываем ответ
            if (response.status.isSuccess()) {
                val userResponse = response.body<NetworkUserResponse>()
                // Маппим сетевую модель в доменную
                val user = userMapper.mapToDomain(userResponse)
                AuthResultNetwork.Success(user)
            } else {
                val errorBody = response.body<NetworkErrorResponse>()
                val error = errorMapper.mapToAppError(
                    errorCode = errorBody.code,
                    message = errorBody.message,
                    details = errorBody.details
                )
                AuthResultNetwork.Error(error)
            }
        } catch (e: IOException) {
            // Ошибка сети
            val error = errorMapper.mapToAppError(
                errorCode = -1,
                message = "Ошибка сети: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        } catch (e: Exception) {
            // Другие ошибки
            val error = errorMapper.mapToAppError(
                errorCode = -2,
                message = "Ошибка: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        }
    }

    override suspend fun logout(token: String): AuthResultNetwork<Unit> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = getApiUrl()

            // Выполняем запрос
            val response = client.post("$apiUrl/auth/logout") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            // Обрабатываем ответ
            if (response.status.isSuccess()) {
                AuthResultNetwork.Success(Unit)
            } else {
                val errorBody = response.body<NetworkErrorResponse>()
                val error = errorMapper.mapToAppError(
                    errorCode = errorBody.code,
                    message = errorBody.message,
                    details = errorBody.details
                )
                AuthResultNetwork.Error(error)
            }
        } catch (e: IOException) {
            // Ошибка сети
            val error = errorMapper.mapToAppError(
                errorCode = -1,
                message = "Ошибка сети: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        } catch (e: Exception) {
            // Другие ошибки
            val error = errorMapper.mapToAppError(
                errorCode = -2,
                message = "Ошибка: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        }
    }

    override suspend fun checkServerStatus(): AuthResultNetwork<ServerStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = getApiUrl()

            // Выполняем запрос
            val response = client.get("$apiUrl/status") {
                contentType(ContentType.Application.Json)
            }

            // Обрабатываем ответ
            if (response.status.isSuccess()) {
                val statusResponse = response.body<NetworkServerStatusResponse>()
                // Конвертируем network-ответ в domain-ответ
                val domainResponse = ServerStatusResponse(
                    status = statusResponse.status,
                    version = statusResponse.version,
                    uptime = statusResponse.uptime,
                    message = statusResponse.message
                )
                AuthResultNetwork.Success(domainResponse)
            } else {
                val errorBody = response.body<NetworkErrorResponse>()
                val error = errorMapper.mapToAppError(
                    errorCode = errorBody.code,
                    message = errorBody.message,
                    details = errorBody.details
                )
                AuthResultNetwork.Error(error)
            }
        } catch (e: IOException) {
            // Ошибка сети
            val error = errorMapper.mapToAppError(
                errorCode = -1,
                message = "Ошибка сети: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        } catch (e: Exception) {
            // Другие ошибки
            val error = errorMapper.mapToAppError(
                errorCode = -2,
                message = "Ошибка: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        }
    }

    override suspend fun updateProfile(token: String, username: String): AuthResultNetwork<User> = withContext(Dispatchers.IO) {
        try {
            val apiUrl = getApiUrl()

            // Выполняем запрос
            val response = client.put("$apiUrl/users/profile") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(mapOf("username" to username))
            }

            // Обрабатываем ответ
            if (response.status.isSuccess()) {
                val userResponse = response.body<NetworkUserResponse>()
                // Маппим сетевую модель в доменную
                val user = userMapper.mapToDomain(userResponse)
                AuthResultNetwork.Success(user)
            } else {
                val errorBody = response.body<NetworkErrorResponse>()
                val error = errorMapper.mapToAppError(
                    errorCode = errorBody.code,
                    message = errorBody.message,
                    details = errorBody.details
                )
                AuthResultNetwork.Error(error)
            }
        } catch (e: IOException) {
            // Ошибка сети
            val error = errorMapper.mapToAppError(
                errorCode = -1,
                message = "Ошибка сети: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        } catch (e: Exception) {
            // Другие ошибки
            val error = errorMapper.mapToAppError(
                errorCode = -2,
                message = "Ошибка: ${e.message}",
                details = e.stackTraceToString()
            )
            AuthResultNetwork.Error(error)
        }
    }
}
