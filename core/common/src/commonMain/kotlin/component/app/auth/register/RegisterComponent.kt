package component.app.auth.register

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

interface RegisterComponent {
    val state: StateFlow<AuthStore.State>

    fun onRegisterClick(email: String, password: String, username: String)
    fun onBackClick()
    fun onLoginClick()
}

class DefaultRegisterComponent(
    override val di: DI,
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    private val onLogin: () -> Unit,
    private val authComponent: AuthComponent
) : RegisterComponent, DIAware, ComponentContext by componentContext {

    private val scope = CoroutineScope(rDispatchers.main + SupervisorJob())

    override val state: StateFlow<AuthStore.State> = authComponent.store.stateFlow

    override fun onRegisterClick(email: String, password: String, username: String) {
        scope.launch {
            authComponent.register(email, password, username)
        }
    }

    override fun onBackClick() {
        onBack()
    }

    override fun onLoginClick() {
        onLogin()
    }
}
