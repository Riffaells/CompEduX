package component.settings

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ui.icon.RIcons

/**
 * Компонент для отображения значка "Экспериментально"
 * Используется для обозначения экспериментальных функций в настройках
 *
 * @param tooltipText Текст, отображаемый при наведении на значок
 * @param titleText Заголовок тултипа
 * @param icon Иконка для отображения (по умолчанию RIcons.ExperimentBeta)
 * @param isExperimental Флаг, указывающий, является ли функция экспериментальной (по умолчанию true)
 * @param badgeSize Размер значка в dp (по умолчанию 24dp)
 * @param iconSize Размер иконки внутри значка в dp (по умолчанию 16dp)
 * @param maxTooltipWidth Максимальная ширина тултипа в dp (по умолчанию 280dp)
 * @param badgeBackgroundColor Основной цвет фона значка (по умолчанию primaryContainer)
 * @param badgeBorderColor Цвет границы значка (по умолчанию primary с прозрачностью 0.7)
 * @param iconTint Цвет иконки (по умолчанию onPrimaryContainer)
 */
@Composable
fun ExperimentalBadge(
    tooltipText: String = "Эта функция находится в экспериментальной стадии и может работать нестабильно",
    titleText: String = "Экспериментальная функция",
    icon: ImageVector = RIcons.ExperimentBeta,
    isExperimental: Boolean = true,
    badgeSize: Int = 24,
    iconSize: Int = 16,
    maxTooltipWidth: Int = 280,
    badgeBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    badgeBorderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
    iconTint: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    if (!isExperimental) return

    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    // Анимация для эффекта пульсации
    var isPulsing by remember { mutableStateOf(false) }
    val pulseScale by animateFloatAsState(
        targetValue = if (isPulsing) 1.1f else 1.0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "PulseAnimation"
    )

    // Запускаем пульсацию при первом отображении
    LaunchedEffect(Unit) {
        while (true) {
            isPulsing = true
            kotlinx.coroutines.delay(2000)
            isPulsing = false
            kotlinx.coroutines.delay(500)
        }
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 3.dp,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .widthIn(max = maxTooltipWidth.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Text(
                        text = tooltipText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            }
        },
        state = tooltipState,
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    scope.launch {
                        tooltipState.show()
                    }
                }
                .size(badgeSize.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            badgeBackgroundColor,
                            badgeBackgroundColor.copy(alpha = 0.8f)
                        )
                    )
                )
                .border(1.dp, badgeBorderColor, CircleShape)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            // Используем переданную иконку
            Icon(
                imageVector = icon,
                contentDescription = titleText,
                tint = iconTint,
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
}
