package model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Сетевая модель пользователя
 * Соответствует ответу сервера с /auth/me эндпоинта
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
    @SerialName("preferences") val preferences: NetworkUserPreferences? = null,
    @SerialName("ratings") val ratings: NetworkUserRatings? = null,
    @SerialName("oauth_providers") val oauthProviders: List<NetworkOAuthProvider> = emptyList()
)

/**
 * Сетевая модель профиля пользователя
 */
@Serializable
data class NetworkUserProfile(
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("bio") val bio: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("website") val website: String? = null,
    @SerialName("github_url") val githubUrl: String? = null,
    @SerialName("linkedin_url") val linkedinUrl: String? = null,
    @SerialName("twitter_url") val twitterUrl: String? = null,
    @SerialName("additional_data") val additionalData: Map<String, String> = emptyMap(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Сетевая модель настроек пользователя
 */
@Serializable
data class NetworkUserPreferences(
    @SerialName("theme") val theme: String? = null,
    @SerialName("font_size") val fontSize: String? = null,
    @SerialName("email_notifications") val emailNotifications: Boolean = false,
    @SerialName("push_notifications") val pushNotifications: Boolean = false,
    @SerialName("beverage_preference") val beveragePreference: String? = null,
    @SerialName("break_reminder") val breakReminder: Boolean = false,
    @SerialName("break_interval_minutes") val breakIntervalMinutes: Int = 0,
    @SerialName("additional_preferences") val additionalPreferences: Map<String, String> = emptyMap(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Сетевая модель рейтингов пользователя
 */
@Serializable
data class NetworkUserRatings(
    @SerialName("contribution_rating") val contributionRating: Float = 0f,
    @SerialName("bot_score") val botScore: Float = 0f,
    @SerialName("expertise_rating") val expertiseRating: Float = 0f,
    @SerialName("competition_rating") val competitionRating: Float = 0f,
    @SerialName("additional_ratings") val additionalRatings: Map<String, String> = emptyMap(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Сетевая модель OAuth провайдера
 */
@Serializable
data class NetworkOAuthProvider(
    @SerialName("provider") val provider: String,
    @SerialName("provider_user_id") val providerUserId: String,
    @SerialName("created_at") val createdAt: String? = null
)
