package component.settings.input

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import component.settings.badge.ExperimentalBadge

/**
 * Компонент для отображения настройки с переключателем
 *
 * @param title Заголовок настройки
 * @param description Описание настройки
 * @param isChecked Текущее состояние переключателя
 * @param onCheckedChange Обработчик изменения состояния переключателя
 * @param accentColor Цвет акцента для переключателя (если null, используется primary)
 * @param isExperimental Флаг, указывающий, является ли настройка экспериментальной
 * @param enabled Флаг, указывающий, доступен ли переключатель для взаимодействия
 */
@Composable
fun SettingToggle(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color? = null,
    isExperimental: Boolean = false,
    enabled: Boolean = true
) {
    val primaryColor = accentColor ?: MaterialTheme.colorScheme.primary
    val containerColor = when {
        accentColor == null -> MaterialTheme.colorScheme.primaryContainer
        else -> primaryColor.copy(alpha = 0.2f)
    }

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = primaryColor,
        checkedTrackColor = containerColor,
        checkedBorderColor = primaryColor.copy(alpha = 0.5f),
        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        disabledCheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledCheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledUncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.6f
                        )
                    )

                    if (isExperimental) {
                        Spacer(modifier = Modifier.width(4.dp))
                        ExperimentalBadge()
                    }
                }

                if (description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = switchColors
            )
        }
    }
}
