package mapper

import model.OAuthProviderDomain
import model.UserDomain
import model.UserPreferencesDomain
import model.UserProfileDomain
import model.UserRatingsDomain
import model.auth.AuthResponseDomain
import model.auth.ServerStatusResponseDomain
import model.auth.NetworkAuthResponse
import model.auth.NetworkServerStatusResponse
import model.user.NetworkOAuthProvider
import model.user.NetworkUserPreferences
import model.user.NetworkUserProfile
import model.user.NetworkUserRatings
import model.user.NetworkUserResponse

/**
 * Преобразует сетевую модель пользователя в доменную
 * @return доменная модель пользователя
 */
fun NetworkUserResponse.toDomain(): UserDomain {
    return UserDomain(
        id = id,
        email = email,
        username = username,
        isActive = isActive,
        isVerified = isVerified,
        role = role,
        authProvider = authProvider,
        lang = lang,
        profile = profile.toDomain(),
        preferences = preferences?.toDomain(),
        ratings = ratings?.toDomain()
    )
}

/**
 * Преобразует сетевую модель профиля в доменную
 * @return доменная модель профиля пользователя
 */
fun NetworkUserProfile.toDomain(): UserProfileDomain {
    return UserProfileDomain(
        firstName = firstName,
        lastName = lastName,
        avatarUrl = avatarUrl,
        bio = bio,
        location = location,
        website = website,
        githubUrl = githubUrl,
        linkedinUrl = linkedinUrl,
        twitterUrl = twitterUrl
    )
}

/**
 * Преобразует сетевую модель настроек в доменную
 * @return доменная модель настроек пользователя
 */
fun NetworkUserPreferences.toDomain(): UserPreferencesDomain {
    return UserPreferencesDomain(
        theme = theme ?: "light",
        fontSize = fontSize ?: "medium",
        emailNotifications = emailNotifications,
        pushNotifications = pushNotifications,
        beveragePreference = beveragePreference ?: "none",
        breakReminder = breakReminder,
        breakIntervalMinutes = breakIntervalMinutes
    )
}

/**
 * Преобразует сетевую модель рейтингов в доменную
 * @return доменная модель рейтингов пользователя
 */
fun NetworkUserRatings.toDomain(): UserRatingsDomain {
    return UserRatingsDomain(
        contributionRating = contributionRating,
        botScore = botScore,
        expertiseRating = expertiseRating,
        competitionRating = competitionRating
    )
}

/**
 * Преобразует сетевую модель OAuth провайдера в доменную
 * @return доменная модель OAuth провайдера
 */
fun NetworkOAuthProvider.toDomain(): OAuthProviderDomain {
    return OAuthProviderDomain(
        provider = provider,
        providerUserId = providerUserId
    )
}

/**
 * Преобразует сетевую модель ответа авторизации в доменную
 * @return доменная модель ответа авторизации
 */
fun NetworkAuthResponse.toDomain(): AuthResponseDomain {
    return AuthResponseDomain(
        accessToken = accessToken,
        refreshToken = refreshToken,
        tokenType = tokenType
    )
}

/**
 * Преобразует сетевую модель ответа о статусе сервера в доменную
 * @return доменная модель ответа о статусе сервера
 */
fun NetworkServerStatusResponse.toDomain(): ServerStatusResponseDomain {
    return ServerStatusResponseDomain(
        status = status,
        version = version,
        uptime = uptime,
        message = message
    )
}
