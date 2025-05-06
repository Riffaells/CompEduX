package component.settings.base

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Современный компонент элемента настроек
 *
 * @param title Заголовок настройки
 * @param description Опциональное описание
 * @param trailingContent Опциональный контент справа (например, переключатель)
 * @param modifier Дополнительный модификатор для стилизации
 */
@Composable
fun FuturisticSettingItem(
    title: String,
    description: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = if (isHovered)
        MaterialTheme.colorScheme.surfaceVariant
    else
        MaterialTheme.colorScheme.surface

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        tonalElevation = if (isHovered) 1.dp else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .hoverable(interactionSource)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Заголовок и описание
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Контент справа (если предоставлен)
            if (trailingContent != null) {
                Box(
                    modifier = Modifier.padding(start = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    trailingContent()
                }
            }
        }
    }
}
