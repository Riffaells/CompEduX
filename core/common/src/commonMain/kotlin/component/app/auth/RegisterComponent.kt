package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.store.RegisterStore
import component.app.auth.store.RegisterStoreFactory
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface RegisterComponent {
    val state: StateFlow<RegisterStore.State>
    fun onEvent(event: RegisterStore.Intent)
}

class DefaultRegisterComponent(
    componentContext: ComponentContext,
    override val di: DI,
    private val onRegisterSuccess: () -> Unit,
    private val onNavigateToLogin: () -> Unit,
    private val onBack: () -> Unit
) : RegisterComponent, DIAware, ComponentContext by componentContext {

    private val registerStoreFactory: RegisterStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        registerStoreFactory.create(
            onRegisterSuccess = onRegisterSuccess,
            onNavigateToLogin = onNavigateToLogin,
            onBack = onBack
        )
    }

    override val state: StateFlow<RegisterStore.State> = store.stateFlow

    override fun onEvent(event: RegisterStore.Intent) {
        store.accept(event)
    }
}
