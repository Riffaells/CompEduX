package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.login.DefaultLoginComponent
import component.app.auth.login.LoginComponent
import component.app.auth.register.DefaultRegisterComponent
import component.app.auth.register.RegisterComponent
import component.app.auth.store.AuthStore
import component.app.auth.store.AuthStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import model.User
import org.kodein.di.*
import repository.auth.AuthRepository
import utils.rDispatchers

/**
 * Параметры для создания компонента аутентификации
 */
data class AuthComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)

/**
 * Параметры для создания компонента входа в систему
 */
data class LoginComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit,
    val onRegister: () -> Unit,
    val onLoginSuccess: () -> Unit
)

/**
 * Параметры для создания компонента регистрации
 */
data class RegisterComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit,
    val onLogin: () -> Unit,
    val onRegisterSuccess: () -> Unit
)

/**
 * Параметры для создания компонента профиля
 */
data class ProfileComponentParams(
    val componentContext: ComponentContext,
    val onLogout: () -> Unit,
    val onBackClicked: () -> Unit
)

interface AuthComponent {
    val state: StateFlow<AuthStore.State>
    val childStack: Value<ChildStack<*, Child>>

    sealed class Child {
        class LoginChild(val component: LoginComponent) : Child()
        class RegisterChild(val component: RegisterComponent) : Child()
        class ProfileChild(val component: ProfileComponent) : Child()
    }

    fun onBackClicked()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultAuthComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    private val storeFactory: StoreFactory,
    private val authRepository: AuthRepository
) : AuthComponent, DIAware, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    private val _store = instanceKeeper.getStore {
        AuthStoreFactory(
            storeFactory = storeFactory,
            authRepository = authRepository
        ).create()
    }

    private val scope = CoroutineScope(rDispatchers.main)

    private val _childStack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialStack = {
            // Проверяем авторизован ли пользователь
            var initialConfiguration = Config.Login

            // If user is already authenticated, go to profile
            scope.launch {
                if (authRepository.isAuthenticated()) {
                    navigation.bringToFront(Config.Profile)
                }
            }

            // Monitor auth state changes
            scope.launch {
                _store.stateFlow.map { it.isAuthenticated }.collect { isAuth ->
                    if (isAuth) {
                        navigation.bringToFront(Config.Profile)
                    }
                }
            }
            listOf(initialConfiguration)
        },
        handleBackButton = true,
        childFactory = ::child
    )

    override val childStack: Value<ChildStack<*, AuthComponent.Child>> = _childStack

    override val state: StateFlow<AuthStore.State> = _store.stateFlow

    override fun onBackClicked() {
        onBack()
    }

    private fun child(config: Config, componentContext: ComponentContext): AuthComponent.Child =
        when (config) {
            Config.Login -> AuthComponent.Child.LoginChild(loginComponent(componentContext))
            Config.Register -> AuthComponent.Child.RegisterChild(registerComponent(componentContext))
            Config.Profile -> AuthComponent.Child.ProfileChild(profileComponent(componentContext))
        }

    private fun loginComponent(componentContext: ComponentContext): LoginComponent {
        val loginComponentFactory by factory<LoginComponentParams, DefaultLoginComponent>()
        return loginComponentFactory(
            LoginComponentParams(
                componentContext = componentContext,
                onBack = onBack,
                onRegister = {
                    navigation.push(Config.Register)
                },
                onLoginSuccess = {
                    navigation.bringToFront(Config.Profile)
                }
            )
        )
    }

    private fun registerComponent(componentContext: ComponentContext): RegisterComponent {
        val registerComponentFactory by factory<RegisterComponentParams, DefaultRegisterComponent>()
        return registerComponentFactory(
            RegisterComponentParams(
                componentContext = componentContext,
                onBack = {
                    navigation.pop()
                },
                onLogin = {
                    navigation.pop()
                },
                onRegisterSuccess = {
                    navigation.bringToFront(Config.Profile)
                }
            )
        )
    }

    private fun profileComponent(componentContext: ComponentContext): ProfileComponent {
        val profileComponentFactory by factory<ProfileComponentParams, DefaultProfileComponent>()
        return profileComponentFactory(
            ProfileComponentParams(
                componentContext = componentContext,
                onLogout = {
                    // Handle logout in the auth store
                    _store.accept(AuthStore.Intent.Logout)
                    navigation.bringToFront(Config.Login)
                },
                onBackClicked = onBack
            )
        )
    }

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
