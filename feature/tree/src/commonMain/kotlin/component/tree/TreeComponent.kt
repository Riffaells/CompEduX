package component.tree

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.flow.StateFlow
import component.tree.store.TreeStore

/**
 * Параметры для создания компонента Tree
 */
data class TreeComponentParams(
    val componentContext: ComponentContext,
    val onBack: () -> Unit
)

/**
 * Компонент для работы с деревом развития
 */
interface TreeComponent {
    /**
     * Текущее состояние компонента
     */
    val state: StateFlow<TreeStore.State>

    /**
     * Обработка события от UI
     */
    fun onEvent(event: TreeStore.Intent)

    /**
     * Обработка нажатия кнопки назад
     */
    fun onBackClicked()
}
