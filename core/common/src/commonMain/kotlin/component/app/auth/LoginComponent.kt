package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.app.auth.store.LoginStore
import component.app.auth.store.LoginStoreFactory
import kotlinx.coroutines.flow.StateFlow

/**
 * Компонент для экрана входа
 */
interface LoginComponent {
    val state: StateFlow<LoginStore.State>

    /**
     * Обработка нажатия кнопки входа
     * @param email Email пользователя
     * @param password Пароль пользователя
     */
    fun onLoginClicked(email: String, password: String)

    /**
     * Переход на экран регистрации
     */
    fun onRegisterClicked()

    /**
     * Обработка нажатия кнопки "Назад"
     */
    fun onBackClicked()
}

/**
 * Реализация компонента входа
 */
class DefaultLoginComponent(
    componentContext: ComponentContext,
    storeFactory: DefaultStoreFactory = DefaultStoreFactory(),
    private val onLogin: (String, String) -> Unit,
    private val onRegister: () -> Unit,
    private val onBack: () -> Unit,
) : LoginComponent, ComponentContext by componentContext {

    private val store =
        instanceKeeper.getStore {
            LoginStoreFactory(
                storeFactory = storeFactory,
            ).create()
        }


    override val state: StateFlow<LoginStore.State> = store?.stateFlow ?: kotlinx.coroutines.flow.MutableStateFlow(LoginStore.State())

    override fun onLoginClicked(email: String, password: String) {
        store?.accept(LoginStore.Intent.Login(email, password)) ?: onLogin(email, password)
    }

    override fun onRegisterClicked() {
        onRegister()
    }

    override fun onBackClicked() {
        onBack()
    }
}
