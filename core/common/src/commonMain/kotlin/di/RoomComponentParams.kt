package di

import com.arkivanov.decompose.ComponentContext

/**
 * Параметры для создания RoomComponent
 */
data class RoomComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)
