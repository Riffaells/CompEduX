package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.AuthComponent.Child.*
import component.app.auth.store.AuthStore
import component.app.auth.store.AuthStoreFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface AuthComponent {
    val childStack: Value<ChildStack<*, Child>>
    val state: StateFlow<AuthStore.State>

    fun onEvent(event: AuthStore.Intent)

    sealed class Child {
        class LoginChild(val component: LoginComponent) : Child()
        class RegisterChild(val component: RegisterComponent) : Child()
        class ProfileChild(val component: ProfileComponent) : Child()
    }
}

class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    override val di: DI
) : AuthComponent, DIAware, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    private val stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Login,
        handleBackButton = true,
        childFactory = ::child,
    )

    override val childStack: Value<ChildStack<*, AuthComponent.Child>> = stack

    private val authStoreFactory: AuthStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        authStoreFactory.create(
            onLoginSuccess = ::onLoginSuccess,
            onRegisterSuccess = ::onRegisterSuccess,
            onLogout = ::onLogout
        )
    }

    override val state: StateFlow<AuthStore.State> = store.stateFlow

    override fun onEvent(event: AuthStore.Intent) {
        store.accept(event)
    }

    private fun child(config: Config, componentContext: ComponentContext): AuthComponent.Child =
        when (config) {
            Config.Login -> LoginChild(loginComponent(componentContext))
            Config.Register -> RegisterChild(registerComponent(componentContext))
            Config.Profile -> ProfileChild(profileComponent(componentContext))
        }

    private fun loginComponent(componentContext: ComponentContext): LoginComponent =
        DefaultLoginComponent(
            componentContext = componentContext,
            di = di,
            onLoginSuccess = { store.accept(AuthStore.Intent.Login) },
            onNavigateToRegister = { navigation.bringToFront(Config.Register) },
            onBack = onBack
        )

    private fun registerComponent(componentContext: ComponentContext): RegisterComponent =
        DefaultRegisterComponent(
            componentContext = componentContext,
            di = di,
            onRegisterSuccess = { store.accept(AuthStore.Intent.Register) },
            onNavigateToLogin = { navigation.bringToFront(Config.Login) },
            onBack = onBack
        )

    private fun profileComponent(componentContext: ComponentContext): ProfileComponent =
        DefaultProfileComponent(
            componentContext = componentContext,
            di = di,
            onLogout = { store.accept(AuthStore.Intent.Logout) },
            onBack = onBack
        )

    private fun onLoginSuccess() {
        navigation.bringToFront(Config.Profile)
    }

    private fun onRegisterSuccess() {
        navigation.bringToFront(Config.Profile)
    }

    private fun onLogout() {
        navigation.bringToFront(Config.Login)
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
