package component.app.room.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.room.auth.store.LoginStore
import component.app.room.auth.store.LoginStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface LoginComponent {
    val state: StateFlow<LoginStore.State>

    fun onAction(action: LoginStore.Intent)
    fun onRegisterClicked()
    fun onBackClicked()
}

class DefaultLoginComponent(
    componentContext: ComponentContext,
    private val onRegister: () -> Unit,
    private val onLoginSuccess: () -> Unit,
    private val onBack: () -> Unit,
    override val di: DI
) : LoginComponent, DIAware, ComponentContext by componentContext {

    private val loginStoreFactory: LoginStoreFactory by instance()

    private val store = instanceKeeper.getStore {
        loginStoreFactory.create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<LoginStore.State> = store.stateFlow

    override fun onAction(action: LoginStore.Intent) {
        when (action) {
            is LoginStore.Intent.Login -> {
                store.accept(action)
                // В реальном приложении здесь была бы проверка успешности входа
                if (state.value.username.isNotEmpty() && state.value.password.isNotEmpty()) {
                    onLoginSuccess()
                }
            }
            else -> store.accept(action)
        }
    }

    override fun onRegisterClicked() {
        onRegister()
    }

    override fun onBackClicked() {
        onBack()
    }
}
