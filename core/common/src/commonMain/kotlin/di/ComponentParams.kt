package di

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.webhistory.WebHistoryController
import component.root.DefaultRootComponent

/**
 * Параметры для создания MainComponent
 */
data class MainComponentParams(
    val componentContext: ComponentContext,
    val onSettingsClicked: () -> Unit
)

/**
 * Параметры для создания SettingsComponent
 */
data class SettingsComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)

/**
 * Параметры для создания RootComponent
 */
data class RootComponentParams(
    val componentContext: ComponentContext,
    val webHistoryController: WebHistoryController? = null,
    val deepLink: DefaultRootComponent.DeepLink? = null
)
