package di

import com.arkivanov.decompose.ComponentContext

/**
 * Параметры для создания компонента аутентификации
 */
data class AuthComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)
