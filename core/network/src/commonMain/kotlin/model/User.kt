package model

/**
 * Доменная модель пользователя
 */
data class User(
    val id: String,
    val email: String,
    val username: String,
    val isActive: Boolean,
    val isVerified: Boolean,
    val role: String,
    val authProvider: String,
    val lang: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val lastLoginAt: String? = null,
    val profile: UserProfile,
    val preferences: UserPreferences,
    val ratings: UserRatings,
    val oauthProviders: List<OAuthProvider> = emptyList()
)

/**
 * Доменная модель профиля пользователя
 */
data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val website: String? = null,
    val githubUrl: String? = null,
    val linkedinUrl: String? = null,
    val twitterUrl: String? = null,
    val additionalData: Map<String, String> = emptyMap()
)

/**
 * Доменная модель предпочтений пользователя
 */
data class UserPreferences(
    val theme: String,
    val fontSize: String,
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val beveragePreference: String,
    val breakReminder: Boolean,
    val breakIntervalMinutes: Int,
    val additionalPreferences: Map<String, String> = emptyMap()
)

/**
 * Доменная модель рейтингов пользователя
 */
data class UserRatings(
    val contributionRating: Float,
    val botScore: Float,
    val expertiseRating: Float,
    val competitionRating: Float,
    val additionalRatings: Map<String, String> = emptyMap()
)

/**
 * Доменная модель OAuth провайдера
 */
data class OAuthProvider(
    val provider: String,
    val providerUserId: String,
    val createdAt: String
)
