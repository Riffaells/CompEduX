package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.login.DefaultLoginComponent
import component.app.auth.login.LoginComponent
import component.app.auth.register.DefaultRegisterComponent
import component.app.auth.register.RegisterComponent
import component.app.auth.store.AuthStore
import component.app.auth.store.AuthStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.factory
import usecase.auth.AuthUseCases
import utils.NavigationExecutor
import utils.rDispatchers

/**
 * Parameters for creating the authentication component.
 *
 * @property componentContext Decompose component context.
 * @property onBack Callback for returning from the authentication component.
 */
data class AuthComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)

/**
 * Parameters for creating the login component.
 *
 * @property componentContext Decompose component context.
 * @property onBack Callback for returning.
 * @property onRegister Callback for navigating to registration.
 * @property onLoginSuccess Callback for successful login.
 */
data class LoginComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit,
    val onRegister: () -> Unit,
    val onLoginSuccess: () -> Unit
)

/**
 * Parameters for creating the registration component.
 *
 * @property componentContext Decompose component context.
 * @property onBack Callback for returning.
 * @property onLogin Callback for navigating to login.
 * @property onRegisterSuccess Callback for successful registration.
 */
data class RegisterComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit,
    val onLogin: () -> Unit,
    val onRegisterSuccess: () -> Unit
)

/**
 * Parameters for creating the profile component.
 *
 * @property componentContext Decompose component context.
 * @property onLogout Callback for logging out.
 * @property onBackClicked Callback for returning.
 */
data class ProfileComponentParams(
    val componentContext: ComponentContext,
    val onLogout: () -> Unit,
    val onBackClicked: () -> Unit
)

/**
 * Interface for the authentication component, responsible for navigation between
 * login, registration, and profile screens, as well as managing the authentication state.
 */
interface AuthComponent {
    /**
     * Authentication state of the user.
     */
    val state: StateFlow<AuthStore.State>

    /**
     * Stack of child components for navigation.
     */
    val childStack: Value<ChildStack<*, Child>>

    /**
     * Sealed class representing authentication child components.
     */
    sealed class Child {
        class LoginChild(val component: LoginComponent) : Child()
        class RegisterChild(val component: RegisterComponent) : Child()
        class ProfileChild(val component: ProfileComponent) : Child()
    }

    // Navigation methods

    /**
     * Navigate to the login screen.
     */
    fun navigateToLogin()

    /**
     * Navigate to the registration screen.
     */
    fun navigateToRegister()

    /**
     * Navigate to the profile screen.
     */
    fun navigateToProfile()

    /**
     * Navigate back to the previous screen in the navigation stack.
     */
    fun navigateBack()

    /**
     * Handle back button click.
     */
    fun onBackClicked()
}

/**
 * Implementation of the authentication component, using Decompose for navigation
 * and MVIKotlin for state management.
 *
 * The component manages navigation between three screens:
 * - Login
 * - Registration
 * - User Profile
 *
 * Authentication state is stored in AuthStore and automatically restored
 * when the component is created.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAuthComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    private val storeFactory: StoreFactory,
    private val authUseCases: AuthUseCases
) : AuthComponent, DIAware, ComponentContext by componentContext {

    /**
     * Stack navigation for screens.
     */
    private val navigation = StackNavigation<Config>()

    /**
     * Coroutine scope bound to the component's lifecycle.
     * Automatically canceled when the component is destroyed.
     */
    private val scope = coroutineScope(rDispatchers.main)

    /**
     * Safe navigation handler that ensures navigation operations
     * are executed on the main thread.
     */
    private val navigationExecutor = NavigationExecutor(
        navigation = navigation,
        scope = scope,
        mainDispatcher = rDispatchers.main,
        logger = { message -> println("Auth Navigation: $message") }
    )

    /**
     * Authentication state store.
     */
    private val _store = instanceKeeper.getStore {
        AuthStoreFactory(
            storeFactory = storeFactory,
            di = di
        ).create()
    }

    /**
     * Stack of child components (Login, Register, Profile).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _childStack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialStack = {
            // Initial configuration - login screen
            listOf(Config.Login)
        },
        handleBackButton = true,
        childFactory = ::child
    )

    /**
     * Component initialization: authentication check and subscribing to state changes.
     */
    init {
        // Asynchronously check the current authentication state
        scope.launch {
            val isAuthenticated = withContext(rDispatchers.io) {
                authUseCases.isAuthenticated()
            }
            if (isAuthenticated) {
                _store.accept(AuthStore.Intent.SetAuthenticated(true))
            }
        }

        // Subscribe to authentication state changes for automatic navigation
        scope.launch {
            _store.stateFlow
                .map { it.isAuthenticated }
                .collect { isAuth ->
                    if (isAuth) {
                        navigationExecutor.navigateTo(Config.Profile)
                    }
                }
        }
    }

    override val childStack: Value<ChildStack<*, AuthComponent.Child>> = _childStack
    override val state: StateFlow<AuthStore.State> = _store.stateFlow

    override fun onBackClicked() {
        onBack()
    }

    override fun navigateToLogin() {
        navigationExecutor.navigateTo(Config.Login)
    }

    override fun navigateToRegister() {
        navigationExecutor.navigateTo(Config.Register)
    }

    override fun navigateToProfile() {
        navigationExecutor.navigateTo(Config.Profile)
    }

    override fun navigateBack() {
        navigationExecutor.pop()
    }

    /**
     * Create a child component based on the configuration.
     */
    private fun child(config: Config, componentContext: ComponentContext): AuthComponent.Child =
        when (config) {
            Config.Login -> AuthComponent.Child.LoginChild(loginComponent(componentContext))
            Config.Register -> AuthComponent.Child.RegisterChild(registerComponent(componentContext))
            Config.Profile -> AuthComponent.Child.ProfileChild(profileComponent(componentContext))
        }

    /**
     * Create a login component.
     */
    private fun loginComponent(componentContext: ComponentContext): LoginComponent {
        val loginComponentFactory by factory<LoginComponentParams, DefaultLoginComponent>()
        return loginComponentFactory(
            LoginComponentParams(
                componentContext = componentContext,
                onBack = onBack,
                onRegister = ::navigateToRegister,
                onLoginSuccess = {
                    _store.accept(AuthStore.Intent.SetAuthenticated(true))
                }
            )
        )
    }

    /**
     * Create a registration component.
     */
    private fun registerComponent(componentContext: ComponentContext): RegisterComponent {
        val registerComponentFactory by factory<RegisterComponentParams, DefaultRegisterComponent>()
        return registerComponentFactory(
            RegisterComponentParams(
                componentContext = componentContext,
                onBack = ::navigateBack,
                onLogin = ::navigateBack,
                onRegisterSuccess = {
                    _store.accept(AuthStore.Intent.SetAuthenticated(true))
                }
            )
        )
    }

    /**
     * Create a profile component.
     */
    private fun profileComponent(componentContext: ComponentContext): ProfileComponent {
        val profileComponentFactory by factory<ProfileComponentParams, DefaultProfileComponent>()
        return profileComponentFactory(
            ProfileComponentParams(
                componentContext = componentContext,
                onLogout = {
                    _store.accept(AuthStore.Intent.Logout)
                    navigationExecutor.navigateTo(Config.Login)
                },
                onBackClicked = onBack
            )
        )
    }

    /**
     * Configurations for navigating between authentication screens.
     */
    @Serializable
    private sealed interface Config {
        @Serializable
        data object Login : Config

        @Serializable
        data object Register : Config

        @Serializable
        data object Profile : Config
    }
}
