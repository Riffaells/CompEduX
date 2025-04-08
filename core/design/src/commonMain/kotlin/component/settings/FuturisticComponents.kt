package component.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Стилизованный фильтр-чип в футуристическом дизайне
 *
 * @param text Текст чипа
 * @param icon Иконка чипа
 * @param selected Выбран ли чип
 * @param onClick Обработчик нажатия
 * @param modifier Модификатор для настройки внешнего вида
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuturisticFilterChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp,
        ),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = modifier.shadow(
            elevation = if (selected) 4.dp else 0.dp,
            shape = RoundedCornerShape(16.dp)
        )
    )
}

/**
 * Стилизованный опциональный чип в футуристическом дизайне
 *
 * @param text Текст чипа
 * @param selected Выбран ли чип
 * @param onClick Обработчик нажатия
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun FuturisticOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected)
            MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surface,
        tonalElevation = if (selected) 4.dp else 0.dp,
        shadowElevation = if (selected) 2.dp else 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected)
                MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier.height(36.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
            ),
            color = if (selected)
                MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Стилизованная карточка для отображения функций в футуристическом стиле.
 *
 * @param text Текст описания функции
 * @param icon Иконка функции
 * @param modifier Модификатор для кастомизации
 */
@Composable
fun FuturisticFeatureCard(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        ),
        modifier = modifier.defaultMinSize(minHeight = 60.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Стилизованный индикатор прогресса в футуристическом стиле.
 *
 * @param progress Значение прогресса от 0.0f до 1.0f
 * @param modifier Модификатор для кастомизации
 */
@Composable
fun FuturisticProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Progress indicator labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "100%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        // Background of progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )

        // Filled part of progress bar with gradient and glow effect
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(8.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(4.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(4.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
        )

        // Progress marker
        Box(
            modifier = Modifier
                .offset(x = (-12).dp)
                .fillMaxWidth(progress)
                .padding(end = 1.dp)
                .height(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
        }
    }
}

/**
 * Стилизованный заголовок секции
 *
 * @param title Заголовок секции
 * @param badge Опциональный значок для отображения рядом с заголовком
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun SectionHeader(
    title: String,
    badge: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(bottom = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium
            )
        )

        if (badge != null) {
            badge()
        }
    }
}

/**
 * Стилизованная кнопка для раскрывающейся секции в футуристическом стиле.
 *
 * @param expanded Состояние раскрытия секции
 * @param onClick Обработчик нажатия
 * @param text Текст кнопки
 * @param modifier Модификатор для кастомизации
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

/**
 * Значок новой функции с всплывающей подсказкой
 *
 * @param tooltipText Текст для всплывающей подсказки
 * @param titleText Заголовок для всплывающей подсказки
 * @param icon Иконка для значка
 */
@Composable
fun NewFeatureBadge(
    tooltipText: String,
    titleText: String = "Новая функция",
    icon: ImageVector = RIcons.ExperimentNew
) {
    var tooltipVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(start = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier
                .size(16.dp)
                .shadow(2.dp, CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = titleText,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .size(12.dp)
                    .padding(2.dp)
            )
        }

        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = tooltipText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            state = rememberTooltipState(),
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clickable { tooltipVisible = !tooltipVisible }
            )
        }
    }
}
