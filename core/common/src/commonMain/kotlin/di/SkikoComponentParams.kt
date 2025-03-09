package di

import com.arkivanov.decompose.ComponentContext

/**
 * Параметры для создания компонента Skiko
 */
data class SkikoComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)
