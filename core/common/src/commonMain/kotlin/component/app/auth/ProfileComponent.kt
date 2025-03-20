package component.app.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.app.auth.store.ProfileStore
import component.app.auth.store.ProfileStoreFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Компонент для экрана профиля
 */
interface ProfileComponent {
    val state: StateFlow<ProfileStore.State>

    /**
     * Обработка нажатия кнопки выхода
     */
    fun onLogoutClicked()

    /**
     * Обработка обновления профиля
     * @param username Новое имя пользователя
     */
    fun onUpdateProfileClicked(username: String)

    /**
     * Обработка нажатия кнопки "Назад"
     */
    fun onBackClicked()
}

/**
 * Реализация компонента профиля
 */
class DefaultProfileComponent(
    componentContext: ComponentContext,
    storeFactory: DefaultStoreFactory = DefaultStoreFactory(),
    private val onLogoutClicked: () -> Unit,
    private val onUpdateProfileClicked: (String) -> Unit,
    private val onBackClicked: () -> Unit,
) : ProfileComponent, ComponentContext by componentContext {

    private val store =
        instanceKeeper.getStore {
            ProfileStoreFactory(
                storeFactory = storeFactory,
            ).create()
        }


    override val state: StateFlow<ProfileStore.State> = store.stateFlow

    override fun onLogoutClicked() {
        store?.accept(ProfileStore.Intent.Logout)
    }

    override fun onUpdateProfileClicked(username: String) {
        store?.accept(ProfileStore.Intent.SaveProfile(username)) ?: onUpdateProfileClicked(username)
    }

    override fun onBackClicked() {
        onBackClicked()
    }
}
