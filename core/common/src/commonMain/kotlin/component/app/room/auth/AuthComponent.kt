package component.app.room.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.room.auth.AuthComponent.Child.*
import component.app.room.auth.store.AuthStore
import component.app.room.auth.store.AuthStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface AuthComponent {
    val childStack: Value<ChildStack<*, Child>>
    val state: StateFlow<AuthStore.State>

    fun onEvent(event: AuthStore.Intent)
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
        serializer = Config.serializer(),
        initialConfiguration = Config.Login,
        handleBackButton = true,
        childFactory = ::child,
    )

    override val childStack: Value<ChildStack<*, AuthComponent.Child>> = stack

    private val authStoreFactory: AuthStoreFactory by instance()

    private val store =
        instanceKeeper.getStore {
            authStoreFactory.create()
        }

    init {
        // Инициализируем состояние аутентификации
        store.accept(AuthStore.Intent.Init)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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

    private fun loginComponent(componentContext: ComponentContext): LoginComponent {
        return DefaultLoginComponent(
            componentContext = componentContext,
            onRegister = ::onRegisterClicked,
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
        store.accept(AuthStore.Intent.Logout)
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
