package component.settings.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect

/**
 * Создает эффект пульсирующего свечения
 *
 * @param baseColor Основной цвет свечения
 * @param pulseColor Цвет пульсации
 * @param alpha Прозрачность эффекта
 * @return Модификатор с эффектом
 */
fun Modifier.glowingPulse(
    baseColor: Color,
    pulseColor: Color = baseColor.copy(alpha = 0.7f),
    alpha: Float = 0.3f
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val glowColor = remember(baseColor, pulseAlpha) {
        baseColor.copy(alpha = alpha * pulseAlpha)
    }

    drawBehind {
        drawCircle(
            color = glowColor,
            radius = size.maxDimension,
            center = Offset(size.width / 2, size.height / 2)
        )
    }
}

/**
 * Создает градиентный фон с пульсацией цвета
 *
 * @param colors Цвета градиента
 * @param pulseIntensity Интенсивность пульсации (0.0-1.0)
 * @return Модификатор с эффектом
 */
fun Modifier.pulsatingGradient(
    colors: List<Color>,
    pulseIntensity: Float = 0.1f
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val pulseState by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val adjustedColors = remember(colors, pulseState) {
        colors.map { color ->
            color.copy(
                red = (color.red + pulseState * pulseIntensity).coerceIn(0f, 1f),
                green = (color.green + pulseState * pulseIntensity).coerceIn(0f, 1f),
                blue = (color.blue + pulseState * pulseIntensity).coerceIn(0f, 1f)
            )
        }
    }

    background(
        brush = Brush.linearGradient(adjustedColors)
    )
}

/**
 * Создает эффект скользящего свечения для прогресс-индикатора
 */
fun Modifier.slidingGlow(
    color: Color,
    slideSpeed: Int = 1000,
    alpha: Float = 0.5f
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "slide")
    val xPos by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(slideSpeed * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "xPos"
    )

    drawWithContent {
        drawContent()
        clipRect {
            val colorAlpha = color.copy(alpha = alpha)
            drawCircle(
                color = colorAlpha,
                radius = size.width / 4,
                center = Offset(
                    x = size.width * xPos,
                    y = size.height / 2
                )
            )
        }
    }
}

/**
 * Создает бесконечную анимацию пульсации
 */
@Composable
fun animatePulse(
    initialValue: Float = 0.2f,
    targetValue: Float = 0.5f,
    durationMillis: Int = 2000
): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    return infiniteTransition.animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )
}
