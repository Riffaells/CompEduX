package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.store.LoginStore
import component.app.auth.store.LoginStoreFactory
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface LoginComponent {
    val state: StateFlow<LoginStore.State>
    fun onEvent(event: LoginStore.Intent)
}

class DefaultLoginComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onLoginSuccess: () -> Unit,
    private val onNavigateToRegister: () -> Unit,
    private val onBack: () -> Unit
) : LoginComponent, DIAware, ComponentContext by componentContext {

    private val loginStoreFactory: LoginStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        loginStoreFactory.create(
            onLoginSuccess = onLoginSuccess,
            onNavigateToRegister = onNavigateToRegister,
            onBack = onBack
        )
    }

    override val state: StateFlow<LoginStore.State> = store.stateFlow

    override fun onEvent(event: LoginStore.Intent) {
        store.accept(event)
    }
}
