package component.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.*

/**
 * Кастомный компонент боковой навигации с эффектом "парения" и размытием фона.
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
fun FloatingNavigationRail(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.85f),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    elevation: Float = 8f,
    cornerRadius: Float = 24f,
    blurType: BlurType = BlurType.FROSTED,
    hazeState: HazeState,
    useProgressiveBlur: Boolean = true,
    animationProgress: Float = 1f,
    content: @Composable ColumnScope.() -> Unit
) {
    // Анимируем ширину контейнера в зависимости от прогресса анимации
    val containerWidth by animateFloatAsState(
        targetValue = 72f * animationProgress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "ContainerWidth"
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

    // Анимируем высоту контейнера
    val containerHeightFraction by animateFloatAsState(
        targetValue = 0.6f * animationProgress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "ContainerHeightFraction"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // Используем базовый контейнер для навигации
        BaseNavigationContainer(
            modifier = Modifier
                .width(containerWidth.dp)
                .fillMaxHeight(containerHeightFraction),
            backgroundColor = backgroundColor.copy(alpha = backgroundAlpha * backgroundColor.alpha),
            contentColor = contentColor,
            elevation = elevation * animationProgress,
            cornerRadius = animatedCornerRadius,
            blurType = blurType,
            hazeState = hazeState,
            useProgressiveBlur = useProgressiveBlur,
            contentAlignment = Alignment.TopCenter,
            progressiveBlurCreator = {
                // Горизонтальный градиент размытия - сильнее слева, слабее справа
                HazeProgressive.horizontalGradient(
                    startIntensity = 1.0f * animationProgress, // Левая сторона (больше размытия)
                    endIntensity = 0.7f * animationProgress    // Правая сторона (меньше размытия)
                )
            }
        ) {
            // Содержимое навигационной панели
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
                content = content
            )
        }
    }
}
