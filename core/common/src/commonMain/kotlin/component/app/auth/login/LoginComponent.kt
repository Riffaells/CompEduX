package component.app.auth.login

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.AuthComponent
import component.app.auth.store.AuthStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import utils.rDispatchers

interface LoginComponent {
    val state: StateFlow<AuthStore.State>

    fun onLoginClick(email: String, password: String)
    fun onBackClick()
    fun onRegisterClick()
}

class DefaultLoginComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    private val onRegister: () -> Unit,
    private val authComponent: AuthComponent
) : LoginComponent, DIAware, ComponentContext by componentContext {

    private val scope = CoroutineScope(rDispatchers.main + SupervisorJob())

    override val state: StateFlow<AuthStore.State> = authComponent.store.stateFlow

    override fun onLoginClick(email: String, password: String) {
        scope.launch {
            authComponent.login(email, password)
        }
    }

    override fun onBackClick() {
        onBack()
    }

    override fun onRegisterClick() {
        onRegister()
    }
}
