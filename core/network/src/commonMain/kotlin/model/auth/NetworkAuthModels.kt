package model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Запрос на регистрацию пользователя (сетевая модель)
 */
@Serializable
data class NetworkRegisterRequest(
    @SerialName("username") val username: String,
    @SerialName("email") val email: String,
    @SerialName("password") val password: String
)

/**
 * Запрос на вход в систему (сетевая модель)
 */
@Serializable
data class NetworkLoginRequest(
    @SerialName("username") val username: String,
    @SerialName("password") val password: String
)

/**
 * Запрос на обновление токена (сетевая модель)
 */
@Serializable
data class NetworkRefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

/**
 * Ответ на запрос аутентификации (сетевая модель)
 * В соответствии с ответом сервера:
 * {
 *   "access_token": "jwt_token",
 *   "refresh_token": "refresh_token",
 *   "token_type": "bearer"
 * }
 */
@Serializable
data class NetworkAuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String
)

/**
 * Модель профиля пользователя
 */
@Serializable
data class NetworkUserProfile(
    @SerialName("first_name") val firstName: String = "",
    @SerialName("last_name") val lastName: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("website") val website: String? = null,
    @SerialName("github_url") val githubUrl: String? = null,
    @SerialName("linkedin_url") val linkedinUrl: String? = null,
    @SerialName("twitter_url") val twitterUrl: String? = null,
    @SerialName("additional_data") val additionalData: Map<String, String> = emptyMap(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

/**
 * Модель предпочтений пользователя
 */
@Serializable
data class NetworkUserPreferences(
    @SerialName("theme") val theme: String,
    @SerialName("font_size") val fontSize: String,
    @SerialName("email_notifications") val emailNotifications: Boolean,
    @SerialName("push_notifications") val pushNotifications: Boolean,
    @SerialName("beverage_preference") val beveragePreference: String,
    @SerialName("break_reminder") val breakReminder: Boolean,
    @SerialName("break_interval_minutes") val breakIntervalMinutes: Int,
    @SerialName("additional_preferences") val additionalPreferences: Map<String, String> = emptyMap(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

/**
 * Модель рейтингов пользователя
 */
@Serializable
data class NetworkUserRatings(
    @SerialName("contribution_rating") val contributionRating: Float,
    @SerialName("bot_score") val botScore: Float,
    @SerialName("expertise_rating") val expertiseRating: Float,
    @SerialName("competition_rating") val competitionRating: Float,
    @SerialName("additional_ratings") val additionalRatings: Map<String, String> = emptyMap(),
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

/**
 * Данные о OAuth провайдерах пользователя
 */
@Serializable
data class NetworkOAuthProvider(
    @SerialName("provider") val provider: String,
    @SerialName("provider_user_id") val providerUserId: String,
    @SerialName("created_at") val createdAt: String
)

/**
 * Полная модель пользователя, соответствующая ответу сервера на /auth/me
 */
@Serializable
data class NetworkUserResponse(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String,
    @SerialName("username") val username: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("is_verified") val isVerified: Boolean,
    @SerialName("role") val role: String,
    @SerialName("auth_provider") val authProvider: String,
    @SerialName("lang") val lang: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("last_login_at") val lastLoginAt: String? = null,
    @SerialName("profile") val profile: NetworkUserProfile,
    @SerialName("preferences") val preferences: NetworkUserPreferences,
    @SerialName("ratings") val ratings: NetworkUserRatings,
    @SerialName("oauth_providers") val oauthProviders: List<NetworkOAuthProvider> = emptyList()
)

/**
 * Ответ о статусе сервера (сетевая модель)
 */
@Serializable
data class NetworkServerStatusResponse(
    @SerialName("status") val status: String,
    @SerialName("version") val version: String,
    @SerialName("uptime") val uptime: Long,
    @SerialName("message") val message: String? = null
)

/**
 * Объект ошибки с сервера (сетевая модель)
 */
@Serializable
data class NetworkErrorResponse(
    @SerialName("code") val code: Int,
    @SerialName("message") val message: String,
    @SerialName("details") val details: String? = null
)
