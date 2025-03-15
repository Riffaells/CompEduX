package component.navigation.effects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Компонент для создания эффекта размытия фона.
 * На разных платформах может работать по-разному, в зависимости от поддержки blur.
 * Если blur не поддерживается, будет использован только полупрозрачный фон.
 *
 * @param radius Радиус размытия в dp
 * @param backgroundColor Цвет фона для дополнительного эффекта
 * @param alpha Прозрачность фона
 * @param shape Форма для обрезки фона
 * @param content Содержимое, которое будет отображаться поверх размытия
 */
@Composable
fun BlurEffect(
    radius: Float = 10f,
    backgroundColor: Color = Color.White,
    alpha: Float = 0.5f,
    shape: Shape = RectangleShape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Пытаемся определить, поддерживается ли blur на текущей платформе
    val supportsBlur = remember {
        try {
            // Проверяем, доступен ли blur, создавая тестовый модификатор
            // Если это вызовет исключение, значит blur не поддерживается
            Modifier.blur(1.dp)
            true
        } catch (e: Throwable) {
            false
        }
    }

    // Основной контейнер
    Box(modifier = modifier) {
        // Слой с размытием или просто фоном
        if (supportsBlur) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = radius.dp)
                    .background(
                        color = backgroundColor.copy(alpha = alpha),
                        shape = shape
                    )
            )
        } else {
            // Если blur не поддерживается, используем только фон
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = backgroundColor.copy(alpha = 0.9f), // Увеличиваем непрозрачность
                        shape = shape
                    )
            )
        }

        // Содержимое поверх размытия
        content()
    }
}

/**
 * Упрощенная версия BlurEffect, которая применяет только фон с прозрачностью
 * для платформ, где blur не поддерживается.
 */
@Composable
fun SimpleBlurEffect(
    backgroundColor: Color = Color.White,
    alpha: Float = 0.8f,
    shape: Shape = RectangleShape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        // Полупрозрачный фон
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = backgroundColor.copy(alpha = alpha),
                    shape = shape
                )
        )

        // Содержимое поверх фона
        content()
    }
}
