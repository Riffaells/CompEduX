package component.settings.input

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Улучшенный слайдер
 *
 * @param value Текущее значение слайдера
 * @param onValueChange Обработчик изменения значения
 * @param valueRange Диапазон значений
 * @param steps Количество шагов
 * @param valueLabel Функция для форматирования текущего значения
 * @param labels Список меток для отображения под слайдером
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun EnhancedSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueLabel: @Composable (Float) -> Unit,
    labels: List<String>? = null,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    val glowAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.5f else 0.2f,
        animationSpec = tween(300)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            valueLabel(value)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            value = value,
            onValueChange = {
                onValueChange(it)
                isDragging = true
            },
            onValueChangeFinished = {
                isDragging = false
            },
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            ),
            modifier = Modifier
                .fillMaxWidth()

        )

        // Отображение меток, если они предоставлены
        labels?.let {
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (label in it) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Улучшенный слайдер с настройками времени
 *
 * @param value Текущее значение в секундах
 * @param onValueChange Обработчик изменения значения
 * @param valueRange Диапазон значений в секундах
 * @param steps Количество шагов
 * @param title Заголовок слайдера
 * @param modifier Модификатор для настройки внешнего вида
 */
@Composable
fun TimeoutSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 5f..120f,
    steps: Int = 23,
    title: String,
    modifier: Modifier = Modifier
) {
    EnhancedSlider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        valueLabel = {
            Text(
                text = "$title: ${it.toInt()} сек",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        labels = listOf(
            "${valueRange.start.toInt()} сек",
            "${((valueRange.endInclusive + valueRange.start) / 2).toInt()} сек",
            "${valueRange.endInclusive.toInt()} сек"
        ),
        modifier = modifier
    )
}
