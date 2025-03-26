package component.app.auth.login

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.store.AuthStore
import component.app.auth.store.LoginStore
import component.app.auth.store.LoginStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import repository.auth.AuthRepository
import utils.rDispatchers

interface LoginComponent {
    val state: StateFlow<LoginStore.State>

    fun onLoginClick(email: String, password: String)
    fun onBackClick()
    fun onRegisterClick()
}

class DefaultLoginComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    private val onRegister: () -> Unit,
    private val onLoginSuccess: () -> Unit
) : LoginComponent, DIAware, ComponentContext by componentContext {

    private val authRepository: AuthRepository by instance()
    private val storeFactory: StoreFactory by instance()

    private val scope = CoroutineScope(rDispatchers.main + SupervisorJob())

    private val loginStore = instanceKeeper.getStore {
        LoginStoreFactory(
            storeFactory = storeFactory,
            authRepository = authRepository
        ).create()
    }

    override val state: StateFlow<LoginStore.State> = loginStore.stateFlow

    override fun onLoginClick(email: String, password: String) {
        loginStore.accept(LoginStore.Intent.HandleLoginClick(email, password))
        // Monitor auth state changes to trigger navigation on success
        scope.launch {
            // This is simplified - in a real implementation, you would monitor
            // the AuthState from a shared AuthStore or Repository to detect successful login
            // For now, we'll assume the LoginStore handles everything
            // You would replace this with actual auth state monitoring
            if (authRepository.isAuthenticated()) {
                onLoginSuccess()
            }
        }
    }

    override fun onBackClick() {
        onBack()
    }

    override fun onRegisterClick() {
        onRegister()
    }
}
