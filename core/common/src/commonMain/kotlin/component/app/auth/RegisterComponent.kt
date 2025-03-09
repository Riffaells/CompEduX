package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import component.app.auth.store.RegisterStore
import component.app.auth.store.RegisterStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

interface RegisterComponent {
    val state: StateFlow<RegisterStore.State>

    fun onNameChanged(name: String)
    fun onEmailChanged(email: String)
    fun onPasswordChanged(password: String)
    fun onConfirmPasswordChanged(confirmPassword: String)
    fun onRegisterClicked()
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

    override fun onNameChanged(name: String) {
        store.accept(RegisterStore.Intent.UpdateName(name))
    }

    override fun onEmailChanged(email: String) {
        store.accept(RegisterStore.Intent.UpdateEmail(email))
    }

    override fun onPasswordChanged(password: String) {
        store.accept(RegisterStore.Intent.UpdatePassword(password))
    }

    override fun onConfirmPasswordChanged(confirmPassword: String) {
        store.accept(RegisterStore.Intent.UpdateConfirmPassword(confirmPassword))
    }

    override fun onRegisterClicked() {
        store.accept(RegisterStore.Intent.Register)
        // В реальном приложении здесь нужно проверить успешность регистрации
        // и только потом вызывать onRegisterSuccess
        // Для примера просто вызываем onRegisterSuccess
        onRegisterSuccess()
    }

    override fun onLoginClicked() {
        onLoginClicked()
    }

    override fun onBackClicked() {
        onBack()
    }
}
