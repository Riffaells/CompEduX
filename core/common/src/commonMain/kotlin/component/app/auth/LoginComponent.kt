package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.store.LoginStore
import component.app.auth.store.LoginStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface LoginComponent {
    val state: StateFlow<LoginStore.State>

    fun onUsernameChanged(username: String)
    fun onEmailChanged(email: String)
    fun onPasswordChanged(password: String)
    fun onLoginClicked()
    fun onRegisterClicked()
    fun onBackClicked()
    fun onToggleLoginMethod()
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

    override fun onUsernameChanged(username: String) {
        store.accept(LoginStore.Intent.UpdateUsername(username))
    }

    override fun onEmailChanged(email: String) {
        store.accept(LoginStore.Intent.UpdateEmail(email))
    }

    override fun onPasswordChanged(password: String) {
        store.accept(LoginStore.Intent.UpdatePassword(password))
    }

    override fun onLoginClicked() {
        store.accept(LoginStore.Intent.Login)
        // В реальном приложении здесь нужно проверить успешность входа
        // и только потом вызывать onLoginSuccess
        // Для примера просто вызываем onLoginSuccess
        onLoginSuccess()
    }

    override fun onRegisterClicked() {
        onRegister()
    }

    override fun onBackClicked() {
        onBack()
    }

    override fun onToggleLoginMethod() {
        store.accept(LoginStore.Intent.ToggleLoginMethod)
    }
}
