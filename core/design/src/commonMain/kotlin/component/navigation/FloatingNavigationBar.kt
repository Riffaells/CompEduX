package component.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
    hazeState: HazeState? = null,
    useProgressiveBlur: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
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
                .height(64.dp),
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            elevation = elevation,
            cornerRadius = cornerRadius,
            blurType = blurType,
            hazeState = hazeState,
            useProgressiveBlur = useProgressiveBlur,
            progressiveBlurCreator = {
                // Вертикальный градиент размытия - сильнее внизу, слабее вверху
                HazeProgressive.verticalGradient(
                    startIntensity = 0.7f, // Верхняя часть (меньше размытия)
                    endIntensity = 1.0f    // Нижняя часть (больше размытия)
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

/**
 * Элемент навигационной панели с анимацией выбора.
 *
 * @param selected Выбран ли данный элемент
 * @param onClick Обработчик нажатия на элемент
 * @param icon Иконка элемента
 * @param label Текстовая метка элемента (опционально)
 * @param selectedColor Цвет элемента в выбранном состоянии
 * @param unselectedColor Цвет элемента в невыбранном состоянии
 * @param contentDescription Описание содержимого для доступности
 */
@Composable
fun FloatingNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    contentDescription: String? = null
) {
    // Анимация цвета фона при выборе элемента
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) selectedColor.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "backgroundColorAnimation"
    )

    // Анимация размера элемента при выборе
    val itemSize by animateDpAsState(
        targetValue = if (selected) 48.dp else 40.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "itemSizeAnimation"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(itemSize)
                .clip(RoundedCornerShape(16.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.graphicsLayer {
                        alpha = if (selected) 1f else 0.7f
                    },
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                if (label != null && selected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    label()
                }
            }
        }
    }
}
