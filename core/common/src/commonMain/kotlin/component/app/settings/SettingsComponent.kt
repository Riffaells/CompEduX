package component.app.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import component.app.settings.store.SettingsStore
import component.app.settings.store.SettingsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import settings.MultiplatformSettings

interface SettingsComponent {
    val state: StateFlow<SettingsStore.State>
    val childStack: Value<ChildStack<SettingsComponent.Config, SettingsComponent.Child>>

    fun onAction(action: SettingsStore.Intent)
    fun onCategorySelected(category: SettingsComponent.SettingsCategory)
    fun onDrawerButtonClicked()
    fun onBackFromCategory()
    fun resetAllSettings()
    fun setDrawerHandler(handler: () -> Unit)

    /**
     * Категории настроек
     */
    enum class SettingsCategory {
        APPEARANCE,
        LANGUAGE,
        NETWORK,
        SECURITY,
        NOTIFICATIONS,
        STORAGE,
        EXPERIMENTAL,
        SYSTEM,
        PROFILE
    }

    /**
     * Конфигурации для навигации
     */
    sealed interface Config {
        @Serializable
        data object Main : Config

        @Serializable
        data class Category(val category: SettingsCategory) : Config
    }

    /**
     * Дочерние компоненты
     */
    sealed interface Child {
        data class CategoryChild(val category: SettingsCategory) : Child
        data object MainChild : Child
    }
}

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val onBack: () -> Unit,
    override val di: DI
) : SettingsComponent, DIAware, ComponentContext by componentContext {

    private val settingsStoreFactory by instance<SettingsStoreFactory>()
    private val navigation = StackNavigation<SettingsComponent.Config>()
    private val multiplatformSettings: MultiplatformSettings by instance()

    private val store = instanceKeeper.getStore {
        SettingsStoreFactory(
            storeFactory = DefaultStoreFactory(),
            di = di
        ).create()
    }

    override val childStack: Value<ChildStack<SettingsComponent.Config, SettingsComponent.Child>> =
        childStack(
            source = navigation,
            initialConfiguration = SettingsComponent.Config.Main,
            serializer = null,
            handleBackButton = true,
            childFactory = ::createChild
        )

    private fun createChild(
        config: SettingsComponent.Config,
        componentContext: ComponentContext
    ): SettingsComponent.Child =
        when (config) {
            is SettingsComponent.Config.Main -> SettingsComponent.Child.MainChild
            is SettingsComponent.Config.Category -> SettingsComponent.Child.CategoryChild(config.category)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<SettingsStore.State> = store.stateFlow

    override fun onAction(action: SettingsStore.Intent) {
        when (action) {
            is SettingsStore.Intent.Back -> onBack()
            else -> store.accept(action)
        }
    }

    override fun onCategorySelected(category: SettingsComponent.SettingsCategory) {
        navigation.push(SettingsComponent.Config.Category(category))
    }

    override fun onBackFromCategory() {
        navigation.pop()
    }

    override fun onDrawerButtonClicked() {
        TODO("onDrawerButtonClicked Not yet implemented")
    }

    override fun resetAllSettings() {
        multiplatformSettings.resetAllSettings()
        // Обновляем состояние после сброса настроек
        store.accept(SettingsStore.Intent.Init)
    }

    override fun setDrawerHandler(handler: () -> Unit) {
        // Implementation needed
    }
}
