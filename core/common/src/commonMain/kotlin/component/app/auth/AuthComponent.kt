package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import component.app.auth.AuthComponent.Child.*
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware

interface AuthComponent {
    val childStack: Value<ChildStack<*, Child>>

    fun onLoginClicked()
    fun onRegisterClicked()
    fun onProfileClicked()
    fun onBackToRoot()

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
        initialConfiguration = Config.Login,
        handleBackButton = true,
        childFactory = ::child,
    )

    override val childStack: Value<ChildStack<*, AuthComponent.Child>> = stack

    private fun child(config: Config, componentContext: ComponentContext): AuthComponent.Child =
        when (config) {
            Config.Login -> LoginChild(loginComponent(componentContext))
            Config.Register -> RegisterChild(registerComponent(componentContext))
            Config.Profile -> ProfileChild(profileComponent(componentContext))
        }

    private fun loginComponent(componentContext: ComponentContext): LoginComponent {
        return DefaultLoginComponent(
            componentContext = componentContext,
            onRegisterClicked = ::onRegisterClicked,
            onLoginSuccess = ::onLoginSuccess,
            onBack = ::onBackToRoot,
            di = di
        )
    }

    private fun registerComponent(componentContext: ComponentContext): RegisterComponent {
        return DefaultRegisterComponent(
            componentContext = componentContext,
            onLoginClicked = ::onLoginClicked,
            onRegisterSuccess = ::onRegisterSuccess,
            onBack = ::onBackToRoot,
            di = di
        )
    }

    private fun profileComponent(componentContext: ComponentContext): ProfileComponent {
        return DefaultProfileComponent(
            componentContext = componentContext,
            onLogout = ::onLogout,
            onBack = ::onBackToRoot,
            di = di
        )
    }

    override fun onLoginClicked() {
        navigation.bringToFront(Config.Login)
    }

    override fun onRegisterClicked() {
        navigation.bringToFront(Config.Register)
    }

    override fun onProfileClicked() {
        navigation.bringToFront(Config.Profile)
    }

    override fun onBackToRoot() {
        onBack()
    }

    private fun onLoginSuccess() {
        // После успешного входа переходим на экран профиля
        navigation.bringToFront(Config.Profile)
    }

    private fun onRegisterSuccess() {
        // После успешной регистрации переходим на экран профиля
        navigation.bringToFront(Config.Profile)
    }

    private fun onLogout() {
        // После выхода возвращаемся на экран входа
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
