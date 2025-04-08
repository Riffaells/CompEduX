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

    // Методы навигации
    fun navigateToLogin()
    fun navigateToRegister()
    fun navigateToProfile()
    fun navigateBack()
    fun onBackClicked()
}

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAuthComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    private val storeFactory: StoreFactory,
    private val authUseCases: AuthUseCases
) : AuthComponent, DIAware, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    // Создаем scope, связанный с жизненным циклом компонента
    // Использование coroutineScope из Essenty гарантирует автоматическую отмену
    // корутин при уничтожении компонента
    private val scope = coroutineScope(rDispatchers.main)

    // Создаем навигационный executor, используя scope, связанный с жизненным циклом
    private val navigationExecutor = NavigationExecutor(
        navigation = navigation,
        scope = scope,
        mainDispatcher = rDispatchers.main,
        logger = { message -> println("Navigation: $message") }
    )

    private val _store = instanceKeeper.getStore {
        AuthStoreFactory(
            storeFactory = storeFactory,
            di = di
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _childStack = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialStack = {
            // Проверяем авторизован ли пользователь
            var initialConfiguration = Config.Login

            // Возвращаем начальный стек навигации
            listOf(initialConfiguration)
        },
        handleBackButton = true,
        childFactory = ::child
    )

    // Подписываемся на изменения состояния в init блоке
    init {
        // Асинхронно проверяем авторизацию при создании компонента
        scope.launch {
            val isAuthenticated = withContext(rDispatchers.io) {
                authUseCases.isAuthenticated()
            }
            if (isAuthenticated) {
                // Устанавливаем флаг авторизации
                _store.accept(AuthStore.Intent.SetAuthenticated(true))
            }
        }

        // Подписка на изменение состояния isAuthenticated
        // Эта подписка будет работать все время жизни компонента
        scope.launch {
            _store.stateFlow
                .map { it.isAuthenticated }
                .collect { isAuth ->
                    if (isAuth) {
                        // Вызываем навигацию в главном потоке
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
        navigationExecutor.push(Config.Register)
    }

    override fun navigateToProfile() {
        navigationExecutor.navigateTo(Config.Profile)
    }

    override fun navigateBack() {
        navigationExecutor.pop()
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
                onRegister = ::navigateToRegister,
                onLoginSuccess = {
                    // Вместо вызова навигации, обновляем состояние
                    _store.accept(AuthStore.Intent.SetAuthenticated(true))
                }
            )
        )
    }

    private fun registerComponent(componentContext: ComponentContext): RegisterComponent {
        val registerComponentFactory by factory<RegisterComponentParams, DefaultRegisterComponent>()
        return registerComponentFactory(
            RegisterComponentParams(
                componentContext = componentContext,
                onBack = ::navigateBack,
                onLogin = ::navigateBack,
                onRegisterSuccess = {
                    // Вместо вызова навигации, обновляем состояние
                    _store.accept(AuthStore.Intent.SetAuthenticated(true))
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
                    // Вместо вызова навигации через MainThreadWorker, используем navigationExecutor
                    navigationExecutor.navigateTo(Config.Login)
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
