package component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Класс для конфигурации навигационных элементов.
 * Позволяет создать список элементов навигации с иконками, метками и обработчиками нажатий.
 */
@Stable
class NavigationConfig {
    /**
     * Список элементов навигации.
     */
    val items = mutableListOf<NavigationItem>()

    /**
     * Добавляет новый элемент навигации в конфигурацию.
     *
     * @param id Уникальный идентификатор элемента
     * @param icon Иконка элемента
     * @param label Текстовая метка элемента
     * @param contentDescription Описание содержимого для доступности
     * @param onClick Обработчик нажатия на элемент
     */
    fun addItem(
        id: String,
        icon: ImageVector,
        label: String,
        contentDescription: String? = null,
        onClick: () -> Unit
    ) {
        items.add(
            NavigationItem(
                id = id,
                icon = icon,
                label = label,
                contentDescription = contentDescription,
                onClick = onClick
            )
        )
    }

    /**
     * Находит элемент навигации по его идентификатору.
     *
     * @param id Идентификатор элемента
     * @return Найденный элемент или null, если элемент не найден
     */
    fun findItemById(id: String): NavigationItem? {
        return items.find { it.id == id }
    }

    /**
     * Класс, представляющий элемент навигации.
     *
     * @property id Уникальный идентификатор элемента
     * @property icon Иконка элемента
     * @property label Текстовая метка элемента
     * @property contentDescription Описание содержимого для доступности
     * @property onClick Обработчик нажатия на элемент
     */
    @Stable
    data class NavigationItem(
        val id: String,
        val icon: ImageVector,
        val label: String,
        val contentDescription: String? = null,
        val onClick: () -> Unit
    ) {
        /**
         * Композабл для отображения иконки элемента.
         */
        @Composable
        fun IconContent() {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}
