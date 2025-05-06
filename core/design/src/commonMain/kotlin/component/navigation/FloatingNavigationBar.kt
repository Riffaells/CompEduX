package component.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState

/**
 * Кастомный компонент нижней навигации с эффектом "парения" и размытием фона.
 *
 * @param modifier Модификатор для настройки внешнего вида компонента
 * @param backgroundColor Цвет фона навигационной панели
 * @param contentColor Цвет содержимого навигационной панели
 * @param elevation Высота тени для эффекта "парения"
 * @param cornerRadius Радиус скругления углов
 * @param blurType Тип эффекта размытия
 * @param hazeState Состояние эффекта размытия, должно быть общим с источником размытия
 * @param useProgressiveBlur Использовать ли прогрессивное размытие (градиент)
 * @param animationProgress Прогресс анимации появления/исчезновения (0.0-1.0)
 * @param content Содержимое навигационной панели
 */
@Composable
fun FloatingNavigationBar(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Float = 8f,
    cornerRadius: Float = 24f,
    blurType: BlurType = BlurType.FROSTED,
    hazeState: HazeState,
    useProgressiveBlur: Boolean = true,
    animationProgress: Float = 1f,
    content: @Composable RowScope.() -> Unit
) {
    // Анимируем высоту контейнера в зависимости от прогресса анимации
    val containerHeight by animateFloatAsState(
        targetValue = 64f * animationProgress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "ContainerHeight"
    )

    // Анимируем прозрачность фона
    val backgroundAlpha by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "BackgroundAlpha"
    )

    // Анимируем радиус скругления
    val animatedCornerRadius by animateFloatAsState(
        targetValue = cornerRadius * animationProgress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "CornerRadius"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Используем базовый контейнер для навигации
        BaseNavigationContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(containerHeight.dp),
            backgroundColor = backgroundColor.copy(alpha = backgroundAlpha * backgroundColor.alpha),
            contentColor = contentColor,
            elevation = elevation * animationProgress,
            cornerRadius = animatedCornerRadius,
            blurType = blurType,
            hazeState = hazeState,
            useProgressiveBlur = useProgressiveBlur,
            progressiveBlurCreator = {
                // Вертикальный градиент размытия - сильнее внизу, слабее вверху
                HazeProgressive.verticalGradient(
                    startIntensity = 0.7f * animationProgress, // Верхняя часть (меньше размытия)
                    endIntensity = 1.0f * animationProgress    // Нижняя часть (больше размытия)
                )
            }
        ) {
            // Содержимое навигационной панели
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}
