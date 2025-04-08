package api.auth

import api.dto.NetworkAuthResponseDto
import api.dto.NetworkUserResponseDto
import api.model.AuthResultNetwork
import client.HttpClientFactory
import config.NetworkConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import model.ApiError
import model.User
import model.UserPreferences
import model.UserProfile
import model.UserRatings
import model.auth.*
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Функция-расширение для конвертации NetworkUserResponseDto в User
 */
private fun NetworkUserResponseDto.toUser(): User {
    return User(
        id = this.id,
        username = this.username,
        email = this.email,
        // Другие поля заполняются дефолтными значениями,
        // так как NetworkUserResponseDto содержит только базовую информацию
        isActive = true,
        isVerified = false,
        role = "user",
        authProvider = "email",
        createdAt = "",
        updatedAt = "",
        profile = UserProfile(),
        preferences = UserPreferences(
            theme = "light",
            fontSize = "medium",
            emailNotifications = true,
            pushNotifications = true,
            beveragePreference = "none",
            breakReminder = true,
            breakIntervalMinutes = 60,
            additionalPreferences = emptyMap()
        ),
        ratings = UserRatings(
            contributionRating = 0f,
            botScore = 0f,
            expertiseRating = 0f,
            competitionRating = 0f,
            additionalRatings = emptyMap()
        )
    )
}

/**
 * Функция для извлечения userId из JWT токена
 */
@OptIn(ExperimentalEncodingApi::class)
private fun extractUserIdFromToken(token: String): String {
    try {
        // Токен JWT имеет формат: header.payload.signature
        val parts = token.split(".")
        if (parts.size != 3) return ""

        // Декодируем часть payload (Base64)
        val payload = parts[1].decodeBase64Url()
        val json = Json.parseToJsonElement(payload).jsonObject

        // Получаем sub (subject) из payload - это обычно userId
        return json["sub"]?.jsonPrimitive?.content ?: ""
    } catch (e: Exception) {
        println("Error extracting userId from token: ${e.message}")
        return ""
    }
}

/**
 * Функция для извлечения expiration из JWT токена (в секундах с эпохи)
 */
@OptIn(ExperimentalEncodingApi::class)
private fun extractExpirationFromToken(token: String): Long {
    try {
        val parts = token.split(".")
        if (parts.size != 3) return 0

        val payload = parts[1].decodeBase64Url()
        val json = Json.parseToJsonElement(payload).jsonObject

        // exp - стандартное поле JWT для времени истечения
        return json["exp"]?.jsonPrimitive?.long ?: 0
    } catch (e: Exception) {
        println("Error extracting expiration from token: ${e.message}")
        return 0
    }
}

/**
 * Функция для декодирования Base64Url строки в обычную строку
 */
@OptIn(ExperimentalEncodingApi::class)
private fun String.decodeBase64Url(): String {
    // Используем встроенный декодер Base64
    return kotlin.io.encoding.Base64.decode(
        // Заменяем символы URL-safe Base64 на стандартные Base64 и добавляем паддинг
        this.replace('-', '+')
            .replace('_', '/')
            .let {
                // Добавляем недостающие символы '=' для корректного декодирования
                when (it.length % 4) {
                    0 -> it
                    2 -> it + "=="
                    3 -> it + "="
                    else -> it
                }
            }
    ).decodeToString()
}

/**
 * Реализация API для аутентификации
 */
