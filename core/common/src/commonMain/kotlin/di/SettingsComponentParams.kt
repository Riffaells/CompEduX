package di

import com.arkivanov.decompose.ComponentContext

/**
 * Параметры для создания компонента настроек
 */
data class SettingsComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)
