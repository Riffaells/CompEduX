package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.app.auth.store.RegisterStore
import component.app.auth.store.RegisterStoreFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Компонент для экрана регистрации
 */
interface RegisterComponent {
    val state: StateFlow<RegisterStore.State>

    /**
     * Обработка нажатия кнопки регистрации
     * @param email Email пользователя
     * @param password Пароль пользователя
     * @param username Имя пользователя
     */
    fun onRegisterClicked(email: String, password: String, username: String)

    /**
     * Обработка нажатия кнопки "Назад"
     */
    fun onBackClicked()
}

/**
 * Реализация компонента регистрации
 */
class DefaultRegisterComponent(
    componentContext: ComponentContext,
    private val onRegister: (String, String, String) -> Unit,
    private val onBack: () -> Unit,
    storeFactory: DefaultStoreFactory = DefaultStoreFactory(),
) : RegisterComponent, ComponentContext by componentContext {

    private val store =
        instanceKeeper.getStore {
            RegisterStoreFactory(
                storeFactory = storeFactory,
            ).create()
        }


    override val state: StateFlow<RegisterStore.State> = store.stateFlow

    override fun onRegisterClicked(email: String, password: String, username: String) {
        store?.accept(RegisterStore.Intent.Register(email, password, username))
    }

    override fun onBackClicked() {
        onBack()
    }
}
