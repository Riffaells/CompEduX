package component.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Класс для конфигурации навигации.
 * Позволяет определить элементы навигации и их свойства.
 */
class NavigationConfig {
    private val _items = mutableListOf<NavigationItem>()

    /**
     * Список элементов навигации.
     */
    val items: List<NavigationItem>
        get() = _items.toList()

    /**
     * Добавляет элемент навигации в конфигурацию.
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
        _items.add(
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
     * Находит элемент навигации по идентификатору.
     *
     * @param id Идентификатор элемента
     * @return Элемент навигации или null, если элемент не найден
     */
    fun findItemById(id: String): NavigationItem? {
        return _items.find { it.id == id }
    }
}

/**
 * Класс, представляющий элемент навигации.
 *
 * @param id Уникальный идентификатор элемента
 * @param icon Иконка элемента
 * @param label Текстовая метка элемента
 * @param contentDescription Описание содержимого для доступности
 * @param onClick Обработчик нажатия на элемент
 */
data class NavigationItem(
    val id: String,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String? = null,
    val onClick: () -> Unit
) {
    /**
     * Создает Composable-функцию для отображения иконки элемента.
     */
    @Composable
    fun IconContent() {
        NavigationIcon(icon = icon, contentDescription = contentDescription)
    }
}
