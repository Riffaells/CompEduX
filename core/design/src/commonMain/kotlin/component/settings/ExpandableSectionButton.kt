package component.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Переиспользуемая кнопка для раскрытия/скрытия раздела настроек
 *
 * @param expanded Текущее состояние раздела (раскрыт/свернут)
 * @param onClick Обработчик нажатия на кнопку
 * @param expandedText Текст, отображаемый на кнопке в раскрытом состоянии
 * @param collapsedText Текст, отображаемый на кнопке в свернутом состоянии
 * @param expandedIcon Иконка для раскрытого состояния
 * @param collapsedIcon Иконка для свернутого состояния
 * @param modifier Модификатор для кнопки
 */
@Composable
fun ExpandableSectionButton(
    expanded: Boolean,
    onClick: () -> Unit,
    expandedText: String = "Скрыть подробности",
    collapsedText: String = "Показать подробности",
    expandedIcon: ImageVector = Icons.Default.KeyboardArrowUp,
    collapsedIcon: ImageVector = Icons.Default.KeyboardArrowDown,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
    ) {
        Icon(
            imageVector = if (expanded) expandedIcon else collapsedIcon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (expanded) expandedText else collapsedText
        )
    }
}

/**
 * Стилизованная кнопка с возможностью раскрытия
 *
 * @param expanded Состояние раскрытия кнопки
 * @param onClick Обработчик нажатия
 * @param text Текст кнопки
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun ExpandableButton(
    expanded: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                           else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
