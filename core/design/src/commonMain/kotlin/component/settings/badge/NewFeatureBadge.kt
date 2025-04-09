package component.settings.badge

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
 * Компонент для отображения значка "Новая функция"
 * Используется для обозначения новых функций в интерфейсе
 *
 * @param tooltipText Текст, отображаемый при наведении на значок
 * @param titleText Заголовок тултипа
 * @param icon Иконка для отображения
 * @param badgeSize Размер значка в dp
 * @param iconSize Размер иконки внутри значка в dp
 * @param maxTooltipWidth Максимальная ширина тултипа в dp
 * @param badgeBackgroundColor Основной цвет фона значка
 * @param iconTint Цвет иконки
 */
@Composable
fun NewFeatureBadge(
    tooltipText: String,
    titleText: String = "Новая функция",
    icon: ImageVector = RIcons.ExperimentNew,
    badgeSize: Int = 24,
    iconSize: Int = 16,
    maxTooltipWidth: Int = 280,
    badgeBackgroundColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.onTertiaryContainer
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    // Анимация для лёгкого эффекта мерцания
    var isGlowing by remember { mutableStateOf(false) }
    val glowScale by animateFloatAsState(
        targetValue = if (isGlowing) 1.08f else 1.0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "GlowAnimation"
    )

    // Запускаем мерцание при первом отображении
    LaunchedEffect(Unit) {
        while (true) {
            isGlowing = true
            kotlinx.coroutines.delay(1500)
            isGlowing = false
            kotlinx.coroutines.delay(1500)
        }
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
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
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
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
        state = tooltipState
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
                .scale(glowScale)
                .size(badgeSize.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            badgeBackgroundColor,
                            badgeBackgroundColor.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = titleText,
                tint = iconTint,
                modifier = Modifier.size(iconSize.dp)
            )
        }
    }
}
