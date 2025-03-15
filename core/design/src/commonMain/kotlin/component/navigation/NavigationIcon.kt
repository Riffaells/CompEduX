package component.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Компонент для отображения иконки в навигации.
 *
 * @param icon Иконка для отображения
 * @param contentDescription Описание содержимого для доступности
 * @param tint Цвет иконки
 * @param modifier Модификатор для настройки внешнего вида компонента
 */
@Composable
fun NavigationIcon(
    icon: ImageVector,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}
