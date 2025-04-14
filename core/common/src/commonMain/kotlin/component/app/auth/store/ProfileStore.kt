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
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.auth.AuthUseCases
import logging.Logger
import repository.auth.AuthRepository
import utils.rDispatchers

interface ProfileStore : Store<ProfileStore.Intent, ProfileStore.State, Nothing> {
    sealed interface Intent {
        data object Init : Intent
        data class UpdateUsername(val username: String) : Intent
        data object SaveProfile : Intent
        data object Logout : Intent
    }

    @Serializable
    data class State(
        val username: String = "",
        val loading: Boolean = false,
        val error: String? = null,
        val errorDetails: String? = null
    )

    sealed interface Message {
        data object StartLoading : Message
        data object StopLoading : Message
        data class SetError(val error: String, val details: String? = null) : Message
        data object ClearError : Message
        data class UpdateUsername(val username: String) : Message
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
            // Initialize state if needed
            scope.launch {
                try {
                    // Get current user to initialize state
                    val userResult = authUseCases.getCurrentUser()
                    if (userResult is DomainResult.Success<UserDomain>) {
                        dispatch(ProfileStore.Message.UpdateUsername(userResult.data.username))
                    }
                } catch (e: Exception) {
                    logger.e("ProfileStore: Error initializing state", e)
                }
            }
        }

        override fun executeIntent(intent: ProfileStore.Intent): Unit =
            try {
                when (intent) {
                    is ProfileStore.Intent.Init -> {
                        logger.d("ProfileStore: Init intent received")
                        // Initialize state handled in executeAction
                    }

                    is ProfileStore.Intent.UpdateUsername -> {
                        logger.d("ProfileStore: Update username to ${intent.username}")
                        dispatch(ProfileStore.Message.UpdateUsername(intent.username))
                    }

                    is ProfileStore.Intent.SaveProfile -> {
                        scope.launch {
                            logger.i("ProfileStore: Saving profile with username: ${state().username}")
                            dispatch(ProfileStore.Message.StartLoading)
                            dispatch(ProfileStore.Message.ClearError)

                            try {
                                // Simulate network delay for better UX
                                delay(500)

                                // Try to find a way to update the user profile
                                // Option 1: Use the repository directly if it's available

                            } catch (e: Exception) {
                                logger.e("ProfileStore: Error updating profile", e)
                                dispatch(
                                    ProfileStore.Message.SetError(
                                        e.message ?: "Error updating profile",
                                        e.stackTraceToString()
                                    )
                                )
                            } finally {
                                dispatch(ProfileStore.Message.StopLoading)
                            }
                        }
                    }

                    is ProfileStore.Intent.Logout -> {
                        scope.launch {
                            logger.i("ProfileStore: Logging out")
                            dispatch(ProfileStore.Message.StartLoading)
                            dispatch(ProfileStore.Message.ClearError)

                            try {
                                // Simulate network delay
                                delay(300)

                                val result = authUseCases.logout()

                                when (result) {
                                    is DomainResult.Success<Unit> -> {
                                        logger.i("ProfileStore: Logout successful")
                                        dispatch(ProfileStore.Message.LogoutSuccess)
                                    }

                                    is DomainResult.Error -> {
                                        logger.w("ProfileStore: Logout failed: ${result.error.message}")
                                        dispatch(
                                            ProfileStore.Message.SetError(
                                                result.error.message,
                                                result.error.details
                                            )
                                        )
                                    }

                                    is DomainResult.Loading -> {
                                        logger.d("ProfileStore: Logout loading")
                                        // Already in loading state
                                    }
                                }
                            } catch (e: Exception) {
                                logger.e("ProfileStore: Error during logout", e)
                                dispatch(
                                    ProfileStore.Message.SetError(
                                        e.message ?: "Error during logout"
                                    )
                                )
                            } finally {
                                dispatch(ProfileStore.Message.StopLoading)
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
                is ProfileStore.Message.SaveProfileSuccess -> copy(loading = false, error = null)
                is ProfileStore.Message.LogoutSuccess -> copy(loading = false, error = null)
            }
    }
}
