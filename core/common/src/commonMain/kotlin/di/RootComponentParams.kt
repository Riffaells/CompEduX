package di

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.webhistory.WebHistoryController
import component.root.DefaultRootComponent

/**
 * Параметры для создания корневого компонента
 */
data class RootComponentParams(
    val componentContext: ComponentContext,
    val webHistoryController: WebHistoryController? = null,
    val deepLink: DefaultRootComponent.DeepLink? = null
)