class AuthApiImpl(
    private val client: HttpClient,
    private val networkConfig: NetworkConfig
) : AuthApi {

    private suspend fun getBaseUrl(): String = networkConfig.getBaseUrl()

    override suspend fun register(request: RegisterRequest): AuthResultNetwork<NetworkAuthResponseDto> {
        return try {
            val baseUrl = getBaseUrl()
            val networkRequest = NetworkRegisterRequest(
                username = request.username,
                email = request.email,
                password = request.password
            )

            val response = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(networkRequest)
            }

            val networkResponse = response.body<NetworkAuthResponse>()

            // Возвращаем успешный результат
            AuthResultNetwork.Success(
                NetworkAuthResponseDto(
                    accessToken = networkResponse.accessToken,
                    refreshToken = networkResponse.refreshToken,
                    tokenType = networkResponse.tokenType
                )
            )
        } catch (e: Exception) {
            handleAuthApiException(e)
        }
    }

    override suspend fun login(request: LoginRequest): AuthResultNetwork<NetworkAuthResponseDto> {
        return try {
            val baseUrl = getBaseUrl()
            val networkRequest = NetworkLoginRequest(
                username = request.username,
                password = request.password
            )

            val response = client.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(networkRequest)
            }

            val networkResponse = response.body<NetworkAuthResponse>()

            // Возвращаем успешный результат
            AuthResultNetwork.Success(
                NetworkAuthResponseDto(
                    accessToken = networkResponse.accessToken,
                    refreshToken = networkResponse.refreshToken,
                    tokenType = networkResponse.tokenType
                )
            )
        } catch (e: Exception) {
            handleAuthApiException(e)
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): AuthResultNetwork<NetworkAuthResponseDto> {
        return try {
            val baseUrl = getBaseUrl()
            val networkRequest = NetworkRefreshTokenRequest(
                refreshToken = request.refreshToken
            )

            val response = client.post("$baseUrl/auth/refresh-token") {
                contentType(ContentType.Application.Json)
                setBody(networkRequest)
            }

            val networkResponse = response.body<NetworkAuthResponse>()

            // Возвращаем успешный результат
            AuthResultNetwork.Success(
                NetworkAuthResponseDto(
                    accessToken = networkResponse.accessToken,
                    refreshToken = networkResponse.refreshToken,
                    tokenType = networkResponse.tokenType
                )
            )
        } catch (e: Exception) {
            handleAuthApiException(e)
        }
    }

    override suspend fun getCurrentUser(token: String): AuthResultNetwork<User> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.get("$baseUrl/auth/me") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            val networkResponse = response.body<NetworkUserResponse>()

            // Маппинг сетевой модели в доменную
            val user = mapNetworkUserToUser(networkResponse)

            AuthResultNetwork.Success(user)
        } catch (e: Exception) {
            handleAuthApiException(e)
        }
    }

    override suspend fun logout(token: String): AuthResultNetwork<Unit> {
        return try {
            val baseUrl = getBaseUrl()
            client.post("$baseUrl/auth/logout") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            AuthResultNetwork.Success(Unit)
        } catch (e: Exception) {
            handleAuthApiException(e)
        }
    }

    override suspend fun checkServerStatus(): AuthResultNetwork<ServerStatusResponse> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.get("$baseUrl/status")

            val networkResponse = response.body<NetworkServerStatusResponse>()

            AuthResultNetwork.Success(
                ServerStatusResponse(
                    status = networkResponse.status,
                    version = networkResponse.version,
                    uptime = networkResponse.uptime,
                    message = networkResponse.message
                )
            )
        } catch (e: Exception) {
            handleAuthApiException(e)
        }
    }

    override suspend fun updateProfile(token: String, username: String): AuthResultNetwork<User> {
        return try {
            val baseUrl = getBaseUrl()
            val response = client.patch("$baseUrl/auth/profile") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(mapOf("username" to username))
            }

            val networkResponse = response.body<NetworkUserResponse>()

            // Маппинг сетевой модели в доменную
            val user = mapNetworkUserToUser(networkResponse)

            AuthResultNetwork.Success(user)
        } catch (e: Exception) {
            handleAuthApiException(e)
        }
    }

    /**
     * Маппинг сетевой модели пользователя в доменную
     */
    private fun mapNetworkUserToUser(networkUser: NetworkUserResponse): User {
        // Преобразуем профиль
        val profile = UserProfile(
            firstName = networkUser.profile.firstName,
            lastName = networkUser.profile.lastName,
            avatarUrl = networkUser.profile.avatarUrl,
            bio = networkUser.profile.bio,
            location = networkUser.profile.location,
            website = networkUser.profile.website,
            githubUrl = networkUser.profile.githubUrl,
            linkedinUrl = networkUser.profile.linkedinUrl,
            twitterUrl = networkUser.profile.twitterUrl,
            additionalData = networkUser.profile.additionalData
        )

        // Преобразуем предпочтения
        val preferences = UserPreferences(
            theme = networkUser.preferences.theme,
            fontSize = networkUser.preferences.fontSize,
            emailNotifications = networkUser.preferences.emailNotifications,
            pushNotifications = networkUser.preferences.pushNotifications,
            beveragePreference = networkUser.preferences.beveragePreference,
            breakReminder = networkUser.preferences.breakReminder,
            breakIntervalMinutes = networkUser.preferences.breakIntervalMinutes,
            additionalPreferences = networkUser.preferences.additionalPreferences
        )

        // Преобразуем рейтинги
        val ratings = UserRatings(
            contributionRating = networkUser.ratings.contributionRating,
            botScore = networkUser.ratings.botScore,
            expertiseRating = networkUser.ratings.expertiseRating,
            competitionRating = networkUser.ratings.competitionRating,
            additionalRatings = networkUser.ratings.additionalRatings
        )

        // Собираем полную модель пользователя
        return User(
            id = networkUser.id,
            email = networkUser.email,
            username = networkUser.username,
            isActive = networkUser.isActive,
            isVerified = networkUser.isVerified,
            role = networkUser.role,
            authProvider = networkUser.authProvider,
            lang = networkUser.lang,
            createdAt = networkUser.createdAt,
            updatedAt = networkUser.updatedAt,
            lastLoginAt = networkUser.lastLoginAt,
            profile = profile,
            preferences = preferences,
            ratings = ratings,
            oauthProviders = emptyList()
        )
    }

    /**
     * Обработка ошибок API аутентификации
     */
    private fun <T> handleAuthApiException(e: Exception): AuthResultNetwork<T> {
        return when (e) {
            is ClientRequestException -> {
                val statusCode = e.response.status.value
                val errorMessage = when {
                    HttpClientFactory.isUnauthorized(e.response) -> "Неавторизованный доступ"
                    HttpClientFactory.isForbidden(e.response) -> "Доступ запрещен"
                    HttpClientFactory.isNotFound(e.response) -> "Ресурс не найден"
                    HttpClientFactory.isServerError(e.response) -> "Ошибка сервера"
                    else -> "Ошибка: ${e.response.status.description}"
                }

                AuthResultNetwork.Error(
                    ApiError(
                        code = statusCode,
                        message = errorMessage
                    )
                )
            }
            is SerializationException -> {
                AuthResultNetwork.Error(
                    ApiError(
                        code = -1,
                        message = "Ошибка при обработке ответа: ${e.message}"
                    )
                )
            }
            else -> {
                AuthResultNetwork.Error(
                    ApiError(
                        code = -1,
                        message = "Неизвестная ошибка: ${e.message}"
                    )
                )
            }
        }
    }
}
