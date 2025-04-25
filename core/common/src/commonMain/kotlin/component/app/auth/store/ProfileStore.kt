package component.app.auth.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import model.DomainResult
import model.UserDomain
import model.UserPreferencesDomain
import model.UserProfileDomain
import model.UserRatingsDomain
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.auth.AuthUseCases
import logging.Logger
import repository.auth.AuthRepository
import navigation.rDispatchers

interface ProfileStore : Store<ProfileStore.Intent, ProfileStore.State, Nothing> {
    sealed interface Intent {
        data object Init : Intent
        data class UpdateUsername(val username: String) : Intent

        // Интенты для обновления профиля
        data class UpdateFirstName(val firstName: String) : Intent
        data class UpdateLastName(val lastName: String) : Intent
        data class UpdateBio(val bio: String) : Intent
        data class UpdateLocation(val location: String) : Intent
        data class UpdateWebsite(val website: String) : Intent
        data class UpdateGithubUrl(val githubUrl: String) : Intent
        data class UpdateLinkedinUrl(val linkedinUrl: String) : Intent
        data class UpdateTwitterUrl(val twitterUrl: String) : Intent

        // Интенты для обновления настроек
        data class UpdateTheme(val theme: String) : Intent
        data class UpdateFontSize(val fontSize: String) : Intent
        data class UpdateEmailNotifications(val enabled: Boolean) : Intent
        data class UpdatePushNotifications(val enabled: Boolean) : Intent
        data class UpdateBeveragePreference(val preference: String) : Intent
        data class UpdateBreakReminder(val enabled: Boolean) : Intent
        data class UpdateBreakInterval(val minutes: Int) : Intent

        // Для внутреннего использования
        data class UpdateProfile(val profile: UserProfileDomain) : Intent
        data class UpdatePreferences(val preferences: UserPreferencesDomain) : Intent

        data object SaveProfile : Intent
        data object Logout : Intent
    }

