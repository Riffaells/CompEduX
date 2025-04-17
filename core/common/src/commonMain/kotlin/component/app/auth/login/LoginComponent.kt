package component.app.auth.login

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.store.LoginStore
import component.app.auth.store.LoginStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.auth.AuthUseCases
import utils.rDispatchers

interface LoginComponent {
    val state: StateFlow<LoginStore.State>

    fun accept(intent: LoginStore.Intent)
    fun onBackClick()
}

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultLoginComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    private val onRegister: () -> Unit,
    private val onLoginSuccess: () -> Unit
) : LoginComponent, DIAware, ComponentContext by componentContext {

    private val authUseCases: AuthUseCases by instance()
    private val storeFactory: StoreFactory by instance()

    private val scope = CoroutineScope(rDispatchers.main + SupervisorJob())

    private val loginStore = instanceKeeper.getStore {
        LoginStoreFactory(
            storeFactory = storeFactory,
            di = di
        ).create()
    }

    override val state: StateFlow<LoginStore.State> = loginStore.stateFlow

    init {
        // Мониторинг изменений состояния для навигации
        scope.launch {
            // Здесь можно отслеживать состояние из state и вызывать соответствующие колбэки
            // Например, если пользователь аутентифицирован или нужно перейти на другой экран
        }
    }

    override fun accept(intent: LoginStore.Intent) {
        loginStore.accept(intent)

        // Обрабатываем дополнительные действия в зависимости от типа Intent
        when (intent) {
            is LoginStore.Intent.Login -> {
                // Проверяем успешность входа
                scope.launch {
                    if (authUseCases.isAuthenticated()) {
                        onLoginSuccess()
                    }
                }
            }

            is LoginStore.Intent.NavigateToRegister -> {
                onRegister()
            }

            else -> { /* Для других интентов никаких действий не требуется */ }
        }
    }

    override fun onBackClick() {
        onBack()
    }
}
