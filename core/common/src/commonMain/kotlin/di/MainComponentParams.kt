package di

import com.arkivanov.decompose.ComponentContext

/**
 * Параметры для создания компонента главного экрана
 */
data class MainComponentParams(
    val componentContext: ComponentContext,
    val onSettingsClicked: () -> Unit,
    val onDevelopmentMapClicked: () -> Unit
)
