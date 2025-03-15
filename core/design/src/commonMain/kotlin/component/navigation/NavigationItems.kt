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

/**
 * Базовый элемент навигации с анимацией выбора.
 * Используется как основа для элементов навигационной панели и боковой навигации.
 *
 * @param selected Выбран ли данный элемент
 * @param onClick Обработчик нажатия на элемент
 * @param icon Иконка элемента
 * @param label Текстовая метка элемента (опционально)
 * @param selectedColor Цвет элемента в выбранном состоянии
 * @param unselectedColor Цвет элемента в невыбранном состоянии
 * @param contentDescription Описание содержимого для доступности
 * @param selectedSize Размер элемента в выбранном состоянии
 * @param unselectedSize Размер элемента в невыбранном состоянии
 * @param content Содержимое элемента
 */
@Composable
fun BaseNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    contentDescription: String? = null,
    selectedSize: Dp = 48.dp,
    unselectedSize: Dp = 40.dp,
    content: @Composable (backgroundColor: Color, itemSize: Dp) -> Unit
) {
    // Анимация цвета фона при выборе элемента
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) selectedColor.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "backgroundColorAnimation"
    )

    // Анимация размера элемента при выборе
    val itemSize by animateDpAsState(
        targetValue = if (selected) selectedSize else unselectedSize,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "itemSizeAnimation"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content(backgroundColor, itemSize)
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
    BaseNavigationItem(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f),
        selectedColor = selectedColor,
        unselectedColor = unselectedColor,
        contentDescription = contentDescription,
        selectedSize = 48.dp,
        unselectedSize = 40.dp
    ) { backgroundColor, itemSize ->
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

/**
 * Элемент боковой навигационной панели с анимацией выбора.
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
fun FloatingNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    contentDescription: String? = null
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BaseNavigationItem(
            selected = selected,
            onClick = onClick,
            selectedColor = selectedColor,
            unselectedColor = unselectedColor,
            contentDescription = contentDescription,
            selectedSize = 56.dp,
            unselectedSize = 48.dp
        ) { backgroundColor, itemSize ->
            // Фон элемента с анимацией
            Box(
                modifier = Modifier
                    .size(itemSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                // Иконка с анимацией прозрачности
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .graphicsLayer {
                            alpha = if (selected) 1f else 0.7f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }
        }

        // Метка (если есть)
        if (label != null && selected) {
            Spacer(modifier = Modifier.height(4.dp))
            label()
        }
    }
}
