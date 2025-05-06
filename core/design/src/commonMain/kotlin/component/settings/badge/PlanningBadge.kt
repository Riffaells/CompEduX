package component.settings.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Компонент для отображения плашки с запланированной функциональностью
 *
 * @param text Текст плашки
 * @param planned Флаг, указывающий, запланирована ли функция или уже реализована
 * @param tooltipText Текст, отображаемый при наведении на значок
 * @param modifier Модификатор для стилизации компонента
 */
@Composable
fun PlanningBadge(
    text: String,
    planned: Boolean = true,
    tooltipText: String = "",
    modifier: Modifier = Modifier
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val badgeColors = if (planned) {
        listOf(
            primaryColor.copy(alpha = 0.7f),
            secondaryColor.copy(alpha = 0.4f)
        )
    } else {
        listOf(
            primaryColor.copy(alpha = 0.8f),
            secondaryColor.copy(alpha = 0.6f)
        )
    }

    if (tooltipText.isNotEmpty()) {
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
                            .widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = if (planned) "Планируется" else "Реализовано",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (planned)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

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
            ) {
                Badge(text, planned, badgeColors, modifier)
            }
        }
    } else {
        Badge(text, planned, badgeColors, modifier)
    }
}

@Composable
private fun Badge(
    text: String,
    planned: Boolean,
    badgeColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(badgeColors)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (planned) FontWeight.Normal else FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center
        )
    }
}
