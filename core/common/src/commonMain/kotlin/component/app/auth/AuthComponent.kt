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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import model.User
import repository.auth.AuthRepository
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
    val store: AuthStore

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

    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String, username: String)
    suspend fun logout()
    suspend fun isAuthenticated(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun updateProfile(username: String)
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

    override val store: AuthStore = _store

    override fun onEvent(event: AuthStore.Intent) {
        _store.accept(event)
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
            di = di,
            componentContext = componentContext,
            onBack = onBack,
            onRegister = {
                navigation.push(Config.Register)
            },
            authComponent = this
        )

    private fun registerComponent(componentContext: ComponentContext): RegisterComponent =
        DefaultRegisterComponent(
            di = di,
            componentContext = componentContext,
            onBack = {
                navigation.pop()
            },
            onLogin = {
                navigation.pop()
            },
            authComponent = this
        )

    private fun profileComponent(componentContext: ComponentContext): ProfileComponent =
        DefaultProfileComponent(
            componentContext = componentContext,
            storeFactory = storeFactory,
            onLogout = {
                _store.accept(AuthStore.Intent.Logout)
                navigation.bringToFront(Config.Login)
            },
            onUpdateProfile = { username ->
                _store.accept(AuthStore.Intent.UpdateProfile(username))
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

    override suspend fun login(email: String, password: String) {
        _store.accept(AuthStore.Intent.Login(email, password))
    }

    override suspend fun register(email: String, password: String, username: String) {
        _store.accept(AuthStore.Intent.Register(email, password, username))
    }

    override suspend fun logout() {
        _store.accept(AuthStore.Intent.Logout)
    }

    override suspend fun isAuthenticated(): Boolean {
        return authRepository.isAuthenticated()
    }

    override suspend fun getCurrentUser(): User? {
        return authRepository.getCurrentUser()
    }

    override suspend fun updateProfile(username: String) {
        _store.accept(AuthStore.Intent.UpdateProfile(username))
    }
}