    @Serializable
    data class State(
        // Базовые поля пользователя
        val id: String = "",
        val email: String = "",
        val username: String = "",
        val isActive: Boolean = true,
        val isVerified: Boolean = false,
        val role: String = "user",
        val authProvider: String = "email",
        val lang: String? = null,

        // Профиль пользователя
        val firstName: String? = null,
        val lastName: String? = null,
        val avatarUrl: String? = null,
        val bio: String? = null,
        val location: String? = null,
        val website: String? = null,
        val githubUrl: String? = null,
        val linkedinUrl: String? = null,
        val twitterUrl: String? = null,

        // Настройки пользователя
        val theme: String = "light",
        val fontSize: String = "medium",
        val emailNotifications: Boolean = true,
        val pushNotifications: Boolean = true,
        val beveragePreference: String = "none",
        val breakReminder: Boolean = true,
        val breakIntervalMinutes: Int = 60,

        // Рейтинги пользователя
        val contributionRating: Float = 0f,
        val botScore: Float = 0f,
        val expertiseRating: Float = 0f,
        val competitionRating: Float = 0f,

        // Состояние UI
        val loading: Boolean = false,
        val error: String? = null,
        val errorDetails: String? = null
    ) {
        // Вспомогательные методы для получения связанных объектов
        fun getUserProfile(): UserProfileDomain = UserProfileDomain(
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

        fun getUserPreferences(): UserPreferencesDomain = UserPreferencesDomain(
            theme = theme,
            fontSize = fontSize,
            emailNotifications = emailNotifications,
            pushNotifications = pushNotifications,
            beveragePreference = beveragePreference,
            breakReminder = breakReminder,
            breakIntervalMinutes = breakIntervalMinutes
        )

        fun getUserRatings(): UserRatingsDomain = UserRatingsDomain(
            contributionRating = contributionRating,
            botScore = botScore,
            expertiseRating = expertiseRating,
            competitionRating = competitionRating
        )
    }

    sealed interface Message {
        data object StartLoading : Message
        data object StopLoading : Message
        data class SetError(val error: String, val details: String? = null) : Message
        data object ClearError : Message
        data class UpdateUserData(val user: UserDomain) : Message
        data class UpdateUsername(val username: String) : Message

        // Сообщения для обновления профиля
        data class UpdateFirstName(val firstName: String) : Message
        data class UpdateLastName(val lastName: String) : Message
        data class UpdateBio(val bio: String) : Message
        data class UpdateLocation(val location: String) : Message
        data class UpdateWebsite(val website: String) : Message
        data class UpdateGithubUrl(val githubUrl: String) : Message
        data class UpdateLinkedinUrl(val linkedinUrl: String) : Message
        data class UpdateTwitterUrl(val twitterUrl: String) : Message

        // Сообщения для обновления настроек
        data class UpdateTheme(val theme: String) : Message
        data class UpdateFontSize(val fontSize: String) : Message
        data class UpdateEmailNotifications(val enabled: Boolean) : Message
        data class UpdatePushNotifications(val enabled: Boolean) : Message
        data class UpdateBeveragePreference(val preference: String) : Message
        data class UpdateBreakReminder(val enabled: Boolean) : Message
        data class UpdateBreakInterval(val minutes: Int) : Message

        data class UpdateProfile(val profile: UserProfileDomain) : Message
        data class UpdatePreferences(val preferences: UserPreferencesDomain) : Message
        data object SaveProfileSuccess : Message
        data object LogoutSuccess : Message
    }
}

class ProfileStoreFactory(
    private val storeFactory: StoreFactory,
    override val di: DI
) : DIAware {
    private val authUseCases by instance<AuthUseCases>()
    private val authRepository by instance<AuthRepository>()
    private val logger by instance<Logger>()

    fun create(): ProfileStore =
        object : ProfileStore, Store<ProfileStore.Intent, ProfileStore.State, Nothing> by storeFactory.create(
            name = "ProfileStore",
            initialState = ProfileStore.State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = { ExecutorImpl() },
            reducer = ReducerImpl
        ) {}

    private inner class ExecutorImpl :
        CoroutineExecutor<ProfileStore.Intent, Unit, ProfileStore.State, ProfileStore.Message, Nothing>(
            rDispatchers.main
        ) {

        override fun executeAction(action: Unit) {
            logger.d("ProfileStore: Initializing")
            scope.launch {
                try {
                    // Get current user to initialize state
                    val userResult = authUseCases.getCurrentUser()
                    if (userResult is DomainResult.Success<UserDomain>) {
                        dispatch(ProfileStore.Message.UpdateUserData(userResult.data))
                    }
                } catch (e: Exception) {
                    logger.e("ProfileStore: Error initializing state", e)
                }
            }
        }

        var l = 0
        override fun executeIntent(intent: ProfileStore.Intent): Unit =
            try {
                when (intent) {
                    is ProfileStore.Intent.Init -> {
                        logger.d("ProfileStore: Init intent received")
                    }

                    is ProfileStore.Intent.UpdateUsername -> {
                        logger.d("ProfileStore: Update username to ${intent.username}")
                        dispatch(ProfileStore.Message.UpdateUsername(intent.username))
                    }

                    // Профиль
                    is ProfileStore.Intent.UpdateFirstName -> {
                        dispatch(ProfileStore.Message.UpdateFirstName(intent.firstName))
                    }

                    is ProfileStore.Intent.UpdateLastName -> {
                        dispatch(ProfileStore.Message.UpdateLastName(intent.lastName))
                    }

                    is ProfileStore.Intent.UpdateBio -> {
                        dispatch(ProfileStore.Message.UpdateBio(intent.bio))
                    }

                    is ProfileStore.Intent.UpdateLocation -> {
                        dispatch(ProfileStore.Message.UpdateLocation(intent.location))
                    }

                    is ProfileStore.Intent.UpdateWebsite -> {
                        dispatch(ProfileStore.Message.UpdateWebsite(intent.website))
                    }

                    is ProfileStore.Intent.UpdateGithubUrl -> {
                        dispatch(ProfileStore.Message.UpdateGithubUrl(intent.githubUrl))
                    }

                    is ProfileStore.Intent.UpdateLinkedinUrl -> {
                        dispatch(ProfileStore.Message.UpdateLinkedinUrl(intent.linkedinUrl))
                    }

                    is ProfileStore.Intent.UpdateTwitterUrl -> {
                        dispatch(ProfileStore.Message.UpdateTwitterUrl(intent.twitterUrl))
                    }

                    // Настройки
                    is ProfileStore.Intent.UpdateTheme -> {
                        dispatch(ProfileStore.Message.UpdateTheme(intent.theme))
                    }

                    is ProfileStore.Intent.UpdateFontSize -> {
                        dispatch(ProfileStore.Message.UpdateFontSize(intent.fontSize))
                    }

                    is ProfileStore.Intent.UpdateEmailNotifications -> {
                        dispatch(ProfileStore.Message.UpdateEmailNotifications(intent.enabled))
                    }

                    is ProfileStore.Intent.UpdatePushNotifications -> {
                        dispatch(ProfileStore.Message.UpdatePushNotifications(intent.enabled))
                    }

                    is ProfileStore.Intent.UpdateBeveragePreference -> {
                        dispatch(ProfileStore.Message.UpdateBeveragePreference(intent.preference))
                    }

                    is ProfileStore.Intent.UpdateBreakReminder -> {
                        dispatch(ProfileStore.Message.UpdateBreakReminder(intent.enabled))
                    }

                    is ProfileStore.Intent.UpdateBreakInterval -> {
                        dispatch(ProfileStore.Message.UpdateBreakInterval(intent.minutes))
                    }

                    is ProfileStore.Intent.UpdateProfile -> {
                        logger.d("ProfileStore: Update profile")
                        dispatch(ProfileStore.Message.UpdateProfile(intent.profile))
                    }

                    is ProfileStore.Intent.UpdatePreferences -> {
                        logger.d("ProfileStore: Update preferences")
                        dispatch(ProfileStore.Message.UpdatePreferences(intent.preferences))
                    }

                    is ProfileStore.Intent.SaveProfile -> {
                        scope.launch {
                            logger.i("ProfileStore: Saving profile with username: ${state().username}")
                            dispatch(ProfileStore.Message.StartLoading)
                            dispatch(ProfileStore.Message.ClearError)



                                // TODO: Реализовать обновление профиля через репозиторий
                                dispatch(ProfileStore.Message.SaveProfileSuccess)


                        }
                    }

                    is ProfileStore.Intent.Logout -> {
                        scope.launch {
                            logger.i("ProfileStore: Logging out")
                            dispatch(ProfileStore.Message.StartLoading)
                            dispatch(ProfileStore.Message.ClearError)


                            val result = authUseCases.logout()
                            l+=1
                            logger.i("ProfileStore: Count $l")

                            result
                                .onSuccess {
                                    logger.i("ProfileStore: Logout successful")
                                    dispatch(ProfileStore.Message.LogoutSuccess)
                                }.onError { error ->
                                    logger.w("ProfileStore: Logout failed: ${error.message}")
                                    dispatch(
                                        ProfileStore.Message.SetError(
                                            error.message,
                                            error.details
                                        )
                                    )
                                }


                        }
                    }
                }
                Unit
            } catch (e: Exception) {
                logger.e("ProfileStore: Error in executeIntent", e)
            }

        private fun handleUpdateResult(result: DomainResult<UserDomain>) {
            when (result) {
                is DomainResult.Success<UserDomain> -> {
                    logger.i("ProfileStore: Profile update successful")
                    dispatch(ProfileStore.Message.SaveProfileSuccess)
                }

                is DomainResult.Error -> {
                    logger.w("ProfileStore: Profile update failed: ${result.error.message}")
                    dispatch(
                        ProfileStore.Message.SetError(
                            result.error.message,
                            result.error.details
                        )
                    )
                }

                is DomainResult.Loading -> {
                    logger.d("ProfileStore: Profile update loading")
                    // Already in loading state
                }
            }
        }
    }

    private object ReducerImpl : Reducer<ProfileStore.State, ProfileStore.Message> {
        override fun ProfileStore.State.reduce(msg: ProfileStore.Message): ProfileStore.State =
            when (msg) {
                is ProfileStore.Message.StartLoading -> copy(loading = true)
                is ProfileStore.Message.StopLoading -> copy(loading = false)
                is ProfileStore.Message.SetError -> copy(
                    loading = false,
                    error = msg.error,
                    errorDetails = msg.details
                )

                is ProfileStore.Message.ClearError -> copy(
                    error = null,
                    errorDetails = null
                )

                is ProfileStore.Message.UpdateUsername -> copy(username = msg.username)

                // Обработка сообщений для профиля
                is ProfileStore.Message.UpdateFirstName -> copy(firstName = msg.firstName)
                is ProfileStore.Message.UpdateLastName -> copy(lastName = msg.lastName)
                is ProfileStore.Message.UpdateBio -> copy(bio = msg.bio)
                is ProfileStore.Message.UpdateLocation -> copy(location = msg.location)
                is ProfileStore.Message.UpdateWebsite -> copy(website = msg.website)
                is ProfileStore.Message.UpdateGithubUrl -> copy(githubUrl = msg.githubUrl)
                is ProfileStore.Message.UpdateLinkedinUrl -> copy(linkedinUrl = msg.linkedinUrl)
                is ProfileStore.Message.UpdateTwitterUrl -> copy(twitterUrl = msg.twitterUrl)

                // Обработка сообщений для настроек
                is ProfileStore.Message.UpdateTheme -> copy(theme = msg.theme)
                is ProfileStore.Message.UpdateFontSize -> copy(fontSize = msg.fontSize)
                is ProfileStore.Message.UpdateEmailNotifications -> copy(emailNotifications = msg.enabled)
                is ProfileStore.Message.UpdatePushNotifications -> copy(pushNotifications = msg.enabled)
                is ProfileStore.Message.UpdateBeveragePreference -> copy(beveragePreference = msg.preference)
                is ProfileStore.Message.UpdateBreakReminder -> copy(breakReminder = msg.enabled)
                is ProfileStore.Message.UpdateBreakInterval -> copy(breakIntervalMinutes = msg.minutes)

                is ProfileStore.Message.UpdateUserData -> {
                    val user = msg.user
                    copy(
                        id = user.id,
                        email = user.email,
                        username = user.username,
                        isActive = user.isActive,
                        isVerified = user.isVerified,
                        role = user.role,
                        authProvider = user.authProvider,
                        lang = user.lang,

                        // Копируем данные профиля, если они есть
                        firstName = user.profile?.firstName,
                        lastName = user.profile?.lastName,
                        avatarUrl = user.profile?.avatarUrl,
                        bio = user.profile?.bio,
                        location = user.profile?.location,
                        website = user.profile?.website,
                        githubUrl = user.profile?.githubUrl,
                        linkedinUrl = user.profile?.linkedinUrl,
                        twitterUrl = user.profile?.twitterUrl,

                        // Копируем настройки, если они есть
                        theme = user.preferences?.theme ?: "light",
                        fontSize = user.preferences?.fontSize ?: "medium",
                        emailNotifications = user.preferences?.emailNotifications ?: true,
                        pushNotifications = user.preferences?.pushNotifications ?: true,
                        beveragePreference = user.preferences?.beveragePreference ?: "none",
                        breakReminder = user.preferences?.breakReminder ?: true,
                        breakIntervalMinutes = user.preferences?.breakIntervalMinutes ?: 60,

                        // Копируем рейтинги, если они есть
                        contributionRating = user.ratings?.contributionRating ?: 0f,
                        botScore = user.ratings?.botScore ?: 0f,
                        expertiseRating = user.ratings?.expertiseRating ?: 0f,
                        competitionRating = user.ratings?.competitionRating ?: 0f
                    )
                }

                is ProfileStore.Message.UpdateProfile -> {
                    val profile = msg.profile
                    copy(
                        firstName = profile.firstName,
                        lastName = profile.lastName,
                        avatarUrl = profile.avatarUrl,
                        bio = profile.bio,
                        location = profile.location,
                        website = profile.website,
                        githubUrl = profile.githubUrl,
                        linkedinUrl = profile.linkedinUrl,
                        twitterUrl = profile.twitterUrl
                    )
                }

                is ProfileStore.Message.UpdatePreferences -> {
                    val prefs = msg.preferences
                    copy(
                        theme = prefs.theme,
                        fontSize = prefs.fontSize,
                        emailNotifications = prefs.emailNotifications,
                        pushNotifications = prefs.pushNotifications,
                        beveragePreference = prefs.beveragePreference,
                        breakReminder = prefs.breakReminder,
                        breakIntervalMinutes = prefs.breakIntervalMinutes
                    )
                }

                is ProfileStore.Message.SaveProfileSuccess -> copy(loading = false, error = null)
                is ProfileStore.Message.LogoutSuccess -> copy(loading = false, error = null)
            }
    }
}
