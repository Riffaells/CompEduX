package component.app.room.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.room.auth.store.RegisterStore
import component.app.room.auth.store.RegisterStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface RegisterComponent {
    val state: StateFlow<RegisterStore.State>

    fun onAction(action: RegisterStore.Intent)
    fun onLoginClicked()
    fun onBackClicked()
}

class DefaultRegisterComponent(
    componentContext: ComponentContext,
    private val onLoginClicked: () -> Unit,
    private val onRegisterSuccess: () -> Unit,
    private val onBack: () -> Unit,
    override val di: DI
) : RegisterComponent, DIAware, ComponentContext by componentContext {

    private val registerStoreFactory: RegisterStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        registerStoreFactory.create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<RegisterStore.State> = store.stateFlow

    override fun onAction(action: RegisterStore.Intent) {
        when (action) {
            is RegisterStore.Intent.Register -> {
                store.accept(action)
                // В реальном приложении здесь была бы проверка успешности регистрации
                val state = state.value
                if (state.username.isNotEmpty() && state.password.isNotEmpty() && state.email.isNotEmpty()) {
                    onRegisterSuccess()
                }
            }
            else -> store.accept(action)
        }
    }

    override fun onLoginClicked() {
        onLoginClicked()
    }

    override fun onBackClicked() {
        onBack()
    }
}
