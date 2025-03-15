package component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.*

/**
 * Базовый контейнер для навигационных компонентов с эффектом "парения" и размытием фона.
 * Используется как основа для FloatingNavigationBar и FloatingNavigationRail.
 *
 * @param modifier Модификатор для настройки внешнего вида компонента
 * @param backgroundColor Цвет фона навигационной панели
 * @param contentColor Цвет содержимого навигационной панели
 * @param elevation Высота тени для эффекта "парения"
 * @param cornerRadius Радиус скругления углов
 * @param blurType Тип эффекта размытия
 * @param hazeState Состояние эффекта размытия, должно быть общим с источником размытия
 * @param useProgressiveBlur Использовать ли прогрессивное размытие (градиент)
 * @param progressiveBlurCreator Функция для создания прогрессивного размытия
 * @param contentAlignment Выравнивание содержимого внутри контейнера
 * @param content Содержимое навигационной панели
 */
@Composable
fun BaseNavigationContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Float = 8f,
    cornerRadius: Float = 24f,
    blurType: BlurType = BlurType.FROSTED,
    hazeState: HazeState? = null,
    useProgressiveBlur: Boolean = true,
    progressiveBlurCreator: () -> HazeProgressive? = { null },
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    // Используем переданное состояние или создаем новое
    val localHazeState = hazeState ?: remember { HazeState() }

    // Получаем стиль размытия в зависимости от выбранного типа
    val blurStyle = when (blurType) {
        BlurType.GLASS -> BlurStyles.glass(backgroundColor = backgroundColor)
        BlurType.FROSTED -> BlurStyles.frostedGlass(backgroundColor = backgroundColor)
        BlurType.ACRYLIC -> BlurStyles.acrylic(backgroundColor = backgroundColor)
        BlurType.MICA -> BlurStyles.mica(backgroundColor = backgroundColor)
        BlurType.NONE -> HazeStyle(backgroundColor = backgroundColor, tint = null) // Пустой стиль, не будет использоваться
    }

    // Контейнер с тенью и скруглением
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(cornerRadius.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(cornerRadius.dp))
            .then(
                if (blurType != BlurType.NONE && hazeState != null) {
                    // Применяем эффект размытия с помощью Haze
                    Modifier.hazeEffect(
                        state = localHazeState,
                        style = blurStyle
                    ) {
                        // Используем Auto для автоматического определения оптимального масштаба
                        // Это улучшает производительность, особенно на мобильных устройствах
                        inputScale = HazeInputScale.Auto

                        // Добавляем прогрессивное размытие (если включено)
                        if (useProgressiveBlur) {
                            // Используем переданный создатель прогрессивного размытия
                            progressiveBlurCreator()?.let { progressive = it }
                        }
                    }
                } else {
                    // Обычный фон без размытия
                    Modifier.background(backgroundColor)
                }
            ),
        contentAlignment = contentAlignment,
        content = content
    )
}
