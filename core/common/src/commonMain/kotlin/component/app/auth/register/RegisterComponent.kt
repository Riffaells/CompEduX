package component.app.auth.register

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.store.RegisterStore
import component.app.auth.store.RegisterStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.auth.AuthUseCases
import navigation.rDispatchers

interface RegisterComponent {
    val state: StateFlow<RegisterStore.State>

    fun accept(intent: RegisterStore.Intent)
    fun onBackClick()
}

class DefaultRegisterComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    private val onLogin: () -> Unit,
    private val onRegisterSuccess: () -> Unit
) : RegisterComponent, DIAware, ComponentContext by componentContext {

    private val authUseCases: AuthUseCases by instance()
    private val storeFactory: StoreFactory by instance()

    private val scope = CoroutineScope(rDispatchers.main + SupervisorJob())

    private val registerStore = instanceKeeper.getStore {
        RegisterStoreFactory(
            storeFactory = storeFactory,
            di = di
        ).create()
    }

    override val state: StateFlow<RegisterStore.State> = registerStore.stateFlow

    init {
        // Мониторинг изменений состояния
        scope.launch {
            state.collectLatest { state ->
                // Проверяем состояние аутентификации
                if (state.isAuthenticated) {
                    onRegisterSuccess()
                }

                // Проверяем необходимость перехода к экрану входа
                if (state.shouldNavigateToLogin) {
                    onLogin()
                    registerStore.accept(RegisterStore.Intent.ErrorShown) // Сбрасываем флаг
                }
            }
        }
    }

    override fun accept(intent: RegisterStore.Intent) {
        registerStore.accept(intent)

        // Обрабатываем дополнительные действия в зависимости от типа Intent
        when (intent) {
            is RegisterStore.Intent.RegisterClicked -> {
                // Проверяем успешность регистрации через Store состояние
                // (мониторинг уже настроен в init блоке)
            }

            is RegisterStore.Intent.NavigateToLogin -> {
                onLogin()
            }

            else -> { /* Для других интентов никаких действий не требуется */ }
        }
    }

    override fun onBackClick() {
        onBack()
    }
}
