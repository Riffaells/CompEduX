package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.app.auth.store.AuthStore
import component.app.auth.store.AuthStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import utils.rDispatchers

/**
 * Параметры для создания компонента аутентификации
 */
data class AuthComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)

interface AuthComponent {
    val state: StateFlow<AuthStore.State>
    val childStack: Value<ChildStack<*, Child>>

    data class State(
        val isLoading: Boolean = false
    )

    sealed class Child {
        class LoginChild(val component: LoginComponent) : Child()
        class RegisterChild(val component: RegisterComponent) : Child()
        class ProfileChild(val component: ProfileComponent) : Child()
    }

    fun onEvent(event: AuthStore.Intent)
    fun onBackClicked()
}

@OptIn(ExperimentalDecomposeApi::class)
class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    storeFactory: DefaultStoreFactory = DefaultStoreFactory(),
) : AuthComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    private val stack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialStack = { listOf(Config.Login) },
        handleBackButton = true,
        childFactory = ::child
    )

    override val childStack: Value<ChildStack<*, AuthComponent.Child>> = stack

    private val store =
        instanceKeeper.getStore {
            AuthStoreFactory(
                storeFactory = storeFactory,
            ).create()
        }


    override val state: StateFlow<AuthStore.State> = store.stateFlow

    init {
        // Подписываемся на изменения в authStore для обнаружения успешной аутентификации
        CoroutineScope(rDispatchers.main).launch {
            store.states.collectLatest { authState ->
                // Если пользователь аутентифицирован и не на экране профиля - переходим на экран профиля
                if (authState.isAuthenticated && stack.value.active.instance !is AuthComponent.Child.ProfileChild) {
                    navigation.replaceAll(Config.Profile)
                }

                // Если пользователь вышел и находился на экране профиля - переходим на экран логина
                if (!authState.isAuthenticated && stack.value.active.instance is AuthComponent.Child.ProfileChild) {
                    navigation.replaceAll(Config.Login)
                }
            }
        }
    }

    override fun onEvent(event: AuthStore.Intent) {
        store.accept(event)
    }

    override fun onBackClicked() {
        onBack()
    }

    private fun child(config: Config, componentContext: ComponentContext): AuthComponent.Child =
        when (config) {
            Config.Login -> AuthComponent.Child.LoginChild(loginComponent(componentContext))
            Config.Register -> AuthComponent.Child.RegisterChild(registerComponent(componentContext))
            Config.Profile -> AuthComponent.Child.ProfileChild(profileComponent(componentContext))
        }

    private fun loginComponent(componentContext: ComponentContext): LoginComponent =
        DefaultLoginComponent(
            componentContext = componentContext,
            onLogin = { email, password ->
                store.accept(AuthStore.Intent.Login(email, password))
            },
            onRegister = {
                navigation.push(Config.Register)
            },
            onBack = onBack
        )

    private fun registerComponent(componentContext: ComponentContext): RegisterComponent =
        DefaultRegisterComponent(
            componentContext = componentContext,
            onRegister = { email, password, username ->
                store.accept(AuthStore.Intent.Register(email, password, username))
            },
            onBack = navigation::pop
        )

    private fun profileComponent(componentContext: ComponentContext): ProfileComponent =
        DefaultProfileComponent(
            componentContext = componentContext,
            onLogoutClicked = {
                store.accept(AuthStore.Intent.Logout)
            },
            onUpdateProfileClicked = { username ->
                store.accept(AuthStore.Intent.UpdateProfile(username))
            },
            onBackClicked = onBack
        )

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
