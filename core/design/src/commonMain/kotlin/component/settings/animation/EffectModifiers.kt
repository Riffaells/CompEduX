package component.settings.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Создает эффект свечения вокруг содержимого
 */
fun Modifier.glowEffect(
    color: Color,
    radius: Float = 20f,
    alpha: Float = 0.5f
): Modifier = composed {
    this.shadow(
        elevation = (radius / 20).dp,
        shape = RectangleShape,
        clip = false,
        ambientColor = color.copy(alpha = alpha),
        spotColor = color.copy(alpha = alpha)
    )
}

/**
 * Создает анимацию пульсации
 */
@Composable
fun rememberPulseAnimation(
    durationMillis: Int = 2000,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f
): Float {
    val infiniteTransition = rememberInfiniteTransition()
    return infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis),
            repeatMode = RepeatMode.Reverse
        )
    ).value
}
