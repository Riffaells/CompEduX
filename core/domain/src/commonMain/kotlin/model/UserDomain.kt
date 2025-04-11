package model

/**
 * Domain model for user
 * Contains basic user information
 */
data class UserDomain(
    val id: String,
    val email: String,
    val username: String,
    val isActive: Boolean = true,
    val isVerified: Boolean = false,
    val role: String = "user",
    val authProvider: String = "email",
    val lang: String? = null,
    val profile: UserProfileDomain? = null,
    val preferences: UserPreferencesDomain? = null,
    val ratings: UserRatingsDomain? = null
)

/**
 * Domain model for user profile
 */
data class UserProfileDomain(
    val firstName: String? = null,
    val lastName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val website: String? = null,
    val githubUrl: String? = null,
    val linkedinUrl: String? = null,
    val twitterUrl: String? = null
)

/**
 * Domain model for user preferences
 */
data class UserPreferencesDomain(
    val theme: String = "light",
    val fontSize: String = "medium",
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val beveragePreference: String = "none",
    val breakReminder: Boolean = true,
    val breakIntervalMinutes: Int = 60
)

/**
 * Domain model for user ratings
 */
data class UserRatingsDomain(
    val contributionRating: Int = 0,
    val botScore: Int = 0,
    val expertiseRating: Int = 0,
    val competitionRating: Int = 0
)

/**
 * Domain model for OAuth provider
 */
data class OAuthProviderDomain(
    val provider: String,
    val providerUserId: String
)
