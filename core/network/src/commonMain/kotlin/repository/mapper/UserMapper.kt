package repository.mapper

import model.OAuthProvider
import model.User
import model.UserPreferences
import model.UserProfile
import model.UserRatings
import model.auth.NetworkUserResponse

/**
 * Маппер для преобразования сетевых моделей пользователей в доменные
 */
interface UserMapper {
    /**
     * Конвертирует сетевую модель пользователя в доменную
     * @param networkUser сетевая модель пользователя
     * @return доменная модель пользователя
     */
    fun mapToDomain(networkUser: NetworkUserResponse): User
}

/**
 * Реализация маппера пользователей
 */
class UserMapperImpl : UserMapper {
    override fun mapToDomain(networkUser: NetworkUserResponse): User {
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

        // Преобразуем провайдеры OAuth
        val oauthProviders = networkUser.oauthProviders.map {
            OAuthProvider(
                provider = it.provider,
                providerUserId = it.providerUserId,
                createdAt = it.createdAt
            )
        }

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
            oauthProviders = oauthProviders
        )
    }
}
