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
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import usecase.auth.AuthUseCases
import utils.rDispatchers

interface RegisterComponent {
    val state: StateFlow<RegisterStore.State>

    fun onRegisterClick(email: String, password: String, confirmPassword: String, username: String)
    fun onBackClick()
    fun onLoginClick()
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

    override fun onRegisterClick(email: String, password: String, confirmPassword: String, username: String) {
        registerStore.accept(RegisterStore.Intent.HandleRegisterClick(
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            username = username
        ))

        // Monitor auth state changes to trigger navigation on success
        scope.launch {
            // This is simplified - in a real implementation, you would monitor
            // the AuthState from a shared AuthStore or Repository to detect successful registration
            // For now, we'll assume the RegisterStore handles everything
            // You would replace this with actual auth state monitoring
            if (authUseCases.isAuthenticated()) {
                onRegisterSuccess()
            }
        }
    }

    override fun onBackClick() {
        onBack()
    }

    override fun onLoginClick() {
        onLogin()
    }
}
